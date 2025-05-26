package com.batelco.migration;

import com.batelco.migration.db.DatabaseConnector;
import com.batelco.migration.xml.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        try (Connection connection = DatabaseConnector.connect()) {
            System.out.println("Select XMLs to generate:");
            System.out.println("1. Customer Accounts (CA)");
            System.out.println("2. Service Accounts (SA)");
            System.out.println("3. Billing Accounts (BA)");
            System.out.println("4. Department Accounts (DA)");
            System.out.println("5. Generate All");
            System.out.print("Enter your choice (1-5): ");
            
            int choice = scanner.nextInt();
            processChoice(connection, choice);

        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void processChoice(Connection conn, int choice) throws SQLException {
        switch (choice) {
            case 1 -> generateCAWithDependents(conn);
            case 2 -> generateXML(conn, "Service", "stg_srvc_acct_t", ServiceAccountXMLGenerator::generateXML);
            case 3 -> generateXML(conn, "Billing", "stg_bill_Acct_T", BAXMLGenerator::generateXML);
            case 4 -> generateXML(conn, "Department", "stg_dept_acct_t", DepartmentAccountXMLGenerator::generateXML);
            case 5 -> generateAllXMLs(conn);
            default -> System.out.println("Invalid choice!");
        }
    }

    private static void generateCAWithDependents(Connection conn) throws SQLException {
        // Generate CA XML
        generateXML(conn, "Customer", "stg_cust_acct_t", XMLGenerator::generateXML);
        
        // Process flags and generate dependents
        List<AccountInfo> accounts = getAccountFlags(conn);
        for (AccountInfo account : accounts) {
            if ("YES".equalsIgnoreCase(account.isEnterAcct())) {
                generateDependentXML(conn, account.accountNo(), "stg_dept_acct_t", DepartmentAccountXMLGenerator::generateXML);
                generateDependentXML(conn, account.accountNo(), "stg_bill_Acct_T", BAXMLGenerator::generateXML);
                generateDependentXML(conn, account.accountNo(), "stg_srvc_acct_t", ServiceAccountXMLGenerator::generateXML);
            }
            if ("YES".equalsIgnoreCase(account.isConsAcct())) {
                generateDependentXML(conn, account.accountNo(), "stg_bill_Acct_T", BAXMLGenerator::generateXML);
                generateDependentXML(conn, account.accountNo(), "stg_srvc_acct_t", ServiceAccountXMLGenerator::generateXML);
            }
        }
    }

    private static void generateAllXMLs(Connection conn) throws SQLException {
        generateCAWithDependents(conn);
        generateXML(conn, "Service", "stg_srvc_acct_t", ServiceAccountXMLGenerator::generateXML);
        generateXML(conn, "Billing", "stg_bill_Acct_T", BAXMLGenerator::generateXML);
        generateXML(conn, "Department", "stg_dept_acct_t", DepartmentAccountXMLGenerator::generateXML);
    }

    private static List<AccountInfo> getAccountFlags(Connection conn) throws SQLException {
        List<AccountInfo> accounts = new ArrayList<>();
        String query = "SELECT account_no, is_enter_acct, is_cons_acct FROM stg_cust_acct_t";
        
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                accounts.add(new AccountInfo(
                    rs.getString("account_no"),
                    rs.getString("is_enter_acct"),
                    rs.getString("is_cons_acct")
                ));
            }
        }
        return accounts;
    }

    private static void generateXML(Connection conn, String type, String table, XMLGeneratorInterface generator) 
            throws SQLException {
        String outputFile = String.format("Create_%s_CMT_Input.xml", type);
        String query = "SELECT * FROM " + table;
        generator.generate(conn, query, outputFile);
    }

    private static void generateDependentXML(Connection conn, String accountNo, String table, XMLGeneratorInterface generator) 
            throws SQLException {
        String outputFile = String.format("%s_%s.xml", table.replace("stg_", "").replace("_t", ""), accountNo);
        String query = String.format("SELECT * FROM %s WHERE account_no = '%s'", table, accountNo);
        generator.generate(conn, query, outputFile);
    }

    @FunctionalInterface
    private interface XMLGeneratorInterface {
        void generate(Connection conn, String query, String outputFile) throws SQLException;
    }

    private record AccountInfo(String accountNo, String isEnterAcct, String isConsAcct) {}
}