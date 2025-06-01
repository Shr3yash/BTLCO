package com.batelco.migration;

import com.batelco.migration.db.DatabaseConnector;
import com.batelco.migration.xml.*;
import java.sql.*;
import java.util.Scanner;

public class Main {
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        try (Connection connection = DatabaseConnector.connect()) {
            System.out.println("Select XML type to generate:");
            System.out.println("1. Customer Accounts (CA)");
            System.out.println("2. Service Accounts (SA)");
            System.out.println("3. Billing Accounts (BA)");
            System.out.println("4. Department Accounts (DA)");
            System.out.println("5. Generate All");
            System.out.println("6. Generate Only Customer (CA) XML");
            System.out.print("Enter your choice (1-6): ");

            int choice = scanner.nextInt();
            processChoice(connection, choice);

        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void processChoice(Connection conn, int choice) throws SQLException {
        switch (choice) {
            case 1 -> {
                generateXML(conn, "Customer", "stg_cust_acct_t", XMLGenerator::generateXML);

                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery("SELECT DISTINCT is_cons_acct, is_enter_acct FROM stg_cust_acct_t")) {

                    boolean isConsumer = false;
                    boolean isEnterprise = false;

                    while (rs.next()) {
                        if ("yes".equalsIgnoreCase(rs.getString("is_cons_acct"))) {
                            isConsumer = true;
                        }
                        if ("yes".equalsIgnoreCase(rs.getString("is_enter_acct"))) {
                            isEnterprise = true;
                        }
                    }

                    if (isConsumer) {
                        generateXML(conn, "Billing", "stg_bill_Acct_T", BAXMLGenerator::generateXML);
                        generateXML(conn, "Service", "stg_srvc_acct_t", ServiceAccountXMLGenerator::generateXML);
                    }
                    if (isEnterprise) {
                        generateXML(conn, "Department", "stg_dept_acct_t", DepartmentAccountXMLGenerator::generateXML);
                        generateXML(conn, "Billing", "stg_bill_Acct_T", BAXMLGenerator::generateXML);
                        generateXML(conn, "Service", "stg_srvc_acct_t", ServiceAccountXMLGenerator::generateXML);
                    }

                } catch (SQLException e) {
                    System.out.println("Error reading flags: " + e.getMessage());
                }
            }
            case 2 -> generateXML(conn, "Service", "stg_srvc_acct_t", ServiceAccountXMLGenerator::generateXML);
            case 3 -> generateXML(conn, "Billing", "stg_bill_Acct_T", BAXMLGenerator::generateXML);
            case 4 -> generateXML(conn, "Department", "stg_dept_acct_t", DepartmentAccountXMLGenerator::generateXML);
            case 5 -> generateAllXMLs(conn);
            case 6 -> generateXML(conn, "Customer", "stg_cust_acct_t", XMLGenerator::generateXML); // Only CA
            default -> System.out.println("Invalid choice!");
        }
    }

    private static void generateAllXMLs(Connection conn) throws SQLException {
        generateXML(conn, "Customer", "stg_cust_acct_t", XMLGenerator::generateXML);
        generateXML(conn, "Service", "stg_srvc_acct_t", ServiceAccountXMLGenerator::generateXML);
        generateXML(conn, "Billing", "stg_bill_Acct_T", BAXMLGenerator::generateXML);
        generateXML(conn, "Department", "stg_dept_acct_t", DepartmentAccountXMLGenerator::generateXML);
    }

    private static void generateXML(Connection conn, String type, String table, XMLGeneratorInterface generator)
            throws SQLException {
        String outputFile = String.format("Create_%s_CMT_Input.xml", type);
        String query = "SELECT * FROM " + table;
        generator.generate(conn, query, outputFile);
        System.out.println("Generated " + outputFile);
    }

    @FunctionalInterface
    private interface XMLGeneratorInterface {
        void generate(Connection conn, String query, String outputFile) throws SQLException;
    }
}
