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
@Api(tags = "유저 정책 북마크 및 신청 API")
public class PolicyInteractionController {

    private final PolicyInteractionService policyInteractionService;

    // ────────────────────────────────────────
    // 📌 북마크 관련 API
    // ────────────────────────────────────────

    @PostMapping("/bookmark")
    @ApiOperation(value = "정책 북마크 추가", notes = "특정 정책을 북마크에 저장합니다. 성공 시 200 OK, 중복/오류 시 400 Bad Request를 반환합니다.")
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
    @ApiOperation(value = "정책 북마크 삭제", notes = "북마크된 정책을 삭제합니다. 성공 시 200 OK, 존재하지 않는 북마크인 경우 404 Not Found를 반환합니다.")
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
    @ApiOperation(value = "북마크 여부 확인", notes = "특정 정책이 북마크되어 있는지 확인합니다. 북마크된 경우 true, 아닌 경우 false를 반환합니다.")
    public ResponseEntity<Boolean> checkBookmark(
            @AuthenticationPrincipal CustomUser customUser,
            @RequestParam Long policyId) {
        
        Long userId = customUser.getMember().getUserId();
        boolean isBookmarked = policyInteractionService.isBookmarked(userId, policyId);
        return ResponseEntity.ok(isBookmarked);
    }

    @GetMapping("/bookmark/list")
    @ApiOperation(value = "사용자 북마크 목록 조회", notes = "현재 사용자가 북마크한 모든 정책 목록을 조회합니다. 북마크가 없는 경우 빈 배열을 반환합니다.")
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
    @ApiOperation(value = "정책 신청 등록", notes = "특정 정책에 신청 기록을 남깁니다. 성공 시 200 OK, 중복 신청/오류 시 400 Bad Request를 반환합니다.")
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
    @ApiOperation(value = "신청 여부 확인", notes = "특정 정책에 신청했는지 확인합니다. 신청한 경우 true, 아닌 경우 false를 반환합니다.")
    public ResponseEntity<Boolean> checkApplication(
            @AuthenticationPrincipal CustomUser customUser,
            @RequestParam Long policyId) {
        
        Long userId = customUser.getMember().getUserId();
        boolean isApplied = policyInteractionService.isApplied(userId, policyId);
        return ResponseEntity.ok(isApplied);
    }

    @GetMapping("/application/list")
    @ApiOperation(value = "사용자 신청 목록 조회", notes = "현재 사용자가 신청한 모든 정책 목록을 조회합니다. 신청 기록이 없는 경우 빈 배열을 반환합니다.")
    public ResponseEntity<List<UserPolicyApplicationVO>> getUserApplications(
            @AuthenticationPrincipal CustomUser customUser) {
        
        Long userId = customUser.getMember().getUserId();
        List<UserPolicyApplicationVO> applications = policyInteractionService.getUserApplications(userId);
        return ResponseEntity.ok(applications);
    }
}