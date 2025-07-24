package org.scoula.codef.domain;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountTransactionVO {

    private Long id;
    private Long accountId;
    private Long amount;
    private String txType;  // 'income', 'expense', 'saving'
    private java.util.Date transactionDateTime;
    private Long balanceAfter;
    private String storeName;
    private String branchName;

    // Getter & Setter 생략
}