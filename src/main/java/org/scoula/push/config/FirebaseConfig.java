package org.scoula.push.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class FirebaseConfig {

    @Value("${firebase.type}") private String type;
    @Value("${firebase.project_id}") private String projectId;
    @Value("${firebase.private_key_id}") private String privateKeyId;
    @Value("${firebase.private_key}") private String privateKey;
    @Value("${firebase.client_email}") private String clientEmail;
    @Value("${firebase.client_id}") private String clientId;
    @Value("${firebase.auth_uri}") private String authUri;
    @Value("${firebase.token_uri}") private String tokenUri;
    @Value("${firebase.auth_provider_x509_cert_url}") private String authProviderCertUrl;
    @Value("${firebase.client_x509_cert_url}") private String clientCertUrl;

    /**
     * FirebaseApp 초기화 후 Bean으로 등록
     */
    @Bean
    public FirebaseApp firebaseApp() {
        try {
            Map<String, Object> credentials = new HashMap<>();
            credentials.put("type", type);
            credentials.put("project_id", projectId);
            credentials.put("private_key_id", privateKeyId);
            credentials.put("private_key", privateKey.replace("\\n", "\n")); // 줄바꿈 처리
            credentials.put("client_email", clientEmail);
            credentials.put("client_id", clientId);
            credentials.put("auth_uri", authUri);
            credentials.put("token_uri", tokenUri);
            credentials.put("auth_provider_x509_cert_url", authProviderCertUrl);
            credentials.put("client_x509_cert_url", clientCertUrl);

            ByteArrayInputStream serviceAccount = new ByteArrayInputStream(
                    new ObjectMapper().writeValueAsBytes(credentials)
            );

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                return FirebaseApp.initializeApp(options);
            } else {
                return FirebaseApp.getInstance();
            }

        } catch (Exception e) {
            throw new RuntimeException("FirebaseApp 초기화 실패", e);
        }
    }

    /**
     * FirebaseMessaging 인스턴스를 Bean으로 등록
     */
    @Bean
    public FirebaseMessaging firebaseMessaging(FirebaseApp firebaseApp) {
        return FirebaseMessaging.getInstance(firebaseApp);
    }
}
