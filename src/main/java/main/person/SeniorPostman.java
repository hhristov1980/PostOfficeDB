package main.person;

import main.postOffice.PostOffice;
import main.shipment.Shipment;
import main.util.Randomizer;

import java.util.concurrent.CopyOnWriteArrayList;

public class SeniorPostman extends Postman{
    private CopyOnWriteArrayList<Shipment> shipments;
    public SeniorPostman(String firstName, String lastName, PostOffice postOffice) {
        super(firstName, lastName, postOffice);
        shipments = new CopyOnWriteArrayList<>();
    }

    @Override
    protected int validateExperience() {
        return Randomizer.getRandomInt(3,20);
    }

    @Override
    public void run() {
        while (true) {
            postOffice.takeShipments(this);
            deliverLetters();
        }
    }

    public void addShipment(Shipment sh){
        shipments.add(sh);
    }

    private void deliverLetters(){
        for(Shipment sh: shipments){
            shipments.remove(sh);
            if(sh.getType().equals("Letter")){
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            else {
                try {
                    Thread.sleep(1500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            System.out.println(getFirstName()+" "+getLastName()+" delivered "+sh.getType()+" to "+sh.getReceiverNames()+" from "+sh.getSenderNames());
        }
    }
}
