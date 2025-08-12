package org.scoula.policyInteraction.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * 리뷰 내용의 욕설 및 비속어 필터링 유틸리티
 */
@Component
@Slf4j
public class ProfanityFilter {
    
    private final Set<String> profanityWords = new HashSet<>();
    private Pattern profanityPattern;
    
    /**
     * 애플리케이션 시작 시 욕설 목록 파일 로드
     */
    @PostConstruct
    public void init() {
        try (InputStream is = getClass().getResourceAsStream("/filter/profanity-words.txt")) {
            if (is == null) {
                log.warn("욕설 필터 파일을 찾을 수 없습니다: /filter/profanity-words.txt");
                return;
            }
            
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                String word;
                while ((word = reader.readLine()) != null) {
                    // 주석과 빈 줄 제외
                    if (!word.trim().isEmpty() && !word.startsWith("#")) {
                        profanityWords.add(word.trim().toLowerCase());
                    }
                }
                
                // 정규식 패턴 생성 (성능 최적화)
                if (!profanityWords.isEmpty()) {
                    String pattern = String.join("|", profanityWords);
                    profanityPattern = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
                }
                
                log.info("욕설 필터 초기화 완료: {}개 금지어 로드", profanityWords.size());
            }
        } catch (IOException e) {
            log.error("욕설 필터 파일 로드 실패", e);
        }
    }
    
    /**
     * 텍스트에 욕설이 포함되어 있는지 검사
     * @param text 검사할 텍스트
     * @return 욕설 포함 여부 (true: 욕설 있음, false: 깨끗함)
     */
    public boolean containsProfanity(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        
        if (profanityPattern == null) {
            return false; // 필터가 초기화되지 않은 경우
        }
        
        // 공백 제거한 텍스트도 검사 (띄어쓰기로 우회 방지)
        String normalizedText = text.toLowerCase().replaceAll("\\s+", "");
        
        // 원본 텍스트 검사
        if (profanityPattern.matcher(text.toLowerCase()).find()) {
            return true;
        }
        
        // 공백 제거한 텍스트 검사
        if (profanityPattern.matcher(normalizedText).find()) {
            return true;
        }
        
        // 개별 단어로도 검사 (더 정확한 매칭)
        for (String word : profanityWords) {
            if (normalizedText.contains(word)) {
                log.debug("욕설 감지: '{}' in text", word);
                return true;
            }
        }
        
        return false;
    }
    
}