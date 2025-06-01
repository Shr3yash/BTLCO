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
        String accountNo = getColumnValue(rs, "ACCOUNT_NO") ;
        String parentType = getParentType(rs);
        String parentRef = getParentRef(rs, parentType);
        String formattedParentRef = String.format("%s_%s", parentType.equals("CUST") ? "CA" : "DA", parentRef);

        writer.write(String.format("  <ActSbsc id=\"%s\" isParent=\"Y\" parenRef=\"%s\">\n", 
                escapeXml(accountNo), escapeXml(formattedParentRef)));

        writer.write("    <Act>\n");
        XMLGenerationUtils.writeElement(writer, "ActNo", accountNo);
        XMLGenerationUtils.writeMappedElement(writer, rs, "CURRENCY", "Curr", tagMap);
        XMLGenerationUtils.writeMappedElement(writer, rs, "CUSTOMER_SEGMENT_LIST", "CustSegList", tagMap);
        XMLGenerationUtils.writeMappedElement(writer, rs, "STATUS", "SubSta", tagMap);
        XMLGenerationUtils.writeMappedElement(writer, rs, "BUSINESS_TYPE", "BType", tagMap);        
        XMLGenerationUtils.writeMappedElement(writer, rs, "AAC_ACCESS", "SrvAACAccess", tagMap);
        XMLGenerationUtils.writeMappedElement(writer, rs, "GL_SEGMENT", "GLSgmt", tagMap);

        writer.write("      <ANArr elem=\"1\">\n");
        XMLGenerationUtils.writeMappedElement(writer, rs, "ADDRESS", "Add", tagMap);
        XMLGenerationUtils.writeMappedElement(writer, rs, "CITY", "City", tagMap);
        XMLGenerationUtils.writeMappedElement(writer, rs, "COUNTRY", "Cnt", tagMap);
        XMLGenerationUtils.writeMappedElement(writer, rs, "FIRST_NAME", "FNm", tagMap);
        XMLGenerationUtils.writeMappedElement(writer, rs, "MIDDLE_NAME", "MNm", tagMap);
        XMLGenerationUtils.writeMappedElement(writer, rs, "LAST_NAME", "LNm", tagMap);
        XMLGenerationUtils.writeMappedElement(writer, rs, "EMAIL_ADDR", "EAdd", tagMap);
        XMLGenerationUtils.writeMappedElement(writer, rs, "TITLE", "Tit", tagMap);
        XMLGenerationUtils.writeMappedElement(writer, rs, "STATE", "Stt", tagMap);
        XMLGenerationUtils.writeMappedElement(writer, rs, "ZIP", "Zip", tagMap);

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
        writeABinfo(writer, rs, formattedParentRef);
        writeAPinfo(writer, rs, accountNo);

        writer.write("  </ActSbsc>\n");
    }

    private static String getParentType(ResultSet rs) throws SQLException {
        String custRef = getColumnValue(rs, "CUST_ACCOUNT_NO");
        return !custRef.isEmpty() ? "CUST" : "DEPT";
    }

    private static String getParentRef(ResultSet rs, String parentType) throws SQLException {
        return getColumnValue(rs, parentType + "_ACCOUNT_NO");
    }

    private static void writeABinfo(OutputStreamWriter writer, ResultSet rs, String formattedParentRef) 
            throws SQLException, IOException {
        writer.write(String.format(
            "    <ABinfo global=\"true\" spnrCnt=\"1\" spnreeCnt=\"2\" elem=\"1\" bal_grp=\"true\" isAccBillinfo=\"Yes\" payInfoRefId=\"%s\">\n",
            escapeXml(formattedParentRef)));

        XMLGenerationUtils.writeMappedElement(writer, rs, "ACTG_CYCLE_DOM", "ACDom", null);
        XMLGenerationUtils.writeMappedElement(writer, rs, "PAY_TYPE", "PTyp", null);
        XMLGenerationUtils.writeMappedElement(writer, rs, "BILLING_STATUS", "BillStat", null);

        writer.write("    </ABinfo>\n");
    }

    private static void writeAPinfo(OutputStreamWriter writer, ResultSet rs, String accountNo) 
            throws SQLException, IOException {
        writer.write(String.format("    <APinfo id=\"%s\" type=\"/payinfo/invoice\">\n", escapeXml(accountNo)));

        XMLGenerationUtils.writeMappedElement(writer, rs, "DUE_DOM", "DDom", null);
        XMLGenerationUtils.writeMappedElement(writer, rs, "PAYMENT_TERM", "PaymentTerm", null);

        writer.write("      <payInvExtn>\n");
        XMLGenerationUtils.writeMappedElement(writer, rs, "INV_ADDRESS", "Add", null);
        XMLGenerationUtils.writeMappedElement(writer, rs, "INV_CITY", "Cty", null);
        XMLGenerationUtils.writeMappedElement(writer, rs, "INV_COUNTRY", "Cntr", null);
        XMLGenerationUtils.writeMappedElement(writer, rs, "DELIVERY_DESCR", "DelDsc", null);
        XMLGenerationUtils.writeMappedElement(writer, rs, "INV_DELIVERY_PREFER", "DelPrf", null);
        XMLGenerationUtils.writeMappedElement(writer, rs, "INV_EMAIL_ADDR", "EAdd", null);
        XMLGenerationUtils.writeMappedElement(writer, rs, "INV_INSTR", "InvInstr", null);
        XMLGenerationUtils.writeMappedElement(writer, rs, "INV_NAME", "Nm", null);
        XMLGenerationUtils.writeMappedElement(writer, rs, "INV_STATE", "Stt", null);
        XMLGenerationUtils.writeMappedElement(writer, rs, "INV_ZIP", "Zip", null);
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

    private static void writePromotions(OutputStreamWriter writer, ResultSet rs, String accountNo) 
            throws SQLException, IOException {
        writer.write(String.format("    <ActProm id=\"%s\" type=\"/profile/acct_extrating\" global=\"true\">\n", escapeXml(accountNo)));
        writer.write("      <PrmNm>TAXEXEMPT</PrmNm>\n");
        writer.write("      <PrmActLvlExtn>\n");
        writer.write("        <ALPArr elem=\"1\">\n");
        writer.write("          <Nam>TAXEXEMPT</Nam>\n");
        writer.write("          <Val>0</Val>\n");
        writer.write("        </ALPArr>\n");
        writer.write("      </PrmActLvlExtn>\n");
        writer.write("    </ActProm>\n");
    }

    private static void writeFieldElement(OutputStreamWriter writer, ResultSet rs,
                                        String columnName, String elementName) 
            throws SQLException, IOException {
        writeFieldElement(writer, rs, columnName, elementName, "string");
    }

    private static void writeFieldElement(OutputStreamWriter writer, ResultSet rs,
                                        String columnName, String elementName, String type) 
            throws SQLException, IOException {
        String value = getColumnValue(rs, columnName);
        writer.write(String.format("          <%s type=\"%s\">%s</%s>\n",
                elementName, type, escapeXml(value), elementName));
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
