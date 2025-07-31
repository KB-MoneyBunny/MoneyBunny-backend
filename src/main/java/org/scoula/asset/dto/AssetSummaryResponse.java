package org.scoula.asset.dto;

import lombok.Data;
import org.scoula.asset.domain.AccountSummaryVO;
import org.scoula.asset.domain.CardSummaryVO;

import java.util.List;

@Data
public class AssetSummaryResponse {
    private Long totalAsset; // 내 자산(계좌 잔액 합)
    private Long thisMonthCardUsed; // 이번달 카드 사용금액
    private List<AccountSummaryVO> accounts; // 계좌 현황
    private List<CardSummaryVO> cards; // 카드 현황
}
