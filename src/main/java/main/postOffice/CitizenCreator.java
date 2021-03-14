package main.postOffice;

import com.mysql.cj.xdevapi.Collection;
import main.connector.DBConnector;
import main.person.Citizen;
import main.util.Constants;
import main.util.Randomizer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class CitizenCreator extends Thread{
    private PostOffice postOffice;

    public CitizenCreator(PostOffice postOffice){
        this.postOffice = postOffice;
    }

    @Override
    public void run() {
        while(true){
            String firstName = Constants.FIRST_NAMES[Randomizer.getRandomInt(0,Constants.FIRST_NAMES.length-1)];
            String lastName = Constants.LAST_NAMES[Randomizer.getRandomInt(0,Constants.LAST_NAMES.length-1)];
            String address = Constants.ADDRESS[Randomizer.getRandomInt(0,Constants.ADDRESS.length-1)];
            Citizen sender = new Citizen(firstName,lastName,address,postOffice);
            registerInPostOfficeSystem(sender);
            sender.start();
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private synchronized void registerInPostOfficeSystem(Citizen sender){
        Connection connection = DBConnector.getInstance().getConnection();
        String insertQuery = "INSERT INTO senders(sender_id, first_name, last_name) VALUES (?,?,?);";
        try(PreparedStatement ps = connection.prepareStatement(insertQuery)){
            ps.setInt(1,sender.getPersonId());
            ps.setString(2, sender.getFirstName());
            ps.setString(3, sender.getLastName());
            ps.executeUpdate();
        } catch (SQLException throwables) {
            System.out.println("Problem with sender register");
        }
//        finally {
//            DBConnector.getInstance().closeConnection();
//        }
    }
}
