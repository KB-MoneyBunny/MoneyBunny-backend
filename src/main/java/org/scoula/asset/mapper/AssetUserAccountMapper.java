package org.scoula.asset.mapper;

import org.scoula.asset.domain.AccountSummaryVO;

import java.util.List;

public interface AssetUserAccountMapper {
    Long findUserIdByLoginId(String loginId);
    List<AccountSummaryVO> findAccountSummariesByUserId(Long userId);

}
