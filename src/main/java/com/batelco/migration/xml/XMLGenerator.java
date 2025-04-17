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

            // Write XML header
            writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            writer.write("<Sbsc " + XmlTagMapping.getRootAttributes() + ">\n");

            while (rs.next()) {
                writeAccountElement(writer, rs, tagMap);
            }

            writer.write("</Sbsc>");
            System.out.println("XML file generated successfully: " + outputFile);

        } catch (SQLException | IOException e) {
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void writeAccountElement(OutputStreamWriter writer, ResultSet rs,
                                          Map<String, String> tagMap)
            throws SQLException, IOException {
        String accountNo = getColumnValue(rs, "ACCOUNT_NO");
        writer.write(String.format(
                "  <ActSbsc id=\"CA_%s\" isParent=\"Y\">\n" +
                "    <Act>\n",
                escapeXml(accountNo)));

        // Write mapped elements
        writeMappedElement(writer, rs, "ACCOUNT_NO", "ActNo", tagMap);
        writeMappedElement(writer, rs, "CURRENCY", "Curr", tagMap);
        writeMappedElement(writer, rs, "CUST_SEG_LIST", "CustSegList", tagMap);
        writeMappedElement(writer, rs, "BUSINESS_TYPE", "BType", tagMap);
        writeMappedElement(writer, rs, "AAC_ACCESS", "SrvAACAccess", tagMap);
        writeMappedElement(writer, rs, "GL_SEGMENT", "GLSgmt", tagMap);
        writeMappedElement(writer, rs, "STATUS", "SubSta", tagMap);

        // myExtension section
        writer.write("      <myExtension>\n");
        String identificationCode = getColumnValue(rs, "IDENTIFICATION_CODE");
        writer.write(String.format(
                "        <IdentificationCode type=\"string\">%s</IdentificationCode>\n",
                escapeXml(identificationCode)));
        String actStaValue = getColumnValue(rs, "ACCOUNT_STATUS");
        writer.write(String.format(
                "        <ActSta>%s</ActSta>\n",
                escapeXml(actStaValue)));
        writer.write("      </myExtension>\n");

        // Address Information
        writer.write("      <ANArr elem=\"1\">\n");
        writeMappedElement(writer, rs, "ADDRESS", "Add", tagMap);
        writeMappedElement(writer, rs, "CITY", "City", tagMap);
        writeMappedElement(writer, rs, "COUNTRY", "Cnt", tagMap);
        writeMappedElement(writer, rs, "FIRST_NAME", "FNm", tagMap);
        writeMappedElement(writer, rs, "LAST_NAME", "LNm", tagMap);
        writer.write("        <Sal>Ms</Sal>\n");
        writer.write("        <Stt>NA</Stt>\n");
        writer.write("        <Tit>Engineer</Tit>\n");
        writer.write("        <Zip>NA</Zip>\n");
        writer.write("      </ANArr>\n");

        // Tax Exemption
        writer.write("      <AEArr>\n");
        writer.write("        <CertNum/>\n");
        writeMappedElement(writer, rs, "PERCENT", "Perc", tagMap);
        writeMappedElement(writer, rs, "TYPE", "Typ", tagMap);
        writer.write("      </AEArr>\n");

        writer.write("    </Act>\n");

        // Dynamic Promotions with ID
        writePromotions(writer, rs, accountNo);  // Pass accountNo to promotions

        writer.write("  </ActSbsc>\n");
    }

    private static void writePromotions(OutputStreamWriter writer, ResultSet rs, String accountNo) 
            throws SQLException, IOException {
        String prmNm = getColumnValue(rs, "PROF_NAME");
        String nam = getColumnValue(rs, "PROF_ACCT_NAME");
        String val = getColumnValue(rs, "VALUE");

        if (!prmNm.isEmpty()) {
            writer.write(String.format(
                "    <ActProm id=\"%s\" type=\"/profile/tab_customer_attributes\" global=\"true\">\n",
                escapeXml(accountNo)  // Use raw account number without CA_ prefix
            ));
            writer.write(String.format("      <PrmNm>%s</PrmNm>\n", escapeXml(prmNm)));
            writer.write("      <PrmActLvlExtn>\n");
            writer.write("        <ALPArr elem=\"1\">\n");
            writer.write(String.format("          <Nam>%s</Nam>\n", escapeXml(nam)));
            writer.write(String.format("          <Val>%s</Val>\n", escapeXml(val)));
            writer.write("        </ALPArr>\n");
            writer.write("      </PrmActLvlExtn>\n");
            writer.write("    </ActProm>\n");
        }
    }

    // Rest of the helper methods remain unchanged
    private static void writeMappedElement(OutputStreamWriter writer, ResultSet rs,
                                         String columnName, String elementName,
                                         Map<String, String> tagMap)
            throws SQLException, IOException {
        try {
            String value = getColumnValue(rs, columnName);
            if (!value.isEmpty()) {
                // Apply value mappings
                switch (elementName) {
                    case "BType":
                        value = XmlTagMapping.getBusinessTypeMapping().getOrDefault(value, value);
                        break;
                    case "SubSta":
                        value = XmlTagMapping.getSubStaMapping().getOrDefault(value, value);
                        break;
                    case "Typ":
                        value = XmlTagMapping.getTypMapping().getOrDefault(value, value);
                        break;
                }

                writer.write(String.format("      <%s>%s</%s>\n",
                        elementName,
                        escapeXml(value),
                        elementName));
            }
        } catch (SQLException e) {
            // Column not found
        }
    }

    private static String getColumnValue(ResultSet rs, String columnName) throws SQLException {
        try {
            String value = rs.getString(columnName);
            return value != null ? value.trim() : "";
        } catch (SQLException e) {
            return "";
        }
    }

    private static String escapeXml(String value) {
        if (value == null) return "";
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }
}