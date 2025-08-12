package org.scoula.policyInteraction.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scoula.policyInteraction.domain.UserPolicyReviewVO;
import org.scoula.policyInteraction.dto.response.ApplicationWithPolicyDTO;
import org.scoula.policyInteraction.dto.response.BookmarkWithPolicyDTO;
import org.scoula.policyInteraction.dto.request.ReviewRequestDTO;
import org.scoula.policyInteraction.dto.response.ReviewWithUserDTO;
import org.scoula.policyInteraction.dto.response.ReviewWithPolicyDTO;
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
@Api(tags = "유저 정책 상호작용 API", description = "유저가 정책 북마크, 신청, 리뷰 CRUD API")
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

    @PutMapping("/application/{policyId}/benefit-status")
    @ApiOperation(value = "혜택 수령 상태 업데이트", notes = "신청한 정책의 혜택 수령 상태를 업데이트합니다. 상태값: RECEIVED(수령 완료), PENDING(처리 중), NOT_ELIGIBLE(수령 불가)")
    public ResponseEntity<Void> updateBenefitStatus(
            @ApiIgnore @AuthenticationPrincipal CustomUser customUser,
            @PathVariable Long policyId,
            @RequestParam String benefitStatus) {
        
        Long userId = customUser.getMember().getUserId();
        
        boolean success = policyInteractionService.updateBenefitStatus(userId, policyId, benefitStatus);
        
        return success ?
                ResponseEntity.ok().build() :
                ResponseEntity.badRequest().build();
    }

    // ────────────────────────────────────────
    // 📌 리뷰 관련 API
    // ────────────────────────────────────────

    @PostMapping("/review/{policyId}")
    @ApiOperation(value = "정책 리뷰 작성", notes = "특정 정책에 대한 리뷰를 작성합니다. 혜택 상태별로 작성 가능")
    public ResponseEntity<Void> addReview(
            @ApiIgnore @AuthenticationPrincipal CustomUser customUser,
            @PathVariable Long policyId,
            @RequestBody ReviewRequestDTO request) {
        
        Long userId = customUser.getMember().getUserId();
        
        boolean success = policyInteractionService.addReview(
                userId, policyId, request.getBenefitStatus(), request.getContent());
        
        return success ?
                ResponseEntity.ok().build() :
                ResponseEntity.badRequest().build();
    }

    @PutMapping("/review/{policyId}")
    @ApiOperation(value = "정책 리뷰 수정", notes = "작성한 리뷰를 수정합니다. 본인이 작성한 리뷰만 수정 가능")
    public ResponseEntity<Void> updateReview(
            @ApiIgnore @AuthenticationPrincipal CustomUser customUser,
            @PathVariable Long policyId,
            @RequestBody ReviewRequestDTO request) {
        
        Long userId = customUser.getMember().getUserId();
        
        boolean success = policyInteractionService.updateReview(
                userId, policyId, request.getBenefitStatus(), request.getContent());
        
        return success ?
                ResponseEntity.ok().build() :
                ResponseEntity.badRequest().build();
    }

    @DeleteMapping("/review/{policyId}")
    @ApiOperation(value = "정책 리뷰 삭제", notes = "작성한 리뷰를 삭제합니다. 본인이 작성한 리뷰만 삭제 가능")
    public ResponseEntity<Void> deleteReview(
            @ApiIgnore @AuthenticationPrincipal CustomUser customUser,
            @PathVariable Long policyId,
            @RequestParam String benefitStatus) {
        
        Long userId = customUser.getMember().getUserId();
        
        boolean success = policyInteractionService.deleteReview(userId, policyId, benefitStatus);
        
        return success ?
                ResponseEntity.ok().build() :
                ResponseEntity.notFound().build();
    }

    @GetMapping("/review/{policyId}/my")
    @ApiOperation(value = "내 리뷰 조회", notes = "특정 정책에 대한 내 리뷰를 조회합니다")
    public ResponseEntity<UserPolicyReviewVO> getMyReview(
            @ApiIgnore @AuthenticationPrincipal CustomUser customUser,
            @PathVariable Long policyId,
            @RequestParam String benefitStatus) {
        
        Long userId = customUser.getMember().getUserId();
        UserPolicyReviewVO review = policyInteractionService.getMyReview(userId, policyId, benefitStatus);
        
        return review != null ?
                ResponseEntity.ok(review) :
                ResponseEntity.notFound().build();
    }

    @GetMapping("/review/{policyId}/list")
    @ApiOperation(value = "정책 리뷰 목록 조회", notes = "특정 정책의 모든 리뷰를 조회합니다 (작성자 정보 포함)")
    public ResponseEntity<List<ReviewWithUserDTO>> getPolicyReviews(@PathVariable Long policyId) {
        
        List<ReviewWithUserDTO> reviews = policyInteractionService.getPolicyReviews(policyId);
        
        return ResponseEntity.ok(reviews);
    }

    @GetMapping("/review/my-list")
    @ApiOperation(value = "내가 작성한 리뷰 목록 조회", notes = "사용자가 작성한 모든 리뷰를 정책 정보와 함께 조회합니다")
    public ResponseEntity<List<ReviewWithPolicyDTO>> getMyReviews(
            @ApiIgnore @AuthenticationPrincipal CustomUser customUser) {
        
        Long userId = customUser.getMember().getUserId();
        List<ReviewWithPolicyDTO> reviews = policyInteractionService.getUserReviews(userId);
        
        return ResponseEntity.ok(reviews);
    }

    // ────────────────────────────────────────
    // 📌 좋아요 관련 API (Redis 기반)
    // ────────────────────────────────────────

    @PostMapping("/review/{reviewId}/like")
    @ApiOperation(value = "리뷰 좋아요 추가", notes = "특정 리뷰에 좋아요를 추가합니다. 이미 좋아요한 경우 400 Bad Request를 반환합니다.")
    public ResponseEntity<Void> addReviewLike(
            @ApiIgnore @AuthenticationPrincipal CustomUser customUser,
            @PathVariable Long reviewId) {
        
        Long userId = customUser.getMember().getUserId();
        
        boolean success = policyInteractionService.addReviewLike(userId, reviewId);
        return success ?
                ResponseEntity.ok().build() :
                ResponseEntity.badRequest().build();
    }

    @DeleteMapping("/review/{reviewId}/like")
    @ApiOperation(value = "리뷰 좋아요 취소", notes = "특정 리뷰의 좋아요를 취소합니다. 좋아요하지 않은 상태인 경우 400 Bad Request를 반환합니다.")
    public ResponseEntity<Void> removeReviewLike(
            @ApiIgnore @AuthenticationPrincipal CustomUser customUser,
            @PathVariable Long reviewId) {
        
        Long userId = customUser.getMember().getUserId();
        
        boolean success = policyInteractionService.removeReviewLike(userId, reviewId);
        return success ?
                ResponseEntity.ok().build() :
                ResponseEntity.badRequest().build();
    }

    @GetMapping("/review/{reviewId}/like/count")
    @ApiOperation(value = "리뷰 좋아요 수 조회", notes = "특정 리뷰의 총 좋아요 수를 조회합니다.")
    public ResponseEntity<Long> getReviewLikeCount(@PathVariable Long reviewId) {
        
        Long likeCount = policyInteractionService.getReviewLikeCount(reviewId);
        return ResponseEntity.ok(likeCount);
    }
}