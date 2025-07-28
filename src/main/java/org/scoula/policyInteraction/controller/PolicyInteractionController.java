package org.scoula.policyInteraction.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scoula.policyInteraction.domain.UserPolicyApplicationVO;
import org.scoula.policyInteraction.domain.YouthPolicyBookmarkVO;
import org.scoula.policyInteraction.service.PolicyInteractionService;
import org.scoula.security.account.domain.CustomUser;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/policy-interaction")
@RequiredArgsConstructor
@Api(tags = "정책 상호작용 API")
public class PolicyInteractionController {

    private final PolicyInteractionService policyInteractionService;

    // ────────────────────────────────────────
    // 📌 북마크 관련 API
    // ────────────────────────────────────────

    @PostMapping("/bookmark")
    @ApiOperation(value = "정책 북마크 추가", notes = "인증된 사용자가 정책을 북마크에 추가합니다")
    public ResponseEntity<Void> addBookmark(
            @AuthenticationPrincipal CustomUser customUser,
            @RequestParam Long policyId) {
        
        Long userId = customUser.getMember().getUserId();
        
        boolean success = policyInteractionService.addBookmark(userId, policyId);
        return success ?
                ResponseEntity.ok().build() :
                ResponseEntity.badRequest().build();
    }

    @DeleteMapping("/bookmark")
    @ApiOperation(value = "정책 북마크 삭제", notes = "인증된 사용자가 정책 북마크를 삭제합니다")
    public ResponseEntity<Void> removeBookmark(
            @AuthenticationPrincipal CustomUser customUser,
            @RequestParam Long policyId) {
        
        Long userId = customUser.getMember().getUserId();
        boolean success = policyInteractionService.removeBookmark(userId, policyId);
        
        return success ?
                ResponseEntity.ok().build() :
                ResponseEntity.notFound().build();
    }

    @GetMapping("/bookmark/check")
    @ApiOperation(value = "북마크 여부 확인", notes = "인증된 사용자의 특정 정책 북마크 여부를 확인합니다")
    public ResponseEntity<Boolean> checkBookmark(
            @AuthenticationPrincipal CustomUser customUser,
            @RequestParam Long policyId) {
        
        Long userId = customUser.getMember().getUserId();
        boolean isBookmarked = policyInteractionService.isBookmarked(userId, policyId);
        return ResponseEntity.ok(isBookmarked);
    }

    @GetMapping("/bookmark/list")
    @ApiOperation(value = "사용자 북마크 목록 조회", notes = "인증된 사용자의 모든 북마크 목록을 조회합니다")
    public ResponseEntity<List<YouthPolicyBookmarkVO>> getUserBookmarks(
            @AuthenticationPrincipal CustomUser customUser) {
        
        Long userId = customUser.getMember().getUserId();
        List<YouthPolicyBookmarkVO> bookmarks = policyInteractionService.getUserBookmarks(userId);
        return ResponseEntity.ok(bookmarks);
    }

    // ────────────────────────────────────────
    // 📌 신청 관련 API
    // ────────────────────────────────────────

    @PostMapping("/application")
    @ApiOperation(value = "정책 신청 등록", notes = "인증된 사용자가 정책에 신청합니다")
    public ResponseEntity<Void> addApplication(
            @AuthenticationPrincipal CustomUser customUser,
            @RequestParam Long policyId) {
        
        Long userId = customUser.getMember().getUserId();
        
        boolean success = policyInteractionService.addApplication(userId, policyId);
        return success ?
                ResponseEntity.ok().build() :
                ResponseEntity.badRequest().build();
    }

    @GetMapping("/application/check")
    @ApiOperation(value = "신청 여부 확인", notes = "인증된 사용자의 특정 정책 신청 여부를 확인합니다")
    public ResponseEntity<Boolean> checkApplication(
            @AuthenticationPrincipal CustomUser customUser,
            @RequestParam Long policyId) {
        
        Long userId = customUser.getMember().getUserId();
        boolean isApplied = policyInteractionService.isApplied(userId, policyId);
        return ResponseEntity.ok(isApplied);
    }

    @GetMapping("/application/list")
    @ApiOperation(value = "사용자 신청 목록 조회", notes = "인증된 사용자의 모든 신청 목록을 조회합니다")
    public ResponseEntity<List<UserPolicyApplicationVO>> getUserApplications(
            @AuthenticationPrincipal CustomUser customUser) {
        
        Long userId = customUser.getMember().getUserId();
        List<UserPolicyApplicationVO> applications = policyInteractionService.getUserApplications(userId);
        return ResponseEntity.ok(applications);
    }
}