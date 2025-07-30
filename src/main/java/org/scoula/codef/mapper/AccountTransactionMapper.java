package org.scoula.codef.mapper;

import org.apache.ibatis.annotations.Param;
import org.scoula.codef.domain.AccountTransactionVO;

import java.util.List;
import java.util.Set;

public interface AccountTransactionMapper {
    void insertAccountTransaction(AccountTransactionVO vo);

    String findLastTransactionDateByAccountId(Long accountId);

    Set<String> findAllTxKeyByAccountIdFromDate(@Param("accountId") Long accountId, @Param("startDate") String startDate);

    void insertAccountTransactions(List<AccountTransactionVO> batch);
}
