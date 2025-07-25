package org.scoula.codef.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CardTransactionVO {

    private Long id;
    private Long categoryId;
    private Long cardId;
    private String approvalNo;
    private Long amount;
    private String paymentType;
    private Integer installmentMonth;
    private java.util.Date transactionDate;
    private String storeName;
    private String storeType;
    private String cancelStatus;
    private Long cancelAmount;
    private String storeName1;

    // Getter & Setter 생략
}