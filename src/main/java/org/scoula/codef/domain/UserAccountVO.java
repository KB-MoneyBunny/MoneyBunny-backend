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
public class UserAccountVO {

    private Long id;
    private Long userId;
    private String bankCode;
    private String accountName;
    private String accountNumber;
    private String accountType;
    private Long balance;
    private java.util.Date createdAt;

    // 1:N
    private List<AccountTransactionVO> accountTransactions;
}