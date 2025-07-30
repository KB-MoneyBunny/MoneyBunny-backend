package org.scoula.codef.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;


@ApiModel(description = "카드 연동(계정 연결) 요청 DTO")
@Data
public class CardConnectRequest {
    @ApiModelProperty(value = "카드사 코드", example = "0311")
    private String organization;
    @ApiModelProperty(value = "카드 로그인 아이디", example = "CAESES0522")
    private String loginId;
    @ApiModelProperty(value = "카드 비밀번호", example = "GGHDUDDD@@")
    private String password;
}
