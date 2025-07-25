// PushNotificationRequest.java
package org.scoula.push.dto;

import lombok.Data;

@Data
public class PushNotificationRequest {
    private String title;
    private String body;
    private String targetToken;
}
