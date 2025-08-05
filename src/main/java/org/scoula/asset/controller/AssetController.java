package org.scoula.asset.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.scoula.asset.domain.AccountSummaryVO;
import org.scoula.asset.domain.AccountTransactionVO;
import org.scoula.asset.domain.CardSummaryVO;
import org.scoula.asset.domain.CardTransactionVO;
import org.scoula.asset.dto.AssetSummaryResponse;
import org.scoula.asset.service.AssetService;
import org.scoula.common.dto.PageResponse;
import org.scoula.push.dto.response.NotificationResponse;
import org.scoula.security.account.domain.CustomUser;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;

@RestController
@RequestMapping("/api/asset")
@RequiredArgsConstructor
@Api(tags = "자산 관련 API", description = "계좌, 카드, 자산 요약 등 자산 관련 API 제공")
public class AssetController {

    private final AssetService assetService;


    @ApiOperation(value = "자산 요약 조회", notes = "내 자산(계좌 합), 이번달 카드 사용금액, 계좌/카드 요약을 조회합니다.")
    @GetMapping("/summary")
    public ResponseEntity<AssetSummaryResponse> getSummary(
                @ApiIgnore @AuthenticationPrincipal CustomUser customUser) {
            Long userId = customUser.getMember().getUserId();
        AssetSummaryResponse summary = assetService.getSummary(userId);
        return ResponseEntity.ok(summary);
    }

    // 계좌 전체 목록
    @ApiOperation(value = "전체 계좌 목록", notes = "사용자의 전체 계좌 리스트를 반환합니다.")
    @GetMapping("/accounts")
    public ResponseEntity<List<AccountSummaryVO>> getAccounts(
            @ApiIgnore @AuthenticationPrincipal CustomUser customUser) {
        System.out.println("customUser = " + customUser);
        Long userId = customUser.getMember().getUserId();
        return ResponseEntity.ok(assetService.getAccounts(userId));
    }

    // 카드 전체 목록
    @ApiOperation(value = "전체 카드 목록", notes = "사용자의 전체 카드 리스트를 반환합니다.")
    @GetMapping("/cards")
    public ResponseEntity<List<CardSummaryVO>> getCards(
            @ApiIgnore @AuthenticationPrincipal CustomUser customUser) {
        Long userId = customUser.getMember().getUserId();
        List<CardSummaryVO> cards = assetService.getCards(userId);
        return ResponseEntity.ok(cards);
    }

    // 계좌 거래내역 조회(페이징 20개씩)
    @ApiOperation(value = "계좌 거래내역 페이징 조회(최근순)", notes = "특정 계좌의 거래내역을 페이지별로 조회합니다. page(0부터), size 지정 가능(default:20)")
    @GetMapping("/accounts/{accountId}/transactions")
    public ResponseEntity<PageResponse<AccountTransactionVO>> getAccountTransactions(
            @ApiIgnore @AuthenticationPrincipal CustomUser customUser,
            @PathVariable Long accountId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String txType
            ) {
        Long userId = customUser.getMember().getUserId();
        PageResponse<AccountTransactionVO> resp = assetService.getAccountTransactions(userId, accountId, page, size, txType);
        return ResponseEntity.ok(resp);
    }

    // 카드 거래내역 조회(페이징 20개씩)
    @ApiOperation(value = "카드 거래내역 페이징 조회(최근순)", notes = "특정 카드의 거래내역을 페이지별로 조회합니다. page(0부터), size 지정 가능(default:20)")
    @GetMapping("/cards/{cardId}/transactions")
    public ResponseEntity<PageResponse<CardTransactionVO>> getCardTransactions(
            @ApiIgnore @AuthenticationPrincipal CustomUser customUser,
            @PathVariable Long cardId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
            ) {
        Long userId = customUser.getMember().getUserId();
        PageResponse<CardTransactionVO> resp = assetService.getCardTransactions(userId, cardId, page, size);
        return ResponseEntity.ok(resp);
    }

}
