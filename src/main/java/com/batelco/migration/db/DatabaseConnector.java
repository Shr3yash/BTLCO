package com.batelco.migration.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnector {

    private static final String JDBC_URL = "jdbc:oracle:thin:@//10.5.119.157:1521/dev2brmpdb.db.dev.oraclevcn.com";
    private static final String USERNAME = "pin1";
    private static final String PASSWORD = "Cgbu1234";

    // Establish a connection to the database
    public static Connection connect() throws SQLException {
        try {
            return DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD);
        } catch (SQLException e) {
            System.out.println("Database connection error: " + e.getMessage());
            throw e;
        }
    }

    // Close the connection safely
    public static void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                System.out.println("Error closing the connection: " + e.getMessage());
            }
        }
    }
}
