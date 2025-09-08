package com.batelco.migration;

import com.batelco.migration.db.DatabaseConnector;
import com.batelco.migration.xml.*;
import java.sql.*;
import java.util.Scanner;

public class Main {
    private static final Scanner scanner = new Scanner(System.in);
    private static final int BATCH_SIZE = 1000;

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
                // Generate CA first
                generateXMLBatched(conn, "Customer", "stg_cust_acct_t", "ACCOUNT_NO", XMLGenerator::generateXML);

                // Then decide what else to generate based on flags in stg_cust_acct_t
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
                        generateXMLBatched(conn, "Billing", "stg_bill_Acct_T", "ACCOUNT_NO", BAXMLGenerator::generateXML);
                        generateXMLBatched(conn, "Service", "stg_srvc_acct_t", "ACCOUNT_NO", ServiceAccountXMLGenerator::generateXML);
                    }
                    if (isEnterprise) {
                        generateXMLBatched(conn, "Department", "stg_dept_acct_t", "ACCOUNT_NO", DepartmentAccountXMLGenerator::generateXML);
                        generateXMLBatched(conn, "Billing", "stg_bill_Acct_T", "ACCOUNT_NO", BAXMLGenerator::generateXML);
                        generateXMLBatched(conn, "Service", "stg_srvc_acct_t", "ACCOUNT_NO", ServiceAccountXMLGenerator::generateXML);
                    }

                } catch (SQLException e) {
                    System.out.println("Error reading flags: " + e.getMessage());
                }
            }
            case 2 -> generateXMLBatched(conn, "Service", "stg_srvc_acct_t", "ACCOUNT_NO", ServiceAccountXMLGenerator::generateXML);
            case 3 -> generateXMLBatched(conn, "Billing", "stg_bill_Acct_T", "ACCOUNT_NO", BAXMLGenerator::generateXML);
            case 4 -> generateXMLBatched(conn, "Department", "stg_dept_acct_t", "ACCOUNT_NO", DepartmentAccountXMLGenerator::generateXML);
            case 5 -> generateAllXMLs(conn);
            case 6 -> generateXMLBatched(conn, "Customer", "stg_cust_acct_t", "ACCOUNT_NO", XMLGenerator::generateXML); // Only CA
            default -> System.out.println("Invalid choice!");
        }
    }

    private static void generateAllXMLs(Connection conn) throws SQLException {
        generateXMLBatched(conn, "Customer", "stg_cust_acct_t", "ACCOUNT_NO", XMLGenerator::generateXML);
        generateXMLBatched(conn, "Service", "stg_srvc_acct_t", "ACCOUNT_NO", ServiceAccountXMLGenerator::generateXML);
        generateXMLBatched(conn, "Billing", "stg_bill_Acct_T", "ACCOUNT_NO", BAXMLGenerator::generateXML);
        generateXMLBatched(conn, "Department", "stg_dept_acct_t", "ACCOUNT_NO", DepartmentAccountXMLGenerator::generateXML);
    }

    /**
     * Generates XML with files split into batches of 1000 accounts each.
     * Filenames follow: CMT_CA_1.xml, CMT_CA_2.xml, ... (or DA/BA/SA accordingly).
     *
     * @param conn       JDBC connection
     * @param type       Logical type name: "Customer", "Service", "Billing", "Department"
     * @param table      Source table name
     * @param orderCol   Column used for deterministic ordering (e.g., ACCOUNT_NO)
     * @param generator  Generator method reference (conn, query, outputFile)
     */
    private static void generateXMLBatched(Connection conn,
                                           String type,
                                           String table,
                                           String orderCol,
                                           XMLGeneratorInterface generator) throws SQLException {
        final int total = countRows(conn, table);
        if (total == 0) {
            System.out.println("No data found for table: " + table);
            return;
        }

        final String prefix = switch (type) {
            case "Customer" -> "CMT_CA";
            case "Service" -> "CMT_SA";
            case "Billing" -> "CMT_BA";
            case "Department" -> "CMT_DA";
            default -> "CMT_" + type.toUpperCase().charAt(0) + type.toUpperCase().charAt(1); // fallback
        };

        int fileIndex = 1;
        for (int startRow = 1; startRow <= total; startRow += BATCH_SIZE) {
            int endRow = Math.min(startRow + BATCH_SIZE - 1, total);
            String paginatedQuery = buildPaginatedQuery(table, orderCol, startRow, endRow);
            String outputFile = String.format("%s_%d.xml", prefix, fileIndex);

            generator.generate(conn, paginatedQuery, outputFile);
            System.out.printf("Generated %s for rows %d..%d (%s)%n", outputFile, startRow, endRow, table);

            fileIndex++;
        }
    }

    /**
     * Counts rows in a given table.
     */
    private static int countRows(Connection conn, String table) throws SQLException {
        String sql = "SELECT COUNT(*) FROM " + table;
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }

    /**
     * Builds a pagination query that returns rows between startRow and endRow (inclusive),
     * using ROW_NUMBER() over an ORDER BY on the given column.
     *
     * This form works on Oracle (11g+ using analytic ROW_NUMBER), SQL Server, and most modern engines.
     * Adjust the ORDER BY column if needed.
     */
    private static String buildPaginatedQuery(String table, String orderCol, int startRow, int endRow) {
        // Ensure identifiers are used as given (simple concat; if you need quoting, add it here)
        return """
               SELECT * FROM (
                   SELECT t_inner.*, ROW_NUMBER() OVER (ORDER BY %s) rn
                   FROM %s t_inner
               )
               WHERE rn BETWEEN %d AND %d
               """.formatted(orderCol, table, startRow, endRow);
    }

    @FunctionalInterface
    private interface XMLGeneratorInterface {
        void generate(Connection conn, String query, String outputFile) throws SQLException;
    }
}
