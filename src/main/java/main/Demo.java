package main;

import main.person.Citizen;
import main.postOffice.CitizenCreator;
import main.postOffice.PostOffice;
import main.postOffice.ReportToTXT;
import main.util.Constants;
import main.util.Randomizer;

public class Demo {
    public static void main(String[] args) {
        PostOffice postOffice = new PostOffice("It Post");
        postOffice.work();
        ReportToTXT reportToTXT = new ReportToTXT(postOffice);
        reportToTXT.setDaemon(true);
        reportToTXT.start();
        CitizenCreator citizenCreator = new CitizenCreator(postOffice);
        citizenCreator.start();
//        for(int i = 0; i<100; i++){
//            String firstName = Constants.FIRST_NAMES[Randomizer.getRandomInt(0,Constants.FIRST_NAMES.length-1)];
//            String lastName = Constants.LAST_NAMES[Randomizer.getRandomInt(0,Constants.LAST_NAMES.length-1)];
//            String address = Constants.ADDRESS[Randomizer.getRandomInt(0,Constants.ADDRESS.length-1)];
//            Citizen sender = new Citizen(firstName,lastName,address,postOffice);
//
//            sender.start();
//            try {
//                Thread.sleep(100);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }

    }


}
