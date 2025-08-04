package org.scoula.asset.mapper;

import org.apache.ibatis.annotations.Param;
import org.scoula.asset.domain.AccountSummaryVO;

import java.util.List;

public interface AssetUserAccountMapper {
    Long findUserIdByLoginId(String loginId);
    List<AccountSummaryVO> findAccountSummariesByUserId(Long userId);

    boolean isAccountOwner(@Param("userId")Long userId, @Param("accountId")Long accountId);
}
