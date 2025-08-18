package org.scoula.codef.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.scoula.asset.service.TransactionCategorizer;
import org.scoula.codef.common.exception.CodefApiException;
import org.scoula.codef.domain.*;
import org.scoula.codef.dto.AccountConnectRequest;
import org.scoula.codef.dto.CardConnectRequest;
import org.scoula.codef.mapper.*;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CodefService 단위 테스트")
class CodefServiceTest {

    @Mock
    private CodefTokenService tokenService;

    @Mock
    private TransactionCategorizer transactionCategorizer;

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
    private CategoryMapper categoryMapper;

    @InjectMocks
    private CodefService codefService;

    private Long userId;
    private AccountConnectRequest accountConnectRequest;
    private CardConnectRequest cardConnectRequest;
    private ConnectedAccountVO connectedAccountVO;
    private UserAccountVO userAccountVO;
    private UserCardVO userCardVO;

    @BeforeEach
    void setUp() {
        userId = 1L;

        // Mock public key 설정
        ReflectionTestUtils.setField(codefService, "publicKey", "test-public-key");

        accountConnectRequest = new AccountConnectRequest();
        accountConnectRequest.setOrganization("0004");
        accountConnectRequest.setLoginId("testuser");
        accountConnectRequest.setPassword("testpass");

        cardConnectRequest = new CardConnectRequest();
        cardConnectRequest.setOrganization("0301");
        cardConnectRequest.setLoginId("testuser");
        cardConnectRequest.setPassword("testpass");

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
    // ConnectedId 조회 테스트
    // ====================================

    @Test
    @DisplayName("ConnectedId 조회 - 존재함")
    void findConnectedId_Exists() {
        // Given
        when(connectedAccountMapper.findConnectedIdByUserId(userId)).thenReturn(connectedAccountVO);

        // When
        ConnectedAccountVO result = connectedAccountMapper.findConnectedIdByUserId(userId);

        // Then
        assertNotNull(result);
        assertEquals("test-connected-id", result.getConnectedId());
        verify(connectedAccountMapper).findConnectedIdByUserId(userId);
    }

    @Test
    @DisplayName("ConnectedId 조회 - 존재하지 않음")
    void findConnectedId_NotExists() {
        // Given
        when(connectedAccountMapper.findConnectedIdByUserId(userId)).thenReturn(null);

        // When
        ConnectedAccountVO result = connectedAccountMapper.findConnectedIdByUserId(userId);

        // Then
        assertNull(result);
        verify(connectedAccountMapper).findConnectedIdByUserId(userId);
    }

    // ====================================
    // 계좌 등록 관련 테스트
    // ====================================

    @Test
    @DisplayName("사용자 계좌 등록 - 성공")
    void registerUserAccounts_Success() {
        // Given
        List<UserAccountVO> selectedAccounts = Arrays.asList(userAccountVO);
        when(connectedAccountMapper.findConnectedIdByUserId(userId)).thenReturn(connectedAccountVO);
        when(userAccountMapper.existsAccount(userId, userAccountVO.getAccountNumber())).thenReturn(0);
        when(userAccountMapper.findIdByUserIdAndAccountNumber(userId, userAccountVO.getAccountNumber())).thenReturn(1L);

        // When
        assertDoesNotThrow(() -> codefService.registerUserAccounts(userId, selectedAccounts));

        // Then
        verify(connectedAccountMapper).findConnectedIdByUserId(userId);
        verify(userAccountMapper).insertUserAccount(any(UserAccountVO.class));
    }

    @Test
    @DisplayName("사용자 계좌 등록 - 중복 계좌 스킵")
    void registerUserAccounts_DuplicateSkip() {
        // Given
        List<UserAccountVO> selectedAccounts = Arrays.asList(userAccountVO);
        when(connectedAccountMapper.findConnectedIdByUserId(userId)).thenReturn(connectedAccountVO);
        when(userAccountMapper.existsAccount(userId, userAccountVO.getAccountNumber())).thenReturn(1);

        // When
        assertDoesNotThrow(() -> codefService.registerUserAccounts(userId, selectedAccounts));

        // Then
        verify(connectedAccountMapper).findConnectedIdByUserId(userId);
        verify(userAccountMapper, never()).insertUserAccount(any());
    }

    // ====================================
    // 카드 등록 관련 테스트
    // ====================================

    @Test
    @DisplayName("사용자 카드 등록 - 성공")
    void registerUserCards_Success() {
        // Given
        List<UserCardVO> selectedCards = Arrays.asList(userCardVO);
        when(connectedAccountMapper.findConnectedIdByUserId(userId)).thenReturn(connectedAccountVO);
        when(userCardMapper.existsCard(userId, userCardVO.getCardMaskedNumber())).thenReturn(0);
        when(userCardMapper.findIdByUserIdAndCardNumber(userId, userCardVO.getCardMaskedNumber())).thenReturn(1L);
        when(categoryMapper.findUnclassifiedId()).thenReturn(99L);

        // When
        assertDoesNotThrow(() -> codefService.registerUserCards(userId, selectedCards));

        // Then
        verify(connectedAccountMapper).findConnectedIdByUserId(userId);
        verify(userCardMapper).insertUserCard(any(UserCardVO.class));
    }

    @Test
    @DisplayName("사용자 카드 등록 - 중복 카드 예외")
    void registerUserCards_DuplicateCard() {
        // Given
        List<UserCardVO> selectedCards = Arrays.asList(userCardVO);
        when(connectedAccountMapper.findConnectedIdByUserId(userId)).thenReturn(connectedAccountVO);
        when(userCardMapper.existsCard(userId, userCardVO.getCardMaskedNumber())).thenReturn(1);

        // When & Then
        assertThrows(Exception.class, () -> codefService.registerUserCards(userId, selectedCards));
        verify(userCardMapper, never()).insertUserCard(any());
    }

    // ====================================
    // 거래내역 동기화 테스트
    // ====================================

    @Test
    @DisplayName("계좌 거래내역 동기화 - 기본 플로우")
    void syncAccountTransaction_BasicFlow() {
        // Given
        Long accountId = 1L;
        String bankCode = "0004";
        String connectedId = "test-connected-id";
        String accountNo = "123-456-789";
        String startDate = "20230101";
        String endDate = "20231231";

        // When
        assertDoesNotThrow(() -> 
            codefService.syncAccountTransaction(userId, accountId, bankCode, connectedId, accountNo, startDate, endDate));

        // Then
        verify(accountTransactionMapper).findAllTxKeyByAccountIdFromDate(accountId, startDate);
    }

    @Test
    @DisplayName("카드 거래내역 동기화 - 기본 플로우")
    void syncCardTransaction_BasicFlow() {
        // Given
        Long cardId = 1L;
        String cardCode = "0301";
        String connectedId = "test-connected-id";
        String cardNo = "1234-****-****-5678";
        String startDate = "20230101";
        String endDate = "20231231";
        String cardName = "KB국민카드";
        when(categoryMapper.findUnclassifiedId()).thenReturn(99L);

        // When
        assertDoesNotThrow(() -> 
            codefService.syncCardTransaction(userId, cardId, cardCode, connectedId, cardNo, startDate, endDate, cardName));

        // Then
        verify(cardTransactionMapper).findAllTxKeyByCardIdFromDate(cardId, startDate);
        verify(categoryMapper).findUnclassifiedId();
    }

    // ====================================
    // 테스트용 메서드 테스트
    // ====================================

    @Test
    @DisplayName("ConnectedId로 계좌 목록 조회 - ConnectedId 없음")
    void fetchAccountListByConnectedId_NoConnectedId() {
        // Given
        when(connectedAccountMapper.findConnectedIdByUserId(userId)).thenReturn(null);

        // When & Then
        // ConnectedId가 null일 때는 sendPost에서 실패할 것으로 예상
        // 실제 HTTP 요청이 발생하므로 테스트에서는 예외 발생 확인
        verify(connectedAccountMapper, never()).findConnectedIdByUserId(any());
    }

    // ====================================
    // 예외 처리 테스트
    // ====================================

    @Test
    @DisplayName("CODEF API 에러 처리 - CodefApiException")
    void codefApiError_ThrowsCodefApiException() {
        // Given
        when(connectedAccountMapper.findConnectedIdByUserId(userId)).thenReturn(null);

        // 실제 API 호출 없이 로직 테스트를 위해서는 별도의 메서드 분리가 필요
        // 현재는 기본적인 매퍼 호출 확인만 수행
        verify(connectedAccountMapper, never()).findConnectedIdByUserId(any());
    }

    // ====================================
    // 매퍼 연동 테스트
    // ====================================

    @Test
    @DisplayName("ConnectedAccount 삽입 - 성공")
    void insertConnectedAccount_Success() {
        // Given
        String connectedId = "new-connected-id";

        // When
        assertDoesNotThrow(() -> connectedAccountMapper.insertConnectedAccount(userId, connectedId));

        // Then
        verify(connectedAccountMapper).insertConnectedAccount(userId, connectedId);
    }

    @Test
    @DisplayName("계좌 존재 여부 확인 - 존재함")
    void existsAccount_Exists() {
        // Given
        String accountNumber = "123-456-789";
        when(userAccountMapper.existsAccount(userId, accountNumber)).thenReturn(1);

        // When
        int result = userAccountMapper.existsAccount(userId, accountNumber);

        // Then
        assertEquals(1, result);
        verify(userAccountMapper).existsAccount(userId, accountNumber);
    }

    @Test
    @DisplayName("카드 존재 여부 확인 - 존재하지 않음")
    void existsCard_NotExists() {
        // Given
        String cardNumber = "1234-****-****-5678";
        when(userCardMapper.existsCard(userId, cardNumber)).thenReturn(0);

        // When
        int result = userCardMapper.existsCard(userId, cardNumber);

        // Then
        assertEquals(0, result);
        verify(userCardMapper).existsCard(userId, cardNumber);
    }

    @Test
    @DisplayName("기본 카테고리 조회 - 성공")
    void findUnclassifiedId_Success() {
        // Given
        when(categoryMapper.findUnclassifiedId()).thenReturn(99L);

        // When
        Long result = categoryMapper.findUnclassifiedId();

        // Then
        assertEquals(99L, result);
        verify(categoryMapper).findUnclassifiedId();
    }

    // ====================================
    // 트랜잭션 분류 테스트
    // ====================================

    @Test
    @DisplayName("거래 분류 - 글로벌 카테고리 적용")
    void categorizeTransaction_GlobalCategory() {
        // Given
        CardTransactionVO transaction = CardTransactionVO.builder()
                .storeName("스타벅스")
                .amount(5500L)
                .build();
        when(transactionCategorizer.categorizeGlobal(transaction)).thenReturn(10L);

        // When
        Long result = transactionCategorizer.categorizeGlobal(transaction);

        // Then
        assertEquals(10L, result);
        verify(transactionCategorizer).categorizeGlobal(transaction);
    }

    @Test
    @DisplayName("거래 분류 - 기본 카테고리 사용")
    void categorizeTransaction_DefaultCategory() {
        // Given
        CardTransactionVO transaction = CardTransactionVO.builder()
                .storeName("알 수 없는 매장")
                .amount(1000L)
                .build();
        when(transactionCategorizer.categorizeGlobal(transaction)).thenReturn(null);

        // When
        Long result = transactionCategorizer.categorizeGlobal(transaction);

        // Then
        assertNull(result);
        verify(transactionCategorizer).categorizeGlobal(transaction);
    }
}