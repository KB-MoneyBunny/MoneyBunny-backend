package org.scoula.policy.dto;

import lombok.Data;

@Data
public class YouthPolicyApiResponse {
    private int resultCode;
    private String resultMessage;
    private YouthPolicyResult result;
}
