package org.scoula.policy.util;

import java.util.ArrayList;
import java.util.List;

public class StringUtils {

    /**
     * 문자열이 null이 아니고 공백이 아닌 문자를 포함하는지 확인
     */
    public static boolean hasText(String str) {
        return str != null && !str.trim().isEmpty();
    }

    /**
     * 쉼표로 구분된 문자열을 리스트로 분리
     */
    public static List<String> splitCommaSeparated(String raw) {
        List<String> result = new ArrayList<>();
        if (hasText(raw)) {
            for (String s : raw.split(",")) {
                String cleaned = s.trim();
                if (!cleaned.isEmpty()) {
                    result.add(cleaned);
                }
            }
        }
        return result;
    }
}