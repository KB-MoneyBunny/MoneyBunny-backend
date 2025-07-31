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
@ApiModel(value = "UserCardVO", description = "사용자 카드 정보 VO")
public class UserCardVO {


    @ApiModelProperty(value = "카드 PK(자동 생성)", example = "null")
    private Long id;
    @ApiModelProperty(value = "회원 PK", example = "null")
    private Long userId;
    @ApiModelProperty(value = "카드사 코드", example = "0309")
    private String issuerCode;
    @ApiModelProperty(value = "카드명", example = "카드의정석 I&U+")
    private String cardName;
    @ApiModelProperty(value = "카드 마스킹 번호", example = "1234********5678")
    private String cardMaskedNumber;
    @ApiModelProperty(value = "카드 타입", example = "본인|신용|Master")
    private String cardType;
    @ApiModelProperty(value = "카드 이미지 URL", example = "https://pc.wooricard.com/webcontent/cdPrdImgFileList/2024/2/13/1931f194-e38e-4c90-87d3-f084acb6218a.png")
    private String cardImage;
    @ApiModelProperty(value = "카드 등록일시", example = "1753835592885")
    private java.util.Date createdAt;

    @ApiModelProperty(value = "카드 거래내역 리스트", example = "null")
    private List<CardTransactionVO> cardTransactions;
}