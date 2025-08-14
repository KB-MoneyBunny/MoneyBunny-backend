package org.scoula.policyInteraction.util;

public class NameMaskingUtil {
    
    /**
     * 이름을 마스킹 처리합니다.
     * - 2글자: 첫 글자 + "*" (예: "박일" -> "박*")
     * - 3글자: 첫 글자 + "*" + 마지막 글자 (예: "박수일" -> "박*일")
     * - 4글자 이상: 첫 글자 + 중간 "*" 개수 + 마지막 글자 (예: "황수민더" -> "황**더")
     * 
     * @param name 마스킹할 이름
     * @return 마스킹된 이름
     */
    public static String maskName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return "";
        }
        
        name = name.trim();
        int length = name.length();
        
        if (length == 1) {
            return name; // 1글자는 그대로 반환
        } else if (length == 2) {
            return name.charAt(0) + "*"; // 첫 글자 + *
        } else if (length == 3) {
            return name.charAt(0) + "*" + name.charAt(2); // 첫 글자 + * + 마지막 글자
        } else {
            // 4글자 이상: 첫 글자 + 중간 * 개수 + 마지막 글자
            StringBuilder masked = new StringBuilder();
            masked.append(name.charAt(0));
            for (int i = 1; i < length - 1; i++) {
                masked.append("*");
            }
            masked.append(name.charAt(length - 1));
            return masked.toString();
        }
    }
}