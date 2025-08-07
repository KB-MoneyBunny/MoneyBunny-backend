package org.scoula.asset;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.scoula.asset.mapper.AssetCategoryMapper;
import org.scoula.asset.service.TransactionCategorizer;
import org.scoula.codef.domain.CardTransactionVO;

import static org.junit.jupiter.api.Assertions.*;

class TransactionCategorizerTest {

    TransactionCategorizer categorizer;

    @BeforeEach
    void setUp() {
        // FakeCategoryMapper를 구현해서 주입
        AssetCategoryMapper fakeMapper = new FakeCategoryMapper();
        categorizer = new TransactionCategorizer(fakeMapper);
        categorizer.initMaps();
    }

    @Test
    void nameMappingTakesPrecedence_overTypeMapping() {
        CardTransactionVO tx = new CardTransactionVO();
        tx.setStoreName("맘스터치 어린이대공원점");
        tx.setStoreName1("");
        tx.setStoreType("편의점"); // typeMap에도 매핑되어 있지만, nameMap 우선

        Long cat = categorizer.categorizeGlobal(tx);
        assertEquals(100L, cat, "storeName '맘스터치' → 식비(100)로 매핑되어야 한다");
    }

    @Test
    void typeMappingWhenNameNoMatch() {
        CardTransactionVO tx = new CardTransactionVO();
        tx.setStoreName("알 수 없는 가맹점");
        tx.setStoreName1("");
        tx.setStoreType("편의점");

        Long cat = categorizer.categorizeGlobal(tx);
        assertEquals(200L, cat, "storeType '편의점' → 편의점(200)로 매핑되어야 한다");
    }

    @Test
    void noMatchReturnsNull() {
        CardTransactionVO tx = new CardTransactionVO();
        tx.setStoreName("FooBar");
        tx.setStoreName1("BazQux");
        tx.setStoreType("UNKNOWN_TYPE");

        Long cat = categorizer.categorizeGlobal(tx);
        assertNull(cat, "매핑 키워드가 없으면 null 반환");
    }

    /**
     * CategoryMapper를 직접 구현한 Fake 클래스.
     * GLOBAL_KEYWORDS에 정의된 키워드 → 카테고리 ID 매핑용
     */
    static class FakeCategoryMapper implements AssetCategoryMapper {
        @Override
        public Long findIdByNameAndGlobal(String name) {
            return switch (name) {
                case "식비"           -> 100L;
                case "편의점"         -> 200L;
                case "교통/자동차"    -> 300L;
                case "카테고리 미지정" -> 999L;
                // 테스트에 필요하면 다른 카테고리도 추가 가능
                default              -> null;
            };
        }
    }
}
