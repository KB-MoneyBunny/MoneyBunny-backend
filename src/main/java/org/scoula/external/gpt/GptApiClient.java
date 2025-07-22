package org.scoula.external.gpt;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scoula.external.gpt.dto.GptRequestDto;
import org.scoula.external.gpt.dto.GptResponseDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
@RequiredArgsConstructor
public class GptApiClient {

    private final RestTemplate restTemplate = new RestTemplate(); // 나중에 Bean 주입으로 바꾸면 더 좋음
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${gpt.secretkey}")
    private String apiKey;

    public GptResponseDto analyzePolicy(GptRequestDto dto) {
        try {
            String prompt = dto.toPrompt();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            //  JSON body 구성 시 ObjectMapper 사용
            ObjectNode systemMessage = objectMapper.createObjectNode();
            systemMessage.put("role", "user");
            systemMessage.put("content", prompt);

            ObjectNode request = objectMapper.createObjectNode();
            request.put("model", "gpt-4o");
            request.set("messages", objectMapper.createArrayNode().add(systemMessage));
            request.put("temperature", 0.2);

            String requestBody = objectMapper.writeValueAsString(request);

            HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    "https://api.openai.com/v1/chat/completions",
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            String json = response.getBody();
            JsonNode root = objectMapper.readTree(json);
            String content = root.path("choices").get(0).path("message").path("content").asText();

            // 세 키를 모두 포함하는 JSON 블록만 추출
            content = content.replaceAll("(?s).*?(\\{[^}]*\"isFinancialSupport\"[^}]*\"estimatedAmount\"[^}]*\"policyBenefitDescription\"[^}]*\\}).*", "$1");

            // 그대로 파싱
            JsonNode resultNode = objectMapper.readTree(content);
            boolean isFinancial = resultNode.path("isFinancialSupport").asBoolean(false);
            long estimated = resultNode.path("estimatedAmount").asLong(0);
            String description = resultNode.path("policyBenefitDescription").asText("금전적 지원 없음");

            return new GptResponseDto(isFinancial, estimated, description);

        } catch (Exception e) {
            log.warn("[GPT 분석 실패]", e);
            return new GptResponseDto(false, 0, "금전적 지원 없음") ;
        }
    }
}
