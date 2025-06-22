package com.batelco.migration.xml;

import com.batelco.migration.config.XmlTagMapping;
import com.batelco.migration.sql.QueryExecutor;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Map;
import java.util.Random;

public class BAXMLGenerator {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    public static void generateXML(Connection connection, String sqlQuery, String outputFile) {
        Map<String, String> tagMap = XmlTagMapping.getBATagMap();

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

        // String isEnterprise = getColumnValue(rs, "IS_ENTER_ACCT");
        String deptAccount = getColumnValue(rs, "DEPT_ACCOUNT_NO");
        String custAccount = getColumnValue(rs, "CUST_ACCOUNT_NO");

        String parentRef;
        // if ("yes".equalsIgnoreCase(isEnterprise) && !deptAccount.isEmpty()) {
        if (!deptAccount.isEmpty()) {
            parentRef = "DA_" + deptAccount;
        } else {
            parentRef = "CA_" + custAccount;
        }

        writer.write(String.format("  <ActSbsc id=\"%s\" isParent=\"Y\" parenRef=\"%s\">\n",
                escapeXml(accountNo), escapeXml(parentRef)));

        writer.write("    <Act>\n");
        writeElement(writer, "ActNo", accountNo);
        writeMappedElement(writer, rs, "CURRENCY", "Curr", tagMap);
        writeMappedElement(writer, rs, "CUSTOMER_SEGMENT_LIST", "CustSegList", tagMap);
        XMLGenerationUtils.writeMappedElement(writer, rs, "STATUS", "SubSta", tagMap);
        writeMappedElement(writer, rs, "BUSINESS_TYPE", "BType", tagMap);
        writeMappedElement(writer, rs, "AAC_ACCESS", "SrvAACAccess", tagMap);
        writeMappedElement(writer, rs, "GL_SEGMENT", "GLSgmt", tagMap);

        writer.write("      <ANArr elem=\"1\">\n");
        writeMappedElement(writer, rs, "ADDRESS", "Add", tagMap);
        writeMappedElement(writer, rs, "CITY", "City", tagMap);
        writeMappedElement(writer, rs, "COUNTRY", "Cnt", tagMap);
        writeMappedElement(writer, rs, "FIRST_NAME", "FNm", tagMap);
        writeMappedElement(writer, rs, "MIDDLE_NAME", "MNm", tagMap);
        writeMappedElement(writer, rs, "LAST_NAME", "LNm", tagMap);
        writeMappedElement(writer, rs, "EMAIL_ADDR", "EAdd", tagMap);
        writeMappedElement(writer, rs, "TITLE", "Tit", tagMap);
        writeMappedElement(writer, rs, "STATE", "Stt", tagMap);
        writeMappedElement(writer, rs, "ZIP", "Zip", tagMap);

        writer.write("        <APhArr id=\"0\">\n");
        writeMappedElement(writer, rs, "PHONE", "Ph", tagMap);
        writeMappedElement(writer, rs, "PHONE_TYPE", "PhTyp", tagMap);
        writer.write("        </APhArr>\n");
        writer.write("      </ANArr>\n");

        writer.write("      <AEArr>\n");
        writer.write("        <CertNum/>\n");
        writeMappedElement(writer, rs, "PERCENT", "Perc", tagMap);
        writeMappedElement(writer, rs, "TYPE", "Typ", tagMap);
        writer.write("      </AEArr>\n");
        writer.write("    </Act>\n");

        writePromotions(writer, rs, accountNo);
        writeABinfo(writer, rs, parentRef);
        writeAPinfo(writer, rs, accountNo);

        writer.write("  </ActSbsc>\n");
    }

    private static void writeAccountElementFlag(OutputStreamWriter writer, ResultSet rs, Map<String, String> tagMap)
            throws SQLException, IOException {
        String accountNo = getColumnValue(rs, "ACCOUNT_NO").replaceAll("_1$", "");

        String isEnterprise = getColumnValue(rs, "IS_ENTER_ACCT");
        boolean isEnterpriseAccount = "yes".equalsIgnoreCase(isEnterprise);

        String parentAccount = isEnterpriseAccount
                ? getColumnValue(rs, "DEPT_ACCOUNT_NO")
                : getColumnValue(rs, "CUST_ACCOUNT_NO");

        String parentPrefix = isEnterpriseAccount ? "DA_" : "CA_";
        String formattedParentRef = parentPrefix + parentAccount;

        writer.write(String.format("  <ActSbsc id=\"%s\" isParent=\"Y\" parenRef=\"%s\">\n",
                escapeXml(accountNo), escapeXml(formattedParentRef)));

        writer.write("    <Act>\n");
        writeElement(writer, "ActNo", accountNo);
        writeMappedElement(writer, rs, "CURRENCY", "Curr", tagMap);
        writeMappedElement(writer, rs, "CUSTOMER_SEGMENT_LIST", "CustSegList", tagMap);
        XMLGenerationUtils.writeMappedElement(writer, rs, "STATUS", "SubSta", tagMap);
        writeMappedElement(writer, rs, "BUSINESS_TYPE", "BType", tagMap);
        writeMappedElement(writer, rs, "AAC_ACCESS", "SrvAACAccess", tagMap);
        writeMappedElement(writer, rs, "GL_SEGMENT", "GLSgmt", tagMap);

        writer.write("      <ANArr elem=\"1\">\n");
        writeMappedElement(writer, rs, "ADDRESS", "Add", tagMap);
        writeMappedElement(writer, rs, "CITY", "City", tagMap);
        writeMappedElement(writer, rs, "COUNTRY", "Cnt", tagMap);
        writeMappedElement(writer, rs, "FIRST_NAME", "FNm", tagMap);
        writeMappedElement(writer, rs, "MIDDLE_NAME", "MNm", tagMap);
        writeMappedElement(writer, rs, "LAST_NAME", "LNm", tagMap);
        writeMappedElement(writer, rs, "EMAIL_ADDR", "EAdd", tagMap);
        writeMappedElement(writer, rs, "TITLE", "Tit", tagMap);
        writeMappedElement(writer, rs, "STATE", "Stt", tagMap);
        writeMappedElement(writer, rs, "ZIP", "Zip", tagMap);

        writer.write("        <APhArr id=\"0\">\n");
        writeMappedElement(writer, rs, "PHONE", "Ph", tagMap);
        writeMappedElement(writer, rs, "PHONE_TYPE", "PhTyp", tagMap);
        writer.write("        </APhArr>\n");
        writer.write("      </ANArr>\n");

        writer.write("      <AEArr>\n");
        writer.write("        <CertNum/>\n");
        writeMappedElement(writer, rs, "PERCENT", "Perc", tagMap);
        writeMappedElement(writer, rs, "TYPE", "Typ", tagMap);
        writer.write("      </AEArr>\n");
        writer.write("    </Act>\n");

        writePromotions(writer, rs, accountNo);
        writeABinfo(writer, rs, formattedParentRef);
        writeAPinfo(writer, rs, accountNo);

        writer.write("  </ActSbsc>\n");
    }

    private static void writeABinfo(OutputStreamWriter writer, ResultSet rs, String formattedParentRef)
            throws SQLException, IOException {
        writer.write(String.format(
                // " <ABinfo global=\"true\" spnrCnt=\"1\" spnreeCnt=\"2\" elem=\"1\"
                // bal_grp=\"true\" isAccBillinfo=\"Yes\" payInfoRefId=\"%s\">\n",
                "    <ABinfo global=\"true\" bal_grp=\"true\" isAccBillinfo=\"Yes\" >\n",
                escapeXml(formattedParentRef)));

        writeMappedElement(writer, rs, "ACTG_CYCLE_DOM", "ACDom", null);
        XMLGenerationUtils.writeMappedElement(writer, rs, "PAY_TYPE", "PTyp", null);

        // Custom static values
        writer.write("      <ActgType>B</ActgType>\n");
        // writer.write(" <ANxt>2024-05-01T00:00:00Z</ANxt>\n");

        // updated anxt tag

        String acDomValue = XMLGenerationUtils.getColumnValue(rs, "ACTG_CYCLE_DOM");
        writer.write(String.format("      <ACDom>%s</ACDom>\n", XMLGenerationUtils.escapeXml(acDomValue)));

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

        String billInfoId = XMLGenerationUtils.getColumnValue(rs, "BILL_INFO_ID");
        if (billInfoId.isEmpty()) {
            billInfoId = "Default BillInfo";
        }
        writer.write(String.format("      <BillInfoId>%s</BillInfoId>\n", XMLGenerationUtils.escapeXml(billInfoId)));
        XMLGenerationUtils.writeMappedElement(writer, rs, "BILLING_STATUS", "BillStat", null);
        writer.write("    </ABinfo>\n");
    }

    private static void writeAPinfo(OutputStreamWriter writer, ResultSet rs, String accountNo)
            throws SQLException, IOException {
        int randomApId = 200000 + new Random().nextInt(100000); // 6-digit ID starting with 2
        writer.write(String.format("    <APinfo id=\"%s\" type=\"/payinfo/invoice\">\n",
                escapeXml(String.valueOf(randomApId))));
        writeMappedElement(writer, rs, "DUE_DOM", "DDom", null);
        writeMappedElement(writer, rs, "PAYMENT_TERM", "PaymentTerm", null);

        writer.write("      <payInvExtn>\n");
        writeMappedElement(writer, rs, "INV_ADDRESS", "Add", null);
        writeMappedElement(writer, rs, "INV_CITY", "Cty", null);
        writeMappedElement(writer, rs, "INV_COUNTRY", "Cntr", null);
        writeMappedElement(writer, rs, "DELIVERY_DESCR", "DelDsc", null);
        String rawVal = getColumnValue(rs, "INV_DELIVERY_PREFER");
        String mappedVal = XmlTagMapping.getDelPrfMapping().getOrDefault(rawVal, rawVal);
        writer.write(String.format("      <DelPrf>%s</DelPrf>%n", escapeXml(mappedVal)));
        writeMappedElement(writer, rs, "INV_EMAIL_ADDR", "EAdd", null);
        writeMappedElement(writer, rs, "INV_INSTR", "InvInstr", null);
        writeMappedElement(writer, rs, "INV_NAME", "Nm", null);
        writeMappedElement(writer, rs, "INV_STATE", "Stt", null);
        writeMappedElement(writer, rs, "INV_ZIP", "Zip", null);
        writer.write("      </payInvExtn>\n");

        writer.write("      <myExtension>\n");
        writer.write("        <myArray table=\"profile_inv_ext_t\" elem=\"1\">\n");

        writeFieldElement(writer, rs, "BTC_FLD_ADR_LINE1", "FldAdLineA");
        writeFieldElement(writer, rs, "BTC_FLD_ADR_LINE2", "FldAdLineB");
        writeFieldElement(writer, rs, "BTC_FLD_ARB_ADR_LINE1", "FldArbAdLineA");
        writeFieldElement(writer, rs, "BTC_FLD_ARB_ADR_LINE2", "FldArbAdLineB");
        writeFieldElement(writer, rs, "BTC_FLD_ARB_NAME", "FldArbName");
        writeFieldElement(writer, rs, "BTC_FLD_BLOCK_NO", "FldBlockNo");
        writeFieldElement(writer, rs, "BTC_FLD_BUILDING_NUMBER", "FldBuildNum");
        writeFieldElement(writer, rs, "BTC_FLD_FLAT_NO", "FldFlatNo");
        writeFieldElement(writer, rs, "BTC_LANG_PREFER", "FldLangPref");
        writeFieldElement(writer, rs, "BTC_LANGUAGE_PREFER", "FldLangPrefer");
        writeFieldElement(writer, rs, "BTC_FLD_PO_BOX", "FldPoBox");
        writeFieldElement(writer, rs, "BTC_FLD_ROAD_NO", "FldRoadNo");
        writeFieldElement(writer, rs, "BTC_EMAIL_ADDR", "FldEmailAddr");
        writeFieldElement(writer, rs, "BTC_MANAGER_EMAIL", "FldManEmail");
        writeFieldElement(writer, rs, "BTC_FLD_PHONE", "FldPhone");
        writeFieldElement(writer, rs, "BTC_FLAG", "FldFlag", "integer");
        writeFieldElement(writer, rs, "BTC_NOTIFICATION_TYPE", "FldNotType");
        writeFieldElement(writer, rs, "BTC_FLD_LANGUAGE", "FldLang");
        writeFieldElement(writer, rs, "BTC_CARE_OF_NAME", "FldCOfName");

        writer.write("        </myArray>\n");
        writer.write("      </myExtension>\n");
        writer.write("    </APinfo>\n");
    }

    /*
     * private static void writePromotions(OutputStreamWriter writer, ResultSet rs,
     * String accountNo)
     * throws SQLException, IOException {
     * writer.write(String.
     * format("    <ActProm id=\"%s\" type=\"/profile/acct_extrating\" global=\"true\">\n"
     * ,
     * escapeXml(accountNo)));
     * writer.write("      <PrmNm>TAXEXEMPT</PrmNm>\n");
     * writer.write("      <PrmActLvlExtn>\n");
     * writer.write("        <ALPArr elem=\"1\">\n");
     * writer.write("          <Nam>TAXEXEMPT</Nam>\n");
     * writer.write("          <Val>0</Val>\n");
     * writer.write("        </ALPArr>\n");
     * writer.write("      </PrmActLvlExtn>\n");
     * writer.write("    </ActProm>\n");
     * }
     */

    private static void writePromotions(OutputStreamWriter writer, ResultSet rs, String accountNo)
            throws SQLException, IOException {
        int randomId = 100000 + new Random().nextInt(100000); // Generates a number from 100000 to 199999
        String generatedId = String.valueOf(randomId);

        writer.write(String.format("    <ActProm id=\"%s\" type=\"/profile/acct_extrating\" global=\"true\">\n",
                escapeXml(generatedId)));
        writer.write("      <PrmNm>TAXEXEMPT</PrmNm>\n");
        writer.write("      <PrmActLvlExtn>\n");
        writer.write("        <ALPArr elem=\"1\">\n");
        writer.write("          <Nam>TAXEXEMPT</Nam>\n");
        writer.write("          <Val>0</Val>\n");
        writer.write("        </ALPArr>\n");
        writer.write("      </PrmActLvlExtn>\n");
        writer.write("    </ActProm>\n");
    }

    private static void writeFieldElement(OutputStreamWriter writer, ResultSet rs, String columnName,
            String elementName)
            throws SQLException, IOException {
        writeFieldElement(writer, rs, columnName, elementName, "string");
    }

    private static void writeFieldElement(OutputStreamWriter writer, ResultSet rs, String columnName,
            String elementName, String type)
            throws SQLException, IOException {
        String value = getColumnValue(rs, columnName);
        writer.write(
                String.format("          <%s type=\"%s\">%s</%s>\n", elementName, type, escapeXml(value), elementName));
    }

    // to be used for a method below this method. do not remove, tagging explictly
    // made for
    // BA xmls.
    private static Map<String, String> getReverseMapping(String type) {
        return switch (type) {
            case "PhTyp" -> Map.of(
                    "0", "Ph",
                    "1", "H",
                    "2", "W",
                    "3", "P", // F also maps to 3, P preferred
                    "4", "PG",
                    "5", "PP",
                    "6", "S");
            case "Typ" -> Map.of(
                    "0", "FED",
                    "1", "STT",
                    "2", "CIT",
                    "4", "SCN",
                    "5", "SCI",
                    "7", "SST");
            case "SubSta" -> Map.of(
                    "10100", "A",
                    "10102", "I",
                    "10103", "C");
            case "BType" -> Map.of(
                    "0", "U",
                    "1", "C",
                    "2", "B",
                    "3", "BAT",
                    "4", "MGD",
                    "5", "MSA",
                    "6", "RES",
                    "7", "UMG",
                    "8", "VIP");
            case "PTyp" -> Map.of(
                    "10001", "INV",
                    "10005", "DD",
                    "10003", "CC",
                    "10007", "NPC");
            default -> Map.of(); // No mapping
        };
    }

    private static void writeMappedElement(OutputStreamWriter writer, ResultSet rs,
            String columnName, String elementName,
            Map<String, String> tagMap)
            throws SQLException, IOException {
        String value = getColumnValue(rs, columnName);

        // Apply reverse mapping if present
        if (tagMap != null && tagMap.containsKey(columnName)) {
            Map<String, String> reverseMap = getReverseMapping(tagMap.get(columnName));
            value = reverseMap.getOrDefault(value, value);
        }

        if (!value.isEmpty()) {
            writer.write(String.format("      <%s>%s</%s>\n",
                    elementName, escapeXml(value), elementName));
        }
    }

    private static void writeElement(OutputStreamWriter writer, String elementName, String value)
            throws IOException {
        writer.write(String.format("      <%s>%s</%s>\n", elementName, escapeXml(value), elementName));
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
        if (value == null)
            return "";
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }
}
