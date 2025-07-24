package org.scoula.userPolicy.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;


@Data                    // getter, setter, toString 자동 생성
@NoArgsConstructor       // 기본 생성자 생성
@AllArgsConstructor      // 모든 필드 생성자 생성
@Builder                 // 빌더 패턴 적용
public class UserPolicyKeywordVO {
    private Long userPolicyConditionId;
    private Long keywordId;
    private Timestamp createdAt;
}
