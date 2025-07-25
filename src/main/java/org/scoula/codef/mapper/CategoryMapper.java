package org.scoula.codef.mapper;

import org.scoula.codef.domain.TransactionCategoryVO;

import java.util.List;

public interface CategoryMapper {
    // 1. 미분류(UNCLASSIFIED) category_id 찾기
    Long findUnclassifiedId();

    // 2. code로 category_id 찾기 (유니크 키 활용)
    Long findIdByCode(String code);

    // 3. 전체 카테고리 리스트
    List<TransactionCategoryVO> findAllCategories();
}
