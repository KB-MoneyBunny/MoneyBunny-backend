package org.scoula.userPolicy.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SearchRequestDTO {
    private int age;
    private String marriage;
    private long income;
    private List<String> regions;
    private List<String> educationLevels;
    private List<String> employmentStatuses;
    private List<String> majors;
    private List<String> specialConditions;
    private List<String> keywords;

    private String searchText; // ← 자유 검색어 (제목, 내용 등에서 검색)
}