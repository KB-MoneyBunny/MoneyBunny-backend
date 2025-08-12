package org.scoula.asset.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scoula.asset.domain.AccountSummaryVO;
import org.scoula.asset.domain.AccountTransactionVO;
import org.scoula.asset.domain.CardSummaryVO;
import org.scoula.asset.domain.CardTransactionVO;
import org.scoula.asset.dto.AssetSummaryResponse;
import org.scoula.asset.mapper.AssetAccountTransactionMapper;
import org.scoula.asset.mapper.AssetCardTransactionMapper;
import org.scoula.asset.mapper.AssetUserAccountMapper;
import org.scoula.asset.mapper.AssetUserCardMapper;
import org.scoula.common.dto.PageResponse;
import org.scoula.common.exception.UnauthorizedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class AssetService {

    private final AssetUserAccountMapper assetUserAccountMapper;
    private final AssetUserCardMapper assetUserCardMapper;
    private final AssetCardTransactionMapper assetCardTransactionMapper;
    private final AssetAccountTransactionMapper assetAccountTransactionMapper;

    public AssetSummaryResponse getSummary(Long userId) {
//        Long userId = assetUserAccountMapper.findUserIdByLoginId(loginId);

        System.out.println("userId = " + userId);

        // 1. 계좌 현황, 총액
        List<AccountSummaryVO> accounts = assetUserAccountMapper.findAccountSummariesByUserId(userId);
        Long totalAsset = accounts.stream()
                .mapToLong(AccountSummaryVO::getBalance)
                .sum();

        log.info("[ASSET] 사용자 {}의 전체 계좌 합산 자산 = {}", userId, totalAsset);

        // 2. 카드 현황(카드별 이번달 사용금액 포함)
        List<CardSummaryVO> cards = assetUserCardMapper.findCardSummariesByUserId(userId);
        for (CardSummaryVO card : cards) {
            Long cardUsed = assetCardTransactionMapper.sumThisMonthUsedByCardId(card.getId());
            card.setThisMonthUsed(cardUsed != null ? cardUsed : 0L);
            log.info("[ASSET] 카드 {}({})의 이번달 사용액: {}", card.getCardName(), card.getId(), card.getThisMonthUsed());
        }

        // 3. 전체 카드 이번달 사용 총액
        Long thisMonthCardUsed = assetCardTransactionMapper.sumThisMonthUsedByUserId(userId);
        log.info("[ASSET] 전체 카드 이번달 사용 총액 = {}", thisMonthCardUsed);

        // 4. 조합
        AssetSummaryResponse resp = new AssetSummaryResponse();
        resp.setTotalAsset(totalAsset);
        resp.setThisMonthCardUsed(thisMonthCardUsed != null ? thisMonthCardUsed : 0L);
        resp.setAccounts(accounts);
        resp.setCards(cards);

        return resp;
    }

    public List<AccountSummaryVO> getAccounts(Long userId) {
//        Long userId = assetUserAccountMapper.findUserIdByLoginId(loginId);
        List<AccountSummaryVO> accounts = assetUserAccountMapper.findAccountSummariesByUserId(userId);
        log.info("[ASSET] 사용자 {}의 계좌 전체 목록 조회 ({}건)", userId, accounts.size());
        return accounts;
    }

    public List<CardSummaryVO> getCards(Long userId) {
//        Long userId = assetUserAccountMapper.findUserIdByLoginId(loginId);
        List<CardSummaryVO> cards = assetUserCardMapper.findCardSummariesByUserId(userId);
        for (CardSummaryVO card : cards) {
            Long cardUsed = assetCardTransactionMapper.sumThisMonthUsedByCardId(card.getId());
            card.setThisMonthUsed(cardUsed != null ? cardUsed : 0L);
            log.info("[ASSET] 카드 {}({})의 이번달 사용액: {}", card.getCardName(), card.getId(), card.getThisMonthUsed());
        }
        log.info("[ASSET] 사용자 {}의 카드 전체 목록 조회 ({}건)", userId, cards.size());
        return cards;
    }

    public PageResponse<AccountTransactionVO> getAccountTransactions(Long userId, Long accountId, int page, int size, String txType) {
        // 계좌 소유자 검증
        if (!assetUserAccountMapper.isAccountOwner(userId, accountId)) {
            throw new UnauthorizedException("계좌 소유자만 접근 가능합니다.");
        }
        int offset = page * size;
        List<AccountTransactionVO> list = assetAccountTransactionMapper.findByAccountIdWithPaging(accountId, offset, size, txType);
        int total = assetAccountTransactionMapper.countByAccountId(accountId, txType);
        log.info("[ASSET] 계좌 {} 거래내역 페이지 조회 - page: {}, size: {}, 총 건수: {}", accountId, page, size, total);
        return new PageResponse<>(list, page, size, total);
    }
    public PageResponse<CardTransactionVO> getCardTransactions(Long userId, Long cardId, int page, int size, String txType) {
        // 카드 소유자 검증
        if (!assetUserCardMapper.isCardOwner(userId, cardId)) {
            throw new UnauthorizedException("카드 소유자만 접근 가능합니다.");
        }
        int offset = page * size;
        List<CardTransactionVO> list = assetCardTransactionMapper.findByCardIdWithPaging(cardId, offset, size, txType);
        int total = assetCardTransactionMapper.countByCardId(cardId, txType);
        log.info("[ASSET] 카드 {} 거래내역 페이지 조회 - page: {}, size: {}, 총 건수: {}", cardId, page, size, total);
        return new PageResponse<>(list, total, page, size);
    }


    public void updateCardTransactionMemo(Long transactionId, String memo) {
        assetCardTransactionMapper.updateMemo(transactionId, memo);
    }

    public void updateAccountTransactionMemo(Long transactionId, String memo) {
        assetAccountTransactionMapper.updateMemo(transactionId, memo);
    }

    public List<CardTransactionVO> getTransportationFees(Long userId) {
        List<CardTransactionVO> transactions = assetCardTransactionMapper.findRecent6MonthsByUserId(userId);
        return transactions.stream()
                .filter(tx -> "후불교통대금".equals(tx.getStoreName()))
                .toList();
    }
}
