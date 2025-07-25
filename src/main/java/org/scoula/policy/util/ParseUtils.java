package org.scoula.policy.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class ParseUtils {

    /**
     * 문자열을 Integer로 안전하게 파싱
     */
    public static Integer parseInteger(String s) {
        try {
            return s == null ? null : Integer.parseInt(s.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 문자열을 Long으로 안전하게 파싱
     */
    public static Long parseLong(String s) {
        try {
            return s == null ? null : Long.parseLong(s.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * yyyyMMdd 형식의 문자열을 LocalDate로 파싱
     */
    public static LocalDate parseLocalDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) return null;
        try {
            return LocalDate.parse(dateStr.trim(), DateTimeFormatter.ofPattern("yyyyMMdd"));
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}