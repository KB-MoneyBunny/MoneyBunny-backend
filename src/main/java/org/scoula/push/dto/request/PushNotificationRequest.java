package org.scoula.push.dto.request;

import lombok.Data;

@Data
public class PushNotificationRequest {
    private String title;
    private String body;
    private String targetToken;
}