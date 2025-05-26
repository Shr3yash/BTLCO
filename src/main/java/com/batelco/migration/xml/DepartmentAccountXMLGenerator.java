package com.batelco.migration.xml;

import com.batelco.migration.config.XmlTagMapping;
import com.batelco.migration.sql.QueryExecutor;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class DepartmentAccountXMLGenerator {

    public static void generateXML(Connection connection, String sqlQuery, String outputFile) {
        Map<String, String> tagMap = XmlTagMapping.getDepartmentTagMap();

        try (ResultSet rs = QueryExecutor.executeQuery(connection, sqlQuery);
             FileOutputStream fos = new FileOutputStream(outputFile);
             OutputStreamWriter writer = new OutputStreamWriter(fos, StandardCharsets.UTF_8)) {

            writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            writer.write("<Sbsc " + XmlTagMapping.getRootAttributes() + ">\n");

            while (rs.next()) {
                writeDepartmentElement(writer, rs, tagMap);
            }

            writer.write("</Sbsc>");
            System.out.println("Department XML generated successfully: " + outputFile);

        } catch (SQLException | IOException e) {
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void writeDepartmentElement(OutputStreamWriter writer, ResultSet rs,
                                             Map<String, String> tagMap)
            throws SQLException, IOException {
        String accountNo = XMLGenerationUtils.getColumnValue(rs, "ACCOUNT_NO");
        String parentAccount = XMLGenerationUtils.getColumnValue(rs, "PARENT_ACCOUNT_NO");

        writer.write(String.format(
            "  <ActSbsc id=\"DA_%s\" isParent=\"Y\" parenRef=\"CA_%s\">\n" +
            "    <Act>\n",
            XMLGenerationUtils.escapeXml(accountNo), 
            XMLGenerationUtils.escapeXml(parentAccount)));

        // Core Elements
        XMLGenerationUtils.writeMappedElement(writer, rs, "ACCOUNT_NO", "ActNo", tagMap);
        XMLGenerationUtils.writeMappedElement(writer, rs, "CURRENCY", "Curr", tagMap);
        XMLGenerationUtils.writeMappedElement(writer, rs, "CUST_SEG_LIST", "CustSegList", tagMap);
        XMLGenerationUtils.writeMappedElement(writer, rs, "STATUS", "SubSta", tagMap);
        XMLGenerationUtils.writeMappedElement(writer, rs, "BUSINESS_TYPE", "BType", tagMap);
        writer.write("      <SrvAACAccess>Department</SrvAACAccess>\n");
        XMLGenerationUtils.writeMappedElement(writer, rs, "GL_SEGMENT", "GLSgmt", tagMap);

        // Address Information
        writer.write("      <ANArr elem=\"1\">\n");
        XMLGenerationUtils.writeMappedElement(writer, rs, "ADDRESS", "Add", tagMap);
        XMLGenerationUtils.writeMappedElement(writer, rs, "CITY", "City", tagMap);
        XMLGenerationUtils.writeMappedElement(writer, rs, "COUNTRY", "Cnt", tagMap);
        XMLGenerationUtils.writeMappedElement(writer, rs, "FIRST_NAME", "FNm", tagMap);
        XMLGenerationUtils.writeMappedElement(writer, rs, "MIDDLE_NAME", "MNm", tagMap);
        XMLGenerationUtils.writeMappedElement(writer, rs, "LAST_NAME", "LNm", tagMap);
        writer.write("        <Stt/>\n");
        writer.write("        <Tit/>\n");
        writer.write("        <Zip/>\n");
        
        // Phone Information
        writePhoneElements(writer, rs);
        writer.write("      </ANArr>\n");

        // Tax Exemption
        writer.write("      <AEArr>\n");
        writer.write("        <CertNum/>\n");
        XMLGenerationUtils.writeMappedElement(writer, rs, "PERCENT", "Perc", tagMap);
        XMLGenerationUtils.writeMappedElement(writer, rs, "TYPE", "Typ", tagMap);
        writer.write("      </AEArr>\n");

        writer.write("    </Act>\n");

        // Billing Information
        writeABinfo(writer, rs, accountNo);
        writer.write("  </ActSbsc>\n");
    }

    private static void writePhoneElements(OutputStreamWriter writer, ResultSet rs)
            throws SQLException, IOException {
        String phone = XMLGenerationUtils.getColumnValue(rs, "PHONE_NUMBER");
        String phoneType = XMLGenerationUtils.getColumnValue(rs, "PHONE_TYPE");
        
        if (!phone.isEmpty()) {
            writer.write("      <APhArr elem=\"0\">\n");
            writer.write(String.format("        <Ph>%s</Ph>\n", XMLGenerationUtils.escapeXml(phone)));
            writer.write(String.format("        <PhTyp>%s</PhTyp>\n", XMLGenerationUtils.escapeXml(phoneType)));
            writer.write("      </APhArr>\n");
        }
    }

    private static void writeABinfo(OutputStreamWriter writer, ResultSet rs, String accountNo)
            throws SQLException, IOException {
        writer.write(String.format(
            "    <ABinfo global=\"true\" spnrCnt=\"1\" spnreeCnt=\"2\" elem=\"1\" " +
            "bal_grp=\"true\" isAccBillinfo=\"Yes\" payInfoRefId=\"DA_%s\">\n",
            XMLGenerationUtils.escapeXml(accountNo)));
        
        XMLGenerationUtils.writeMappedElement(writer, rs, "ACCOUNTING_DOM", "ACDom", null);
        XMLGenerationUtils.writeMappedElement(writer, rs, "BILLING_STATUS", "BillStat", null);
        writer.write("    </ABinfo>\n");
    }
}