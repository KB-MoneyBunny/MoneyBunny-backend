package org.scoula.asset.domain;

import lombok.Data;

@Data
public class CardSummaryVO {
    private Long id;
    private String issuerCode;
    private String cardName;
    private String cardMaskedNumber;
    private String cardType;
    private String cardImage;
    private Long thisMonthUsed; // 이번달 사용금액 (서비스에서 set)
}

