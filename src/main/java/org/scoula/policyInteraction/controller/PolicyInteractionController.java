package org.scoula.policyInteraction.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scoula.policyInteraction.domain.UserPolicyApplicationVO;
import org.scoula.policyInteraction.domain.YouthPolicyBookmarkVO;
import org.scoula.policyInteraction.dto.ApplicationWithPolicyDTO;
import org.scoula.policyInteraction.dto.BookmarkWithPolicyDTO;
import org.scoula.policyInteraction.service.PolicyInteractionService;
import org.scoula.security.account.domain.CustomUser;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/policy-interaction")
@RequiredArgsConstructor
@Api(tags = "유저 정책 북마크 및 신청 API", description = "유저가 정책 북마크 및 신청 CRUD API")
public class PolicyInteractionController {

    private final PolicyInteractionService policyInteractionService;

    // ────────────────────────────────────────
    // 📌 북마크 관련 API
    // ────────────────────────────────────────

    @PostMapping("/bookmark/{policyId}")
    @ApiOperation(value = "정책 북마크 추가", notes = "특정 정책을 북마크에 저장합니다. 성공 시 200 OK, 중복/오류 시 400 Bad Request를 반환합니다.")
    public ResponseEntity<Void> addBookmark(
            @ApiIgnore @AuthenticationPrincipal CustomUser customUser,
            @PathVariable Long policyId) {
        
        Long userId = customUser.getMember().getUserId();
        
        boolean success = policyInteractionService.addBookmark(userId, policyId);
        return success ?
                ResponseEntity.ok().build() :
                ResponseEntity.badRequest().build();
    }

    @DeleteMapping("/bookmark/{policyId}")
    @ApiOperation(value = "정책 북마크 삭제", notes = "북마크된 정책을 삭제합니다. 성공 시 200 OK, 존재하지 않는 북마크인 경우 404 Not Found를 반환합니다.")
    public ResponseEntity<Void> removeBookmark(
            @ApiIgnore @AuthenticationPrincipal CustomUser customUser,
            @PathVariable Long policyId) {
        
        Long userId = customUser.getMember().getUserId();
        boolean success = policyInteractionService.removeBookmark(userId, policyId);
        
        return success ?
                ResponseEntity.ok().build() :
                ResponseEntity.notFound().build();
    }

    @GetMapping("/bookmark/list")
    @ApiOperation(value = "사용자 북마크 목록 조회", notes = "현재 사용자가 북마크한 모든 정책 목록을 정책 정보와 함께 조회합니다. 북마크가 없는 경우 빈 배열을 반환합니다.")
    public ResponseEntity<List<BookmarkWithPolicyDTO>> getUserBookmarks(
            @ApiIgnore @AuthenticationPrincipal CustomUser customUser) {
        
        Long userId = customUser.getMember().getUserId();
        List<BookmarkWithPolicyDTO> bookmarks = policyInteractionService.getUserBookmarks(userId);
        return ResponseEntity.ok(bookmarks);
    }

    // ────────────────────────────────────────
    // 📌 신청 관련 API
    // ────────────────────────────────────────

    @PostMapping("/application/{policyId}")
    @ApiOperation(value = "정책 신청 등록", notes = "특정 정책에 신청 기록을 남깁니다. 성공 시 200 OK, 중복 신청/오류 시 400 Bad Request를 반환합니다.")
    public ResponseEntity<Void> addApplication(
            @ApiIgnore @AuthenticationPrincipal CustomUser customUser,
            @PathVariable Long policyId) {
        
        Long userId = customUser.getMember().getUserId();
        
        boolean success = policyInteractionService.addApplication(userId, policyId);
        return success ?
                ResponseEntity.ok().build() :
                ResponseEntity.badRequest().build();
    }

    @GetMapping("/application/list")
    @ApiOperation(value = "사용자 신청 목록 조회", notes = "현재 사용자가 신청한 모든 정책 목록을 정책 정보와 함께 조회합니다. 신청 기록이 없는 경우 빈 배열을 반환합니다.")
    public ResponseEntity<List<ApplicationWithPolicyDTO>> getUserApplications(
            @ApiIgnore @AuthenticationPrincipal CustomUser customUser) {
        
        Long userId = customUser.getMember().getUserId();
        List<ApplicationWithPolicyDTO> applications = policyInteractionService.getUserApplications(userId);
        return ResponseEntity.ok(applications);
    }

    @PutMapping("/application/{policyId}/complete")
    @ApiOperation(value = "정책 신청 완료 처리", notes = "신청 등록된 정책을 실제 신청 완료 상태로 변경합니다. 성공 시 200 OK, 신청 기록이 없거나 이미 완료된 경우 400 Bad Request를 반환합니다.")
    public ResponseEntity<Void> completeApplication(
            @ApiIgnore @AuthenticationPrincipal CustomUser customUser,
            @PathVariable Long policyId) {
        
        Long userId = customUser.getMember().getUserId();
        
        boolean success = policyInteractionService.completeApplication(userId, policyId);
        return success ?
                ResponseEntity.ok().build() :
                ResponseEntity.badRequest().build();
    }

    @DeleteMapping("/application/{policyId}")
    @ApiOperation(value = "정책 신청 기록 삭제", notes = "신청 등록된 정책 기록을 삭제합니다. 성공 시 200 OK, 신청 기록이 없는 경우 404 Not Found를 반환합니다.")
    public ResponseEntity<Void> removeApplication(
            @ApiIgnore @AuthenticationPrincipal CustomUser customUser,
            @PathVariable Long policyId) {
        
        Long userId = customUser.getMember().getUserId();
        
        boolean success = policyInteractionService.removeApplication(userId, policyId);
        return success ?
                ResponseEntity.ok().build() :
                ResponseEntity.notFound().build();
    }

    @GetMapping("/application/incomplete")
    @ApiOperation(value = "미완료 신청 정책 조회", notes = "사용자의 미완료 신청 정책(is_applied=false) 중 하나를 조회합니다. 미완료 신청이 없는 경우 404 Not Found를 반환합니다.")
    public ResponseEntity<ApplicationWithPolicyDTO> getIncompleteApplication(
            @ApiIgnore @AuthenticationPrincipal CustomUser customUser) {
        
        Long userId = customUser.getMember().getUserId();
        ApplicationWithPolicyDTO application = policyInteractionService.getIncompleteApplication(userId);
        
        return application != null ?
                ResponseEntity.ok(application) :
                ResponseEntity.notFound().build();
    }
}