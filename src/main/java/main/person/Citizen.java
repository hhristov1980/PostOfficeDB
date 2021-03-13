package main.person;

import main.postOffice.PostOffice;
import main.shipment.Letter;
import main.shipment.Parcel;
import main.shipment.Shipment;
import main.util.Constants;
import main.util.Randomizer;

import java.sql.Date;
import java.time.LocalDate;
import java.util.Random;

public class Citizen extends Person{
    private String address;
    private Shipment shipment;
    private int shipmentsToSend;


    public Citizen(String firstName, String lastName, String address, PostOffice postOffice) {
        super(firstName, lastName, postOffice);

        if(address.length()>0){
            this.address = address;
        }
        this.shipmentsToSend = Randomizer.getRandomInt(1,5);
    }

    @Override
    public void run() {
        for(int i = 0; i<shipmentsToSend; i++) {
            while (shipment == null) {
                createShipment();
                postOffice.sendShipment(this);

            }
            shipment = null;
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void createShipment(){
        String firstName = Constants.FIRST_NAMES[Randomizer.getRandomInt(0,Constants.FIRST_NAMES.length-1)];
        String lastName = Constants.LAST_NAMES[Randomizer.getRandomInt(0,Constants.LAST_NAMES.length-1)];
        String address = Constants.ADDRESS[Randomizer.getRandomInt(0,Constants.ADDRESS.length-1)];
        LocalDate date = LocalDate.now();

        this.shipment = new Random().nextBoolean()? new Letter(getFirstName()+" "+getLastName(),this.address,firstName+" "+lastName,address,date,personId)
                : new Parcel(getFirstName()+" "+getLastName(),this.address,firstName+" "+lastName,address,date,personId);

    }

    public String getAddress() {
        return address;
    }

    public Shipment getShipment() {
        return shipment;
    }

}
