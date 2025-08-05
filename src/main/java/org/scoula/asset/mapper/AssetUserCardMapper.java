package org.scoula.asset.mapper;

import org.apache.ibatis.annotations.Param;
import org.scoula.asset.domain.CardSummaryVO;

import java.util.List;

public interface AssetUserCardMapper {
    List<CardSummaryVO> findCardSummariesByUserId(Long userId);

    boolean isCardOwner(@Param("userId")Long userId, @Param("cardId")Long cardId);

}
