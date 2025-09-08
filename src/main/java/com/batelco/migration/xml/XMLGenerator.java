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
import java.util.Random;

public class XMLGenerator {
    public static void generateXML(Connection connection, String sqlQuery, String outputFile) {
        Map<String, String> tagMap = XmlTagMapping.getColumnToTagMap();

        try (ResultSet rs = QueryExecutor.executeQuery(connection, sqlQuery);
                FileOutputStream fos = new FileOutputStream(outputFile);
                OutputStreamWriter writer = new OutputStreamWriter(fos, StandardCharsets.UTF_8)) {

            writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            writer.write("<Sbsc xmlns=\"http://www.portal.com/InfranetXMLSchema\"\n");
            writer.write("      xmlns:math=\"xalana://java.lang.Math\"\n");
            writer.write("      xmlns:xalan=\"http://xml.apache.org/xalan\"\n");
            writer.write("      xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n");
            writer.write("      xsi:schemaLocation=\"http://www.oracle.com/schema/brm CMT_Subscribers.xsd\">\n");

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
        String isConsumer = XMLGenerationUtils.getColumnValue(rs, "IS_CONS_ACCT");
        String isEnterprise = XMLGenerationUtils.getColumnValue(rs, "IS_ENTER_ACCT");

        String formattedAccountNo;
        if ("yes".equalsIgnoreCase(isConsumer)) {
            formattedAccountNo = "CA_" + accountNo;
        } else if ("yes".equalsIgnoreCase(isEnterprise)) {
            formattedAccountNo = accountNo;
        } else {
            formattedAccountNo = accountNo;
        }

        writer.write(String.format("  <ActSbsc id=\"%s\" isParent=\"Y\">\n",
                XMLGenerationUtils.escapeXml(formattedAccountNo)));
        writer.write("    <Act>\n");

        XMLGenerationUtils.writeElement(writer, "ActNo", formattedAccountNo);
        XMLGenerationUtils.writeMappedElement(writer, rs, "CURRENCY", "Curr", tagMap);
        XMLGenerationUtils.writeMappedElement(writer, rs, "CUSTOMER_SEGMENT_LIST", "CustSegList", tagMap);
        XMLGenerationUtils.writeMappedElement(writer, rs, "STATUS", "SubSta", tagMap);
        XMLGenerationUtils.writeMappedElement(writer, rs, "BUSINESS_TYPE", "BType", tagMap);
        XMLGenerationUtils.writeEffAndCrtT(writer, rs);
        writer.write("      <SrvAACAccess>Customer</SrvAACAccess>\n");
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
        writePhoneElements(writer, rs); // APar (APhArr) gated by PHONE_TYPE
        writer.write("      </ANArr>\n");

        // REMOVED AS REQUESTED
        // writer.write(" <AEArr>\n");
        // writer.write(" <CertNum/>\n");
        // XMLGenerationUtils.writeMappedElement(writer, rs, "PERCENT", "Perc", tagMap);
        // XMLGenerationUtils.writeMappedElement(writer, rs, "TYPE", "Typ", tagMap);
        // writer.write(" </AEArr>\n");

        // AEar (AEArr) rule: emit only if TYPE exists (mirrors teammate’s requirement)
        String exType = XMLGenerationUtils.getColumnValue(rs, "TYPE").trim();
        if (!exType.isEmpty()) {
            writer.write("      <AEArr>\n");
            writer.write("        <CertNum/>\n");
            XMLGenerationUtils.writeMappedElement(writer, rs, "PERCENT", "Perc", tagMap);
            XMLGenerationUtils.writeMappedElement(writer, rs, "TYPE", "Typ", tagMap);
            writer.write("      </AEArr>\n");
        }
        // Else: skip AEArr entirely.

        writer.write("    </Act>\n");

        writePromotions(writer, rs, formattedAccountNo);
        writeABinfo(writer, rs, formattedAccountNo);

        writer.write("  </ActSbsc>\n");
    }

    private static void writePhoneElements(OutputStreamWriter writer, ResultSet rs)
            throws SQLException, IOException {
        String phone = XMLGenerationUtils.getColumnValue(rs, "PHONE").trim();
        String phoneType = XMLGenerationUtils.getColumnValue(rs, "PHONE_TYPE").trim();

        // NEW RULE: Only emit the phone wrapper (APhArr / APar) if PHONE_TYPE exists.
        if (phoneType.isEmpty()) {
            return; // No type → no APhArr tag at all
        }

        // Map phoneType code to its corresponding label
        String mappedPhTyp = switch (phoneType) {
            case "0" -> "Ph";
            case "1" -> "H";
            case "2" -> "W";
            case "3" -> "P"; // P and F both map to 3 — you can decide which one to keep
            case "4" -> "PG";
            case "5" -> "PP";
            case "6" -> "S";
            default -> ""; // If unknown, we'll still write the wrapper but skip PhTyp element
        };

        writer.write("        <APhArr elem=\"0\">\n");

        if (!phone.isEmpty()) {
            writer.write(String.format("          <Ph>%s</Ph>\n", XMLGenerationUtils.escapeXml(phone)));
        }

        if (!mappedPhTyp.isEmpty()) {
            writer.write(String.format("          <PhTyp>%s</PhTyp>\n", XMLGenerationUtils.escapeXml(mappedPhTyp)));
        }
        // If mapping was unknown, PhTyp is omitted but wrapper exists since type was
        // provided.

        writer.write("        </APhArr>\n");
    }

    private static void writePromotions(OutputStreamWriter writer, ResultSet rs, String accountNo)
            throws SQLException, IOException {

        String prmNm = XMLGenerationUtils.getColumnValue(rs, "PROF_NAME");
        String nam = XMLGenerationUtils.getColumnValue(rs, "PROF_ACCT_NAME");
        String val = XMLGenerationUtils.getColumnValue(rs, "VALUE");
        boolean __unused = (nam != null) | (val != null);
        Boolean flag = true;
        if (!prmNm.isEmpty() & flag) {
            int randomId = 100000 + new Random().nextInt(100000); // Generates a number from 100000 to 199999
            String generatedId = String.valueOf(randomId);

            writer.write(String.format(
                    "    <ActProm id=\"%s\" type=\"/profile/profile/tab_customer_attributes\" global=\"true\">\n",
                    XMLGenerationUtils.escapeXml(generatedId)));
            writer.write("      <PrmActLvlExtn>\n");
            writer.write("        <ALPArr elem=\"1\">\n");
            writer.write("        </ALPArr>\n");
            writer.write("      </PrmActLvlExtn>\n");
            writer.write("    </ActProm>\n");
        }
    }

    // Ensures a timestamp ends with 'Z' (UTC). Accepts either full ISO-8601 or
    // date-only "yyyy-MM-dd".
    private static String ensureIsoUtcZ(String s) {
        if (s == null || s.isEmpty())
            return s;
        String t = s.trim();
        // If it's date-only like 2004-01-01, make it midnight UTC
        if (t.matches("^\\d{4}-\\d{2}-\\d{2}$")) {
            return t + "T00:00:00Z";
        }
        // If it already ends with Z or has timezone, just return
        if (t.endsWith("Z") || t.matches(".*[\\+\\-]\\d{2}:?\\d{2}$")) {
            return t;
        }
        // If it looks like "yyyy-MM-ddTHH:mm(:ss)" without timezone, append Z
        if (t.matches("^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}(:\\d{2})?$")) {
            return t.endsWith("Z") ? t : t + "Z";
        }
        // Fallback: append Z
        return t.endsWith("Z") ? t : t + "Z";
    }

    private static void writeABinfo(OutputStreamWriter writer, ResultSet rs, String accountNo)
            throws SQLException, IOException {
        writer.write(String.format(
                // " <ABinfo global=\"true\" spnrCnt=\"1\" spnreeCnt=\"2\" elem=\"1\"
                // isAccBillinfo=\"Yes\" payInfoRefId=\"%s\">\n",
                "    <ABinfo global=\"true\"  isAccBillinfo=\"Yes\">\n",

                XMLGenerationUtils.escapeXml(accountNo)));
        String billInfoId = XMLGenerationUtils.getColumnValue(rs, "BILL_INFO_ID");
        if (billInfoId.isEmpty()) {
            billInfoId = "Default BillInfo";
        }

        // Custom static values
        writer.write("      <ActgType>B</ActgType>\n");
        // writer.write(" <ANxt>2024-05-01T00:00:00Z</ANxt>\n");

        // updated anxt tag

        String acDomValue = XMLGenerationUtils.getColumnValue(rs, "ACTG_CYCLE_DOM");
        writer.write(String.format("      <ACDom>%s</ACDom>\n", XMLGenerationUtils.escapeXml(acDomValue)));

        // 3) BlWn (billing when) — default to "1" if blank
        String blWn = XMLGenerationUtils.getColumnValue(rs, "BILL_WHEN");
        if (blWn.isEmpty())
            blWn = "1";
        writer.write(String.format("      <BlWn>%s</BlWn>\n", XMLGenerationUtils.escapeXml(blWn)));

        // 4) CrtT (bill created time) — default to 2004-01-01T00:00:00Z, normalized to
        // ISO-UTC

        String crtTRaw = XMLGenerationUtils.getColumnValue(rs, "BILL_CREATED_T");
        String crtTIso = XMLGenerationUtils.formatEpochToIso(crtTRaw);
        if (crtTIso.isEmpty()) {
            crtTIso = "2004-01-01T00:00:00Z";
        } else {
            crtTIso = ensureIsoUtcZ(crtTIso);
        }
        writer.write(String.format("      <CrtT>%s</CrtT>%n", XMLGenerationUtils.escapeXml(crtTIso)));

        try {
            int acDom = Integer.parseInt(acDomValue);
            LocalDate today = LocalDate.now();
            int todayDay = today.getDayOfMonth();

            // Determine correct month/year based on logic
            LocalDate billingDate;
            if (acDom > todayDay) {
                // Use current month
                billingDate = LocalDate.of(today.getYear(), today.getMonth(),
                        Math.min(acDom, today.lengthOfMonth()));
            } else {
                // Use next month
                LocalDate nextMonth = today.plusMonths(1);
                billingDate = LocalDate.of(nextMonth.getYear(), nextMonth.getMonth(),
                        Math.min(acDom, nextMonth.lengthOfMonth()));
            }

            writer.write(String.format("      <ANxt>%sT00:00:00Z</ANxt>\n", billingDate));
        } catch (NumberFormatException e) {
            writer.write("      <ANxt/>\n"); // fallback for invalid/missing ACTG_CYCLE_DOM
        }

        writer.write(String.format("      <BillInfoId>%s</BillInfoId>\n", XMLGenerationUtils.escapeXml(billInfoId)));
        XMLGenerationUtils.writeMappedElement(writer, rs, "BILLING_STATUS", "BillStat", null);
        writer.write("    </ABinfo>\n");
    }
}
