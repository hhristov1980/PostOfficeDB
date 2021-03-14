package main.shipment;

import java.time.LocalDate;

public class Letter extends Shipment{
    public Letter(String senderNames, String senderAddress, String receiverNames, String receiverAddress, LocalDate date, int senderId) {
        super(senderNames, senderAddress, receiverNames, receiverAddress, date, senderId);
    }

    @Override
    protected String validateType() {
        return "Letter";
    }

    @Override
    protected double validatePrice() {
        return 0.5;
    }

    @Override
    protected boolean validateFragile() {
        return false;
    }

    @Override
    protected int validateLength() {
        return 10;
    }

    @Override
    protected int validateWidth() {
        return 5;
    }

    @Override
    protected int validateHeight() {
        return 0;
    }
}
