package org.scoula.codef.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.scoula.asset.dto.AssetSummaryResponse;
import org.scoula.asset.service.AssetService;
import org.scoula.codef.service.SyncService;
import org.scoula.security.account.domain.CustomUser;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

@RestController
@RequestMapping("api/codef/sync")
@RequiredArgsConstructor
@Api(tags = "CODEF 동기화 API", description = "CODEF 계좌·카드 거래내역 동기화 컨트롤러")
public class SyncController {

    private final SyncService syncService;
    private final AssetService assetService;

    // 1. 계좌 전체 동기화
    @ApiOperation(
            value = "사용자 계좌 동기화",
            notes = "현재 로그인된 사용자의 모든 계좌 거래내역을 CODEF에서 최신화합니다. (비동기 동작, 202 Accepted 반환)"
    )
    @PostMapping("/accounts")
    public ResponseEntity<AssetSummaryResponse> syncAccounts(
            @ApiIgnore @AuthenticationPrincipal CustomUser customUser) {
        Long userId = customUser.getMember().getUserId();
        syncService.syncAccountsAsync(userId);
        return ResponseEntity.ok(assetService.getSummary(userId));
    }

    // 2. 카드 전체 동기화
    @ApiOperation(
            value = "사용자 카드 동기화",
            notes = "현재 로그인된 사용자의 모든 카드 승인내역을 CODEF에서 최신화합니다. (비동기 동작, 202 Accepted 반환)"
    )
    @PostMapping("/cards")
    public ResponseEntity<AssetSummaryResponse> syncCards(
            @ApiIgnore @AuthenticationPrincipal CustomUser customUser) {
        Long userId = customUser.getMember().getUserId();
        syncService.syncCardsAsync(userId);
        return ResponseEntity.ok(assetService.getSummary(userId));
    }
}

