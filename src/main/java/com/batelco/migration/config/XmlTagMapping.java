package com.batelco.migration.config;

import java.util.HashMap;
import java.util.Map;

public class XmlTagMapping {
    public static Map<String, String> getColumnToTagMap() {
        Map<String, String> tagMap = new HashMap<>();
        // Direct mappings
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
        tagMap.put("LAST_NAME", "LNm");
        tagMap.put("PERCENT", "Perc");
        tagMap.put("TYPE", "Typ");
        return tagMap;
    }

    public static Map<String, String> getBusinessTypeMapping() {
        Map<String, String> mapping = new HashMap<>();
        mapping.put("0", "U");
        mapping.put("1", "C");
        mapping.put("2", "B");
        mapping.put("3", "BAT");
        mapping.put("4", "MGD");
        mapping.put("5", "MSA");
        mapping.put("6", "RES");
        mapping.put("7", "UMG");
        mapping.put("8", "VIP");
        return mapping;
    }

    public static Map<String, String> getSubStaMapping() {
        Map<String, String> mapping = new HashMap<>();
        mapping.put("10100", "A");
        mapping.put("10102", "I");
        mapping.put("10103", "C");
        return mapping;
    }

    public static Map<String, String> getTypMapping() {
        Map<String, String> mapping = new HashMap<>();
        mapping.put("0", "FED");
        mapping.put("1", "STT");
        mapping.put("2", "CIT");
        mapping.put("4", "SCN");
        mapping.put("5", "SCI");
        mapping.put("7", "SST");
        return mapping;
    }

    public static String getRootAttributes() {
        return "xmlns=\"http://www.portal.com/InfranetXMLSchema\" " +
                "xmlns:math=\"xalana://java.lang.Math\" " +
                "xmlns:xalan=\"http://xml.apache.org/xalan\" " +
                "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
                "xsi:schemaLocation=\"http://www.oracle.com/schema/brm CMT_Subscribers.xsd\"";
    }

    // BA-specific mappings
    public static Map<String, String> getBATagMap() {
        Map<String, String> tagMap = new HashMap<>();
        tagMap.put("ACCOUNT_NO", "ActNo");
        tagMap.put("CURRENCY", "Curr");
        // Add all other BA-specific mappings
        return tagMap;
    }

    public static Map<String, String> getBATypMapping() {
        Map<String, String> mapping = new HashMap<>();
        mapping.put("0", "FED");
        mapping.put("1", "STT");
        // Add other BA-specific type mappings
        return mapping;
    }

    //Department Account mappings will be below. 
    
    public static Map<String, String> getDepartmentTagMap() {
        Map<String, String> tagMap = new HashMap<>();
        tagMap.put("ACCOUNT_NO", "ActNo");
        tagMap.put("CURRENCY", "Curr");
        tagMap.put("CUST_SEG_LIST", "CustSegList");
        tagMap.put("STATUS", "SubSta");
        tagMap.put("BUSINESS_TYPE", "BType");
        tagMap.put("GL_SEGMENT", "GLSgmt");
        tagMap.put("TYPE", "Typ");
        return tagMap;
    }
    
    public static Map<String, String> getDepartmentTypeMapping() {
        Map<String, String> mapping = new HashMap<>();
        mapping.put("0", "FED");
        mapping.put("1", "STT");
        mapping.put("2", "CIT");
        // Add other department-specific mappings
        return mapping;
    }

    // Service Account mappings begin here
    public static Map<String, String> getServiceTagMap() {
        Map<String, String> tagMap = new HashMap<>();
        tagMap.put("ACCOUNT_NO", "ActNo");
        tagMap.put("CURRENCY", "Curr");
        tagMap.put("CUST_SEG_LIST", "CustSegList");
        tagMap.put("STATUS", "SubSta");
        tagMap.put("BUSINESS_TYPE", "BType");
        tagMap.put("GL_SEGMENT", "GLSgmt");
        tagMap.put("TYPE", "Typ");
        return tagMap;
    }

    public static Map<String, String> getServiceTypeMapping() {
        Map<String, String> mapping = new HashMap<>();
        mapping.put("0", "FED");
        mapping.put("1", "STT");
        // Add other service-specific mappings
        return mapping;
    }
}