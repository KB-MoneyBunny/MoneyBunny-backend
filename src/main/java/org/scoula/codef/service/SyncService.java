package org.scoula.codef.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scoula.codef.domain.ConnectedAccountVO;
import org.scoula.codef.domain.UserAccountVO;
import org.scoula.codef.domain.UserCardVO;
import org.scoula.codef.mapper.*;
import org.scoula.codef.util.RetryUtil;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SyncService {

    private final ConnectedAccountMapper connectedAccountMapper;
    private final UserAccountMapper userAccountMapper;
    private final AccountTransactionMapper accountTransactionMapper;
    private final UserCardMapper userCardMapper;
    private final CardTransactionMapper cardTransactionMapper;
    private final CodefService codefService;



    /**
     * [비동기] 사용자의 모든 계좌 거래내역을 최신 상태로 동기화
     * - 각 계좌의 가장 최근 거래일 이후(없으면 1년 전부터) CODEF 거래내역을 불러와 DB에 추가
     * - 실패시 1초간격 3회 재시도
     */
    @Async
    public void syncAccountsAsync(String loginId) {
        Long userId = connectedAccountMapper.findIdByLoginId(loginId);

        // 1. 사용자 보유 계좌 전체 조회
        List<UserAccountVO> accountList = userAccountMapper.findByUserId(userId);

        log.info("[SYNC][계좌] userId={} 계좌 {}건 동기화 시작!", userId, accountList.size());

        if (accountList.isEmpty()) {
            log.info("[SYNC][계좌] 동기화할 계좌 없음. userId={}", userId);
            return;
        }

        ConnectedAccountVO vo = connectedAccountMapper.findConnectedIdByUserId(userId);
        String connectedId = vo.getConnectedId();

        // 2. 계좌별로 최신 거래일자 조회
        for (UserAccountVO account : accountList) {
            String accountNo = account.getAccountNumber();
            Long accountId = account.getId();
            String bankCode = account.getBankCode();

            // 이 계좌의 DB 내 가장 최근 거래일 (yyyyMMdd)
            String lastDbDate = accountTransactionMapper.findLastTransactionDateByAccountId(accountId);

            // 2. null이면 1년 전, 아니면 lastDbDate부터
            String startDate = (lastDbDate == null)
                    ? LocalDate.now().minusYears(1).format(DateTimeFormatter.BASIC_ISO_DATE)
                    : lastDbDate;
            String endDate = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);

            log.info("[SYNC][계좌] 기간 {}~{} 동기화 시도", startDate, endDate);

            // 3. 최신 내역 CODEF로 요청
            try {
                RetryUtil.retryVoid(3, 1000, () -> {
                    codefService.syncAccountTransaction(userId, accountId, bankCode, connectedId, accountNo, startDate, endDate);
                });
                log.info("[SYNC][계좌] 동기화 완료!");
            } catch (Exception e) {
                log.error("[SYNC][계좌] 동기화 3회 재시도 실패", e);
            }
        }
    }


    /**
     * [비동기] 사용자의 모든 카드 거래내역을 최신 상태로 동기화
     * - 각 카드의 가장 최근 거래일 이후(없으면 1년 전부터) CODEF 승인내역을 불러와 DB에 추가
     * - 실패시 1초간격 3회 재시도
     */
    @Async
    public void syncCardsAsync(String loginId) {
        Long userId = connectedAccountMapper.findIdByLoginId(loginId);

        // 1. 사용자 보유 카드 전체 조회
        List<UserCardVO> cardList = userCardMapper.findByUserId(userId);

        log.info("[SYNC][카드] userId={} 카드 {}건 동기화 시작!", userId, cardList.size());

        if (cardList.isEmpty()) {
            log.info("[SYNC][카드] 동기화할 카드 없음. userId={}", userId);
            return;
        }

        ConnectedAccountVO vo = connectedAccountMapper.findConnectedIdByUserId(userId);
        String connectedId = vo.getConnectedId();

        for (UserCardVO card : cardList) {
            String cardNo = card.getCardMaskedNumber();
            Long cardId = card.getId();
            String cardCode = card.getIssuerCode();
            String cardName = card.getCardName();

            // 카드의 DB 내 가장 최근 거래일 (yyyyMMdd)
            String lastDbDate = cardTransactionMapper.findLastTransactionDateByCardId(cardId);
            String startDate = (lastDbDate == null)
                    ? LocalDate.now().minusYears(1).format(DateTimeFormatter.BASIC_ISO_DATE)
                    : lastDbDate;
            String endDate = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);

            log.info("[SYNC][카드] 기간 {}~{} 동기화 시도", startDate, endDate);

            // 3. 최신 내역 CODEF로 요청
            try {
                RetryUtil.retryVoid(3, 1000, () -> {
                    codefService.syncCardTransaction(userId, cardId, cardCode, connectedId, cardNo, startDate, endDate, cardName);
                });
                log.info("[SYNC][카드] 동기화 완료!");
            } catch (Exception e) {
                log.error("[SYNC][카드] 동기화 3회 재시도 실패", e);
            }
        }
    }
}

