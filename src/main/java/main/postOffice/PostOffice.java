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

import java.sql.*;
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
            JuniorPostman juniorPostman = new JuniorPostman(firstName,lastName,this);
            postmen.add(juniorPostman);
            registerCollector(juniorPostman);
            firstName = Constants.FIRST_NAMES[Randomizer.getRandomInt(0,Constants.FIRST_NAMES.length-1)];
            lastName = Constants.LAST_NAMES[Randomizer.getRandomInt(0,Constants.LAST_NAMES.length-1)];
            SeniorPostman seniorPostman = new SeniorPostman(firstName,lastName,this);
            postmen.add(seniorPostman);
            registerDeliverer(seniorPostman);
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
            putShipmentDetailsInDBByPO(shipment);
            putShipmentDetailsInArchive(shipment);

        }
        else {
            //20% chance letter to PO; 80% chance letter to PB
            if(Randomizer.getRandomInt(1,10)>2){
                PostBox postBox = postBoxes.get(Randomizer.getRandomInt(0,postBoxes.size()-1));
                postBox.addLetter((Letter) shipment);
                System.out.println(citizen.getFirstName()+" "+citizen.getLastName()+" send a "+shipment.getType()+" via PostBox");
            }
            else {
                storage.add(shipment);
                System.out.println(citizen.getFirstName()+" "+citizen.getLastName()+" send a "+shipment.getType()+" at PostOffice");
                putShipmentDetailsInDBByPO(shipment);
                putShipmentDetailsInArchive(shipment);

            }
        }
        turnover+=shipment.getPrice();
        notifyAll();
    }

    public synchronized void collectLetters(JuniorPostman juniorPostman){
        if(storage.size()>=50){
            try {
                System.out.println("Too many shipments at office! "+juniorPostman.getFirstName()+" "+juniorPostman.getLastName()+" is waiting!");
                notifyAll();
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
        System.out.println(juniorPostman.getFirstName()+" "+juniorPostman.getLastName()+" has "+letters.size()+" letters in his bag");
        for(Letter l: letters){
            putShipmentDetailsInArchive(l);
            putShipmentDetailsInDBByJP(l,juniorPostman);
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
                        updateShipmentInfo(shipment,seniorPostman);
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


    private synchronized void putShipmentDetailsInDBByJP(Shipment sh, JuniorPostman juniorPostman){
        String fragile = "No";
        if(sh.isFragile()){
            fragile = "Yes";
        }
        Connection connection = DBConnector.getInstance().getConnection();
        String insertQuery = "INSERT INTO archive (shipment_type, sent_at, sender_id, collected_by, fragile, price) VALUES (?,?,?,?,?,?);";
        try(PreparedStatement ps = connection.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS)){
            ps.setString(1,sh.getType());
            ps.setDate(2,Date.valueOf(sh.getDate()));
            ps.setInt(3,sh.getSenderId());
            ps.setInt(4,juniorPostman.getPersonId());
            ps.setString(5,fragile);
            ps.setDouble(6,sh.getPrice());
            ps.executeUpdate();
            ResultSet key = ps.getGeneratedKeys();
            key.next();
            int shipmentId = (int) key.getLong(1);
            sh.setShipmentID(shipmentId);

        } catch (SQLException e) {
            System.out.println("Problem with insert JP query");
        }
//        finally {
//            DBConnector.getInstance().closeConnection();
//        }
    }

    private synchronized void putShipmentDetailsInDBByPO(Shipment sh){
        String fragile = "No";
        if(sh.isFragile()){
            fragile = "Yes";
        }
        Connection connection = DBConnector.getInstance().getConnection();
        String insertQuery = "INSERT INTO archive (shipment_type, sent_at, sender_id, fragile, price) VALUES (?,?,?,?,?);";
        try(PreparedStatement ps = connection.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS)){
            ps.setString(1,sh.getType());
            ps.setDate(2,Date.valueOf(sh.getDate()));
            ps.setInt(3,sh.getSenderId());
            ps.setString(4,fragile);
            ps.setDouble(5,sh.getPrice());
            ps.executeUpdate();
            ResultSet key = ps.getGeneratedKeys();
            key.next();
            int shipmentId = (int) key.getLong(1);
            sh.setShipmentID(shipmentId);

        } catch (SQLException e) {
            System.out.println("Problem with insert PO query");
        }
//        finally {
//            DBConnector.getInstance().closeConnection();
//        }
    }

    private synchronized void registerDeliverer(SeniorPostman seniorPostman){
        Connection connection = DBConnector.getInstance().getConnection();
        String insertQuery = "INSERT INTO deliverers(deliverer_id, first_name, last_name) VALUES (?,?,?);";
        try(PreparedStatement ps = connection.prepareStatement(insertQuery)){
            ps.setInt(1,seniorPostman.getPersonId());
            ps.setString(2, seniorPostman.getFirstName());
            ps.setString(3, seniorPostman.getLastName());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Problem with postmen register");
        }
//        finally {
//            DBConnector.getInstance().closeConnection();
//        }
    }

    private synchronized void registerCollector(JuniorPostman juniorPostman){
        Connection connection = DBConnector.getInstance().getConnection();
        String insertQuery = "INSERT INTO collectors(collector_id, first_name, last_name) VALUES (?,?,?);";
        try(PreparedStatement ps = connection.prepareStatement(insertQuery)){
            ps.setInt(1,juniorPostman.getPersonId());
            ps.setString(2, juniorPostman.getFirstName());
            ps.setString(3, juniorPostman.getLastName());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Problem with postmen register");
        }
//        finally {
//            DBConnector.getInstance().closeConnection();
//        }
    }

    private synchronized void updateShipmentInfo(Shipment shipment, SeniorPostman seniorPostman){
        Connection connection = DBConnector.getInstance().getConnection();
        String updateQuery = "UPDATE archive SET delivered_by = ? WHERE shipment_id = ?;";
        try(PreparedStatement ps = connection.prepareStatement(updateQuery)){
            ps.setInt(1,seniorPostman.getPersonId());
            ps.setInt(2,shipment.getShipmentID());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Problem with update info");
        }
//        finally {
//            DBConnector.getInstance().closeConnection();
//        }
    }

}
