package org.scoula.asset.mapper;

import org.apache.ibatis.annotations.Param;
import org.scoula.asset.domain.AccountTransactionVO;

import java.time.LocalDateTime;
import java.util.List;

public interface AssetAccountTransactionMapper {
    List<AccountTransactionVO> findByAccountIdWithPaging(@Param("accountId") Long accountId,
                                                         @Param("offset") int offset,
                                                         @Param("size") int size,
                                                         @Param("txType") String txType);

    int countByAccountId(@Param("accountId") Long accountId,
                         @Param("txType") String txType);

    void updateMemo(@Param("transactionId") Long transactionId, @Param("memo") String memo);

    boolean existsRentTransactionByUserId(@Param("userId") Long userId);

    List<AccountTransactionVO> findByAccountIdFiltered(
            @Param("accountId") Long accountId,
            @Param("offset") int offset,
            @Param("size") int size,
            @Param("txType") String txType,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("q") String q,
            @Param("sort") String sort // latest | oldest
    );

    int countByAccountIdFiltered(
            @Param("accountId") Long accountId,
            @Param("txType") String txType,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("q") String q
    );
}
