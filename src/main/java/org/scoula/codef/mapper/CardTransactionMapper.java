package org.scoula.codef.mapper;

import org.apache.ibatis.annotations.Param;
import org.scoula.codef.domain.CardTransactionVO;

import java.util.List;
import java.util.Set;

public interface CardTransactionMapper {
    // 카드 거래내역 저장
    int insertCardTransaction(CardTransactionVO tx);

    String findLastTransactionDateByCardId(Long cardId);

    Set<String> findAllTxKeyByCardIdFromDate(@Param("cardId")Long cardId, @Param("startDate")String startDate);

    void insertCardTransactions(List<CardTransactionVO> batch);
}
