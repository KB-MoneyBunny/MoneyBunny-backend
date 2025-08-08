package org.scoula.asset.mapper;

import org.apache.ibatis.annotations.Param;
import org.scoula.asset.domain.AccountTransactionVO;

import java.util.List;

public interface AssetAccountTransactionMapper {
    List<AccountTransactionVO> findByAccountIdWithPaging(@Param("accountId") Long accountId,
                                                         @Param("offset") int offset,
                                                         @Param("size") int size,
                                                         @Param("txType") String txType);

    int countByAccountId(@Param("accountId") Long accountId,
                         @Param("txType") String txType);

    void updateMemo(@Param("transactionId") Long transactionId, @Param("memo") String memo);

}
