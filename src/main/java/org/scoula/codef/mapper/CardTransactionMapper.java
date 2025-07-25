package org.scoula.codef.mapper;

import org.scoula.codef.domain.CardTransactionVO;

import java.util.List;

public interface CardTransactionMapper {
    // 1. 카드 거래내역 저장
    int insertCardTransaction(CardTransactionVO tx);

    // 2. 카드별 전체 거래내역 조회
    List<CardTransactionVO> findCardTransactions(Long cardId);

    // 3. 미분류 거래내역 조회 (GPT 분류 대상 찾기)
    List<CardTransactionVO> findUnclassifiedTransactions(Long cardId);

    // 4. 거래내역 카테고리 업데이트
    int updateTransactionCategory(Long txId, Long categoryId);
}
