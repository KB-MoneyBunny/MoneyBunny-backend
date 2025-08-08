package org.scoula.asset.service;

import org.scoula.asset.mapper.AssetCategoryMapper;
import org.scoula.codef.domain.CardTransactionVO;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

@Service
public class TransactionCategorizer {

    private final AssetCategoryMapper assetCategoryMapper;
    private final Map<String, Long> typeMap = new HashMap<>();
    private final Map<String, Long> nameMap = new HashMap<>();

    //글로벌 카테고리 이름 → 키워드 리스트 매핑
    private static final Map<String, List<String>> GLOBAL_KEYWORDS = Map.ofEntries(
            Map.entry("교통/자동차", List.of("택시", "버스", "지하철", "철도", "충전소", "주유소")),
            Map.entry("생활", List.of("생활용품", "생활", "다이소")),
            Map.entry("레저와 여가생활", List.of("놀이공원", "영화관", "노래방", "레저", "여가", "티빙", "오락실")),
            Map.entry("마트", List.of("마트", "이마트", "롯데마트", "홈플러스", "코스트코")),
            Map.entry("편의점", List.of("편의점", "CU", "GS25", "세븐일레븐", "이마트24")),
            Map.entry("쇼핑", List.of("백화점", "아울렛", "무신사", "지마켓", "11번가", "유니클로")),
            Map.entry("온라인 쇼핑", List.of("전자상거래", "인터넷PG", "옥션")),
            Map.entry("커피와 디저트", List.of("커피", "카페", "투썸", "스타벅스", "디저트")),
            Map.entry("뷰티", List.of("뷰티", "화장품", "미용실")),
            Map.entry("식비", List.of("한식", "일식", "패스트푸드", "스넥/휴게음식점", "버거", "맘스터치", "치킨", "아이스크림")),
            Map.entry("보험과 금융", List.of("보험", "금융", "이체", "송금")),
            Map.entry("건강과 의료", List.of("약국", "의원", "병원", "진료")),
            Map.entry("교육", List.of("학원", "교육", "강의")),
            Map.entry("여행", List.of("항공", "호텔", "리조트", "아고다")),
            Map.entry("주류", List.of("주점", "맥주", "와인", "소주")),
            Map.entry("카테고리 미지정", List.of())
    );

    public TransactionCategorizer(AssetCategoryMapper assetCategoryMapper) {
        this.assetCategoryMapper = assetCategoryMapper;
    }



    @PostConstruct
    public void initMaps() {
        for (Entry<String, List<String>> entry : GLOBAL_KEYWORDS.entrySet()) {
            String categoryName = entry.getKey();
            Long catId = assetCategoryMapper.findIdByNameAndGlobal(categoryName);
            for (String keyword : entry.getValue()) {
                typeMap.put(keyword, catId);
                nameMap.put(keyword, catId);
            }
        }
    }

    /**
     * 주어진 거래에 대해 글로벌 룰(type/name) 기반으로 카테고리를 분류
     */
    public Long categorizeGlobal(CardTransactionVO tx) {
        // 1) store_name / store_name1 키워드 매핑 최우선
        String s1 = tx.getStoreName()  != null ? tx.getStoreName()  : "";
        String s2 = tx.getStoreName1() != null ? tx.getStoreName1() : "";
        for (Entry<String, Long> entry : nameMap.entrySet()) {
            String key = entry.getKey();
            if (s1.contains(key) || s2.contains(key)) {
                return entry.getValue();
            }
        }

        // 2) store_type 기반 매핑
        Long cat = typeMap.get(tx.getStoreType());
        if (cat != null) {
            return cat;
        }

        // 3) 둘 다 매칭 안 되면 null → 서비스 레이어에서 미분류 처리
        return null;
    }
}