package org.scoula.asset.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.scoula.asset.domain.AccountSummaryVO;
import org.scoula.asset.domain.AccountTransactionVO;
import org.scoula.asset.domain.CardSummaryVO;
import org.scoula.asset.domain.CardTransactionVO;
import org.scoula.asset.dto.*;
import org.scoula.asset.service.AssetService;
import org.scoula.common.dto.PageResponse;
import org.scoula.push.dto.response.NotificationResponse;
import org.scoula.security.account.domain.CustomUser;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;
import java.util.Map;

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
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String txType
            ) {
        Long userId = customUser.getMember().getUserId();
        PageResponse<CardTransactionVO> resp = assetService.getCardTransactions(userId, cardId, page, size, txType);
        return ResponseEntity.ok(resp);
    }


    @ApiOperation(value = "카드 거래 메모 수정", notes = "특정 카드 거래내역의 메모를 수정합니다.")
    @PostMapping("/cards/{transactionId}/memo")
    public ResponseEntity<String> updateCardTransactionMemo(
            @AuthenticationPrincipal CustomUser customUser,
            @PathVariable Long transactionId,
            @RequestBody MemoRequest request
    ) {
        assetService.updateCardTransactionMemo(transactionId, request.getMemo());
        String updatedMemo = request.getMemo();
        return ResponseEntity.ok(updatedMemo);
    }

    @ApiOperation(value = "계좌 거래 메모 수정", notes = "특정 계좌 거래내역의 메모를 수정합니다.")
    @PostMapping("/accounts/{transactionId}/memo")
    public ResponseEntity<String> updateAccountTransactionMemo(
            @AuthenticationPrincipal CustomUser customUser,
            @PathVariable Long transactionId,
            @RequestBody MemoRequest request
    ) {
        assetService.updateAccountTransactionMemo(transactionId, request.getMemo());
        String updatedMemo = request.getMemo();
        return ResponseEntity.ok(updatedMemo);
    }


    @ApiOperation(value = "월간 지출 개요", notes = "선택 월의 총지출, 전월 대비 증감, 카테고리 합계, 최근 6개월 추세를 한 번에 제공합니다.")
    @GetMapping("/spending/overview")
    public ResponseEntity<SpendingOverviewDTO> getSpendingOverview(
            @ApiIgnore @AuthenticationPrincipal CustomUser customUser,
            @RequestParam int year,
            @RequestParam int month,
            @RequestParam(defaultValue = "6") int trendMonths
    ) {
        Long userId = customUser.getMember().getUserId();
        SpendingOverviewDTO dto = assetService.getSpendingOverview(userId, year, month, trendMonths);
        return ResponseEntity.ok(dto);
    }


    @ApiOperation(value = "카테고리별 거래내역(월별)", notes = "특정 카테고리의 해당 월 카드 거래내역을 반환합니다.")
    @GetMapping("/spending/category/{categoryId}")
    public ResponseEntity<List<CardTransactionVO>> getCategoryTransactions(
            @ApiIgnore @AuthenticationPrincipal CustomUser customUser,
            @PathVariable Long categoryId,
            @RequestParam int year,
            @RequestParam int month) {
        Long userId = customUser.getMember().getUserId();
        List<CardTransactionVO> txs = assetService.getCategoryTransactions(userId, categoryId, year, month);
        return ResponseEntity.ok(txs);
    }


    @ApiOperation(value = "거래 카테고리 수정", notes = "단일 거래의 카테고리를 업데이트합니다.")
    @PatchMapping("/transactions/{transactionId}/category")
    public ResponseEntity<Void> updateTransactionCategory(
            @ApiIgnore @AuthenticationPrincipal CustomUser customUser,
            @PathVariable Long transactionId,
            @RequestBody CategoryUpdateRequest request) {
        assetService.updateTransactionCategory(transactionId, request.getCategoryId());
        return ResponseEntity.ok().build();
    }


    @ApiOperation(value = "최근 6개월 후불교통대금 거래내역 조회", notes = "로그인한 사용자의 모든 카드 거래내역 중 최근 6개월간 후불교통대금 거래내역을 리스트로 반환합니다.")
    @GetMapping("/cards/transportation-fees")
    public ResponseEntity<List<CardTransactionVO>> getTransportationFees(
            @ApiIgnore @AuthenticationPrincipal CustomUser customUser) {
        Long userId = customUser.getMember().getUserId();
        List<CardTransactionVO> fees = assetService.getTransportationFees(userId);
        return ResponseEntity.ok(fees);
    }

    @ApiOperation(value = "월세 거래내역 존재 여부", notes = "사용자의 전체 계좌 거래내역 중 memo가 '월세'인 거래가 있는지 여부를 반환합니다.")
    @GetMapping("/accounts/rent-exists")
    public ResponseEntity<Boolean> existsRentTransaction(
            @ApiIgnore @AuthenticationPrincipal CustomUser customUser) {
        Long userId = customUser.getMember().getUserId();
        boolean exists = assetService.existsRentTransaction(userId);
        return ResponseEntity.ok(exists);
    }

    @ApiOperation(value = "한국산업인력공단 카드 결제내역 존재 여부", notes = "카드 결제내역 중 store_name이 '한국산업인력공단'인 거래가 있는지 여부를 반환합니다.")
    @GetMapping("/cards/hrdkorea-exists")
    public ResponseEntity<Boolean> existsHrdKoreaCardTransaction(
            @ApiIgnore @AuthenticationPrincipal CustomUser customUser) {
        Long userId = customUser.getMember().getUserId();
        boolean exists = assetService.existsHrdKoreaCardTransaction(userId);
        return ResponseEntity.ok(exists);
    }

}
