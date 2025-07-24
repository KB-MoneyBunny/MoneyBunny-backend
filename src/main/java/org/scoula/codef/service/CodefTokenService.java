package org.scoula.codef.service;


import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class CodefTokenService {

    @Value("${codef.client_id}")
    private String clientId;

    @Value("${codef.client_secret}")
    private String clientSecret;

    public String getAccessToken() {
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

            System.out.println(response.toString());

            return response.toString().split("\"access_token\":\"")[1].split("\"")[0];

        } catch (Exception e) {
            throw new RuntimeException("토큰 발급 실패", e);
        }
    }
}