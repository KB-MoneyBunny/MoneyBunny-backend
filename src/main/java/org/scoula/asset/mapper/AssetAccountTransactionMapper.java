package org.scoula.asset.mapper;

import org.apache.ibatis.annotations.Param;
import org.scoula.asset.domain.AccountTransactionVO;

import java.util.List;

public interface AssetAccountTransactionMapper {
    List<AccountTransactionVO> findByAccountIdWithPaging(@Param("accountId") Long accountId,
                                                         @Param("offset") int offset,
                                                         @Param("size") int size);

    int countByAccountId(Long accountId);

}
