package org.scoula.codef.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
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
import org.scoula.security.account.domain.CustomUser;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/codef")
@Api(tags = "CODEF 연동 API", description = "CODEF 계좌·카드 초기 등록 및 거래내역 추가 컨트롤러")
public class CodefController {

    private final CodefService codefService;

    // 1. 계좌목록만 프론트로 전달
    @ApiOperation(value = "CODEF 계좌연동/목록조회", notes = "CODEF 계좌 연결 및 계좌목록 반환")
    @PostMapping("/connect-account")
    public ResponseEntity<List<UserAccountVO>> connectAndFetchAccounts(
            @ApiIgnore @AuthenticationPrincipal CustomUser customUser,
            @RequestBody AccountConnectRequest request) {
        Long userId = customUser.getMember().getUserId();
        List<UserAccountVO> accounts = codefService.connectAndFetchAccounts(userId, request);
        return ResponseEntity.ok(accounts);
    }

    // 2. 프론트에서 선택한 계좌 등록 후 데이터 DB 추가
    @ApiOperation(value = "계좌등록 & 거래내역 저장", notes = "계좌목록에서 사용자가 선택한 계좌에 대한 데이터 DB 추가")
    @PostMapping("/register-accounts")
    public ResponseEntity<Void> registerAccounts(
            @ApiIgnore @AuthenticationPrincipal CustomUser customUser,
            @RequestBody List<UserAccountVO> selectedAccounts) {
        Long userId = customUser.getMember().getUserId();
        codefService.registerUserAccounts(userId, selectedAccounts);
        return ResponseEntity.ok().build();
    }

    // 1. 카드목록만 프론트로 전달
    @ApiOperation(value = "CODEF 카드연동/목록조회", notes = "CODEF 카드 계정 연결 및 카드 목록 반환")
    @PostMapping("/connect-card")
    public ResponseEntity<List<UserCardVO>> connectAndFetchCards(
            @ApiIgnore @AuthenticationPrincipal CustomUser customUser,
            @RequestBody CardConnectRequest request) {
        Long userId = customUser.getMember().getUserId();
        List<UserCardVO> cards = codefService.connectAndFetchCards(userId, request);
        return ResponseEntity.ok(cards);
    }

    // 2. 카드 선택 후 등록 후 데이터 DB 추가
    @ApiOperation(value = "카드등록 & 거래내역 저장", notes = "카드목록에서 사용자가 선택한 카드에 대한 데이터 DB 추가")
    @PostMapping("/register-cards")
    public ResponseEntity<Void> registerCards(
            @ApiIgnore @AuthenticationPrincipal CustomUser customUser,
            @RequestBody List<UserCardVO> selectedCards) {
        Long userId = customUser.getMember().getUserId();
        codefService.registerUserCards(userId, selectedCards);
        return ResponseEntity.ok().build();
    }












    /*
    * test용 API
    * */
//    @PostMapping("/account-list")
//    public ResponseEntity<String> getAccountList() {
//        Long userId = 13L;
//        String result = codefService.fetchAccountListByConnectedId(userId);
//        return ResponseEntity.ok(result);
//    }
//
//    @PostMapping("/delete-accounts")
//    public ResponseEntity<String> deleteAccounts(@RequestBody Map<String, Object> body) {
//        String resultJson = codefService.deleteAccountsRaw(body);
//        return ResponseEntity.ok()
//                .header("Content-Type", "application/json")
//                .body(resultJson);
//    }


}
