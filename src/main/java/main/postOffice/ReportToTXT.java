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

public class ReportToTXT extends Thread{

    private PostOffice postOffice;

    public ReportToTXT(PostOffice postOffice){
        this.postOffice = postOffice;
    }

    @Override
    public void run() {
        while (true){
            try {
                Thread.sleep(30000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            LocalDateTime time = LocalDateTime.now();
            File f = new File("Report at: "+time+".txt");
            try(PrintStream ps = new PrintStream(f)){
                ps.println("=================== REPORT AT: "+time+"==================");
                printTurnover(ps);
                printShipmentsStatistics(ps);
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
            ps.println("--------------------------------");
            ps.println();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
//        finally {
//            DBConnector.getInstance().closeConnection();
//        }
    }

    private synchronized void printShipmentsStatistics(PrintStream ps){
        Connection connection = DBConnector.getInstance().getConnection();
        String selectQuery1 = "SELECT shipment_type, COUNT(shipment_type) FROM archive GROUP BY shipment_type;";
        String selectQuery2 = "SELECT COUNT(shipment_type)*100/(SELECT COUNT(shipment_type) FROM archive) FROM archive WHERE shipment_type = \"Letter\";";
        String selectQuery3 = "SELECT CONCAT(c.first_name,\" \",c.last_name), COUNT(a.collected_by) FROM collectors c LEFT JOIN archive a ON c.collector_id = a.collected_by GROUP BY c.collector_id, a.collected_by;";
        String selectQuery4 = "SELECT CONCAT(d.first_name,\" \",d.last_name), COUNT(a.delivered_by) FROM deliverers d LEFT JOIN archive a ON d.deliverer_id = a.delivered_by GROUP BY d.deliverer_id, a.delivered_by;";
        String selectQuery5 = "SELECT CONCAT(d.first_name,\" \",d.last_name), COUNT(a.shipment_type) FROM deliverers d LEFT JOIN archive a ON d.deliverer_id = a.delivered_by WHERE a.shipment_type = \"Letter\" GROUP BY d.deliverer_id, a.delivered_by ;";
        String selectQuery6 = "SELECT CONCAT(d.first_name,\" \",d.last_name), COUNT(a.shipment_type) FROM deliverers d LEFT JOIN archive a ON d.deliverer_id = a.delivered_by WHERE a.shipment_type = \"Parcel\" GROUP BY d.deliverer_id, a.delivered_by ;";
        int totalShipments = 0;
        try(Statement st1 = connection.createStatement();
            Statement st2 = connection.createStatement();
            Statement st3 = connection.createStatement();
            Statement st4 = connection.createStatement();
            Statement st5 = connection.createStatement();
            Statement st6 = connection.createStatement()){
           ResultSet rows1 = st1.executeQuery(selectQuery1);
           while (rows1.next()){
               ps.println(rows1.getString(1)+": \t"+rows1.getInt(2));
               totalShipments+=rows1.getInt(2);
           }
           ps.println("Total shipments: "+totalShipments);
            ps.println("--------------------------------");
            ps.println();
           ResultSet rows2 = st2.executeQuery(selectQuery2);
           rows2.next();
           ps.println("Total letter percentage: "+Math.round(rows2.getDouble(1))+"%");
            ps.println("--------------------------------");
            ps.println();
           ResultSet rows3 = st3.executeQuery(selectQuery3);
           ps.println("Collected shipments by Post Office or Collector:");
           while (rows3.next()){
               ps.println(rows3.getString(1)+" collected "+rows3.getInt(2)+ " shipments.");
           }
            ps.println("--------------------------------");
            ps.println();
           ResultSet rows4 = st4.executeQuery(selectQuery4);
            ps.println("Delivered shipments by:");
            while (rows4.next()){
                ps.println(rows4.getString(1)+" delivered "+rows4.getInt(2)+ " shipments.");
            }
            ps.println("--------------------------------");
            ps.println();
            ResultSet rows5 = st5.executeQuery(selectQuery5);
            ps.println("Delivered letters by:");
            while (rows5.next()){
                ps.println(rows5.getString(1)+" delivered "+rows5.getInt(2)+ " letters.");
            }
            ps.println("--------------------------------");
            ps.println();
            ResultSet rows6 = st6.executeQuery(selectQuery6);
            ps.println("Delivered parcels by:");
            while (rows6.next()){
                ps.println(rows6.getString(1)+" delivered "+rows6.getInt(2)+ " parcels.");
            }
            ps.println("--------------------------------");
            ps.println();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
