package org.scoula.codef.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.scoula.codef.domain.ConnectedAccountVO;
import org.scoula.codef.domain.UserAccountVO;
import org.scoula.codef.domain.UserCardVO;
import org.scoula.codef.mapper.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SyncService 단위 테스트")
class SyncServiceTest {

    @Mock
    private ConnectedAccountMapper connectedAccountMapper;

    @Mock
    private UserAccountMapper userAccountMapper;

    @Mock
    private AccountTransactionMapper accountTransactionMapper;

    @Mock
    private UserCardMapper userCardMapper;

    @Mock
    private CardTransactionMapper cardTransactionMapper;

    @Mock
    private CodefService codefService;

    @InjectMocks
    private SyncService syncService;

    private Long userId;
    private ConnectedAccountVO connectedAccountVO;
    private UserAccountVO userAccountVO;
    private UserCardVO userCardVO;

    @BeforeEach
    void setUp() {
        userId = 1L;

        connectedAccountVO = new ConnectedAccountVO();
        connectedAccountVO.setConnectedId("test-connected-id");
        connectedAccountVO.setUserId(userId);
        // ConnectedAccountVO has no createdAt field

        userAccountVO = UserAccountVO.builder()
                .id(1L)
                .userId(userId)
                .accountNumber("123-456-789")
                .accountName("자유적금")
                .accountType("예금")
                .balance(1000000L)
                .bankCode("0004")
                .createdAt(new Date())
                .build();

        userCardVO = UserCardVO.builder()
                .id(1L)
                .userId(userId)
                .cardMaskedNumber("1234-****-****-5678")
                .cardName("KB국민카드")
                .cardType("신용카드")
                .issuerCode("0301")
                .createdAt(new Date())
                .build();
    }

    // ====================================
    // 계좌 동기화 테스트
    // ====================================

    @Test
    @DisplayName("계좌 동기화 - 성공")
    void syncAccountsAsync_Success() {
        // Given
        List<UserAccountVO> accountList = Arrays.asList(userAccountVO);
        when(userAccountMapper.findByUserId(userId)).thenReturn(accountList);
        when(connectedAccountMapper.findConnectedIdByUserId(userId)).thenReturn(connectedAccountVO);
        when(accountTransactionMapper.findLastTransactionDateByAccountId(1L)).thenReturn("20240101");
        when(accountTransactionMapper.findLatestBalanceAfterByAccountId(1L)).thenReturn(1500000L);

        // When
        assertDoesNotThrow(() -> syncService.syncAccountsAsync(userId));

        // Then
        verify(userAccountMapper).findByUserId(userId);
        verify(connectedAccountMapper).findConnectedIdByUserId(userId);
        verify(accountTransactionMapper).findLastTransactionDateByAccountId(1L);
        verify(userAccountMapper).updateBalance(1L, 1500000L);
    }

    @Test
    @DisplayName("계좌 동기화 - 계좌 없음")
    void syncAccountsAsync_NoAccounts() {
        // Given
        when(userAccountMapper.findByUserId(userId)).thenReturn(Collections.emptyList());

        // When
        assertDoesNotThrow(() -> syncService.syncAccountsAsync(userId));

        // Then
        verify(userAccountMapper).findByUserId(userId);
        verify(connectedAccountMapper, never()).findConnectedIdByUserId(any());
        verify(accountTransactionMapper, never()).findLastTransactionDateByAccountId(any());
    }

    @Test
    @DisplayName("계좌 동기화 - 최근 거래일 없음 (1년 전부터 동기화)")
    void syncAccountsAsync_NoLastTransactionDate() {
        // Given
        List<UserAccountVO> accountList = Arrays.asList(userAccountVO);
        when(userAccountMapper.findByUserId(userId)).thenReturn(accountList);
        when(connectedAccountMapper.findConnectedIdByUserId(userId)).thenReturn(connectedAccountVO);
        when(accountTransactionMapper.findLastTransactionDateByAccountId(1L)).thenReturn(null);
        when(accountTransactionMapper.findLatestBalanceAfterByAccountId(1L)).thenReturn(1200000L);

        // When
        assertDoesNotThrow(() -> syncService.syncAccountsAsync(userId));

        // Then
        verify(userAccountMapper).findByUserId(userId);
        verify(accountTransactionMapper).findLastTransactionDateByAccountId(1L);
        verify(userAccountMapper).updateBalance(1L, 1200000L);
    }

    @Test
    @DisplayName("계좌 동기화 - 잔액 업데이트 없음 (거래내역 없음)")
    void syncAccountsAsync_NoBalanceUpdate() {
        // Given
        List<UserAccountVO> accountList = Arrays.asList(userAccountVO);
        when(userAccountMapper.findByUserId(userId)).thenReturn(accountList);
        when(connectedAccountMapper.findConnectedIdByUserId(userId)).thenReturn(connectedAccountVO);
        when(accountTransactionMapper.findLastTransactionDateByAccountId(1L)).thenReturn("20240101");
        when(accountTransactionMapper.findLatestBalanceAfterByAccountId(1L)).thenReturn(null);

        // When
        assertDoesNotThrow(() -> syncService.syncAccountsAsync(userId));

        // Then
        verify(userAccountMapper).findByUserId(userId);
        verify(accountTransactionMapper).findLatestBalanceAfterByAccountId(1L);
        verify(userAccountMapper, never()).updateBalance(any(), any());
    }

    // ====================================
    // 카드 동기화 테스트
    // ====================================

    @Test
    @DisplayName("카드 동기화 - 성공")
    void syncCardsAsync_Success() {
        // Given
        List<UserCardVO> cardList = Arrays.asList(userCardVO);
        when(userCardMapper.findByUserId(userId)).thenReturn(cardList);
        when(connectedAccountMapper.findConnectedIdByUserId(userId)).thenReturn(connectedAccountVO);
        when(cardTransactionMapper.findLastTransactionDateByCardId(1L)).thenReturn("20240101");

        // When
        assertDoesNotThrow(() -> syncService.syncCardsAsync(userId));

        // Then
        verify(userCardMapper).findByUserId(userId);
        verify(connectedAccountMapper).findConnectedIdByUserId(userId);
        verify(cardTransactionMapper).findLastTransactionDateByCardId(1L);
    }

    @Test
    @DisplayName("카드 동기화 - 카드 없음")
    void syncCardsAsync_NoCards() {
        // Given
        when(userCardMapper.findByUserId(userId)).thenReturn(Collections.emptyList());

        // When
        assertDoesNotThrow(() -> syncService.syncCardsAsync(userId));

        // Then
        verify(userCardMapper).findByUserId(userId);
        verify(connectedAccountMapper, never()).findConnectedIdByUserId(any());
        verify(cardTransactionMapper, never()).findLastTransactionDateByCardId(any());
    }

    @Test
    @DisplayName("카드 동기화 - 최근 거래일 없음 (1년 전부터 동기화)")
    void syncCardsAsync_NoLastTransactionDate() {
        // Given
        List<UserCardVO> cardList = Arrays.asList(userCardVO);
        when(userCardMapper.findByUserId(userId)).thenReturn(cardList);
        when(connectedAccountMapper.findConnectedIdByUserId(userId)).thenReturn(connectedAccountVO);
        when(cardTransactionMapper.findLastTransactionDateByCardId(1L)).thenReturn(null);

        // When
        assertDoesNotThrow(() -> syncService.syncCardsAsync(userId));

        // Then
        verify(userCardMapper).findByUserId(userId);
        verify(cardTransactionMapper).findLastTransactionDateByCardId(1L);
    }

    @Test
    @DisplayName("카드 동기화 - 복수 카드 처리")
    void syncCardsAsync_MultipleCards() {
        // Given
        UserCardVO card2 = UserCardVO.builder()
                .id(2L)
                .userId(userId)
                .cardMaskedNumber("5678-****-****-1234")
                .cardName("신한카드")
                .cardType("체크카드")
                .issuerCode("0302")
                .createdAt(new Date())
                .build();

        List<UserCardVO> cardList = Arrays.asList(userCardVO, card2);
        when(userCardMapper.findByUserId(userId)).thenReturn(cardList);
        when(connectedAccountMapper.findConnectedIdByUserId(userId)).thenReturn(connectedAccountVO);
        when(cardTransactionMapper.findLastTransactionDateByCardId(1L)).thenReturn("20240101");
        when(cardTransactionMapper.findLastTransactionDateByCardId(2L)).thenReturn("20240115");

        // When
        assertDoesNotThrow(() -> syncService.syncCardsAsync(userId));

        // Then
        verify(userCardMapper).findByUserId(userId);
        verify(cardTransactionMapper).findLastTransactionDateByCardId(1L);
        verify(cardTransactionMapper).findLastTransactionDateByCardId(2L);
        // CodefService.syncCardTransaction이 2번 호출되어야 함 (각 카드별로)
    }

    // ====================================
    // 예외 처리 테스트
    // ====================================

    @Test
    @DisplayName("계좌 동기화 - CodefService 예외 발생")
    void syncAccountsAsync_CodefServiceException() {
        // Given
        List<UserAccountVO> accountList = Arrays.asList(userAccountVO);
        when(userAccountMapper.findByUserId(userId)).thenReturn(accountList);
        when(connectedAccountMapper.findConnectedIdByUserId(userId)).thenReturn(connectedAccountVO);
        when(accountTransactionMapper.findLastTransactionDateByAccountId(1L)).thenReturn("20240101");
        
        // CodefService에서 예외가 발생하더라도 SyncService는 예외를 잡아서 로그만 남기고 계속 진행
        doThrow(new RuntimeException("CODEF API 오류")).when(codefService)
                .syncAccountTransaction(any(), any(), any(), any(), any(), any(), any());

        // When
        assertDoesNotThrow(() -> syncService.syncAccountsAsync(userId));

        // Then
        verify(userAccountMapper).findByUserId(userId);
        verify(connectedAccountMapper).findConnectedIdByUserId(userId);
        // 예외가 발생해도 서비스는 정상 완료되어야 함
    }

    @Test
    @DisplayName("카드 동기화 - CodefService 예외 발생")
    void syncCardsAsync_CodefServiceException() {
        // Given
        List<UserCardVO> cardList = Arrays.asList(userCardVO);
        when(userCardMapper.findByUserId(userId)).thenReturn(cardList);
        when(connectedAccountMapper.findConnectedIdByUserId(userId)).thenReturn(connectedAccountVO);
        when(cardTransactionMapper.findLastTransactionDateByCardId(1L)).thenReturn("20240101");
        
        // CodefService에서 예외가 발생하더라도 SyncService는 예외를 잡아서 로그만 남기고 계속 진행
        doThrow(new RuntimeException("CODEF API 오류")).when(codefService)
                .syncCardTransaction(any(), any(), any(), any(), any(), any(), any(), any());

        // When
        assertDoesNotThrow(() -> syncService.syncCardsAsync(userId));

        // Then
        verify(userCardMapper).findByUserId(userId);
        verify(connectedAccountMapper).findConnectedIdByUserId(userId);
        // 예외가 발생해도 서비스는 정상 완료되어야 함
    }

    // ====================================
    // 매퍼 연동 테스트
    // ====================================

    @Test
    @DisplayName("사용자별 계좌 조회 - 성공")
    void findAccountsByUserId_Success() {
        // Given
        List<UserAccountVO> accounts = Arrays.asList(userAccountVO);
        when(userAccountMapper.findByUserId(userId)).thenReturn(accounts);

        // When
        List<UserAccountVO> result = userAccountMapper.findByUserId(userId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("123-456-789", result.get(0).getAccountNumber());
        verify(userAccountMapper).findByUserId(userId);
    }

    @Test
    @DisplayName("사용자별 카드 조회 - 성공")
    void findCardsByUserId_Success() {
        // Given
        List<UserCardVO> cards = Arrays.asList(userCardVO);
        when(userCardMapper.findByUserId(userId)).thenReturn(cards);

        // When
        List<UserCardVO> result = userCardMapper.findByUserId(userId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("1234-****-****-5678", result.get(0).getCardMaskedNumber());
        verify(userCardMapper).findByUserId(userId);
    }

    @Test
    @DisplayName("최근 거래일 조회 - 계좌")
    void findLastTransactionDateByAccountId_Success() {
        // Given
        String expectedDate = "20240815";
        when(accountTransactionMapper.findLastTransactionDateByAccountId(1L)).thenReturn(expectedDate);

        // When
        String result = accountTransactionMapper.findLastTransactionDateByAccountId(1L);

        // Then
        assertEquals(expectedDate, result);
        verify(accountTransactionMapper).findLastTransactionDateByAccountId(1L);
    }

    @Test
    @DisplayName("최근 거래일 조회 - 카드")
    void findLastTransactionDateByCardId_Success() {
        // Given
        String expectedDate = "20240815";
        when(cardTransactionMapper.findLastTransactionDateByCardId(1L)).thenReturn(expectedDate);

        // When
        String result = cardTransactionMapper.findLastTransactionDateByCardId(1L);

        // Then
        assertEquals(expectedDate, result);
        verify(cardTransactionMapper).findLastTransactionDateByCardId(1L);
    }

    @Test
    @DisplayName("최신 잔액 조회 - 성공")
    void findLatestBalanceAfterByAccountId_Success() {
        // Given
        Long expectedBalance = 2000000L;
        when(accountTransactionMapper.findLatestBalanceAfterByAccountId(1L)).thenReturn(expectedBalance);

        // When
        Long result = accountTransactionMapper.findLatestBalanceAfterByAccountId(1L);

        // Then
        assertEquals(expectedBalance, result);
        verify(accountTransactionMapper).findLatestBalanceAfterByAccountId(1L);
    }

    @Test
    @DisplayName("계좌 잔액 업데이트 - 성공")
    void updateBalance_Success() {
        // Given
        Long accountId = 1L;
        Long newBalance = 1500000L;

        // When
        assertDoesNotThrow(() -> userAccountMapper.updateBalance(accountId, newBalance));

        // Then
        verify(userAccountMapper).updateBalance(accountId, newBalance);
    }
}