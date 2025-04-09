package com.batelco.migration;

import java.sql.Connection;
import java.sql.SQLException;

import com.batelco.migration.db.DatabaseConnector;
import com.batelco.migration.xml.XMLGenerator;

public class Main {
    public static void main(String[] args) {
        // SQL Query to fetch the data
        String sqlQuery = "SELECT * FROM stg_cust_acct";
        
        // Output XML file path
        String outputFile = "outputBillingInfo.xml";
        
        // Generate XML
        // XMLGenerator.generateXML(sqlQuery, outputFile);
          try (Connection connection = DatabaseConnector.connect()) {
            // Call your method to generate the XML
            XMLGenerator.generateXML(connection, sqlQuery, outputFile);
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
