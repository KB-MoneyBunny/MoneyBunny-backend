package org.scoula.asset.domain;

import lombok.*;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountTransactionVO {
    private Long id;
    private Long accountId;
    private Long amount;
    private String txType; // income/expense/saving
    private java.util.Date transactionDateTime;
    private Long balanceAfter;
    private String storeName;
    private String branchName;
    private String memo;
}
