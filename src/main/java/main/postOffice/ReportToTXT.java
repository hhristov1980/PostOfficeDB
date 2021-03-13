package main.postOffice;

import main.connector.DBConnector;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;

public class ReportToTXT extends Thread{

    private PostOffice postOffice;

    public ReportToTXT(PostOffice postOffice){
        this.postOffice = postOffice;
    }

    @Override
    public void run() {
        while (true){
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            LocalDateTime time = LocalDateTime.now();
            File f = new File("Report at: "+time+".txt");
            try(PrintStream ps = new PrintStream(f)){
                ps.println("=================== REPORT AT: "+time+"==================");
                printTurnover(ps);
                printTotalShipments(ps);
                //MORE UPDATES TO COME
                //MORE TABLES TO COME

                ps.println("============================ END OF REPORT ========================");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    private synchronized void printTurnover(PrintStream ps){
        Connection connection = DBConnector.getInstance().getConnection();
        String selectQuery = "SELECT SUM(price) FROM archive;";
        try(Statement st = connection.createStatement()) {
            ResultSet rs = st.executeQuery(selectQuery);
           rs.next();
           ps.println("Total turnover: "+rs.getDouble(1));
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } finally {
            DBConnector.getInstance().closeConnection();
        }
    }

    private synchronized void printTotalShipments(PrintStream ps){
        Connection connection = DBConnector.getInstance().getConnection();
        String selectQuery = "SELECT shipment_type, COUNT(shipment_type) FROM archive GROUP BY shipment_type;";
        String selectQuery2 = "SELECT COUNT(shipment_type)*100/(SELECT COUNT(shipment_type) FROM archive) FROM archive WHERE shipment_type = \"Letter\";";
        int totalShipments = 0;
        try(Statement st1 = connection.createStatement();
            Statement st2 = connection.createStatement()){
           ResultSet rows1 = st1.executeQuery(selectQuery);
           while (rows1.next()){
               ps.println(rows1.getString(1)+": "+rows1.getInt(2));
               totalShipments+=rows1.getInt(2);
           }
           ps.println("Total shipments: "+totalShipments);
           ResultSet rows2 = st2.executeQuery(selectQuery2);
           rows2.next();
           ps.println("Total letter percentage: "+Math.round(rows2.getDouble(1))+"%%");
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

    }
}
