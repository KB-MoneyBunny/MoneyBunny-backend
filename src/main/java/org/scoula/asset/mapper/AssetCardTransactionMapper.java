package org.scoula.asset.mapper;

import org.apache.ibatis.annotations.Param;
import org.scoula.asset.domain.CardTransactionVO;
import org.scoula.asset.dto.CategorySpending;
import org.scoula.asset.dto.MonthlyTrendDTO;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

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

    List<CategorySpending> findMonthlyCategorySpending(
            @Param("userId") Long userId,
            @Param("year") int year,
            @Param("month") int month
    );

    List<MonthlyTrendDTO> findMonthlyTrend(Map<String, Object> param);

    List<CardTransactionVO> findCategoryTransactions(@Param("userId") Long userId, @Param("categoryId") Long categoryId, @Param("year") int year, @Param("month") int month);

    int updateTransactionCategory(@Param("transactionId") Long transactionId, @Param("categoryId") Long categoryId);

    long findMonthlyTotal(@Param("userId") Long userId, @Param("year") int year, @Param("month") int month);

    List<CardTransactionVO> findAllByUserId(@Param("userId") Long userId);

    List<CardTransactionVO> findRecent6MonthsByUserId(@Param("userId") Long userId);

    boolean existsHrdKoreaCardTransactionByUserId(@Param("userId") Long userId);

    List<CardTransactionVO> findByCardIdWithFilters(
            @Param("cardId") Long cardId,
            @Param("offset") int offset,
            @Param("size") int size,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("q") String q,
            @Param("txType") String txType,    // expense | refund
            @Param("sort") String sort         // "DESC" | "ASC"
    );

    int countByCardIdWithFilters(
            @Param("cardId") Long cardId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("q") String q,
            @Param("txType") String txType
    );

}
