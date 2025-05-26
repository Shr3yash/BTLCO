package com.batelco.migration.xml;

import com.batelco.migration.config.XmlTagMapping;
import com.batelco.migration.sql.QueryExecutor;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class ServiceAccountXMLGenerator {

    public static void generateXML(Connection connection, String sqlQuery, String outputFile) {
        Map<String, String> tagMap = XmlTagMapping.getServiceTagMap();

        try (ResultSet rs = QueryExecutor.executeQuery(connection, sqlQuery);
             FileOutputStream fos = new FileOutputStream(outputFile);
             OutputStreamWriter writer = new OutputStreamWriter(fos, StandardCharsets.UTF_8)) {

            writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            writer.write("<Sbsc " + XmlTagMapping.getRootAttributes() + ">\n");

            while (rs.next()) {
                writeServiceElement(writer, rs, tagMap);
            }

            writer.write("</Sbsc>");
            System.out.println("Service XML generated successfully: " + outputFile);

        } catch (SQLException | IOException e) {
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void writeServiceElement(OutputStreamWriter writer, ResultSet rs,
                                          Map<String, String> tagMap)
            throws SQLException, IOException {
        String accountNo = XMLGenerationUtils.getColumnValue(rs, "ACCOUNT_NO");
        String parentAccount = XMLGenerationUtils.getColumnValue(rs, "PARENT_ACCOUNT_NO");
        String payParentAccount = XMLGenerationUtils.getColumnValue(rs, "PAY_PARENT_ACCOUNT_NO");

        writer.write(String.format(
            "  <ActSbsc id=\"%s\" parenRef=\"%s\" payParenRef=\"%s\">\n" +
            "    <Act>\n",
            XMLGenerationUtils.escapeXml(accountNo),
            XMLGenerationUtils.escapeXml(parentAccount),
            XMLGenerationUtils.escapeXml(payParentAccount)));

        // Core Service Elements
        XMLGenerationUtils.writeMappedElement(writer, rs, "ACCOUNT_NO", "ActNo", tagMap);
        XMLGenerationUtils.writeMappedElement(writer, rs, "CURRENCY", "Curr", tagMap);
        XMLGenerationUtils.writeMappedElement(writer, rs, "CUST_SEG_LIST", "CustSegList", tagMap);
        XMLGenerationUtils.writeMappedElement(writer, rs, "STATUS", "SubSta", tagMap);
        XMLGenerationUtils.writeMappedElement(writer, rs, "BUSINESS_TYPE", "BType", tagMap);
        writer.write("      <SrvAACAccess>Service</SrvAACAccess>\n");
        XMLGenerationUtils.writeMappedElement(writer, rs, "GL_SEGMENT", "GLSgmt", tagMap);

        // Address Information
        writer.write("      <ANArr elem=\"1\">\n");
        XMLGenerationUtils.writeMappedElement(writer, rs, "ADDRESS", "Add", tagMap);
        XMLGenerationUtils.writeMappedElement(writer, rs, "CITY", "City", tagMap);
        XMLGenerationUtils.writeMappedElement(writer, rs, "COUNTRY", "Cnt", tagMap);
        XMLGenerationUtils.writeMappedElement(writer, rs, "FIRST_NAME", "FNm", tagMap);
        XMLGenerationUtils.writeMappedElement(writer, rs, "MIDDLE_NAME", "MNm", tagMap);
        XMLGenerationUtils.writeMappedElement(writer, rs, "LAST_NAME", "LNm", tagMap);
        writer.write("        <EAdd/>\n");
        writer.write("        <Tit/>\n");
        writer.write("        <Stt/>\n");
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

        // Promotions
        writePromotions(writer, rs, accountNo);

        // Billing Information
        writeABinfo(writer, rs, parentAccount);

        // Payment Information
        writeAPinfo(writer, rs, accountNo);

        writer.write("  </ActSbsc>\n");
    }

    private static void writePhoneElements(OutputStreamWriter writer, ResultSet rs)
            throws SQLException, IOException {
        String phone = XMLGenerationUtils.getColumnValue(rs, "PHONE_NUMBER");
        String phoneType = XMLGenerationUtils.getColumnValue(rs, "PHONE_TYPE");
        
        writer.write("      <APhArr id=\"0\">\n");
        writer.write(String.format("        <Ph>%s</Ph>\n", XMLGenerationUtils.escapeXml(phone)));
        writer.write(String.format("        <PhTyp>%s</PhTyp>\n", XMLGenerationUtils.escapeXml(phoneType)));
        writer.write("      </APhArr>\n");
    }

    private static void writePromotions(OutputStreamWriter writer, ResultSet rs, String accountNo) 
            throws SQLException, IOException {
        String prmNm = XMLGenerationUtils.getColumnValue(rs, "PROF_NAME");
        String nam = XMLGenerationUtils.getColumnValue(rs, "PROF_ACCT_NAME");
        String val = XMLGenerationUtils.getColumnValue(rs, "VALUE");

        if (!prmNm.isEmpty()) {
            writer.write(String.format(
                "    <ActProm id=\"%s\" type=\"/profile/acct_extrating\" global=\"true\">\n",
                XMLGenerationUtils.escapeXml(accountNo)));
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

    private static void writeABinfo(OutputStreamWriter writer, ResultSet rs, String parentAccount)
            throws SQLException, IOException {
        writer.write(String.format(
            "    <ABinfo global=\"true\" spnrCnt=\"1\" spnreeCnt=\"2\" elem=\"74728\" " +
            "bal_grp=\"true\" isAccBillinfo=\"Yes\" payInfoRefId=\"%s\">\n",
            XMLGenerationUtils.escapeXml(parentAccount)));
        
        XMLGenerationUtils.writeMappedElement(writer, rs, "ACCOUNTING_DOM", "ACDom", null);
        XMLGenerationUtils.writeMappedElement(writer, rs, "PAYMENT_TYPE", "PTyp", null);
        XMLGenerationUtils.writeMappedElement(writer, rs, "BILL_INFO_ID", "BillInfoId", null);
        XMLGenerationUtils.writeMappedElement(writer, rs, "BILLING_STATUS", "BillStat", null);
        
        writer.write("    </ABinfo>\n");
    }

    private static void writeAPinfo(OutputStreamWriter writer, ResultSet rs, String accountNo)
            throws SQLException, IOException {
        writer.write(String.format(
            "    <APinfo id=\"%s\" type=\"/payinfo/subord\">\n",
            XMLGenerationUtils.escapeXml(accountNo)));
        
        XMLGenerationUtils.writeMappedElement(writer, rs, "PAYMENT_TERM", "PaymentTerm", null);
        
        writer.write("    </APinfo>\n");
    }
}