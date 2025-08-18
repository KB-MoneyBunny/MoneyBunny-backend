package org.scoula.asset.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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

import java.time.LocalDate;
import java.util.Date;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AssetService 단위 테스트")
class AssetServiceTest {

    @Mock
    private AssetUserAccountMapper assetUserAccountMapper;

    @Mock
    private AssetUserCardMapper assetUserCardMapper;

    @Mock
    private AssetCardTransactionMapper assetCardTransactionMapper;

    @Mock
    private AssetAccountTransactionMapper assetAccountTransactionMapper;

    @InjectMocks
    private AssetService assetService;

    private Long userId;
    private Long accountId;
    private Long cardId;
    private AccountSummaryVO accountSummary;
    private CardSummaryVO cardSummary;
    private AccountTransactionVO accountTransaction;
    private CardTransactionVO cardTransaction;

    @BeforeEach
    void setUp() {
        userId = 1L;
        accountId = 100L;
        cardId = 200L;

        accountSummary = new AccountSummaryVO();
        accountSummary.setId(accountId);
        accountSummary.setBankCode("0004");
        accountSummary.setAccountName("자유입출금");
        accountSummary.setAccountNumber("123-45-6789");
        accountSummary.setAccountType("예금");
        accountSummary.setBalance(5000000L);

        cardSummary = new CardSummaryVO();
        cardSummary.setId(cardId);
        cardSummary.setIssuerCode("0301");
        cardSummary.setCardName("KB국민카드");
        cardSummary.setCardMaskedNumber("1234-****-****-5678");
        cardSummary.setCardType("신용카드");
        cardSummary.setThisMonthUsed(0L);

        accountTransaction = AccountTransactionVO.builder()
                .id(1L)
                .accountId(accountId)
                .txType("출금")
                .amount(50000L)
                .memo("ATM 출금")
                .transactionDateTime(new Date())
                .build();

        cardTransaction = CardTransactionVO.builder()
                .id(1L)
                .cardId(cardId)
                .storeName("스타벅스")
                .amount(5500L)
                .memo("커피")
                .transactionDate(new Date())
                .build();
    }

    // ====================================
    // 자산 요약 조회 테스트
    // ====================================

    @Test
    @DisplayName("자산 요약 조회 - 성공")
    void getSummary_Success() {
        // Given
        List<AccountSummaryVO> accounts = Arrays.asList(accountSummary);
        List<CardSummaryVO> cards = Arrays.asList(cardSummary);
        
        when(assetUserAccountMapper.findAccountSummariesByUserId(userId)).thenReturn(accounts);
        when(assetUserCardMapper.findCardSummariesByUserId(userId)).thenReturn(cards);
        when(assetCardTransactionMapper.sumThisMonthUsedByCardId(cardId)).thenReturn(150000L);
        when(assetCardTransactionMapper.sumThisMonthUsedByUserId(userId)).thenReturn(150000L);

        // When
        AssetSummaryResponse result = assetService.getSummary(userId);

        // Then
        assertNotNull(result);
        assertEquals(5000000L, result.getTotalAsset());
        assertEquals(150000L, result.getThisMonthCardUsed());
        assertEquals(1, result.getAccounts().size());
        assertEquals(1, result.getCards().size());
        verify(assetUserAccountMapper).findAccountSummariesByUserId(userId);
        verify(assetUserCardMapper).findCardSummariesByUserId(userId);
    }

    // ====================================
    // 계좌 목록 조회 테스트
    // ====================================

    @Test
    @DisplayName("계좌 목록 조회 - 성공")
    void getAccounts_Success() {
        // Given
        List<AccountSummaryVO> accounts = Arrays.asList(accountSummary);
        when(assetUserAccountMapper.findAccountSummariesByUserId(userId)).thenReturn(accounts);

        // When
        List<AccountSummaryVO> result = assetService.getAccounts(userId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("0004", result.get(0).getBankCode());
        verify(assetUserAccountMapper).findAccountSummariesByUserId(userId);
    }

    // ====================================
    // 카드 목록 조회 테스트
    // ====================================

    @Test
    @DisplayName("카드 목록 조회 - 성공")
    void getCards_Success() {
        // Given
        List<CardSummaryVO> cards = Arrays.asList(cardSummary);
        when(assetUserCardMapper.findCardSummariesByUserId(userId)).thenReturn(cards);
        when(assetCardTransactionMapper.sumThisMonthUsedByCardId(cardId)).thenReturn(150000L);

        // When
        List<CardSummaryVO> result = assetService.getCards(userId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("KB국민카드", result.get(0).getCardName());
        assertEquals(150000L, result.get(0).getThisMonthUsed());
        verify(assetUserCardMapper).findCardSummariesByUserId(userId);
        verify(assetCardTransactionMapper).sumThisMonthUsedByCardId(cardId);
    }

    // ====================================
    // 계좌 거래내역 조회 테스트
    // ====================================

    @Test
    @DisplayName("계좌 거래내역 조회 - 성공")
    void getAccountTransactions_Success() {
        // Given
        when(assetUserAccountMapper.isAccountOwner(userId, accountId)).thenReturn(true);
        when(assetAccountTransactionMapper.findByAccountIdFiltered(
                eq(accountId), eq(0), eq(10), eq("출금"), any(), any(), eq("커피"), eq("latest")))
                .thenReturn(Arrays.asList(accountTransaction));
        when(assetAccountTransactionMapper.countByAccountIdFiltered(
                eq(accountId), eq("출금"), any(), any(), eq("커피")))
                .thenReturn(1);

        // When
        PageResponse<AccountTransactionVO> result = assetService.getAccountTransactions(
                userId, accountId, 0, 10, "출금", LocalDate.now(), LocalDate.now(), "커피", "latest");

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(1, result.getTotalElements());
        verify(assetUserAccountMapper).isAccountOwner(userId, accountId);
        verify(assetAccountTransactionMapper).findByAccountIdFiltered(any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("계좌 거래내역 조회 - 권한 없음")
    void getAccountTransactions_Unauthorized() {
        // Given
        when(assetUserAccountMapper.isAccountOwner(userId, accountId)).thenReturn(false);

        // When & Then
        assertThrows(UnauthorizedException.class, () ->
                assetService.getAccountTransactions(userId, accountId, 0, 10, null, null, null, null, null));
        
        verify(assetUserAccountMapper).isAccountOwner(userId, accountId);
        verify(assetAccountTransactionMapper, never()).findByAccountIdFiltered(any(), any(), any(), any(), any(), any(), any(), any());
    }

    // ====================================
    // 카드 거래내역 조회 테스트
    // ====================================

    @Test
    @DisplayName("카드 거래내역 조회 - 성공")
    void getCardTransactions_Success() {
        // Given
        when(assetUserCardMapper.isCardOwner(userId, cardId)).thenReturn(true);
        when(assetCardTransactionMapper.findByCardIdWithFilters(
                eq(cardId), eq(0), eq(10), any(), any(), eq("스타벅스"), eq("일반"), eq("DESC")))
                .thenReturn(Arrays.asList(cardTransaction));
        when(assetCardTransactionMapper.countByCardIdWithFilters(
                eq(cardId), any(), any(), eq("스타벅스"), eq("일반")))
                .thenReturn(1);

        // When
        PageResponse<CardTransactionVO> result = assetService.getCardTransactions(
                userId, cardId, 0, 10, LocalDate.now(), LocalDate.now(), "스타벅스", "일반", "desc");

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(1, result.getTotalElements());
        verify(assetUserCardMapper).isCardOwner(userId, cardId);
        verify(assetCardTransactionMapper).findByCardIdWithFilters(any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("카드 거래내역 조회 - 권한 없음")
    void getCardTransactions_Unauthorized() {
        // Given
        when(assetUserCardMapper.isCardOwner(userId, cardId)).thenReturn(false);

        // When & Then
        assertThrows(UnauthorizedException.class, () ->
                assetService.getCardTransactions(userId, cardId, 0, 10, null, null, null, null, null));
        
        verify(assetUserCardMapper).isCardOwner(userId, cardId);
        verify(assetCardTransactionMapper, never()).findByCardIdWithFilters(any(), any(), any(), any(), any(), any(), any(), any());
    }

    // ====================================
    // 메모 업데이트 테스트
    // ====================================

    @Test
    @DisplayName("카드 거래내역 메모 업데이트 - 성공")
    void updateCardTransactionMemo_Success() {
        // Given
        Long transactionId = 1L;
        String memo = "새로운 메모";

        // When
        assertDoesNotThrow(() -> assetService.updateCardTransactionMemo(transactionId, memo));

        // Then
        verify(assetCardTransactionMapper).updateMemo(transactionId, memo);
    }

    @Test
    @DisplayName("계좌 거래내역 메모 업데이트 - 성공")
    void updateAccountTransactionMemo_Success() {
        // Given
        Long transactionId = 1L;
        String memo = "새로운 메모";

        // When
        assertDoesNotThrow(() -> assetService.updateAccountTransactionMemo(transactionId, memo));

        // Then
        verify(assetAccountTransactionMapper).updateMemo(transactionId, memo);
    }

    // ====================================
    // 지출 트렌드 조회 테스트
    // ====================================

    @Test
    @DisplayName("지출 트렌드 조회 - 성공")
    void getSpendingTrend_Success() {
        // Given
        MonthlyTrendDTO trend1 = new MonthlyTrendDTO(2024, 8, 500000L);
        MonthlyTrendDTO trend2 = new MonthlyTrendDTO(2024, 7, 450000L);
        
        when(assetCardTransactionMapper.findMonthlyTrend(any(Map.class)))
                .thenReturn(Arrays.asList(trend1, trend2));

        // When
        List<MonthlyTrendDTO> result = assetService.getSpendingTrend(userId, 2024, 8, 6);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(500000L, result.get(0).getTotalAmount());
        verify(assetCardTransactionMapper).findMonthlyTrend(any(Map.class));
    }

    // ====================================
    // 카테고리별 거래내역 조회 테스트
    // ====================================

    @Test
    @DisplayName("카테고리별 거래내역 조회 - 성공")
    void getCategoryTransactions_Success() {
        // Given
        Long categoryId = 1L;
        when(assetCardTransactionMapper.findCategoryTransactions(userId, categoryId, 2024, 8))
                .thenReturn(Arrays.asList(cardTransaction));

        // When
        List<CardTransactionVO> result = assetService.getCategoryTransactions(userId, categoryId, 2024, 8);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(assetCardTransactionMapper).findCategoryTransactions(userId, categoryId, 2024, 8);
    }

    // ====================================
    // 거래내역 카테고리 변경 테스트
    // ====================================

    @Test
    @DisplayName("거래내역 카테고리 변경 - 성공")
    void updateTransactionCategory_Success() {
        // Given
        Long transactionId = 1L;
        Long newCategoryId = 2L;
        when(assetCardTransactionMapper.updateTransactionCategory(transactionId, newCategoryId))
                .thenReturn(1);

        // When
        assertDoesNotThrow(() -> assetService.updateTransactionCategory(transactionId, newCategoryId));

        // Then
        verify(assetCardTransactionMapper).updateTransactionCategory(transactionId, newCategoryId);
    }

    @Test
    @DisplayName("거래내역 카테고리 변경 - 실패")
    void updateTransactionCategory_Failure() {
        // Given
        Long transactionId = 1L;
        Long newCategoryId = 2L;
        when(assetCardTransactionMapper.updateTransactionCategory(transactionId, newCategoryId))
                .thenReturn(0);

        // When & Then
        assertThrows(RuntimeException.class, () ->
                assetService.updateTransactionCategory(transactionId, newCategoryId));
        
        verify(assetCardTransactionMapper).updateTransactionCategory(transactionId, newCategoryId);
    }

    // ====================================
    // 지출 개요 조회 테스트
    // ====================================

    @Test
    @DisplayName("지출 개요 조회 - 성공")
    void getSpendingOverview_Success() {
        // Given
        CategorySpending categorySpending = CategorySpending.builder()
                .categoryId(1L)
                .amount(100000L)
                .build();
        
        MonthlyTrendDTO trendData = new MonthlyTrendDTO(2024, 8, 500000L);

        when(assetCardTransactionMapper.findMonthlyTotal(userId, 2024, 8)).thenReturn(500000L);
        when(assetCardTransactionMapper.findMonthlyTotal(userId, 2024, 7)).thenReturn(450000L);
        when(assetCardTransactionMapper.findMonthlyCategorySpending(userId, 2024, 8))
                .thenReturn(Arrays.asList(categorySpending));
        when(assetCardTransactionMapper.findMonthlyTrend(any(Map.class)))
                .thenReturn(Arrays.asList(trendData));

        // When
        SpendingOverviewDTO result = assetService.getSpendingOverview(userId, 2024, 8, 6);

        // Then
        assertNotNull(result);
        assertEquals(500000L, result.getTotalSpending());
        assertEquals(450000L, result.getPrevMonth().getTotalSpending());
        assertEquals(1, result.getCategories().size());
        assertEquals(6, result.getTrend().size());
        verify(assetCardTransactionMapper).findMonthlyTotal(userId, 2024, 8);
        verify(assetCardTransactionMapper).findMonthlyCategorySpending(userId, 2024, 8);
    }

    // ====================================
    // 교통비 조회 테스트
    // ====================================

    @Test
    @DisplayName("교통비 조회 - 성공")
    void getTransportationFees_Success() {
        // Given
        CardTransactionVO transportTransaction = CardTransactionVO.builder()
                .id(1L)
                .cardId(cardId)
                .storeName("후불교통대금")
                .amount(1500L)
                .transactionDate(new Date())
                .build();
        
        when(assetCardTransactionMapper.findRecent6MonthsByUserId(userId))
                .thenReturn(Arrays.asList(transportTransaction, cardTransaction));

        // When
        List<CardTransactionVO> result = assetService.getTransportationFees(userId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("후불교통대금", result.get(0).getStoreName());
        verify(assetCardTransactionMapper).findRecent6MonthsByUserId(userId);
    }

    // ====================================
    // 특정 거래 존재 여부 확인 테스트
    // ====================================

    @Test
    @DisplayName("월세 거래내역 존재 여부 확인 - 존재함")
    void existsRentTransaction_Exists() {
        // Given
        when(assetAccountTransactionMapper.existsRentTransactionByUserId(userId)).thenReturn(true);

        // When
        boolean result = assetService.existsRentTransaction(userId);

        // Then
        assertTrue(result);
        verify(assetAccountTransactionMapper).existsRentTransactionByUserId(userId);
    }

    @Test
    @DisplayName("HRD Korea 카드 거래내역 존재 여부 확인 - 존재하지 않음")
    void existsHrdKoreaCardTransaction_NotExists() {
        // Given
        when(assetCardTransactionMapper.existsHrdKoreaCardTransactionByUserId(userId)).thenReturn(false);

        // When
        boolean result = assetService.existsHrdKoreaCardTransaction(userId);

        // Then
        assertFalse(result);
        verify(assetCardTransactionMapper).existsHrdKoreaCardTransactionByUserId(userId);
    }
}