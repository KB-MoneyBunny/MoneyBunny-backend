package org.scoula.policy.domain;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PolicyRegionVO {

    private Long id;
    private String regionCode; //  시군구 코드
}
