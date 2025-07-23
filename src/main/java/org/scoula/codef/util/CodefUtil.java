package org.scoula.codef.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class CodefUtil {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * CODEF 응답에서 result.code가 "CF-00000"이면 true (성공), 아니면 false (실패)
     */
    public static boolean isSuccess(String json) {
        try {
            JsonNode root = objectMapper.readTree(json);
            String resultCode = root.path("result").path("code").asText();
            return "CF-00000".equals(resultCode);
        } catch (Exception e) {
            return false; // JSON 파싱 실패도 실패로 간주
        }
    }

    /**
     * CODEF 응답에서 result.message 메시지 추출 (없으면 null)
     */
    public static String getResultMessage(String json) {
        try {
            JsonNode root = objectMapper.readTree(json);
            return root.path("result").path("message").asText(null);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * CODEF 응답에서 result.code를 직접 가져오기
     */
    public static String getResultCode(String json) {
        try {
            JsonNode root = objectMapper.readTree(json);
            return root.path("result").path("code").asText(null);
        } catch (Exception e) {
            return null;
        }
    }

    public static String getConnectedId(String json) {
        try {
            JsonNode root = objectMapper.readTree(json);
            return root.path("data").path("connectedId").asText(null);
        } catch (Exception e) {
            return null;
        }
    }

    public static JsonNode getAccountList(String json) {
        try {
            JsonNode root = objectMapper.readTree(json);
            return root.path("data").path("resAccountList");
        } catch (Exception e) {
            return null;
        }
    }
}
