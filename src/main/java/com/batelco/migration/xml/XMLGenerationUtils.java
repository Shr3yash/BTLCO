package com.batelco.migration.xml;

import com.batelco.migration.config.XmlTagMapping;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.sql.ResultSetMetaData;

public class XMLGenerationUtils {

    public static String escapeXml(String value) {
        if (value == null)
            return "";
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }

    public static String getColumnValue(ResultSet rs, String columnName) {
        try {
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();
            boolean columnExists = false;
            for (int i = 1; i <= columnCount; i++) {
                if (metaData.getColumnLabel(i).equalsIgnoreCase(columnName)) {
                    columnExists = true;
                    break;
                }
            }
            if (!columnExists) {
                System.out.println("Column not found: " + columnName);
                return "";
            }

            String value = rs.getString(columnName);
            if (value == null) {
                System.out.println("Null value for column: " + columnName);
            }
            return value == null ? "" : value.trim();

        } catch (SQLException e) {
            System.out.println("Error retrieving column: " + columnName);
            e.printStackTrace();
            return "";
        }
    }

    public static void writeElement(OutputStreamWriter writer, String elementName, String value) throws IOException {
        writer.write(String.format("      <%s>%s</%s>%n", elementName, escapeXml(value), elementName));
    }

    public static void writeMappedElement(OutputStreamWriter writer, ResultSet rs,
            String columnName, String elementName,
            Map<String, String> tagMap) throws IOException {
        String value = getColumnValue(rs, columnName);

        // Centralized element-specific mappings
        Map<String, Map<String, String>> elementMappings = Map.of(
                "BType", XmlTagMapping.getBusinessTypeMapping(),
                "SubSta", XmlTagMapping.getSubStaMapping(),
                "Typ", XmlTagMapping.getTypMapping(),
                "PTyp", Map.of("10001", "INV", "10007", "NPC"),
                "PhTyp", Map.of(
                        "Ph", "0", "H", "1", "W", "2", "P", "3",
                        "F", "3", "PG", "4", "PP", "5", "S", "6"),
                "DelPrf", XmlTagMapping.getDelPrfMapping());

        // Apply mapping if available
        if (elementMappings.containsKey(elementName)) {
            value = elementMappings.get(elementName).getOrDefault(value, value);
        }

        // Always write the tag, even if value is empty
        writer.write(String.format("      <%s>%s</%s>%n",
                elementName,
                escapeXml(value),
                elementName));
    }

}
