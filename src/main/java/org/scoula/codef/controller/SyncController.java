package org.scoula.codef.controller;

import lombok.RequiredArgsConstructor;
import org.scoula.codef.service.SyncService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/codef/sync")
@RequiredArgsConstructor
public class SyncController {

    private final SyncService syncService;

    // 1. 계좌 전체 동기화
    @PostMapping("/accounts")
    public ResponseEntity<Void> syncAccounts() {
        String loginId = "hong1";
        syncService.syncAccountsAsync(loginId);
        return ResponseEntity.accepted().build(); // 202 응답, 프론트는 로딩중!
    }

    // 2. 카드 전체 동기화
    @PostMapping("/cards")
    public ResponseEntity<Void> syncCards() {
        String loginId = "hong1";
        syncService.syncCardsAsync(loginId);
        return ResponseEntity.accepted().build();
    }
}

