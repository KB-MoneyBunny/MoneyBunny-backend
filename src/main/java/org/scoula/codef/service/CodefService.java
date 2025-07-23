package org.scoula.codef.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.mybatis.spring.annotation.MapperScan;
import org.scoula.codef.common.exception.CodefApiException;
import org.scoula.codef.domain.AccountTransactionVO;
import org.scoula.codef.domain.UserAccountVO;
import org.scoula.codef.dto.AccountConnectRequest;
import org.scoula.codef.mapper.AccountTransactionMapper;
import org.scoula.codef.mapper.ConnectedAccountMapper;
import org.scoula.codef.mapper.UserAccountMapper;
import org.scoula.codef.util.CodefUtil;
import org.scoula.codef.util.RSAUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

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



    public List<UserAccountVO> connectAndFetchAccounts(AccountConnectRequest request){
        try {

            String loginId = "admin1";

            Long userId = connectedAccountMapper.findIdByLoginId(loginId);

            System.out.println("userId = " + userId);

            // 1. connected_accounts 테이블에서 connectedId 먼저 조회
            String connectedId = connectedAccountMapper.findConnectedIdByUserId(userId);

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
                System.out.println("Service에서 CodefApiException 던짐! " + e.getMessage());
                throw (CodefApiException) e;
            }
//            throw new RuntimeException("계좌 연결 및 조회 실패", e);
            System.out.println("Service에서 INTERNAL_ERROR로 던짐! " + e.getMessage());
            throw new CodefApiException("INTERNAL_ERROR", "계좌 연결 및 조회 실패");
        }
    }

    public void registerUserAccounts(String loginId, List<UserAccountVO> selectedAccounts) {
        Long userId = connectedAccountMapper.findIdByLoginId(loginId);
        String connectedId = connectedAccountMapper.findConnectedIdByUserId(userId);

        // 오늘 기준 1년 전 ~ 오늘 (yyyyMMdd)
        LocalDate today = LocalDate.now();
        String endDate = today.format(DateTimeFormatter.BASIC_ISO_DATE);
        String startDate = today.minusYears(1).format(DateTimeFormatter.BASIC_ISO_DATE);

        for (UserAccountVO account : selectedAccounts) {
            if (userAccountMapper.existsAccount(userId, account.getAccountNumber()) > 0 ) continue;
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

}
