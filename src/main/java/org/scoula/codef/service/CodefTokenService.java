package org.scoula.codef.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scoula.common.util.RedisUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class CodefTokenService {

    private static final String CODEF_TOKEN_KEY = "codef:accessToken";

    @Value("${codef.client_id}")
    private String clientId;

    @Value("${codef.client_secret}")
    private String clientSecret;

    private final RedisUtil redisUtil;

    public String getAccessToken() {
        // 1. Redis에서 먼저 꺼내기
        String cachedToken = redisUtil.get(CODEF_TOKEN_KEY);
        if (cachedToken != null) {
            log.info("[CODEF] Redis에서 토큰 가져옴");
            return cachedToken;
        }
        // 2. 없으면 CODEF에 새로 요청
        try {
            URL url = new URL("https://oauth.codef.io/oauth/token");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            String auth = clientId + ":" + clientSecret;
            conn.setRequestProperty("Authorization", "Basic " +
                    Base64.getEncoder().encodeToString(auth.getBytes()));

            conn.setDoOutput(true);
            String params = "grant_type=client_credentials&scope=read";

            try (OutputStream os = conn.getOutputStream()) {
                os.write(params.getBytes());
            }


            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) response.append(line);
            in.close();

            String result = response.toString();

            String accessToken = result.split("\"access_token\":\"")[1].split("\"")[0];

            // 3. Redis에 6일 23시간(=604,200초) 저장 (7일 만료라 1시간 버퍼)
            redisUtil.set(CODEF_TOKEN_KEY, accessToken, 604200, TimeUnit.SECONDS);

            log.info("[CODEF] 토큰 새로 발급 & Redis 저장 완료 (유효기간: 7일)");
            return accessToken;

        } catch (Exception e) {
            log.error("[CODEF] 토큰 발급 실패", e);
            throw new RuntimeException("토큰 발급 실패", e);
        }
    }
}