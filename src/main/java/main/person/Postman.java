package main.person;

import main.postOffice.PostOffice;
import main.shipment.Shipment;

import java.util.concurrent.CopyOnWriteArrayList;

public abstract class Postman extends Person{
    private int experience;


    public Postman(String firstName, String lastName, PostOffice postOffice) {
        super(firstName, lastName, postOffice);
        this.experience = validateExperience();

    }


    protected abstract int validateExperience();

    @Override
    public abstract void run();
}
