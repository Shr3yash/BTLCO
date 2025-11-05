package com.batelco.migration.xml;

import com.batelco.migration.config.XmlTagMapping;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.sql.ResultSetMetaData;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

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

    public static void writePhTypElement(OutputStreamWriter writer, ResultSet rs,
            String columnName, String elementName)
            throws SQLException, IOException {
        String code = getColumnValue(rs, columnName);
        String label = switch (code) {
            case "0" -> "Ph";
            case "1" -> "H";
            case "2" -> "W";
            case "3" -> "P"; // Choose "P" or "F"
            case "4" -> "PG";
            case "5" -> "PP";
            case "6" -> "S";
            default -> code;
        };
        writer.write(String.format("          <%s>%s</%s>%n", elementName, escapeXml(label), elementName));
    }

    public static void writeTypElement(OutputStreamWriter writer, ResultSet rs,
            String columnName, String elementName)
            throws SQLException, IOException {
        String code = getColumnValue(rs, columnName);
        String label = switch (code) {
            case "0" -> "FED";
            case "1" -> "STT";
            case "2" -> "CIT";
            case "4" -> "SCN";
            case "5" -> "SCI";
            case "7" -> "SST";
            default -> code;
        };
        writer.write(String.format("          <%s>%s</%s>%n", elementName, escapeXml(label), elementName));
    }

    public static void writeMappedElement(OutputStreamWriter writer, ResultSet rs,
            String columnName, String elementName,
            Map<String, String> tagMap) throws IOException {

        String raw = getColumnValue(rs, columnName);
        String value = (raw == null) ? null : raw.trim();

        // Centralized element-specific mappings
        Map<String, Map<String, String>> elementMappings = Map.of(
                "BType", XmlTagMapping.getBusinessTypeMapping(),
                "SubSta", XmlTagMapping.getSubStaMapping(),
                "Typ", XmlTagMapping.getTypMapping(),
                "PTyp", Map.of(
                        "10001", "INV",
                        "10007", "NPC"),
                "PhTyp", Map.of(
                        "0", "Ph",
                        "1", "H",
                        "2", "W",
                        "3", "P",
                        "4", "PG",
                        "5", "PP",
                        "6", "S"),
                "DelPrf", XmlTagMapping.getDelPrfMapping(),
                // NEW: BillStat mapping
                "BillStat", Map.of(
                        "1", "0") // convert 1 -> 0
        );

        // Apply mapping if available
        if (elementMappings.containsKey(elementName) && value != null) {
            value = elementMappings.get(elementName).getOrDefault(value, value);
        }

        // Element-specific defaults (applied when value is null/blank)
        Map<String, String> elementDefaults = Map.of(
                "BillStat", "0" // defaulted to 0 as req by azra
        );

        if ((value == null || value.isEmpty()) && elementDefaults.containsKey(elementName)) {
            value = elementDefaults.get(elementName);
        }

        // Always write the tag, even if value is empty
        writer.write(String.format("      <%s>%s</%s>%n",
                elementName,
                escapeXml(value == null ? "" : value),
                elementName));
    }

    // epoch/ISO formatting helpers for Eff/CrtT

    private static final DateTimeFormatter OUTPUT_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");

    public static String formatEpochToIso(String epochRaw) {
        if (epochRaw == null || epochRaw.isBlank()) {
            return "";
        }

        long epochSeconds = Long.parseLong(epochRaw.trim()); // BRM timestamps are seconds since epoch
        Instant instant = Instant.ofEpochSecond(epochSeconds);

        // key line: use the server's default timezone at runtime
        ZoneId serverZone = ZoneId.systemDefault();

        ZonedDateTime zdt = instant.atZone(serverZone);

        // Example output: 2024-01-30T02:30:00+05:30
        // or 2024-01-30T00:00:00+03:00, etc., depending on server config
        return OUTPUT_FMT.format(zdt);
    }

    public static void writeEffAndCrtT(OutputStreamWriter writer, ResultSet rs)
            throws SQLException, IOException {
        String effRaw = getColumnValue(rs, "ACC_EFFECTIVE_T");
        String crtRaw = getColumnValue(rs, "ACC_CREATED_T");

        String effIso = formatEpochToIso(effRaw);
        String crtIso = formatEpochToIso(crtRaw);

        if (!effIso.isEmpty()) {
            writer.write(String.format("      <Eff>%s</Eff>%n", escapeXml(effIso)));
        }
        if (!crtIso.isEmpty()) {
            writer.write(String.format("      <CrtT>%s</CrtT>%n", escapeXml(crtIso)));
        }
    }

    public static String buildABinfoOpenTag(String scope, String formattedParentRef) {
        String balGrpName;
        boolean addHasNoPayInfoRef = false;

        switch (scope) {
            case "CA":
            case "DA":
                balGrpName = "Default Balance Group";
                addHasNoPayInfoRef = true;
                break;
            case "BA":
                balGrpName = "Account Level Balance Group";
                break;
            case "SA":
                balGrpName = "Account Level Balance Group";
                break;
            default: // safe default if unknown scope
                balGrpName = "Default Balance Group";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("    <ABinfo");
        sb.append(" bal_grp=\"true\"");
        sb.append(" bal_grp_name=\"").append(XMLGenerationUtils.escapeXml(balGrpName)).append("\"");
        sb.append(" elem=\"1\"");
        sb.append(" global=\"true\"");
        if (addHasNoPayInfoRef) {
            sb.append(" hasNoPayInfoRef=\"true\"");
        }
        sb.append(" isAccBillinfo=\"Y\"");

        if ("SA".equals(scope)) {
            sb.append(" parentElem=\"1\"");
            if (formattedParentRef != null && !formattedParentRef.isEmpty()) {
                sb.append(" payingParenRefId=\"")
                        .append(XMLGenerationUtils.escapeXml(formattedParentRef))
                        .append("\"");
            }
        }

        sb.append(">\n");
        return sb.toString();
    }

}
