package com.careerbridge.util;

import java.util.Map;
import java.util.StringJoiner;

/** Minimal JSON helper (no external library): build strings and read top-level string values. */
public final class Json {
    private Json() { }

    /** Returns a JSON string literal, escaping quotes, backslashes and control characters. null becomes JSON null. */
    public static String quote(String s) {
        if (s == null) return "null";
        StringBuilder b = new StringBuilder("\"");
        for (char c : s.toCharArray()) {
            switch (c) {
                case '\\' -> b.append("\\\\");
                case '"' -> b.append("\\\"");
                case '\n' -> b.append("\\n");
                case '\r' -> b.append("\\r");
                case '\t' -> b.append("\\t");
                default -> {
                    if (c < 0x20) b.append(String.format("\\u%04x", (int) c));
                    else b.append(c);
                }
            }
        }
        return b.append('"').toString();
    }

    public static String object(Map<String, String> values) {
        StringJoiner j = new StringJoiner(",", "{", "}");
        values.forEach((k, v) -> j.add(quote(k) + ":" + quote(v)));
        return j.toString();
    }

    /** Reads the string value of a key; returns "" if the key is missing or the value is not a string. */
    public static String get(String json, String key) {
        String needle = "\"" + key + "\"";
        int p = json.indexOf(needle);
        if (p < 0) return "";
        p = json.indexOf(':', p + needle.length());
        if (p < 0) return "";
        p++;
        while (p < json.length() && Character.isWhitespace(json.charAt(p))) p++;
        if (p >= json.length() || json.charAt(p) != '"') return "";
        p++;
        StringBuilder b = new StringBuilder();
        boolean esc = false;
        for (; p < json.length(); p++) {
            char c = json.charAt(p);
            if (esc) {
                switch (c) {
                    case 'n' -> b.append('\n');
                    case 't' -> b.append('\t');
                    case 'r' -> b.append('\r');
                    case 'b' -> b.append('\b');
                    case 'f' -> b.append('\f');
                    case 'u' -> {
                        if (p + 4 < json.length()) {
                            try { b.append((char) Integer.parseInt(json.substring(p + 1, p + 5), 16)); p += 4; }
                            catch (NumberFormatException e) { b.append('u'); }
                        } else b.append('u');
                    }
                    default -> b.append(c);
                }
                esc = false;
            } else if (c == '\\') esc = true;
            else if (c == '"') break;
            else b.append(c);
        }
        return b.toString();
    }
}
