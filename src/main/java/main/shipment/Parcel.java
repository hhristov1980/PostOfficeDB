package main.shipment;

import main.util.Randomizer;


import java.sql.Date;
import java.time.LocalDate;
import java.util.Random;

public class Parcel extends Shipment{



    public Parcel(String senderNames, String senderAddress, String receiverNames, String receiverAddress, LocalDate date, int senderId) {
        super(senderNames, senderAddress, receiverNames, receiverAddress,date,senderId);

    }

    @Override
    protected String validateType() {
        return "Parcel";
    }

    @Override
    protected double validatePrice() {
        double price = 2.0;
        if(isFragile){
            price*=1.5;
        }
        if(length>=60 || width>=60 || height>=60){
            price*=1.5;
        }
        return price;
    }

    @Override
    protected boolean validateFragile() {
        return new Random().nextBoolean();
    }

    @Override
    protected int validateLength() {
        return Randomizer.getRandomInt(20,120);
    }

    @Override
    protected int validateWidth() {
        return Randomizer.getRandomInt(10,100);
    }

    @Override
    protected int validateHeight() {
        return Randomizer.getRandomInt(5,80);
    }

}
