package org.scoula.policy.domain.region;

import lombok.Data;
import org.scoula.policy.util.StringUtils;

import java.util.ArrayList;
import java.util.List;


@Data
public class PolicyRegionVO {

    private Long id;
    private String regionCode; //  시군구 코드

    /**
     * 쉼표로 구분된 지역코드 문자열을 PolicyRegionVO 리스트로 변환
     */
    public static List<PolicyRegionVO> fromCommaSeparated(String raw) {
        List<PolicyRegionVO> list = new ArrayList<>();
        for (String regionCode : StringUtils.splitCommaSeparated(raw)) {
            PolicyRegionVO vo = new PolicyRegionVO();
            vo.setRegionCode(regionCode);
            list.add(vo);
        }
        return list;
    }
}
