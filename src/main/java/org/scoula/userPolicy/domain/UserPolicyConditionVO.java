package org.scoula.userPolicy.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.util.List;

@Data                    // getter, setter, toString 자동 생성
@NoArgsConstructor       // 기본 생성자 생성
@AllArgsConstructor      // 모든 필드 생성자 생성
@Builder                 // 빌더 패턴 적용
public class UserPolicyConditionVO {
    private Long id;
    private Long userId;
    private int age;
    private String marriage;
    private long income;
    private int money_rank;
    private int period_rank;
    private int Popularity_rank;


    //조건 목록 추가
    private List<UserRegionVO> regions;
    private List<UserEducationLevelVO> educationLevels;
    private List<UserEmploymentStatusVO> employmentStatuses;
    private List<UserMajorVO> majors;
    private List<UserSpecialConditionVO> specialConditions;
    private List<UserPolicyKeywordVO> keywords;

    private Timestamp createdAt;
    private Timestamp updatedAt;
}
