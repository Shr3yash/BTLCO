package com.batelco.migration.xml;

import com.batelco.migration.config.XmlTagMapping;
import com.batelco.migration.sql.QueryExecutor;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class XMLGenerator {
    public static void generateXML(Connection connection, String sqlQuery, String outputFile) {
        Map<String, String> tagMap = XmlTagMapping.getColumnToTagMap();
    
        try (ResultSet rs = QueryExecutor.executeQuery(connection, sqlQuery);
             FileOutputStream fos = new FileOutputStream(outputFile);
             OutputStreamWriter writer = new OutputStreamWriter(fos, StandardCharsets.UTF_8)) {
    
            // Debug: print the columns returned by the SQL query
            try {
                java.sql.ResultSetMetaData meta = rs.getMetaData();
                int columnCount = meta.getColumnCount();
                System.out.println("Columns returned by SQL query:");
                for (int i = 1; i <= columnCount; i++) {
                    System.out.println(" - " + meta.getColumnName(i));
                }
            } catch (SQLException metaEx) {
                System.out.println("Failed to read ResultSet metadata.");
                metaEx.printStackTrace();
            }
    
            // Write XML header
            writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            writer.write("<Sbsc " + XmlTagMapping.getRootAttributes() + ">\n");
    
            boolean foundRows = false;
            while (rs.next()) {
                foundRows = true;
                writeAccountElement(writer, rs, tagMap);
            }
    
            writer.write("</Sbsc>");
    
            if (foundRows) {
                System.out.println("XML file generated successfully: " + outputFile);
            } else {
                System.out.println("No data found for query: " + sqlQuery);
            }
    
        } catch (SQLException | IOException e) {
            System.out.println("Error while generating XML: " + e.getMessage());
            e.printStackTrace();
        }
    }
    

    private static void writeAccountElement(OutputStreamWriter writer, ResultSet rs,
                                            Map<String, String> tagMap)
            throws SQLException, IOException {
        String accountNo = XMLGenerationUtils.getColumnValue(rs, "ACCOUNT_NO");
        String formattedAccountNo = accountNo.startsWith("CA_")
                ? accountNo
                : "CA_" + accountNo;

        writer.write(String.format(
                "  <ActSbsc id=\"%s\" isParent=\"Y\">\n" +
                        "    <Act>\n",
                XMLGenerationUtils.escapeXml(formattedAccountNo)));

        // Write mapped elements
        XMLGenerationUtils.writeMappedElement(writer, rs, "ACCOUNT_NO", "ActNo", tagMap);
        XMLGenerationUtils.writeMappedElement(writer, rs, "CURRENCY", "Curr", tagMap);
        XMLGenerationUtils.writeMappedElement(writer, rs, "CUST_SEG_LIST", "CustSegList", tagMap);
        XMLGenerationUtils.writeMappedElement(writer, rs, "BUSINESS_TYPE", "BType", tagMap);
        XMLGenerationUtils.writeMappedElement(writer, rs, "AAC_ACCESS", "SrvAACAccess", tagMap);
        XMLGenerationUtils.writeMappedElement(writer, rs, "GL_SEGMENT", "GLSgmt", tagMap);
        XMLGenerationUtils.writeMappedElement(writer, rs, "STATUS", "SubSta", tagMap);

        // Address Information
        writer.write("      <ANArr elem=\"1\">\n");
        XMLGenerationUtils.writeMappedElement(writer, rs, "ADDRESS", "Add", tagMap);
        XMLGenerationUtils.writeMappedElement(writer, rs, "CITY", "City", tagMap);
        XMLGenerationUtils.writeMappedElement(writer, rs, "COUNTRY", "Cnt", tagMap);
        XMLGenerationUtils.writeMappedElement(writer, rs, "FIRST_NAME", "FNm", tagMap);
        XMLGenerationUtils.writeMappedElement(writer, rs, "LAST_NAME", "LNm", tagMap);
        writer.write("        <Sal>Ms</Sal>\n");
        writer.write("        <Stt>NA</Stt>\n");
        writer.write("        <Tit>Engineer</Tit>\n");
        writer.write("        <Zip>NA</Zip>\n");
        writer.write("      </ANArr>\n");

        // Tax Exemption
        writer.write("      <AEArr>\n");
        writer.write("        <CertNum/>\n");
        XMLGenerationUtils.writeMappedElement(writer, rs, "PERCENT", "Perc", tagMap);
        XMLGenerationUtils.writeMappedElement(writer, rs, "TYPE", "Typ", tagMap);
        writer.write("      </AEArr>\n");

        writer.write("    </Act>\n");

        // Dynamic Promotions
        writePromotions(writer, rs, accountNo);

        writer.write("  </ActSbsc>\n");
    }

    private static void writePromotions(OutputStreamWriter writer, ResultSet rs, String accountNo)
            throws SQLException, IOException {
        String prmNm = XMLGenerationUtils.getColumnValue(rs, "PROF_NAME");
        String nam = XMLGenerationUtils.getColumnValue(rs, "PROF_ACCT_NAME");
        String val = XMLGenerationUtils.getColumnValue(rs, "VALUE");

        if (!prmNm.isEmpty()) {
            writer.write(String.format(
                    "    <ActProm id=\"%s\" type=\"/profile/tab_customer_attributes\" global=\"true\">\n",
                    XMLGenerationUtils.escapeXml(accountNo)
            ));
            writer.write(String.format("      <PrmNm>%s</PrmNm>\n", XMLGenerationUtils.escapeXml(prmNm)));
            writer.write("      <PrmActLvlExtn>\n");
            writer.write("        <ALPArr elem=\"1\">\n");
            writer.write(String.format("          <Nam>%s</Nam>\n", XMLGenerationUtils.escapeXml(nam)));
            writer.write(String.format("          <Val>%s</Val>\n", XMLGenerationUtils.escapeXml(val)));
            writer.write("        </ALPArr>\n");
            writer.write("      </PrmActLvlExtn>\n");
            writer.write("    </ActProm>\n");
        }
    }
}
