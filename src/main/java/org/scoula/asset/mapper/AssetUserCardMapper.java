package org.scoula.asset.mapper;

import org.scoula.asset.domain.CardSummaryVO;

import java.util.List;

public interface AssetUserCardMapper {
    List<CardSummaryVO> findCardSummariesByUserId(Long userId);
}
