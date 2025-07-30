package org.scoula.userPolicy.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.scoula.userPolicy.domain.*;

import java.util.List;

@Data
@NoArgsConstructor       // 기본 생성자
@AllArgsConstructor      // 모든 필드 생성자
@Builder
public class TestResultRequestDTO {
    private int age;
    private String marriage;
    private long income;
    private List<String> regions;
    private List<String> educationLevels;
    private List<String> employmentStatuses;
    private List<String> majors;
    private List<String> specialConditions;
    private List<String> keywords;
    private int money_rank;
    private int period_rank;
    private int Popularity_rank;
}


