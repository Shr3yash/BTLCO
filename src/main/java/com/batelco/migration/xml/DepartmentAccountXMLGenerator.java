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

        // Enhance SQL query if target is stg_dept_acct_t
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
        System.out.println("AccountNo: " + accountNo + ", IS_ENTER_ACCT: '" + isEnterprise + "'");

        String actId = "yes".equalsIgnoreCase(isEnterprise) ? "DA_" + accountNo : accountNo;
        String parentRef = "yes".equalsIgnoreCase(isEnterprise) ? "DA_" + parentAccount : parentAccount;

        writer.write(String.format("  <ActSbsc id=\"%s\" isParent=\"Y\" parenRef=\"%s\">\n",
                XMLGenerationUtils.escapeXml(actId), XMLGenerationUtils.escapeXml(parentRef)));

        writer.write("    <Act>\n");
        XMLGenerationUtils.writeElement(writer, "ActNo", actId);
        XMLGenerationUtils.writeMappedElement(writer, rs, "CURRENCY", "Curr", tagMap);
        XMLGenerationUtils.writeMappedElement(writer, rs, "CUSTOMER_SEGMENT_LIST", "CustSegList", tagMap);
        XMLGenerationUtils.writeMappedElement(writer, rs, "STATUS", "SubSta", tagMap);
        XMLGenerationUtils.writeMappedElement(writer, rs, "BUSINESS_TYPE", "BType", tagMap);
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
        writePhoneElements(writer, rs);
        writer.write("      </ANArr>\n");

        writer.write("      <AEArr>\n");
        writer.write("        <CertNum/>\n");
        XMLGenerationUtils.writeMappedElement(writer, rs, "PERCENT", "Perc", tagMap);
        XMLGenerationUtils.writeMappedElement(writer, rs, "TYPE", "Typ", tagMap);
        writer.write("      </AEArr>\n");

        writer.write("    </Act>\n");
        writeABinfo(writer, rs, actId);
        writer.write("  </ActSbsc>\n");
    }

    private static void writePhoneElements(OutputStreamWriter writer, ResultSet rs)
            throws SQLException, IOException {
        String phone = XMLGenerationUtils.getColumnValue(rs, "PHONE");
        String phoneType = XMLGenerationUtils.getColumnValue(rs, "PHONE_TYPE");

        String mappedPhTyp = switch (phoneType) {
            case "Ph" -> "0";
            case "H" -> "1";
            case "W" -> "2";
            case "P", "F" -> "3";
            case "PG" -> "4";
            case "PP" -> "5";
            case "S" -> "6";
            default -> phoneType;
        };

        writer.write("        <APhArr elem=\"0\">\n");
        writer.write(String.format("          <Ph>%s</Ph>\n", XMLGenerationUtils.escapeXml(phone)));
        writer.write(String.format("          <PhTyp>%s</PhTyp>\n", XMLGenerationUtils.escapeXml(mappedPhTyp)));
        writer.write("        </APhArr>\n");
    }

    private static void writeABinfo(OutputStreamWriter writer, ResultSet rs, String actId)
            throws SQLException, IOException {
        writer.write(String.format(
                "    <ABinfo global=\"true\" spnrCnt=\"1\" spnreeCnt=\"2\" elem=\"1\" bal_grp=\"true\" isAccBillinfo=\"Yes\" payInfoRefId=\"%s\">\n",
                XMLGenerationUtils.escapeXml(actId)));
        XMLGenerationUtils.writeMappedElement(writer, rs, "ACTG_CYCLE_DOM", "ACDom", null);
        XMLGenerationUtils.writeMappedElement(writer, rs, "BILLING_STATUS", "BillStat", null);
        writer.write("    </ABinfo>\n");
    }
}