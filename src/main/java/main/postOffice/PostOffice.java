package main.postOffice;

import main.connector.DBConnector;
import main.person.Citizen;
import main.person.JuniorPostman;
import main.person.Postman;
import main.person.SeniorPostman;
import main.postBox.PostBox;
import main.shipment.Letter;
import main.shipment.Parcel;
import main.shipment.Shipment;
import main.util.Constants;
import main.util.Randomizer;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.*;

public class PostOffice {
    private String name;
    private volatile double turnover;
    private CopyOnWriteArrayList <PostBox> postBoxes;
    private CopyOnWriteArrayList<Shipment> storage;
    private CopyOnWriteArraySet<Postman> postmen;
    private ConcurrentSkipListMap<LocalDate, ConcurrentSkipListMap<String, CopyOnWriteArrayList<Integer>>> archive;
    private BlockingQueue<Shipment> itemsForDeliver;

    public PostOffice (String name){
        if(name.length()>0){
            this.name = name;
        }
        postBoxes = new CopyOnWriteArrayList<>();
        storage = new CopyOnWriteArrayList<>();
        archive = new ConcurrentSkipListMap<>();
        itemsForDeliver = new LinkedBlockingQueue<>();
        postmen = new CopyOnWriteArraySet<>();
        for(int i = 0; i<25; i++){
            postBoxes.add(new PostBox());
        }
        for(int i = 0; i<5; i++){
            String firstName = Constants.FIRST_NAMES[Randomizer.getRandomInt(0,Constants.FIRST_NAMES.length-1)];
            String lastName = Constants.LAST_NAMES[Randomizer.getRandomInt(0,Constants.LAST_NAMES.length-1)];
            postmen.add(new JuniorPostman(firstName,lastName,this));
            firstName = Constants.FIRST_NAMES[Randomizer.getRandomInt(0,Constants.FIRST_NAMES.length-1)];
            lastName = Constants.LAST_NAMES[Randomizer.getRandomInt(0,Constants.LAST_NAMES.length-1)];
            SeniorPostman seniorPostman = new SeniorPostman(firstName,lastName,this);
            postmen.add(seniorPostman);
        }
    }

    public void work(){
        for(Postman p: postmen){
            p.start();
        }
    }

    public synchronized void sendShipment(Citizen citizen){
        Shipment shipment = citizen.getShipment();
        if(shipment.getType().equals("Parcel")){
            storage.add(shipment);
            System.out.println(citizen.getFirstName()+" "+citizen.getLastName()+" send a "+shipment.getType()+" at PostOffice");

        }
        else {
            if(new Random().nextBoolean()){
                PostBox postBox = postBoxes.get(Randomizer.getRandomInt(0,postBoxes.size()-1));
                postBox.addLetter((Letter) shipment);
                System.out.println(citizen.getFirstName()+" "+citizen.getLastName()+" send a "+shipment.getType()+" via PostBox");
            }
            else {
                storage.add(shipment);
                System.out.println(citizen.getFirstName()+" "+citizen.getLastName()+" send a "+shipment.getType()+" at PostOffice");

            }
        }
        turnover+=shipment.getPrice();
        putShipmentDetailsInArchive(shipment);
        putShipmentDetailsInDB(shipment);
        notifyAll();
    }

    public synchronized void collectLetters(JuniorPostman juniorPostman){
        if(storage.size()>=50){
            try {
                System.out.println("Too many shipments at office! "+juniorPostman.getFirstName()+" "+juniorPostman.getLastName()+" is waiting!");
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        else {
            for(PostBox pb: postBoxes){
                if(pb.getLetters().size()>0){
                    System.out.println(juniorPostman.getFirstName()+" "+juniorPostman.getLastName()+" collected "+pb.getLetters().size()+" letters! from PB "+pb.getPostBoxId());
                    juniorPostman.addLetters(pb.emptyBox());
                }
            }
        }
    }

    public synchronized void putLettersInStorage(JuniorPostman juniorPostman){
        ArrayList<Letter> letters = new ArrayList<>();
        letters.addAll(juniorPostman.emptyBag());
        for(Letter l: letters){
            putShipmentDetailsInArchive(l);
            putShipmentDetailsInDB(l);
            storage.add(l);
        }
        if(storage.size()>=50){
            itemsForDeliver.addAll(storage);
            storage.clear();
            notifyAll();
        }
    }

    public synchronized void takeShipments(SeniorPostman seniorPostman){
        if(itemsForDeliver.size()==0){
            System.out.println("No shipments for deliver "+seniorPostman.getFirstName()+" "+seniorPostman.getLastName()+" is waiting!");
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        else {
            for(int i = 0; i<Constants.PACKAGES_PER_SENIOR_POSTMAN; i++){
                if(itemsForDeliver.size()>0){
                    try {
                        Shipment shipment = itemsForDeliver.take();
                        seniorPostman.addShipment(shipment);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private synchronized void putShipmentDetailsInArchive(Shipment sh){
        if(!archive.containsKey(sh.getDate())){
            archive.put(sh.getDate(), new ConcurrentSkipListMap<>());
        }
        if(!archive.get(sh.getDate()).containsKey(sh.getType())){
            archive.get(sh.getDate()).put(sh.getType(), new CopyOnWriteArrayList<>());
        }
        archive.get(sh.getDate()).get(sh.getType()).add(sh.getShipmentID());
    }


    private synchronized void putShipmentDetailsInDB(Shipment sh){
        Connection connection = DBConnector.getInstance().getConnection();
        String insertQuery = "INSERT INTO archive (shipment_type, sent_at, sender_names, sender_address, receiver_names, receiver_address, price) VALUES (?,?,?,?,?,?,?);";
        try(PreparedStatement ps = connection.prepareStatement(insertQuery)){
            ps.setString(1,sh.getType());
            ps.setDate(2,Date.valueOf(sh.getDate()));
            ps.setString(3,sh.getSenderNames());
            ps.setString(4,sh.getSenderAddress());
            ps.setString(5,sh.getReceiverNames());
            ps.setString(6,sh.getReceiverAddress());
            ps.setDouble(7,sh.getPrice());
            ps.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Problem with insert query");
        } finally {
            DBConnector.getInstance().closeConnection();
        }
    }
}
