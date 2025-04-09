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
        tagMap.put("IDENTIFICATION_CODE", "IDENTIFICATION_CODE");
        tagMap.put("ADDRESS", "Add");
        tagMap.put("CITY", "City");
        tagMap.put("COUNTRY", "Cnt");
        tagMap.put("FIRST_NAME", "FNm");
        tagMap.put("LAST_NAME", "LNm");
        tagMap.put("PERCENT", "Perc");
        tagMap.put("TYPE", "Typ");
        return tagMap;
    }
    
    public static String getRootAttributes() {
        return "xmlns=\"http://www.portal.com/InfranetXMLSchema\" " +
               "xmlns:math=\"xalana://java.lang.Math\" " +
               "xmlns:xalan=\"http://xml.apache.org/xalan\" " +
               "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
               "xsi:schemaLocation=\"http://www.oracle.com/schema/brm CMT_Subscribers.xsd\"";
    }
}