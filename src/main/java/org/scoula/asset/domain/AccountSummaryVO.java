package org.scoula.asset.domain;

import lombok.Data;

@Data
public class AccountSummaryVO {
    private Long id;
    private String bankCode;
    private String accountName;
    private String accountNumber;
    private String accountType;
    private Long balance;
}

