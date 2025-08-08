package org.scoula.asset.mapper;

import org.apache.ibatis.annotations.Param;
import org.scoula.asset.domain.CardTransactionVO;

import java.util.List;

public interface AssetCardTransactionMapper {
    Long sumThisMonthUsedByUserId(Long userId);
    Long sumThisMonthUsedByCardId(Long cardId);

    List<CardTransactionVO> findByCardIdWithPaging(@Param("cardId") Long cardId,
                                                   @Param("offset") int offset,
                                                   @Param("size") int size,
                                                   @Param("txType") String txType);

    int countByCardId(@Param("cardId") Long cardId,
                      @Param("txType") String txType);


    void updateMemo(@Param("transactionId") Long transactionId, @Param("memo") String memo);
}
