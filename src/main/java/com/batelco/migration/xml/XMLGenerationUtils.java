package com.batelco.migration.xml;

import com.batelco.migration.config.XmlTagMapping;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

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
            String value = rs.getString(columnName);
            if (value == null) {
                System.out.println("Null value for column: " + columnName);
            }
            return value == null ? "" : value;
        } catch (SQLException e) {
            System.out.println("Column not found: " + columnName);
            e.printStackTrace();
            return "";
        }
    }

    public static void writeMappedElement(OutputStreamWriter writer, ResultSet rs,
            String columnName, String elementName,
            Map<String, String> tagMap) throws IOException {
        String value = getColumnValue(rs, columnName);
        if (!value.isEmpty()) {
            // Apply element-specific mappings
            switch (elementName) {
                case "BType":
                    value = XmlTagMapping.getBusinessTypeMapping().getOrDefault(value, value);
                    break;
                case "SubSta":
                    value = XmlTagMapping.getSubStaMapping().getOrDefault(value, value);
                    break;
                case "Typ":
                    value = XmlTagMapping.getTypMapping().getOrDefault(value, value);
                    break;
            }

            writer.write(String.format("      <%s>%s</%s>\n",
                    elementName,
                    escapeXml(value),
                    elementName));
        }
    }

}
