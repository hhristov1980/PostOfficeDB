package main.shipment;

import main.exceptions.WrongShipmentData;

import java.sql.Date;
import java.time.LocalDate;


public abstract class Shipment {

    private int shipmentID;
    private String type;
    private int senderId;
    private int postman_id;
    private String senderNames;
    private String senderAddress;
    private String receiverNames;
    private String receiverAddress;
    private double price;
    private LocalDate date;
    protected boolean isFragile;
    protected int length;
    protected int width;
    protected int height;

    public Shipment (String senderNames, String senderAddress, String receiverNames, String receiverAddress, LocalDate date, int senderId){
        if(checker(senderNames,senderAddress,receiverNames,receiverAddress)){
            this.senderNames = senderNames;
            this.senderAddress = senderAddress;
            this.receiverNames = receiverNames;
            this.receiverAddress = receiverAddress;
            this.senderId = senderId;
            this.type = validateType();
            this.isFragile = validateFragile();
            this.length = validateLength();
            this.width = validateWidth();
            this.height = validateHeight();
            this.price = validatePrice();
            this.date = date;

        }
        else {
            try {
                throw new WrongShipmentData();
            } catch (WrongShipmentData e) {
                System.out.println("You put wrong data on your shipment! Please make a new one!");
            }
        }

    }
    protected abstract String validateType();
    protected abstract boolean validateFragile();
    protected abstract int validateLength();
    protected abstract int validateWidth();
    protected abstract int validateHeight();
    protected abstract double validatePrice();



    private boolean checker(String senderNames, String senderAddress, String receiverNames, String receiverAddress){
        if(senderNames.length()>0 && senderAddress.length()>0 && receiverNames.length()>0 && receiverAddress.length()>0){
            return !senderNames.equals(receiverNames) || !senderAddress.equals(receiverAddress);
        }
        return false;
    }

    public String getType() {
        return type;
    }

    public String getSenderNames() {
        return senderNames;
    }

    public double getPrice() {
        return price;
    }

    public String getReceiverNames() {
        return receiverNames;
    }

    public LocalDate getDate() {
        return date;
    }

    public int getShipmentID() {
        return shipmentID;
    }

    public String getReceiverAddress() {
        return receiverAddress;
    }

    public String getSenderAddress() {
        return senderAddress;
    }

    public boolean isFragile() {
        return isFragile;
    }

    public void setPostman_id(int postman_id) {
        this.postman_id = postman_id;
    }

    public int getPostman_id() {
        return postman_id;
    }

    public int getSenderId() {
        return senderId;
    }

    public void setShipmentID(int shipmentID) {
        this.shipmentID = shipmentID;
    }
}
