package com.batelco.migration.xml;

import com.batelco.migration.config.XmlTagMapping;
import com.batelco.migration.sql.QueryExecutor;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class DepartmentAccountXMLGenerator {

    public static void generateXML(Connection connection, String sqlQuery, String outputFile) {
        Map<String, String> tagMap = XmlTagMapping.getDepartmentTagMap();

        String enhancedQuery = sqlQuery.toLowerCase().contains("from stg_dept_acct_t")
                ? """
                          SELECT d.*, c.IS_ENTER_ACCT
                          FROM stg_dept_acct_t d
                          LEFT JOIN stg_cust_acct_t c
                          ON d.CUST_ACCOUNT_NO = c.ACCOUNT_NO
                        """
                : sqlQuery;

        try (ResultSet rs = QueryExecutor.executeQuery(connection, enhancedQuery);
             FileOutputStream fos = new FileOutputStream(outputFile);
             OutputStreamWriter writer = new OutputStreamWriter(fos, StandardCharsets.UTF_8)) {

            writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            writer.write("<Sbsc xmlns=\"http://www.portal.com/InfranetXMLSchema\"\n");
            writer.write("      xmlns:math=\"xalana://java.lang.Math\"\n");
            writer.write("      xmlns:xalan=\"http://xml.apache.org/xalan\"\n");
            writer.write("      xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n");
            writer.write("      xsi:schemaLocation=\"http://www.oracle.com/schema/brm CMT_Subscribers.xsd\">\n");

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
        String parentAccount = XMLGenerationUtils.getColumnValue(rs, "CUST_ACCOUNT_NO");
        String isEnterprise = XMLGenerationUtils.getColumnValue(rs, "IS_ENTER_ACCT");

        String actId = "yes".equalsIgnoreCase(isEnterprise) ? "DA_" + accountNo : accountNo;
        String parentRef = parentAccount;

        writer.write(String.format("  <ActSbsc id=\"%s\" isParent=\"Y\" parenRef=\"%s\">\n",
                XMLGenerationUtils.escapeXml(actId), XMLGenerationUtils.escapeXml(parentRef)));

        writer.write("    <Act>\n");
        XMLGenerationUtils.writeElement(writer, "ActNo", actId);
        XMLGenerationUtils.writeMappedElement(writer, rs, "CURRENCY", "Curr", tagMap);
        XMLGenerationUtils.writeMappedElement(writer, rs, "CUSTOMER_SEGMENT_LIST", "CustSegList", tagMap);
        XMLGenerationUtils.writeMappedElement(writer, rs, "STATUS", "SubSta", tagMap);
        XMLGenerationUtils.writeMappedElement(writer, rs, "BUSINESS_TYPE", "BType", tagMap);
        XMLGenerationUtils.writeEffAndCrtT(writer, rs);
        writer.write("      <SrvAACAccess>Department</SrvAACAccess>\n");
        XMLGenerationUtils.writeMappedElement(writer, rs, "GL_SEGMENT", "GLSgmt", tagMap);

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

        // PHONE/TYPE → Only emit phone array if PHONE_TYPE (type) exists.
        // If phone type is absent, the wrapper tag (APhArr / APar-equivalent) must NOT appear.
        writePhoneElements(writer, rs, tagMap);

        // EXEMPTIONS/TYPE → If exemptions type is absent, the wrapper tag (AEar) must NOT appear.
        // (No-op here unless you later add exemptions emission; this preserves the rule.)
        // writeExemptionsElements(writer, rs, tagMap); // Intentionally omitted unless types exist.

        writer.write("      </ANArr>\n");
        writer.write("    </Act>\n");

        writeABinfo(writer, rs, actId);
        writer.write("  </ActSbsc>\n");
    }

    private static void writePhoneElements(OutputStreamWriter writer, ResultSet rs, Map<String, String> tagMap)
            throws SQLException, IOException {
        String phone = XMLGenerationUtils.getColumnValue(rs, "PHONE");
        String phoneType = XMLGenerationUtils.getColumnValue(rs, "PHONE_TYPE");

        // Change: gate the entire array on PHONE_TYPE presence.
        if (!phoneType.isEmpty()) {
            writer.write("        <APhArr id=\"0\">\n");

            if (!phone.isEmpty()) {
                XMLGenerationUtils.writeMappedElement(writer, rs, "PHONE", "Ph", tagMap);
            }

            // PHONE_TYPE exists by guard above; emit type-specific element(s)
            XMLGenerationUtils.writePhTypElement(writer, rs, "PHONE_TYPE", "PhTyp");

            writer.write("        </APhArr>\n");
        }
        // Else: do not emit APhArr (aka APar-equivalent) at all.
    }

    private static void writeABinfo(OutputStreamWriter writer, ResultSet rs, String actId)
            throws SQLException, IOException {
        writer.write("    <ABinfo global=\"true\" isAccBillinfo=\"Yes\">\n");
        writer.write("      <ActgType>B</ActgType>\n");
        String acDomValue = XMLGenerationUtils.getColumnValue(rs, "ACTG_CYCLE_DOM");
        writer.write(String.format("      <ACDom>%s</ACDom>\n", XMLGenerationUtils.escapeXml(acDomValue)));

        try {
            int acDom = Integer.parseInt(acDomValue);
            LocalDate today = LocalDate.now();
            int todayDay = today.getDayOfMonth();

            LocalDate billingDate;
            if (todayDay < acDom) {
                // Use this month
                billingDate = LocalDate.of(today.getYear(), today.getMonth(), Math.min(acDom, today.lengthOfMonth()));
            } else {
                // Use next month
                LocalDate nextMonth = today.plusMonths(1);
                billingDate = LocalDate.of(nextMonth.getYear(), nextMonth.getMonth(), Math.min(acDom, nextMonth.lengthOfMonth()));
            }

            writer.write(String.format("      <ANxt>%sT00:00:00Z</ANxt>\n", billingDate));
        } catch (NumberFormatException e) {
            writer.write("      <ANxt/>\n");
        }

        String billInfoId = XMLGenerationUtils.getColumnValue(rs, "BILL_INFO_ID");
        if (billInfoId.isEmpty()) {
            billInfoId = "Default BillInfo";
        }

        writer.write(String.format("      <BillInfoId>%s</BillInfoId>\n", XMLGenerationUtils.escapeXml(billInfoId)));
        XMLGenerationUtils.writeMappedElement(writer, rs, "BILLING_STATUS", "BillStat", null);
        writer.write("    </ABinfo>\n");
    }
}
