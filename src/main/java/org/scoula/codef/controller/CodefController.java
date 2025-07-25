package org.scoula.codef.controller;

import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.scoula.codef.domain.ConnectedAccountVO;
import org.scoula.codef.domain.UserAccountVO;
import org.scoula.codef.domain.UserCardVO;
import org.scoula.codef.dto.AccountConnectRequest;
import org.scoula.codef.dto.CardConnectRequest;
import org.scoula.codef.dto.ErrorResponse;
import org.scoula.codef.mapper.ConnectedAccountMapper;
import org.scoula.codef.service.CodefService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/codef")
public class CodefController {

    private final CodefService codefService;

    // 1. 계좌목록만 프론트로 전달
    @PostMapping("/connect-account")
    public ResponseEntity<List<UserAccountVO>> connectAndFetchAccounts(@RequestBody AccountConnectRequest request) {
        List<UserAccountVO> accounts = codefService.connectAndFetchAccounts(request);
        return ResponseEntity.ok(accounts);
    }

    // 2. 프론트에서 선택한 계좌 등록 후 데이터 DB 추가
    @PostMapping("/register-accounts")
    public ResponseEntity<Void> registerAccounts(@RequestBody List<UserAccountVO> selectedAccounts) {
        String loginId = "admin1";
        codefService.registerUserAccounts(loginId, selectedAccounts);
        return ResponseEntity.ok().build();
    }

    // 1. 카드목록만 프론트로 전달
    @PostMapping("/connect-card")
    public ResponseEntity<List<UserCardVO>> connectAndFetchCards(@RequestBody CardConnectRequest request) {
        List<UserCardVO> cards = codefService.connectAndFetchCards(request);
        return ResponseEntity.ok(cards);
    }

    // 2. 카드 선택 후 등록 후 데이터 DB 추가
    @PostMapping("/register-cards")
    public ResponseEntity<Void> registerCards(@RequestBody List<UserCardVO> selectedCards) {
        String loginId = "admin1"; // ★ 실제 구현에선 세션/토큰에서 꺼내기!
        codefService.registerUserCards(loginId, selectedCards);
        return ResponseEntity.ok().build();
    }












    /*
    * test용 API
    * */
    @PostMapping("/account-list")
    public ResponseEntity<String> getAccountList(@RequestParam String connectedId) {
        String result = codefService.fetchAccountListByConnectedId(connectedId);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/delete-accounts")
    public ResponseEntity<String> deleteAccounts(@RequestBody Map<String, Object> body) {
        String resultJson = codefService.deleteAccountsRaw(body);
        return ResponseEntity.ok()
                .header("Content-Type", "application/json")
                .body(resultJson);
    }


}
