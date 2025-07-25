package org.scoula.policy.domain.keyword;

import lombok.Data;
import org.scoula.policy.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Data
public class PolicyKeywordVO {
    private Long id;
    private String keyword;

    /**
     * 쉼표로 구분된 키워드 문자열을 PolicyKeywordVO 리스트로 변환
     */
    public static List<PolicyKeywordVO> fromCommaSeparated(String raw) {
        List<PolicyKeywordVO> list = new ArrayList<>();
        for (String keyword : StringUtils.splitCommaSeparated(raw)) {
            PolicyKeywordVO vo = new PolicyKeywordVO();
            vo.setKeyword(keyword);
            list.add(vo);
        }
        return list;
    }
}
