package orm;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class dbAdapter {
    //=====================================
    //private static Connection dbconnection = getConnection();

    private static dbAdapter dbadapter = null;
    private Connection connection;
    private Statement Stmt;

    //=====================================
    private dbAdapter() {
        ArrayList<Object> values = this.CreateConnection();
        this.connection = (Connection) values.get(0);
        this.Stmt = (Statement) values.get(1);
    }
    //=====================================
    private ArrayList<Object> CreateConnection() {
        Connection connection = null;
        Statement statement = null;
        String host = "localhost";
        String port = "3306";
        String dbname = this.getClass().getPackage().getName();
        String user = "root";
        String password = "";

        final String ConnectionString = "jdbc:mysql://" + host + ":" + port + "/";
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println(e.getMessage());
        }
        try {
            connection = DriverManager.getConnection(ConnectionString, user, password);
            statement = connection.createStatement();
            ResultSet resultSet = connection.getMetaData().getCatalogs();
            boolean databaseExists=false;
            //iterate each catalog in the ResultSet
            while (resultSet.next()) {
                // Get the database name, which is at position 1
                String databaseName = resultSet.getString(1);
                if (databaseName.equals(dbname)) {
                    databaseExists = true;
                }
            }
            resultSet.close();
            if (!databaseExists) {
                String dbCreate = "CREATE DATABASE " + dbname;
            statement.executeUpdate(dbCreate);
            System.out.println("Database "+dbname+" created successfully.");
            }
            else{
              System.out.println("Database already exists!");
            }
            final String ConnectionString2 = ConnectionString + dbname;
            connection = DriverManager.getConnection(ConnectionString2, user, password);
            System.out.println("Connection to database initialised.");
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        ArrayList<Object> return_values = new ArrayList<>();
        return_values.add(connection);
        return_values.add(statement);
        return  return_values;
    }

    //=====================================
    public Connection getConnection() {
        return connection;
    }

    //=====================================
    public void endConnection() {
        try {
            getStmt().close();
            connection.close();
            System.out.println("Connection is now closed.");
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }
    //=====================================

    public static dbAdapter getInstance() {
        if (dbadapter == null) {
            dbadapter = new dbAdapter();
            
        }
        return dbadapter;
    }
    //=====================================

    public Statement getStmt() {
        return this.Stmt;
    }

    private void setStmt(Statement Stmt) {
        this.Stmt = Stmt;
    }

}
