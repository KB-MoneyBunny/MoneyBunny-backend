package org.scoula.codef.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserAccountDto {
    private String account;        // 계좌번호
    private String accountName;    // 계좌명
    private String accountMasked;  // 마스킹 계좌
    private String accountType;    // 입출금/예금 등
    private Long balance;          // 잔액
}