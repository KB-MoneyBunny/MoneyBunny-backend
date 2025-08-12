package org.scoula.asset.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scoula.asset.domain.AccountSummaryVO;
import org.scoula.asset.domain.AccountTransactionVO;
import org.scoula.asset.domain.CardSummaryVO;
import org.scoula.asset.domain.CardTransactionVO;
import org.scoula.asset.dto.*;
import org.scoula.asset.mapper.AssetAccountTransactionMapper;
import org.scoula.asset.mapper.AssetCardTransactionMapper;
import org.scoula.asset.mapper.AssetUserAccountMapper;
import org.scoula.asset.mapper.AssetUserCardMapper;
import org.scoula.common.dto.PageResponse;
import org.scoula.common.exception.UnauthorizedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

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


    public List<MonthlyTrendDTO> getSpendingTrend(Long userId, int baseYear, int baseMonth, int monthCount) {
        // 1. 기준월에서 5개월 전까지 구하기 (즉, baseMonth - 5)
        LocalDate base = LocalDate.of(baseYear, baseMonth, 1);
        LocalDate start = base.minusMonths(monthCount - 1);

        int startYear = start.getYear();
        int startMonth = start.getMonthValue();

        // 2. MyBatis 파라미터 맵 만들기
        Map<String, Object> param = new HashMap<>();
        param.put("userId", userId);
        param.put("year", baseYear);
        param.put("month", baseMonth);
        param.put("startYear", startYear);
        param.put("startMonth", startMonth);

        // 3. 쿼리 호출 (mapper에서 LIMIT 6 사용, 최신순 정렬)
        return assetCardTransactionMapper.findMonthlyTrend(param);
    }


    public List<CardTransactionVO> getCategoryTransactions(Long userId, Long categoryId, int year, int month) {
        return assetCardTransactionMapper.findCategoryTransactions(userId, categoryId, year, month);
    }

    @Transactional
    public void updateTransactionCategory(Long transactionId, Long newCategoryId) {
        int updated = assetCardTransactionMapper.updateTransactionCategory(transactionId, newCategoryId);
        if (updated != 1) {
            throw new RuntimeException("카테고리 변경 실패! 거래내역을 확인하세요.");
        }
    }

    public SpendingOverviewDTO getSpendingOverview(Long userId, int year, int month, int trendMonths) {
        // 기준월 & 전월 계산
        LocalDate selected = LocalDate.of(year, month, 1);
        LocalDate prev = selected.minusMonths(1);

        // 1) 이번 달 총액
        long totalNow = assetCardTransactionMapper.findMonthlyTotal(userId, year, month); // 없으면 0 반환하도록

        // 2) 전월 총액
        long totalPrev = assetCardTransactionMapper.findMonthlyTotal(userId, prev.getYear(), prev.getMonthValue());

        // 3) 카테고리 합계
        List<CategorySpending> rawCats = assetCardTransactionMapper.findMonthlyCategorySpending(userId, year, month);

        // 퍼센트 계산
        double denom = (totalNow > 0) ? totalNow : 1.0;
        List<SpendingOverviewDTO.Category> categories = rawCats.stream()
                .map(c -> SpendingOverviewDTO.Category.builder()
                        .categoryId(c.getCategoryId())
                        .amount(c.getAmount())
                        .percentage(round1(c.getAmount() * 100.0 / denom))
                        .build())
                .toList();

        // 4) MoM 계산
        long diff = totalNow - totalPrev;
        Double percent = (totalPrev == 0)
                ? null                              // 전월이 0이면 %는 null(프론트에서 “—” 처리 추천)
                : round1(diff * 100.0 / totalPrev); // 소수1자리

        // 5) 트렌드 (최근 trendMonths개월, 기준월 포함)
        List<MonthlyTrendDTO> trendRows = getSpendingTrend(userId, year, month, trendMonths);
        // DB에 빈 달이 있으면 0으로 메우기
        List<SpendingOverviewDTO.Trend> trend = fillMonthlySeries(selected, trendMonths, trendRows);

        return SpendingOverviewDTO.builder()
                .selected(new SpendingOverviewDTO.Selected(year, month))
                .totalSpending(totalNow)
                .prevMonth(new SpendingOverviewDTO.PrevMonth(prev.getYear(), prev.getMonthValue(), totalPrev))
                .momChange(new SpendingOverviewDTO.MomChange(diff, percent))
                .categories(categories)
                .trend(trend)
                .build();
    }

    private static double round1(double v) {
        return Math.round(v * 10.0) / 10.0;
    }

    private List<SpendingOverviewDTO.Trend> fillMonthlySeries(
            LocalDate anchor, int months, List<MonthlyTrendDTO> rows) {

        // rows → map(year-month → totalAmount)
        Map<String, Long> map = rows.stream().collect(Collectors.toMap(
                r -> r.getYear() + "-" + r.getMonth(),
                MonthlyTrendDTO::getTotalAmount,
                (a, b) -> a
        ));

        List<SpendingOverviewDTO.Trend> out = new ArrayList<>();
        for (int i = months - 1; i >= 0; i--) {
            LocalDate d = anchor.minusMonths(i);
            String key = d.getYear() + "-" + d.getMonthValue();
            long amt = map.getOrDefault(key, 0L);
            out.add(new SpendingOverviewDTO.Trend(d.getYear(), d.getMonthValue(), amt));
        }
        return out;
    }

}

