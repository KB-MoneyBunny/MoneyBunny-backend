package org.scoula.codef.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.scoula.codef.common.exception.AlreadyRegisteredCardException;
import org.scoula.codef.common.exception.CodefApiException;
import org.scoula.codef.domain.*;
import org.scoula.codef.dto.AccountConnectRequest;
import org.scoula.codef.dto.CardConnectRequest;
import org.scoula.codef.mapper.*;
import org.scoula.codef.util.AesUtil;
import org.scoula.codef.util.CodefUtil;
import org.scoula.codef.util.RSAUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
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

//    private final GptApiClient gptApiClient;


    public List<UserAccountVO> connectAndFetchAccounts(AccountConnectRequest request) {
        try {

            String loginId = "admin1";

            Long userId = connectedAccountMapper.findIdByLoginId(loginId);

            System.out.println("userId = " + userId);

            // 1. connected_accounts 테이블에서 connectedId 먼저 조회
            ConnectedAccountVO vo = connectedAccountMapper.findConnectedIdByUserId(userId);
            String connectedId = vo.getConnectedId();

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
                System.out.println("계정 연결 응답 = " + decodedResponse);

//                if (!CodefUtil.isSuccess(decodedResponse)) {
//                    // throw new RuntimeException("계정 연결 실패: " + CodefUtil.getResultMessage(decodedResponse));
//                }

                if (!CodefUtil.isSuccess(decodedResponse)) {
                    JsonNode errorNode = objectMapper.readTree(decodedResponse)
                            .get("data").get("errorList").get(0);

                    String errorCode = errorNode.get("code").asText();
                    String errorMessage = errorNode.get("message").asText();

                    throw new CodefApiException(errorCode, errorMessage);
                }

                connectedId = objectMapper.readTree(decodedResponse)
                        .get("data").get("connectedId").asText();

                System.out.println("발급된 connectedId = " + connectedId);

                // DB에 connectedId 저장
                connectedAccountMapper.insertConnectedAccount(userId, connectedId);

            } else {
                // 이미 connectedId가 있으면 계정 추가로 처리
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

                System.out.println("계정 추가 응답 = " + decodedAddResponse);

                if (!CodefUtil.isSuccess(decodedAddResponse)) {
                    JsonNode errorNode = objectMapper.readTree(decodedAddResponse)
                            .get("data").get("errorList").get(0);

                    String errorCode = errorNode.get("code").asText();
                    String errorMessage = errorNode.get("message").asText();

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

            System.out.println("계좌조회 응답 = " + decodedAccountResponse);

            JsonNode root = objectMapper.readTree(decodedAccountResponse);

            if (!CodefUtil.isSuccess(decodedAccountResponse)) {
                throw new RuntimeException("계좌 조회 실패: " + CodefUtil.getResultMessage(decodedAccountResponse));
            }

            JsonNode accountList = root.get("data").get("resDepositTrust");


            System.out.println("decodedAccountResponse = " + decodedAccountResponse);


            if (accountList.isMissingNode() || !accountList.isArray()) {
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

            return result;

        } catch (Exception e) {
            if (e instanceof CodefApiException) {
                throw (CodefApiException) e;
            }
            throw new CodefApiException("INTERNAL_ERROR", "계좌 연결 및 조회 실패");
        }
    }

    public void registerUserAccounts(String loginId, List<UserAccountVO> selectedAccounts) {
        Long userId = connectedAccountMapper.findIdByLoginId(loginId);
        ConnectedAccountVO vo = connectedAccountMapper.findConnectedIdByUserId(userId);
        String connectedId = vo.getConnectedId();

        // 오늘 기준 1년 전 ~ 오늘 (yyyyMMdd)
        LocalDate today = LocalDate.now();
        String endDate = today.format(DateTimeFormatter.BASIC_ISO_DATE);
        String startDate = today.minusYears(1).format(DateTimeFormatter.BASIC_ISO_DATE);

        for (UserAccountVO account : selectedAccounts) {

            if (userAccountMapper.existsAccount(userId, account.getAccountNumber()) > 0) continue;
            account.setUserId(userId);
            userAccountMapper.insertUserAccount(account);

            // 2. 계좌 PK(ID) 얻기 (insert 후 select or MyBatis selectKey 활용)
            Long accountId = userAccountMapper.findIdByUserIdAndAccountNumber(userId, account.getAccountNumber());

            // 3. 거래내역 조회 및 저장
            List<AccountTransactionVO> txList = fetchAndParseAccountTransactions(
                    account.getBankCode(), connectedId, account.getAccountNumber(), startDate, endDate
            );
            for (AccountTransactionVO tx : txList) {
                System.out.println("AccountTransactionVO 도는중~~~~~~~");
                tx.setAccountId(accountId);
                accountTransactionMapper.insertAccountTransaction(tx);
            }
        }
    }


    // 거래내역 API 호출 및 파싱
    public List<AccountTransactionVO> fetchAndParseAccountTransactions(
            String bankCode, String connectedId, String accountNumber, String startDate, String endDate) {
        List<AccountTransactionVO> txList = new ArrayList<>();
        System.out.println("fetchAndParseAccountTransactions 들어옴");
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

            System.out.println("apiResp = " + apiResp);

            String decodedResp = URLDecoder.decode(apiResp, StandardCharsets.UTF_8);

            System.out.println("decodedResp = " + decodedResp);


            JsonNode root = objectMapper.readTree(decodedResp);
            if (!CodefUtil.isSuccess(decodedResp)) {
                throw new CodefApiException("TRANSACTION_ERROR", "거래내역 조회 실패");
            }
            JsonNode txArr = root.get("data").get("resTrHistoryList");
            if (txArr == null || !txArr.isArray()) return txList;

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

                System.out.println("txtxtxtxtxtxtxtxtxtxtx = " + tx);
                txList.add(tx);
            }

        } catch (Exception e) {
            System.out.println("거래내역 파싱 실패! " + e.getMessage());
        }
        System.out.println("txListtxListtxListtxListtxListtxList = " + txList);
        return txList;
    }


    public List<UserCardVO> connectAndFetchCards(CardConnectRequest request) {
        try {
            String loginId = "admin1";
            Long userId = connectedAccountMapper.findIdByLoginId(loginId);

            // 1. connectedId 조회 or 생성
            ConnectedAccountVO vo = connectedAccountMapper.findConnectedIdByUserId(userId);
            String connectedId = vo.getConnectedId();
            String encryptedPw = RSAUtil.encryptRSA(request.getPassword(), publicKey);

            if (connectedId == null) {
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

                if (!CodefUtil.isSuccess(decodedResponse)) {
                    JsonNode errorNode = objectMapper.readTree(decodedResponse)
                            .get("data").get("errorList").get(0);
                    String errorCode = errorNode.get("code").asText();
                    String errorMessage = errorNode.get("message").asText();
                    throw new CodefApiException(errorCode, errorMessage);
                }

                connectedId = objectMapper.readTree(decodedResponse)
                        .get("data").get("connectedId").asText();

                // DB에 connectedId 저장
                connectedAccountMapper.insertConnectedAccount(userId, connectedId);

            } else {
                // 이미 connectedId가 있으면 카드 계정 추가 (추가 연결)
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

                if (!CodefUtil.isSuccess(decodedAddResponse)) {
                    JsonNode errorNode = objectMapper.readTree(decodedAddResponse)
                            .get("data").get("errorList").get(0);
                    String errorCode = errorNode.get("code").asText();
                    String errorMessage = errorNode.get("message").asText();
                    throw new CodefApiException(errorCode, errorMessage);
                }
            }

            // 3. 카드 목록 조회 (CODEF 카드조회 API 엔드포인트로)
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

            System.out.println("카드 목록 조회: decodedCardListResponse = " + decodedCardListResponse);

            JsonNode root = objectMapper.readTree(decodedCardListResponse);

            if (!CodefUtil.isSuccess(decodedCardListResponse)) {
                throw new RuntimeException("카드 목록 조회 실패: " + CodefUtil.getResultMessage(decodedCardListResponse));
            }

            List<UserCardVO> result = new ArrayList<>();

            JsonNode dataNode = root.get("data");

            // 1. 여러장 카드
            // resCardList 인지는 확인 필요.. 제가 한 은행에 여러장의 카드가 없어요 ㅜㅜ
            JsonNode cardListNode = dataNode.get("resCardList");
            if (cardListNode != null && cardListNode.isArray()) {
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
            return result;


        } catch (Exception e) {
            if (e instanceof CodefApiException) throw (CodefApiException) e;
            throw new CodefApiException("INTERNAL_ERROR", "카드 계정 연결 및 조회 실패");
        }
    }

    @Transactional
    public void registerUserCards(String loginId, List<UserCardVO> selectedCards) {
        Long userId = connectedAccountMapper.findIdByLoginId(loginId);
        ConnectedAccountVO vo = connectedAccountMapper.findConnectedIdByUserId(userId);
        String connectedId = vo.getConnectedId();


        // 오늘 기준 1년 전 ~ 오늘 (yyyyMMdd)
        LocalDate today = LocalDate.now();
        String endDate = today.format(DateTimeFormatter.BASIC_ISO_DATE);
        String startDate = today.minusYears(1).format(DateTimeFormatter.BASIC_ISO_DATE);
        Long categoryId = categoryMapper.findUnclassifiedId();

        for (UserCardVO card : selectedCards) {
            // 1. 이미 등록된 카드면 skip
            if (userCardMapper.existsCard(userId, card.getCardMaskedNumber()) > 0) {
                throw new AlreadyRegisteredCardException(card.getCardMaskedNumber());
            }
            card.setUserId(userId);
            userCardMapper.insertUserCard(card);

            // 2. 카드 PK(ID) 얻기 (insert 후 select or selectKey)
            Long cardId = userCardMapper.findIdByUserIdAndCardNumber(userId, card.getCardMaskedNumber());

            // 3. 거래내역 조회 및 저장
            List<CardTransactionVO> txList = fetchAndParseCardTransactions(
                    card.getIssuerCode(),
                    connectedId,
                    startDate,
                    endDate,
                    card.getCardName(),
                    card.getCardMaskedNumber()
            );

            for (CardTransactionVO tx : txList) {
                tx.setCardId(cardId);
                tx.setCategoryId(categoryId);
                System.out.println("카드 소비 내역 저장중~~~~~~~");
                cardTransactionMapper.insertCardTransaction(tx);
            }

            // 4. GPT 태그 비동기 분류
//            classifyCardTransactionsAsync(cardId);
        }
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

            String apiResp = sendPost(url, reqBody);
            String decodedResp = URLDecoder.decode(apiResp, StandardCharsets.UTF_8);

            System.out.println("카드 승인 내역" + decodedResp);

            JsonNode root = objectMapper.readTree(decodedResp);
            if (!CodefUtil.isSuccess(decodedResp)) {
                throw new CodefApiException("CARD_TRANSACTION_ERROR", "카드 승인내역 조회 실패");
            }

            JsonNode txArr = root.get("data");
            if (txArr == null || !txArr.isArray()) return txList;

            for (JsonNode node : txArr) {
                // 2. 거래 일시 파싱 (yyyyMMdd + HHmmss → yyyy-MM-dd HH:mm:ss)
                String usedDate = node.path("resUsedDate").asText("");
                String usedTime = node.path("resUsedTime").asText("");

                String dateTimeStr = usedDate + usedTime;
                LocalDateTime txDateTime;
                try {
                    txDateTime = LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
                } catch (Exception e) {
                    txDateTime = null;
                }

                // 3. 금액 파싱 (null/빈값 방지)
                long amount = 0L;
                try {
                    amount = Long.parseLong(node.path("resUsedAmount").asText("0"));
                } catch (Exception e) {
                    amount = 0L;
                }

                // 파싱 전 String을 변수에 받고
                String installmentMonthStr = node.path("resInstallmentMonth").asText("");
                Integer installmentMonth = null;
                if (!installmentMonthStr.isEmpty()) {
                    try {
                        installmentMonth = Integer.valueOf(installmentMonthStr);
                    } catch (Exception e) {
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
                txList.add(tx);
            }
        } catch (Exception e) {
//            log.error("[카드 승인내역 파싱 실패]", e);
            System.out.println("카드 승인내역 파싱 실패 " + e.getMessage());
        }
        return txList;
    }

//    @Async
//    public void classifyCardTransactionsAsync(Long cardId) {
//        List<CardTransactionVO> txList = cardTransactionMapper.findUnclassifiedByCardId(cardId);
//        for (CardTransactionVO tx : txList) {
//            try {
//                String prompt = makePrompt(tx);
//                GptCategoryResponse resp = gptApiClient.classifyTransaction(prompt);
//                Long categoryId = categoryMapper.findIdByCode(resp.getCategoryCode());
//                if (categoryId == null) categoryId = categoryMapper.findIdByCode("other");
//                cardTransactionMapper.updateCategory(tx.getId(), categoryId);
//            } catch (Exception e) {
//                log.error("카테고리 분류 실패: txId={}", tx.getId(), e);
//            }
//        }
//    }

    private String makePrompt(CardTransactionVO tx) {
        return String.format(
                "아래 카드 거래내역을 한 카테고리로 분류해줘.\n가맹점명: %s\n가맹점명2: %s\n업종: %s\n카테고리 후보: [식비, 쇼핑, 교통, 주거/공과금, 건강/의료, 취미/여가, 교육/자기계발, 선물/경조사, 기타]\n카테고리명(영문코드: food/shopping/transport/housing/health/leisure/education/event/other)만 딱 응답해줘.",
                tx.getStoreName(), tx.getStoreName1(), tx.getStoreType()
        );
    }






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
            System.out.println("조회 응답 response2 = " + response2);
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
            System.out.println("삭제 응답 response2 = " + response2);
            return response2;
        } catch (Exception e) {
            throw new RuntimeException("계정 삭제 실패: " + e.getMessage());
        }
    }

}
