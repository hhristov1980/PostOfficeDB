package main.person;

import main.postOffice.PostOffice;
import main.shipment.Letter;
import main.util.Randomizer;

import java.util.concurrent.CopyOnWriteArrayList;

public class JuniorPostman extends Postman{
    CopyOnWriteArrayList<Letter> letters;
    public JuniorPostman(String firstName, String lastName, PostOffice postOffice) {
        super(firstName, lastName, postOffice);
        letters = new CopyOnWriteArrayList<>();
    }

    @Override
    protected int validateExperience() {
        return Randomizer.getRandomInt(0,3);
    }

    @Override
    public void run() {
        while (true) {
            postOffice.collectLetters(this);
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            postOffice.putLettersInStorage(this);
        }

    }

    public CopyOnWriteArrayList<Letter> emptyBag(){
        CopyOnWriteArrayList <Letter> letters1 = new CopyOnWriteArrayList<>();
        for(Letter l: letters){
            letters1.add(l);
        }
        letters.clear();
        return letters1;
    }

    public void addLetters(CopyOnWriteArrayList<Letter> letters){
        this.letters.addAll(letters);
    }
}
