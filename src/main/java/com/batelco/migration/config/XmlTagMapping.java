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
            "8", "VIP"
        );
    }

    public static Map<String, String> getSubStaMapping() {
        return Map.of(
            "10100", "A",
            "10102", "I",
            "10103", "C"
        );
    }

    public static Map<String, String> getTypMapping() {
        return Map.of(
            "0", "FED",
            "1", "STT",
            "2", "CIT",
            "4", "SCN",
            "5", "SCI",
            "7", "SST"
        );
    }

    public static Map<String, String> getPTypMapping() {
        return Map.of(
            "10001", "INV",
            "10005", "DD",
            "10003", "CC",
            "10007", "NPC"
        );
    }

    public static Map<String, String> getPhTypMapping() {
        return Map.of(
            "Ph", "0",
            "H", "1",
            "W", "2",
            "P", "3",
            "F", "3",
            "PG", "4",
            "PP", "5",
            "S", "6"
        );
    }

    public static Map<String, String> getDelPrfMapping() {
        return Map.of(
            "E", "0",
            "P", "1",
            "F", "2"
        );
    }
    public static Map<String, String> getBATagMap() {
        return getColumnToTagMap();
    }
    
    public static Map<String, String> getDepartmentTagMap() {
        return getColumnToTagMap();
    }
    
    public static Map<String, String> getServiceTagMap() {
        return getColumnToTagMap();
    }
    

    public static String getRootAttributes() {
        return "xmlns=\"http://www.portal.com/InfranetXMLSchema\" " +
               "xmlns:math=\"xalana://java.lang.Math\" " +
               "xmlns:xalan=\"http://xml.apache.org/xalan\" " +
               "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
               "xsi:schemaLocation=\"http://www.oracle.com/schema/brm CMT_Subscribers.xsd\"";
    }
} 
