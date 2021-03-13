package main.shipment;

import main.util.Randomizer;


import java.sql.Date;
import java.time.LocalDate;
import java.util.Random;

public class Parcel extends Shipment{
    private double length;
    private double width;
    private double height;


    public Parcel(String senderNames, String senderAddress, String receiverNames, String receiverAddress, LocalDate date, int senderId) {
        super(senderNames, senderAddress, receiverNames, receiverAddress,date,senderId);
        //random generated dimensions and characteristics

        this.length = Randomizer.getRandomInt(5,120);
        this.width = Randomizer.getRandomInt(5,120);
        this.height = Randomizer.getRandomInt(5,120);

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

}
