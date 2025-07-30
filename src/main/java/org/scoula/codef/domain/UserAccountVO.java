package org.scoula.codef.domain;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ApiModel(value = "UserAccountVO", description = "사용자 계좌 정보 VO")
public class UserAccountVO {

    @ApiModelProperty(value = "계좌 PK (자동 생성)", example = "null")
    private Long id;
    @ApiModelProperty(value = "사용자 PK (자동 매핑)", example = "null")
    private Long userId;
    @ApiModelProperty(value = "은행 코드", example = "0020")
    private String bankCode;
    @ApiModelProperty(value = "계좌 이름", example = "스무살우리 통장")
    private String accountName;
    @ApiModelProperty(value = "계좌 번호", example = "1234567890123")
    private String accountNumber;
    @ApiModelProperty(value = "계좌 타입(예: 11: 입출금)", example = "11")
    private String accountType;
    @ApiModelProperty(value = "계좌 잔액", example = "492924")
    private Long balance;
    @ApiModelProperty(value = "계좌 등록 일시", example = "21753774483293")
    private java.util.Date createdAt;

    // 1:N
    @ApiModelProperty(value = "계좌 거래내역 리스트", example = "null")
    private List<AccountTransactionVO> accountTransactions;
}