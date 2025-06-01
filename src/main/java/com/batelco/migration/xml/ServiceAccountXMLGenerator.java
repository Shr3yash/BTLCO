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
            writer.write("<Sbsc xmlns=\"http://www.portal.com/InfranetXMLSchema\"\n");
            writer.write("      xmlns:math=\"xalana://java.lang.Math\"\n");
            writer.write("      xmlns:xalan=\"http://xml.apache.org/xalan\"\n");
            writer.write("      xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n");
            writer.write("      xsi:schemaLocation=\"http://www.oracle.com/schema/brm CMT_Subscribers.xsd\">\n");

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
        String parentRef = XMLGenerationUtils.getColumnValue(rs, "BILL_ACCOUNT_NO");
        String payParentRef = XMLGenerationUtils.getColumnValue(rs, "BILL_ACCOUNT_NO");

        writer.write(String.format("  <ActSbsc id=\"%s\" parenRef=\"%s\" payParenRef=\"%s\">\n",
                XMLGenerationUtils.escapeXml(accountNo),
                XMLGenerationUtils.escapeXml(parentRef),
                XMLGenerationUtils.escapeXml(payParentRef)));

        writer.write("    <Act>\n");
        XMLGenerationUtils.writeMappedElement(writer, rs, "ACCOUNT_NO", "ActNo", tagMap);
        XMLGenerationUtils.writeMappedElement(writer, rs, "CURRENCY", "Curr", tagMap);
        XMLGenerationUtils.writeMappedElement(writer, rs, "CUSTOMER_SEGMENT_LIST", "CustSegList", tagMap);
        XMLGenerationUtils.writeMappedElement(writer, rs, "STATUS", "SubSta", tagMap);
        XMLGenerationUtils.writeMappedElement(writer, rs, "BUSINESS_TYPE", "BType", tagMap);
        writer.write("      <SrvAACAccess>Service</SrvAACAccess>\n");
        XMLGenerationUtils.writeMappedElement(writer, rs, "GL_SEGMENT", "GLSgmt", tagMap);

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
        writer.write("        <APhArr id=\"0\">\n");
        XMLGenerationUtils.writeMappedElement(writer, rs, "PHONE", "Ph", tagMap);
        XMLGenerationUtils.writeMappedElement(writer, rs, "PHONE_TYPE", "PhTyp", tagMap);
        writer.write("        </APhArr>\n");
        writer.write("      </ANArr>\n");

        writer.write("      <AEArr>\n");
        writer.write("        <CertNum/>\n");
        XMLGenerationUtils.writeMappedElement(writer, rs, "PERCENT", "Perc", tagMap);
        XMLGenerationUtils.writeMappedElement(writer, rs, "TYPE", "Typ", tagMap);
        writer.write("      </AEArr>\n");
        writer.write("    </Act>\n");

        writePromotions(writer, rs, accountNo);
        writeABinfo(writer, rs, parentRef);
        writeAPinfo(writer, rs, accountNo);

        writer.write("  </ActSbsc>\n");
    }

    private static void writePromotions(OutputStreamWriter writer, ResultSet rs, String accountNo)
            throws SQLException, IOException {
        writer.write(String.format("    <ActProm id=\"%s\" type=\"/profile/acct_extrating\" global=\"true\">\n",
                XMLGenerationUtils.escapeXml(accountNo)));
        writer.write("      <PrmNm>TAXEXEMPT</PrmNm>\n");
        writer.write("      <PrmActLvlExtn>\n");
        writer.write("        <ALPArr elem=\"1\">\n");
        writer.write("          <Nam>TAXEXEMPT</Nam>\n");
        writer.write("          <Val>0</Val>\n");
        writer.write("        </ALPArr>\n");
        writer.write("      </PrmActLvlExtn>\n");
        writer.write("    </ActProm>\n");
    }

    private static void writeABinfo(OutputStreamWriter writer, ResultSet rs, String parentAccount)
            throws SQLException, IOException {
        writer.write(String.format(
                "    <ABinfo global=\"true\" spnrCnt=\"1\" spnreeCnt=\"2\" elem=\"74728\" bal_grp=\"true\" isAccBillinfo=\"Yes\" payInfoRefId=\"%s\">\n",
                XMLGenerationUtils.escapeXml(parentAccount)));
        XMLGenerationUtils.writeMappedElement(writer, rs, "ACTG_CYCLE_DOM", "ACDom", null);
        String rawPayType = XMLGenerationUtils.getColumnValue(rs, "PAY_TYPE");
        String mappedPayType = switch (rawPayType) {
            case "10001" -> "INV";
            case "10007" -> "NPC";
            default -> rawPayType;
        };
        writer.write(String.format("      <PTyp>%s</PTyp>%n", XMLGenerationUtils.escapeXml(mappedPayType)));
                XMLGenerationUtils.writeMappedElement(writer, rs, "BILL_INFO_ID", "BillInfoId", null);
        XMLGenerationUtils.writeMappedElement(writer, rs, "BILLING_STATUS", "BillStat", null);
        writer.write("    </ABinfo>\n");
    }

    private static void writeAPinfo(OutputStreamWriter writer, ResultSet rs, String accountNo)
            throws SQLException, IOException {
        writer.write(String.format("    <APinfo id=\"%s\" type=\"/payinfo/subord\">\n",
                XMLGenerationUtils.escapeXml(accountNo)));
        XMLGenerationUtils.writeMappedElement(writer, rs, "PAYMENT_TERM", "PaymentTerm", null);
        writer.write("    </APinfo>\n");
    }
}
