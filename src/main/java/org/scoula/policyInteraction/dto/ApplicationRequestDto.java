package org.scoula.policyInteraction.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApplicationRequestDto {
    private Long userId;
    private Long policyId;
    private String applicationUrl;
}
