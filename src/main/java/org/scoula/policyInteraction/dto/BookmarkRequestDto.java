package org.scoula.policyInteraction.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BookmarkRequestDto {
    private Long userId;
    private Long policyId;
}