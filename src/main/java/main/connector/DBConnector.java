package main.connector;

import com.google.gson.Gson;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnector {
    private static DBConnector instance;
    private Connection connection;
    private DBCredentials credentials;


    public static DBConnector getInstance() {
        if(instance ==null){
            instance = new DBConnector();
        }
        return instance;
    }

    private DBConnector(){
        loadCredentials();
        if(credentials != null){
            initializeConnection();
        }
    }


    private void initializeConnection(){
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            try {
                connection = DriverManager.getConnection("jdbc:mysql://" + credentials.host + ":" + credentials.port + credentials.dbScheme, credentials.username, credentials.password);
            } catch (SQLException e) {
                System.out.println("Sorry! Communication error!");
            }
        } catch (ClassNotFoundException e) {
            System.out.println("Sorry! You cannot use mySQL!");
        }
    }

    private void loadCredentials(){
        Gson gson = new Gson();
        try {
            DBCredentials credentials = gson.fromJson(new FileReader("db_settings.json"),DBCredentials.class);
            if(credentials==null){
                System.out.println("No db credentials in the file");
                return;
            }
            this.credentials = credentials;

        } catch (FileNotFoundException e) {
            System.out.println("Error while reading credentials from file " + e.getMessage() );
        }
    }


    private class DBCredentials{
        private String host;
        private String port;
        private String dbScheme;
        private String username;
        private String password;
    }

    public Connection getConnection() {
        if(connection==null){
            initializeConnection();
        }
        return connection;
    }

    public void closeConnection(){
        try {
            connection.close();
        } catch (SQLException e) {
            System.out.println("Fail while closing connection - " + e.getMessage());
        }
        connection = null;

    }
}
