package org.scoula.asset.domain;

import lombok.*;

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
    private String paymentType; // single/installment/other
    private Integer installmentMonth;
    private java.util.Date transactionDate;
    private String storeName;
    private String storeName1;
    private String storeType;
    private String cancelStatus; // normal/cancel/partial_cancel/reject
    private Long cancelAmount;
}
