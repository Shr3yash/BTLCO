package com.batelco.migration.xml;

import com.batelco.migration.config.XmlTagMapping;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

public class XMLGenerationUtils {

    public static String escapeXml(String value) {
        if (value == null) return "";
        return value.replace("&", "&amp;")
                    .replace("<", "&lt;")
                    .replace(">", "&gt;")
                    .replace("\"", "&quot;")
                    .replace("'", "&apos;");
    }

    public static String getColumnValue(ResultSet rs, String columnName) throws SQLException {
        try {
            String value = rs.getString(columnName);
            return value != null ? value.trim() : "";
        } catch (SQLException e) {
            return "";
        }
    }

    public static void writeMappedElement(OutputStreamWriter writer, ResultSet rs,
                                          String columnName, String elementName,
                                          Map<String, String> tagMap) throws SQLException, IOException {
        try {
            String value = getColumnValue(rs, columnName);
            if (!value.isEmpty()) {
                // for appluing element-specific mappings
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
        } catch (SQLException e) {
            // Optionally log or handle the missing column
        }
    }
}
