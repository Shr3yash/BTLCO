package com.batelco.migration.xml;

import com.batelco.migration.config.XmlTagMapping;
import com.batelco.migration.sql.QueryExecutor;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

public class BAXMLGenerator {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    public static void generateXML(Connection connection, String sqlQuery, String outputFile) {
        Map<String, String> tagMap = XmlTagMapping.getBATagMap();

        try (ResultSet rs = QueryExecutor.executeQuery(connection, sqlQuery);
             FileOutputStream fos = new FileOutputStream(outputFile);
             OutputStreamWriter writer = new OutputStreamWriter(fos, StandardCharsets.UTF_8)) {

            writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            writer.write("<Sbsc " + XmlTagMapping.getRootAttributes() + ">\n");

            while (rs.next()) {
                writeAccountElement(writer, rs, tagMap);
            }

            writer.write("</Sbsc>");
            System.out.println("BA XML file generated successfully: " + outputFile);

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
            "  <ActSbsc id=\"%s\" isParent=\"Y\">\n" +
            "    <Act>\n",
            escapeXml(accountNo)));

        // Core Account Information
        writeMappedElement(writer, rs, "ACCOUNT_NO", "ActNo", tagMap);
        writeMappedElement(writer, rs, "CURRENCY", "Curr", tagMap);
        writeMappedElement(writer, rs, "CUST_SEG_LIST", "CustSegList", tagMap);
        writeMappedElement(writer, rs, "BUSINESS_TYPE", "BType", tagMap);
        writeMappedElement(writer, rs, "AAC_ACCESS", "SrvAACAccess", tagMap);
        writeMappedElement(writer, rs, "GL_SEGMENT", "GLSgmt", tagMap);
        writeMappedElement(writer, rs, "STATUS", "SubSta", tagMap);

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

        // Promotions
        writePromotions(writer, rs, accountNo);

        // Billing Account Information
        writeABinfo(writer, rs);

        // Payment Information
        writeAPinfo(writer, rs, accountNo);

        writer.write("  </ActSbsc>\n");
    }

    private static void writeABinfo(OutputStreamWriter writer, ResultSet rs) throws SQLException, IOException {
        writer.write(String.format(
            "    <ABinfo global=\"true\" spnrCnt=\"1\" spnreeCnt=\"2\" elem=\"74728\" " +
            "bal_grp=\"true\" isAccBillinfo=\"Yes\" payInfoRefId=\"%s\">\n",
            escapeXml(getColumnValue(rs, "ACCOUNT_NO"))));

        writeMappedElement(writer, rs, "ACDOM", "ACDom", null);
        writeMappedElement(writer, rs, "ACTG_TYPE", "ActgType", null);
        writeTimestampElement(writer, rs, "EFFECT_T", "EffecT");
        writeMappedElement(writer, rs, "BILLING_STATUS", "BISta", null);
        writeMappedElement(writer, rs, "PENDING_RECEIVABLE", "PendingRecv", null);
        writeTimestampElement(writer, rs, "A_NEXT", "ANxt");
        writeTimestampElement(writer, rs, "CREATION_T", "CrtT");
        writeMappedElement(writer, rs, "BILL_WINDOW", "BlWn", null);
        writeMappedElement(writer, rs, "BILL_SEGMENT", "BlSgmnt", null);
        writeMappedElement(writer, rs, "PAYMENT_TYPE", "PTyp", null);
        writeMappedElement(writer, rs, "BILL_STATUS", "BillStat", null);
        writeMappedElement(writer, rs, "BILL_INFO_ID", "BillInfoId", null);

        writer.write("    </ABinfo>\n");
    }

    private static void writeAPinfo(OutputStreamWriter writer, ResultSet rs, String accountNo) 
            throws SQLException, IOException {
        writer.write(String.format(
            "    <APinfo id=\"%s\" type=\"/payinfo/invoice\">\n",
            escapeXml(accountNo)));

        writeMappedElement(writer, rs, "DUE_DOM", "DDom", null);
        writeMappedElement(writer, rs, "PAYMENT_TERM", "PaymentTerm", null);
        writeMappedElement(writer, rs, "REL_DUE", "RelDue", null);
        writeTimestampElement(writer, rs, "AP_CRT_T", "CrtT");

        // Payment Invoice Extension
        writer.write("      <payInvExtn>\n");
        writeMappedElement(writer, rs, "CUSTOMER_NAME", "Nm", null);
        writeMappedElement(writer, rs, "ADDRESS", "Add", null);
        writeMappedElement(writer, rs, "CITY", "Cty", null);
        writeMappedElement(writer, rs, "STATE", "Stt", null);
        writeMappedElement(writer, rs, "ZIP", "Zip", null);
        writeMappedElement(writer, rs, "COUNTRY", "Cntr", null);
        writeMappedElement(writer, rs, "EMAIL", "EAdd", null);
        writeMappedElement(writer, rs, "DELIVERY_PREF", "DelPrf", null);
        writeMappedElement(writer, rs, "DELIVERY_DESC", "DelDsc", null);
        writer.write("      </payInvExtn>\n");

        // MyExtension section
        writer.write("      <myExtension>\n");
        writer.write("        <myArray table=\"profile_inv_ext_t\" elem=\"1\">\n");
        writeOptionalElement(writer, rs, "ADR_LINE1");
        writeOptionalElement(writer, rs, "ADR_LINE2");
        writeOptionalElement(writer, rs, "ARB_ADR_LINE1");
        writeOptionalElement(writer, rs, "ARB_ADR_LINE2");
        writeOptionalElement(writer, rs, "ARB_NAME");
        writeMappedElement(writer, rs, "BLOCK_NO", "BLOCK_NO", null);
        writeMappedElement(writer, rs, "BUILDING_NO", "BUILDING_NUMBER", null);
        writeMappedElement(writer, rs, "FLAT_NO", "FLAT_NO", null);
        writeOptionalElement(writer, rs, "LANG_PREFER");
        writeOptionalElement(writer, rs, "PO_BOX");
        writeMappedElement(writer, rs, "ROAD_NO", "ROAD_NO", null);
        writeMappedElement(writer, rs, "EMAIL", "EMAIL_ADDR", null);
        writeOptionalElement(writer, rs, "MANAGER_EMAIL");
        writeMappedElement(writer, rs, "PHONE", "PHONE", null);
        writeOptionalElement(writer, rs, "LANGUAGE");
        writer.write("        </myArray>\n");
        writer.write("      </myExtension>\n");

        writer.write("    </APinfo>\n");
    }

    private static void writePromotions(OutputStreamWriter writer, ResultSet rs, String accountNo) 
            throws SQLException, IOException {
        String prmNm = getColumnValue(rs, "PROF_NAME");
        String nam = getColumnValue(rs, "PROF_ACCT_NAME");
        String val = getColumnValue(rs, "VALUE");

        if (!prmNm.isEmpty()) {
            writer.write(String.format(
                "    <ActProm id=\"%s\" type=\"/profile/tab_customer_attributes\" global=\"true\">\n",
                escapeXml(accountNo)));
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

    private static void writeTimestampElement(OutputStreamWriter writer, ResultSet rs, 
                                            String columnName, String elementName)
            throws SQLException, IOException {
        try {
            Date date = rs.getTimestamp(columnName);
            if (date != null) {
                writer.write(String.format("      <%s>%s</%s>\n",
                        elementName,
                        DATE_FORMAT.format(date),
                        elementName));
            }
        } catch (SQLException e) {
            // Column not found or null value
        }
    }

    private static void writeOptionalElement(OutputStreamWriter writer, ResultSet rs, String columnName)
            throws SQLException, IOException {
        String value = getColumnValue(rs, columnName);
        if (!value.isEmpty()) {
            writer.write(String.format("          <%s type=\"string\">%s</%s>\n",
                    columnName,
                    escapeXml(value),
                    columnName));
        } else {
            writer.write(String.format("          <%s type=\"string\"/>\n", columnName));
        }
    }

    private static void writeMappedElement(OutputStreamWriter writer, ResultSet rs,
                                         String columnName, String elementName,
                                         Map<String, String> tagMap)
            throws SQLException, IOException {
        try {
            String value = getColumnValue(rs, columnName);
            if (!value.isEmpty()) {
                // Apply BA-specific mappings
                switch(elementName) {
                    case "SrvAACAccess":
                        value = "Billing";  // Hardcoded for BA
                        break;
                    case "Typ":
                        value = XmlTagMapping.getBATypMapping().getOrDefault(value, value);
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