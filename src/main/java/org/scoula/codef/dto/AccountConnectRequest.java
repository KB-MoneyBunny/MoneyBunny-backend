package org.scoula.codef.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

// 1. DTO 클래스: 사용자 입력값 받기
@ApiModel(description = "계좌 연동(계정 연결) 요청 DTO")
@Data
public class AccountConnectRequest {
    @ApiModelProperty(value = "은행_코드", example = "0004")
    private String organization;
    @ApiModelProperty(value = "은행_아이디", example = "CAESES0522")
    private String loginId;
    @ApiModelProperty(value = "은행_비밀번호", example = "GGHDUDDD@@")
    private String password;
}
