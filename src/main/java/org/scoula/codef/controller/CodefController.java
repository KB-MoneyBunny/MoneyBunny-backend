package org.scoula.codef.controller;

import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.scoula.codef.domain.UserAccountVO;
import org.scoula.codef.dto.AccountConnectRequest;
import org.scoula.codef.dto.ErrorResponse;
import org.scoula.codef.service.CodefService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/codef")
public class CodefController {

    private final CodefService codefService;


    // 1. 계좌목록만 프론트로 전달 (DB 저장 X)
    @PostMapping("/connect-account")
    public ResponseEntity<List<UserAccountVO>> connectAndFetchAccounts(@RequestBody AccountConnectRequest request) {
        List<UserAccountVO> accounts = codefService.connectAndFetchAccounts(request);
        return ResponseEntity.ok(accounts);
    }

    // 2. 프론트에서 선택한 계좌 등록 (DB 저장 O)
    @PostMapping("/register-accounts")
    public ResponseEntity<Void> registerAccounts(@RequestBody List<UserAccountVO> selectedAccounts) {
        String loginId = "admin1";
        codefService.registerUserAccounts(loginId, selectedAccounts);
        return ResponseEntity.ok().build();  // 등록 성공만 반환
    }
}
