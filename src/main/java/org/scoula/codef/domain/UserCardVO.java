package org.scoula.codef.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserCardVO {

    private Long id;
    private Long userId;
    private String issuerCode;
    private String cardName;
    private String cardMaskedNumber;
    private String cardType;
    private String cardImage;
    private java.util.Date createdAt;

    // 1:N
    private List<CardTransactionVO> cardTransactions;
}