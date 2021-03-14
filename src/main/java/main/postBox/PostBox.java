package main.postBox;

import main.shipment.Letter;

import java.util.concurrent.CopyOnWriteArrayList;

public class PostBox {
    private static int uniqueId = 1;
    private int postBoxId;
    private CopyOnWriteArrayList<Letter> letters;

    public PostBox(){
        this.postBoxId = uniqueId++;
        letters = new CopyOnWriteArrayList<>();
    }

    public void addLetter(Letter l){
        letters.add(l);
    }
    public CopyOnWriteArrayList<Letter> emptyBox(){
        CopyOnWriteArrayList <Letter> letters1 = new CopyOnWriteArrayList<>();
        for(Letter l: letters){
            letters1.add(l);
        }
        letters.clear();
        return letters1;
    }

    public int getPostBoxId() {
        return postBoxId;
    }

    public CopyOnWriteArrayList<Letter> getLetters() {
        return letters;
    }
}
