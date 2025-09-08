package com.batelco.migration.config;

import java.util.HashMap;
import java.util.Map;

public class XmlTagMapping {
    public static Map<String, String> getColumnToTagMap() {
        Map<String, String> tagMap = new HashMap<>();
        tagMap.put("ACCOUNT_NO", "ActNo");
        tagMap.put("CURRENCY", "Curr");
        tagMap.put("CUST_SEG_LIST", "CustSegList");
        tagMap.put("ACCOUNT_STATUS", "SubSta");
        tagMap.put("BUSINESS_TYPE", "BType");
        tagMap.put("AAC_ACCESS", "SrvAACAccess");
        tagMap.put("GL_SEGMENT", "GLSgmt");
        tagMap.put("IDENTIFICATION_CODE", "IdentificationCode");
        tagMap.put("ADDRESS", "Add");
        tagMap.put("CITY", "City");
        tagMap.put("COUNTRY", "Cnt");
        tagMap.put("FIRST_NAME", "FNm");
        tagMap.put("MIDDLE_NAME", "MNm");
        tagMap.put("LAST_NAME", "LNm");
        tagMap.put("EMAIL_ADDR", "EAdd");
        tagMap.put("TITLE", "Tit");
        tagMap.put("STATE", "Stt");
        tagMap.put("ZIP", "Zip");
        tagMap.put("PHONE", "Ph");
        tagMap.put("PHONE_TYPE", "PhTyp");
        tagMap.put("PERCENT", "Perc");
        tagMap.put("TYPE", "Typ");
        return tagMap;
    }

    // ---------- NEW: ABinfo child-tag mapping (columns -> XML tags) ----------
    // Make sure your SQL returns these column names (aliases OK).
    public static Map<String, String> getABinfoTagMap() {
        Map<String, String> m = new HashMap<>();
        m.put("ACTG_TYPE",        "ActgType");      // e.g., 'B'
        m.put("AC_DOM",           "ACDom");         // e.g., '01'
        m.put("BILL_WHEN",        "BlWn");          // NEW
        m.put("BILL_CREATED_T",   "CrtT");          // NEW (ISO 8601 UTC)
        m.put("ACTG_NEXT_T",      "ANxt");          // NEW (ISO 8601 UTC)
        m.put("PAY_TYPE",         "PTyp");          // e.g., 'PREPAID'
        m.put("BI_STATUS",        "BISta");         // e.g., 'A'
        m.put("BILL_STATUS",      "BillStat");      // e.g., '0'
        m.put("BILLINFO_ID",      "BillInfoId");    // e.g., 'Prepaid Bill Info'
        return m;
    }

    // ---------- OPTIONAL: ABinfo attribute mapping ----------
    // These are not column->tag, but column->attributeName for the <ABinfo ...> open tag.
    // If some are constants, you can hardcode them in the generator instead.
    public static Map<String, String> getABinfoAttributeMap() {
        Map<String, String> attrs = new HashMap<>();
        attrs.put("GLOBAL_FLAG",     "global");         // 'true'/'false'
        attrs.put("SPNR_CNT",        "spnrCnt");        // '0'
        attrs.put("SPNREE_CNT",      "spnreeCnt");      // '0'
        attrs.put("ELEM",            "elem");           // '1'
        attrs.put("BAL_GRP",         "bal_grp");        // 'false'
        attrs.put("IS_ACC_BILLINFO", "isAccBillinfo");  // 'Yes'
        attrs.put("PAY_INFO_REF_ID", "payInfoRefId");   // '61352144'
        return attrs;
    }

    // If you want a single merged map for services that also includes ABinfo child tags:
    public static Map<String, String> getServiceTagMap() {
        Map<String, String> merged = new HashMap<>(getColumnToTagMap());
        merged.putAll(getABinfoTagMap());
        return merged;
    }

    public static Map<String, String> getBATagMap() {
        return getColumnToTagMap();
    }

    public static Map<String, String> getDepartmentTagMap() {
        return getColumnToTagMap();
    }

    // ----- existing code below -----

    public static Map<String, String> getBusinessTypeMapping() {
        return Map.of(
                "0", "U",
                "1", "C",
                "2", "B",
                "3", "BAT",
                "4", "MGD",
                "5", "MSA",
                "6", "RES",
                "7", "UMG",
                "8", "VIP");
    }

    public static Map<String, String> getSubStaMapping() {
        return Map.of(
                "10100", "A",
                "10102", "I",
                "10103", "C");
    }

    public static Map<String, String> getTypMapping() {
        return Map.of(
                "0", "FED",
                "1", "STT",
                "2", "CIT",
                "4", "SCN",
                "5", "SCI",
                "7", "SST");
    }

    public static Map<String, String> getPTypMapping() {
        return Map.of(
                "10001", "INV",
                "10005", "DD",
                "10003", "CC",
                "10007", "NPC");
    }

    public static Map<String, String> getPhTypMapping() {
        return Map.of(
                "0", "Ph",
                "1", "H",
                "2", "W",
                "3", "P", // both "P" and "F" map to 3 originally, F to be retained
                "4", "PG",
                "5", "PP",
                "6", "S");
    }

    public static Map<String, String> getDelPrfMapping() {
        return Map.of(
                "0", "E",
                "1", "P",
                "2", "F");
    }

    public static String getRootAttributes() {
        return "xmlns=\"http://www.portal.com/InfranetXMLSchema\" " +
                "xmlns:math=\"xalana://java.lang.Math\" " +
                "xmlns:xalan=\"http://xml.apache.org/xalan\" " +
                "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
                "xsi:schemaLocation=\"http://www.oracle.com/schema/brm CMT_Subscribers.xsd\"";
    }
}
