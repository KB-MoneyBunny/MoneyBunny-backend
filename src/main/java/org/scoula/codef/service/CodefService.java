package org.scoula.codef.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scoula.codef.common.exception.AlreadyRegisteredCardException;
import org.scoula.codef.common.exception.CodefApiException;
import org.scoula.codef.domain.*;
import org.scoula.codef.dto.AccountConnectRequest;
import org.scoula.codef.dto.CardConnectRequest;
import org.scoula.codef.mapper.*;
import org.scoula.codef.util.CodefUtil;
import org.scoula.codef.util.RSAUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CodefService {

    private final CodefTokenService tokenService;
    @Value("${codef.public_key}")
    private String publicKey;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ConnectedAccountMapper connectedAccountMapper;
    private final UserAccountMapper userAccountMapper;
    private final AccountTransactionMapper accountTransactionMapper;
    private final UserCardMapper userCardMapper;
    private final CardTransactionMapper cardTransactionMapper;
    private final CategoryMapper categoryMapper;

    /**
     * 사용자의 은행 계정 연결 및 계좌 목록 조회
     * - 계정 연결(connectedId 발급/추가) 후 계좌 목록을 CODEF에서 가져와 반환
     * - 계정 연결에 실패하면 예외 발생
     */
    public List<UserAccountVO> connectAndFetchAccounts(String loginId, AccountConnectRequest request) {
        try {
            Long userId = connectedAccountMapper.findIdByLoginId(loginId);
            log.info("[CODEF 계좌연동] loginId={}, userId={}", loginId, userId);

            // 1. connected_accounts 테이블에서 connectedId 먼저 조회
            ConnectedAccountVO vo = connectedAccountMapper.findConnectedIdByUserId(userId);
            String connectedId = (vo == null) ? null : vo.getConnectedId();
            log.debug("connectedId 조회 결과: {}", connectedId);

            // 2. 없으면 새로 발급 후 저장
            if (connectedId == null) {
                String encryptedPw = RSAUtil.encryptRSA(request.getPassword(), publicKey);

                // 3. 계정 연결 요청
                String connectUrl = "https://development.codef.io/v1/account/create";
                String connectBody = """
                        {
                          "accountList": [{
                            "countryCode": "KR",
                            "businessType": "BK",
                            "clientType": "P",
                            "organization": "%s",
                            "loginType": "1",
                            "id": "%s",
                            "password": "%s"
                          }]
                        }
                        """.formatted(request.getOrganization(), request.getLoginId(), encryptedPw);

                String connectResponse = sendPost(connectUrl, connectBody);
                String decodedResponse = URLDecoder.decode(connectResponse, StandardCharsets.UTF_8);
                log.debug("[CODEF 계정연결 응답] {}", decodedResponse);

//                if (!CodefUtil.isSuccess(decodedResponse)) {
//                    // throw new RuntimeException("계정 연결 실패: " + CodefUtil.getResultMessage(decodedResponse));
//                }

                if (!CodefUtil.isSuccess(decodedResponse)) {
                    JsonNode errorNode = objectMapper.readTree(decodedResponse)
                            .get("data").get("errorList").get(0);

                    String errorCode = errorNode.get("code").asText();
                    String errorMessage = errorNode.get("message").asText();

                    log.error("[CODEF 계정연결 실패] code={}, msg={}", errorCode, errorMessage);

                    throw new CodefApiException(errorCode, errorMessage);
                }

                connectedId = objectMapper.readTree(decodedResponse)
                        .get("data").get("connectedId").asText();
                log.info("[CODEF connectedId 발급완료] userId={}, connectedId={}", userId, connectedId);

                // DB에 connectedId 저장
                connectedAccountMapper.insertConnectedAccount(userId, connectedId);

            } else {
                // 이미 connectedId가 있으면 계정 추가로 처리
                log.info("[CODEF 계좌연동] 기존 connectedId 사용: {}", connectedId);
                String encryptedPw = RSAUtil.encryptRSA(request.getPassword(), publicKey);

                String addUrl = "https://development.codef.io/v1/account/add";
                String addBody = """
                        {
                          "accountList": [{
                            "countryCode": "KR",
                            "businessType": "BK",
                            "clientType": "P",
                            "organization": "%s",
                            "loginType": "1",
                            "id": "%s",
                            "password": "%s"
                          }],
                          "connectedId": "%s"
                        }
                        """.formatted(
                        request.getOrganization(),
                        request.getLoginId(),
                        encryptedPw,
                        connectedId
                );

                String addResponse = sendPost(addUrl, addBody);
                String decodedAddResponse = URLDecoder.decode(addResponse, StandardCharsets.UTF_8);

                log.debug("[CODEF 계정추가 응답] {}", decodedAddResponse);

                if (!CodefUtil.isSuccess(decodedAddResponse)) {
                    JsonNode errorNode = objectMapper.readTree(decodedAddResponse)
                            .get("data").get("errorList").get(0);

                    String errorCode = errorNode.get("code").asText();
                    String errorMessage = errorNode.get("message").asText();

                    log.error("[CODEF 계정추가 실패] code={}, msg={}", errorCode, errorMessage);

                    throw new CodefApiException(errorCode, errorMessage);
                }
            }


            // 3. 계좌목록 조회
            String accountUrl = "https://development.codef.io/v1/kr/bank/p/account/account-list";
            String accountBody = """
                    {
                      "organization": "%s",
                      "connectedId": "%s"
                    }
                    """.formatted(request.getOrganization(), connectedId);

            String response = sendPost(accountUrl, accountBody);
            String decodedAccountResponse = URLDecoder.decode(response, StandardCharsets.UTF_8);
            log.debug("[CODEF 계좌목록 응답] {}", decodedAccountResponse);

            JsonNode root = objectMapper.readTree(decodedAccountResponse);

            if (!CodefUtil.isSuccess(decodedAccountResponse)) {
                log.error("[CODEF 계좌조회 실패] {}", CodefUtil.getResultMessage(decodedAccountResponse));
                throw new RuntimeException("계좌 조회 실패: " + CodefUtil.getResultMessage(decodedAccountResponse));
            }

            JsonNode accountList = root.get("data").get("resDepositTrust");

            if (accountList.isMissingNode() || !accountList.isArray()) {
                log.warn("[CODEF 계좌목록 비어있음] resDepositTrust 없음/배열아님");
                throw new RuntimeException("resDepositTrust가 비어있거나 배열이 아님");
            }

            List<UserAccountVO> result = new ArrayList<>();
            for (JsonNode node : accountList) {
                result.add(UserAccountVO.builder()
                        .accountNumber(node.get("resAccount").asText())
                        .accountName(node.get("resAccountName").asText())
                        .accountType(node.get("resAccountDeposit").asText())
                        .balance(Long.parseLong(node.get("resAccountBalance").asText()))
                        .bankCode(request.getOrganization())
                        .createdAt(new Date())
                        .build()
                );
            }
            log.info("[CODEF 계좌목록 파싱완료] 개수={}", result.size());

            return result;

        } catch (Exception e) {
            log.error("[CODEF 계좌연동/조회 실패] loginId={}, error={}", loginId, e.getMessage(), e);
            if (e instanceof CodefApiException) {
                throw (CodefApiException) e;
            }
            throw new CodefApiException("INTERNAL_ERROR", "계좌 연결 및 조회 실패");
        }
    }


    /**
     * (트랜잭션) 사용자의 선택 계좌 목록을 DB에 등록
     * - 계좌별로 1년치 거래내역을 CODEF에서 조회해 배치로 저장
     */
    @Transactional
    public void registerUserAccounts(String loginId, List<UserAccountVO> selectedAccounts) {
        Long userId = connectedAccountMapper.findIdByLoginId(loginId);
        ConnectedAccountVO vo = connectedAccountMapper.findConnectedIdByUserId(userId);
        String connectedId = vo.getConnectedId();

        log.info("[계좌등록] 계좌 등록 프로세스 시작: loginId={}, userId={}, 등록요청 계좌수={}", loginId, userId, selectedAccounts.size());

        // 오늘 기준 1년 전 ~ 오늘 (yyyyMMdd)
        LocalDate today = LocalDate.now();
        String endDate = today.format(DateTimeFormatter.BASIC_ISO_DATE);
        String startDate = today.minusYears(1).format(DateTimeFormatter.BASIC_ISO_DATE);

        for (UserAccountVO account : selectedAccounts) {

            if (userAccountMapper.existsAccount(userId, account.getAccountNumber()) > 0) {
                log.info("[계좌등록] 이미 등록된 계좌 skip: userId={}", userId);
                continue;
            }
            account.setUserId(userId);
            userAccountMapper.insertUserAccount(account);
            log.info("[계좌등록] 신규 계좌 DB 저장");

            // 2. 계좌 PK(ID) 얻기 (insert 후 select or MyBatis selectKey 활용)
            Long accountId = userAccountMapper.findIdByUserIdAndAccountNumber(userId, account.getAccountNumber());

            // 3. 거래내역 조회 및 저장
            List<AccountTransactionVO> txList = fetchAndParseAccountTransactions(
                    account.getBankCode(), connectedId, account.getAccountNumber(), startDate, endDate
            );
            log.info("[계좌등록] 거래내역 조회 완료: 내역건수={}",  txList.size());

            for (int i = 0; i < txList.size(); i += 500) {
                List<AccountTransactionVO> batch = txList.subList(i, Math.min(i + 500, txList.size()));
                for (AccountTransactionVO tx : batch) {
                    tx.setAccountId(accountId);
                }
                accountTransactionMapper.insertAccountTransactions(batch);
                log.info("[계좌등록] 거래내역 배치 저장: 저장건수={}",  batch.size());
            }
        }
    }



    /**
     * 특정 계좌의 거래내역을 CODEF API로 조회하고 파싱
     * - startDate~endDate 범위의 거래내역을 조회
     * - 파싱하여 AccountTransactionVO 리스트로 반환
     */
    // 거래내역 API 호출 및 파싱
    public List<AccountTransactionVO> fetchAndParseAccountTransactions(
            String bankCode, String connectedId, String accountNumber, String startDate, String endDate) {
        List<AccountTransactionVO> txList = new ArrayList<>();
        log.info("[거래내역] 거래내역 조회 시작: bankCode={}, 기간={}-{}", bankCode, startDate, endDate);
        try {
            String reqBody = """
                    {
                        "organization": "%s",
                        "connectedId": "%s",
                        "account": "%s",
                        "startDate": "%s",
                        "endDate": "%s",
                        "orderBy": "0",
                        "inquiryType": "1"
                    }
                    """.formatted(bankCode, connectedId, accountNumber, startDate, endDate);


            String url = "https://development.codef.io/v1/kr/bank/p/account/transaction-list";
            String apiResp = sendPost(url, reqBody);

            String decodedResp = URLDecoder.decode(apiResp, StandardCharsets.UTF_8);
            log.debug("[거래내역] 디코드된 응답: {}", decodedResp);

            JsonNode root = objectMapper.readTree(decodedResp);
            if (!CodefUtil.isSuccess(decodedResp)) {
                log.warn("[거래내역] CODEF API 실패: {}", CodefUtil.getResultMessage(decodedResp));
                throw new CodefApiException("TRANSACTION_ERROR", "거래내역 조회 실패");
            }
            JsonNode txArr = root.get("data").get("resTrHistoryList");
            if (txArr == null || !txArr.isArray()) {
                log.info("[거래내역] 반환된 거래내역 없음");
                return txList;
            }

            for (JsonNode node : txArr) {
                String out = node.path("resAccountOut").asText("");
                String in = node.path("resAccountIn").asText("");

                // 입출금 구분
                String txType;
                long amount;
                if (!out.isEmpty() && Long.parseLong(out) > 0) {
                    txType = "expense";
                    amount = Long.parseLong(out);
                } else if (!in.isEmpty() && Long.parseLong(in) > 0) {
                    txType = "income";
                    amount = Long.parseLong(in);
                } else {
                    txType = "saving";
                    amount = 0L; // 예금/적금 등은 따로 분기 필요
                }

                // 거래 일시 합치기 (yyyyMMdd + HHmmss → yyyy-MM-dd HH:mm:ss)
                String date = node.path("resAccountTrDate").asText();
                String time = node.path("resAccountTrTime").asText();
                String dateTimeStr = date + time;
                LocalDateTime txDateTime = LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

                AccountTransactionVO tx = AccountTransactionVO.builder()
                        .amount(amount)
                        .txType(txType)
                        .transactionDateTime(Timestamp.valueOf(txDateTime))
                        .balanceAfter(node.path("resAfterTranBalance").asLong(0))
                        .storeName(node.path("resAccountDesc3").asText())
                        .branchName(node.path("resAccountDesc4").asText())
                        .build();

                log.debug("[거래내역] 저장 예정 거래내역: {}", tx);
                txList.add(tx);
            }

            log.info("[거래내역] 거래내역 파싱 및 생성 완료:  건수={}", txList.size());

        } catch (Exception e) {
            log.error("[거래내역] 거래내역 파싱 실패! bankCode={}, 에러={}", bankCode, e.getMessage(), e);
        }
        return txList;
    }


    /**
     * 사용자의 카드 계정 연결 및 카드 목록 조회
     * - 카드 계정 연결(connectedId 발급/추가) 후 카드 목록을 CODEF에서 가져와 반환
     * - 카드 연결에 실패하면 예외 발생
     */
    public List<UserCardVO> connectAndFetchCards(String loginId, CardConnectRequest request) {
        try {
            Long userId = connectedAccountMapper.findIdByLoginId(loginId);
            log.info("[카드] 카드 계정 연결/조회 시작: loginId={}, issuer={}", loginId, request.getOrganization());

            // 1. connectedId 조회 or 생성
            ConnectedAccountVO vo = connectedAccountMapper.findConnectedIdByUserId(userId);
            String connectedId = (vo == null) ? null : vo.getConnectedId();
            String encryptedPw = RSAUtil.encryptRSA(request.getPassword(), publicKey);

            if (connectedId == null) {
                log.info("[카드] connectedId 없음 → 계정 연결 요청 진행");
                String connectUrl = "https://development.codef.io/v1/account/create";
                String connectBody = """
                        {
                          "accountList": [{
                            "countryCode": "KR",
                            "businessType": "CD",
                            "clientType": "P",
                            "organization": "%s",
                            "loginType": "1",
                            "id": "%s",
                            "password": "%s"
                          }]
                        }
                        """.formatted(
                        request.getOrganization(),
                        request.getLoginId(),
                        encryptedPw
                );

                String connectResponse = sendPost(connectUrl, connectBody);
                String decodedResponse = URLDecoder.decode(connectResponse, StandardCharsets.UTF_8);


                log.debug("[카드] 계정 연결 API 응답: {}", decodedResponse);

                if (!CodefUtil.isSuccess(decodedResponse)) {
                    JsonNode errorNode = objectMapper.readTree(decodedResponse)
                            .get("data").get("errorList").get(0);
                    String errorCode = errorNode.get("code").asText();
                    String errorMessage = errorNode.get("message").asText();
                    log.warn("[카드] 계정 연결 실패: {} - {}", errorCode, errorMessage);
                    throw new CodefApiException(errorCode, errorMessage);
                }

                connectedId = objectMapper.readTree(decodedResponse)
                        .get("data").get("connectedId").asText();

                log.info("[카드] connectedId 신규 발급 완료: {}", connectedId);

                // DB에 connectedId 저장
                connectedAccountMapper.insertConnectedAccount(userId, connectedId);

            } else {
                // 이미 connectedId가 있으면 카드 계정 추가 (추가 연결)
                log.info("[카드] connectedId 존재 → 카드 계정 추가 연결 진행");
                String addUrl = "https://development.codef.io/v1/account/add";
                String addBody = """
                        {
                          "accountList": [{
                            "countryCode": "KR",
                            "businessType": "CD",
                            "clientType": "P",
                            "organization": "%s",
                            "loginType": "1",
                            "id": "%s",
                            "password": "%s"
                          }],
                          "connectedId": "%s"
                        }
                        """.formatted(
                        request.getOrganization(),
                        request.getLoginId(),
                        encryptedPw,
                        connectedId
                );

                String addResponse = sendPost(addUrl, addBody);
                String decodedAddResponse = URLDecoder.decode(addResponse, StandardCharsets.UTF_8);

                log.debug("[카드] 계정 추가 API 응답: {}", decodedAddResponse);

                if (!CodefUtil.isSuccess(decodedAddResponse)) {
                    JsonNode errorNode = objectMapper.readTree(decodedAddResponse)
                            .get("data").get("errorList").get(0);
                    String errorCode = errorNode.get("code").asText();
                    String errorMessage = errorNode.get("message").asText();
                    log.warn("[카드] 계정 추가 실패: {} - {}", errorCode, errorMessage);
                    throw new CodefApiException(errorCode, errorMessage);
                }
            }

            // 3. 카드 목록 조회
            String cardListUrl = "https://development.codef.io/v1/kr/card/p/account/card-list";
            String cardListBody = """
                    {
                        "organization": "%s",
                        "connectedId": "%s",
                        "inquiryType": "1"
                    }
                    """.formatted(request.getOrganization(), connectedId);

            String response = sendPost(cardListUrl, cardListBody);
            String decodedCardListResponse = URLDecoder.decode(response, StandardCharsets.UTF_8);

            log.debug("[카드] 카드목록 API 응답: {}", decodedCardListResponse);

            JsonNode root = objectMapper.readTree(decodedCardListResponse);

            if (!CodefUtil.isSuccess(decodedCardListResponse)) {
                log.warn("[카드] 카드목록 조회 실패: {}", CodefUtil.getResultMessage(decodedCardListResponse));
                throw new RuntimeException("카드 목록 조회 실패: " + CodefUtil.getResultMessage(decodedCardListResponse));
            }

            List<UserCardVO> result = new ArrayList<>();

            JsonNode dataNode = root.get("data");

            // 1. 여러장 카드
            // resCardList 인지는 확인 필요.. 제가 한 은행에 여러장의 카드가 없어요 ㅜㅜ
            JsonNode cardListNode = dataNode.get("resCardList");
            if (cardListNode != null && cardListNode.isArray()) {
                log.info("[카드] 여러장 카드 목록 반환: {}장", cardListNode.size());
                for (JsonNode node : cardListNode) {
                    result.add(UserCardVO.builder()
                            .cardMaskedNumber(node.path("resCardNo").asText())
                            .cardName(node.path("resCardName").asText())
                            .cardType(node.path("resCardType").asText())
                            .issuerCode(request.getOrganization())
                            .cardImage(node.path("resImageLink").asText())
                            .createdAt(new Date())
                            .build()
                    );
                }
            } else {
                // 2. 단일 카드
                if (dataNode.has("resCardNo")) {
                    log.info("[카드] 단일 카드만 반환됨");
                    result.add(UserCardVO.builder()
                            .cardMaskedNumber(dataNode.path("resCardNo").asText())
                            .cardName(dataNode.path("resCardName").asText())
                            .cardType(dataNode.path("resCardType").asText())
                            .issuerCode(request.getOrganization())
                            .cardImage(dataNode.path("resImageLink").asText())
                            .createdAt(new Date())
                            .build()
                    );
                }
            }
            log.info("[카드] 카드목록 파싱 완료: loginId={}, 반환건수={}", loginId, result.size());
            return result;


        } catch (Exception e) {
            log.error("[카드] 카드 계정 연결 및 조회 실패", e);
            if (e instanceof CodefApiException) throw (CodefApiException) e;
            throw new CodefApiException("INTERNAL_ERROR", "카드 계정 연결 및 조회 실패");
        }
    }


    /**
     * (트랜잭션) 사용자의 선택 카드 목록을 DB에 등록
     * - 카드별로 1년치 거래내역을 CODEF에서 조회해 배치로 저장
     */
    @Transactional
    public void registerUserCards(String loginId, List<UserCardVO> selectedCards) {
        Long userId = connectedAccountMapper.findIdByLoginId(loginId);
        ConnectedAccountVO vo = connectedAccountMapper.findConnectedIdByUserId(userId);
        String connectedId = vo.getConnectedId();

        log.info("[카드등록] 시작: loginId={}, 등록요청 개수={}", loginId, selectedCards.size());

        // 오늘 기준 1년 전 ~ 오늘 (yyyyMMdd)
        LocalDate today = LocalDate.now();
        String endDate = today.format(DateTimeFormatter.BASIC_ISO_DATE);
        String startDate = today.minusYears(1).format(DateTimeFormatter.BASIC_ISO_DATE);
        Long categoryId = categoryMapper.findUnclassifiedId();

        for (UserCardVO card : selectedCards) {
            // 1. 이미 등록된 카드면 skip
            if (userCardMapper.existsCard(userId, card.getCardMaskedNumber()) > 0) {
                log.warn("[카드등록] 중복카드(이미 등록됨): userId={}", userId);
                throw new AlreadyRegisteredCardException(card.getCardMaskedNumber());
            }
            card.setUserId(userId);
            userCardMapper.insertUserCard(card);
            log.info("[카드등록] 카드 등록 성공");

            // 2. 카드 PK(ID) 얻기
            Long cardId = userCardMapper.findIdByUserIdAndCardNumber(userId, card.getCardMaskedNumber());
            log.debug("[카드등록] 카드 PK 조회: cardId={}", cardId);

            // 3. 거래내역 조회 및 저장
            List<CardTransactionVO> txList = fetchAndParseCardTransactions(
                    card.getIssuerCode(),
                    connectedId,
                    startDate,
                    endDate,
                    card.getCardName(),
                    card.getCardMaskedNumber()
            );
            log.info("[카드등록] 거래내역 조회 완료: cardId={}, 내역건수={}", cardId, txList.size());
            log.debug("[카드등록] 거래내역 샘플: {}", txList.isEmpty() ? "없음" : txList.get(0));

            for (int i = 0; i < txList.size(); i += 500) {
                List<CardTransactionVO> batch = txList.subList(i, Math.min(i + 500, txList.size()));
                for (CardTransactionVO tx : batch) {
                    tx.setCardId(cardId);
                    tx.setCategoryId(categoryId);
                }
                cardTransactionMapper.insertCardTransactions(batch);
                log.debug("[카드등록] 배치 insert: cardId={}, batchStart={}, batchEnd={}", cardId, i, Math.min(i + 500, txList.size()));
            }
            log.info("[카드등록] 카드 등록+내역저장 완료: 전체내역={}",  txList.size());
        }
        log.info("[카드등록] 전체 완료: loginId={}, 요청카드수={}", loginId, selectedCards.size());
    }



    // Codef 카드 거래내역 API 호출 및 파싱
    public List<CardTransactionVO> fetchAndParseCardTransactions(
            String cardCode,
            String connectedId,
            String startDate,
            String endDate,
            String cardName,
            String cardNo
    ) {
        List<CardTransactionVO> txList = new ArrayList<>();
        try {
            // 1. CODEF 카드 승인내역 API 호출
            String url = "https://development.codef.io/v1/kr/card/p/account/approval-list";
            String reqBody = """
        {
            "connectedId": "%s",
            "organization": "%s",
            "startDate": "%s",
            "endDate": "%s",
            "orderBy": "0",
            "inquiryType": "0",
            "cardName": "%s",
            "cardNo": "%s",
            "memberStoreInfoType": "1"
        }
        """.formatted(connectedId, cardCode, startDate, endDate, cardName, cardNo);

            log.info("[카드거래] CODEF 승인내역 API 호출: 기간={}-{}", startDate, endDate);

            String apiResp = sendPost(url, reqBody);
            String decodedResp = URLDecoder.decode(apiResp, StandardCharsets.UTF_8);

            log.debug("[카드거래] CODEF 응답: {}", decodedResp);

            JsonNode root = objectMapper.readTree(decodedResp);
            if (!CodefUtil.isSuccess(decodedResp)) {
                log.error("[카드거래] CODEF 응답 실패: {}", CodefUtil.getResultMessage(decodedResp));
                throw new CodefApiException("CARD_TRANSACTION_ERROR", "카드 승인내역 조회 실패");
            }

            JsonNode txArr = root.get("data");
            if (txArr == null || !txArr.isArray()) {
                log.warn("[카드거래] 승인내역 데이터 없음 or 배열 아님");
                return txList;
            }

            for (JsonNode node : txArr) {
                // 2. 거래 일시 파싱 (yyyyMMdd + HHmmss → yyyy-MM-dd HH:mm:ss)
                String usedDate = node.path("resUsedDate").asText("");
                String usedTime = node.path("resUsedTime").asText("");

                String dateTimeStr = usedDate + usedTime;
                LocalDateTime txDateTime;
                try {
                    txDateTime = LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
                } catch (Exception e) {
                    log.warn("[카드거래] 일시 파싱 실패: usedDate={}, usedTime={}", usedDate, usedTime);
                    txDateTime = null;
                }

                // 3. 금액 파싱 (null/빈값 방지)
                long amount = 0L;
                try {
                    amount = Long.parseLong(node.path("resUsedAmount").asText("0"));
                } catch (Exception e) {
                    log.warn("[카드거래] 금액 파싱 실패: {}", node.path("resUsedAmount").asText(""));
                    amount = 0L;
                }

                // 파싱 전 String을 변수에 받고
                String installmentMonthStr = node.path("resInstallmentMonth").asText("");
                Integer installmentMonth = null;
                if (!installmentMonthStr.isEmpty()) {
                    try {
                        installmentMonth = Integer.valueOf(installmentMonthStr);
                    } catch (Exception e) {
                        log.warn("[카드거래] 할부개월 파싱 실패: {}", installmentMonthStr);
                        installmentMonth = null;
                    }
                }

                // cancelAmount도 마찬가지!
                String cancelAmountStr = node.path("resCancelAmount").asText("");
                Long cancelAmount = null;
                if (!cancelAmountStr.isEmpty()) {
                    try {
                        cancelAmount = Long.valueOf(cancelAmountStr);
                    } catch (Exception e) {
                        log.warn("[카드거래] 취소금액 파싱 실패: {}", cancelAmountStr);
                        cancelAmount = null;
                    }
                }


                // 4. CardTransactionVO 생성
                CardTransactionVO tx = CardTransactionVO.builder()
                        .transactionDate(txDateTime != null ? Timestamp.valueOf(txDateTime) : null)
                        .storeName(node.path("resMemberStoreName").asText(""))
                        .amount(amount)
                        .paymentType(node.path("resPaymentType").asText(""))
                        .installmentMonth(installmentMonth)
                        .approvalNo(node.path("resApprovalNo").asText(""))
                        .storeType(node.path("resMemberStoreType").asText(""))
                        .cancelStatus(node.path("resCancelYN").asText(""))
                        .cancelAmount(cancelAmount)
                        .storeName1(node.path("resMemberStoreName1").asText(""))
                        .build();

                log.debug("[거래내역] 저장 예정 거래내역: {}", tx);
                txList.add(tx);
            }
            log.info("[거래내역] 거래내역 파싱 및 생성 완료: 건수={}", txList.size());
        } catch (Exception e) {
            log.error("[카드거래] 승인내역 파싱 실패: , msg={}", e.getMessage(), e);
        }
        return txList;
    }


    /**
     * 사용자의 특정 계좌의 거래내역을 CODEF와 동기화(신규만 insert)
     * - DB 내 기존 거래와 CODEF 거래내역을 비교해 신규건만 insert
     */
    public void syncAccountTransaction(Long userId, Long accountId, String bankCode, String connectedId, String accountNo, String startDate, String endDate) {
        log.info("⚡ [CODEF] 거래내역 fetch 시작: bankCode={}, 기간={}-{}", bankCode, startDate, endDate);

        // 1. CODEF 거래내역 불러오기
        List<AccountTransactionVO> apiTxList = fetchAndParseAccountTransactions(
                bankCode, connectedId, accountNo, startDate, endDate
        );

        log.info("⚡ [CODEF] API에서 받은 거래내역 개수: {}", apiTxList.size());

        // 2. 해당 계좌의 모든 기존 거래 (중복 비교 키 세트로!)
        Set<String> dbTxKeySet = accountTransactionMapper.findAllTxKeyByAccountIdFromDate(accountId, startDate);
        log.debug("[DB] 기존 거래내역(중복키) 개수: {}", dbTxKeySet.size());


        int newTxCount = 0;
        for (AccountTransactionVO tx : apiTxList) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String dateStr = sdf.format(tx.getTransactionDateTime());
            String key = dateStr + "|" + tx.getAmount() + "|" + tx.getTxType();

//            if (dbTxKeySet.contains(key)) continue;
            if (dbTxKeySet.contains(key)) {
                log.debug("⏭[중복] {} → skip", key);
                continue;
            }
            tx.setAccountId(accountId);
            accountTransactionMapper.insertAccountTransaction(tx);
            newTxCount++;
            log.debug("[신규] {} → insert", key);
            dbTxKeySet.add(key);
        }
        log.info("[Sync] 동기화 완료! 신규 {}건 추가",  newTxCount);
    }




    /**
     * 사용자의 특정 카드의 승인내역을 CODEF와 동기화(신규만 insert)
     * - DB 내 기존 승인내역과 CODEF 승인내역을 비교해 신규건만 insert
     */
    public void syncCardTransaction(Long userId, Long cardId, String cardCode, String connectedId, String cardNo, String startDate, String endDate, String cardName) {
        log.info("[Sync] 카드거래 동기화 시작: cardId={}, 기간={}~{}", cardId, startDate, endDate);

        // 1. CODEF 카드거래내역 불러오기
        List<CardTransactionVO> apiTxList = fetchAndParseCardTransactions(cardCode, connectedId, startDate, endDate, cardName, cardNo);
        log.info("[CODEF] API 거래내역 개수: {}", apiTxList.size());

        // 2. 해당 카드의 기존 거래키 세트 (approval_no + card_id)
        Set<String> dbTxKeySet = cardTransactionMapper.findAllTxKeyByCardIdFromDate(cardId, startDate);
        log.debug("[DB] 기존 승인번호+카드ID 키 개수: {}", dbTxKeySet.size());

        int insertCount = 0;

        for (CardTransactionVO tx : apiTxList) {
            String key = tx.getApprovalNo() + "|" + cardId;
            log.debug("   [중복체크] key: {}", key);

            if (dbTxKeySet.contains(key)) {
                log.debug("⏭[중복] {} → skip", key);
                continue;
            }

            log.debug("  신규 삽입: {}", key);
            tx.setCardId(cardId);
            insertCount++;
            cardTransactionMapper.insertCardTransaction(tx);
            dbTxKeySet.add(key);
        }

        log.info("[Sync] 카드 {} 동기화 완료! 신규 {}건 추가", cardId, insertCount);
    }








    /**
     * CODEF API에 POST 요청 전송 (Bearer 토큰 포함)
     * - HTTP 요청/응답 바이트 변환 등 공통 처리
     */
    private String sendPost(String urlStr, String jsonBody) throws Exception {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Authorization", "Bearer " + tokenService.getAccessToken());
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(jsonBody.getBytes(StandardCharsets.UTF_8));
        }
//        try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
//            return reader.lines().collect(Collectors.joining());
//        }
        int responseCode = conn.getResponseCode();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                        responseCode >= 400 ? conn.getErrorStream() : conn.getInputStream()
                ))) {
            return reader.lines().collect(Collectors.joining());
        }
    }







    /*
    * 테스트를 위한 서비스.
    * fetchAccountListByConnectedId -> 등록 계좌 조회
    * deleteAccountsRaw -> 등록된 계좌 삭제
    * */
    public String fetchAccountListByConnectedId(String connectedId) {
        String url = "https://development.codef.io/v1/account/list";
        String body = String.format("""
        {
            "connectedId": "%s"
        }
        """, connectedId);

        try {
            String response = sendPost(url, body);
            String response2 = URLDecoder.decode(response, StandardCharsets.UTF_8);
            return response2;
        } catch (Exception e) {
            throw new RuntimeException("계정 목록 조회 실패: " + e.getMessage());
        }
    }

    public String deleteAccountsRaw(Map<String, Object> body) {
        String url = "https://development.codef.io/v1/account/delete";
        try {
            ObjectMapper mapper = new ObjectMapper();
            String jsonBody = mapper.writeValueAsString(body);

            String response = sendPost(url, jsonBody);
            String response2 = URLDecoder.decode(response, StandardCharsets.UTF_8);
            return response2;
        } catch (Exception e) {
            throw new RuntimeException("계정 삭제 실패: " + e.getMessage());
        }
    }

}
