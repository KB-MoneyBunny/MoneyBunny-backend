package org.scoula.policyInteraction.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scoula.policy.domain.PolicyVectorVO;
import org.scoula.policy.mapper.PolicyMapper;
import org.scoula.policyInteraction.domain.UserPolicyApplicationVO;
import org.scoula.policyInteraction.domain.UserPolicyReviewVO;
import org.scoula.userPolicy.domain.UserVectorVO;
import org.scoula.policyInteraction.domain.YouthPolicyBookmarkVO;
import org.scoula.policyInteraction.dto.response.ApplicationWithPolicyDTO;
import org.scoula.policyInteraction.dto.response.BookmarkWithPolicyDTO;
import org.scoula.policyInteraction.dto.response.ReviewWithUserDTO;
import org.scoula.policyInteraction.dto.response.ReviewWithPolicyDTO;
import org.scoula.policyInteraction.mapper.PolicyInteractionMapper;
import org.scoula.userPolicy.util.UserVectorUtil;
import org.scoula.userPolicy.mapper.UserPolicyMapper;
import org.scoula.policyInteraction.util.NameMaskingUtil;
import org.scoula.policyInteraction.util.ProfanityFilter;
import org.scoula.policyInteraction.exception.ReviewException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PolicyInteractionService {
    
    private final PolicyInteractionMapper policyInteractionMapper;
    private final PolicyMapper policyMapper;
    private final UserPolicyMapper userPolicyMapper;
    private final org.scoula.common.util.RedisUtil redisUtil;
    
    @Autowired
    private ProfanityFilter profanityFilter;
    
    // ────────────────────────────────────────
    // 북마크 관련
    // ────────────────────────────────────────
    
    /** 북마크 추가 */
    @Transactional
    public boolean addBookmark(Long userId, Long policyId) {
        // 이미 북마크 되어있는지 확인
        YouthPolicyBookmarkVO existing = policyInteractionMapper.selectBookmark(userId, policyId);
        if (existing != null) {
            log.info("이미 북마크된 정책입니다. userId: {}, policyId: {}", userId, policyId);
            return false;
        }
        
        YouthPolicyBookmarkVO bookmark = YouthPolicyBookmarkVO.builder()
                .userId(userId)
                .policyId(policyId)
                .build();
                
        int result = policyInteractionMapper.insertBookmark(bookmark);
        
        // 북마크 성공 시 사용자 벡터 갱신 (가중치: 0.3)
        if (result > 0) {
            updateUserVectorWithLinearInterpolation(userId, policyId, 0.3);
        }
        
        return result > 0;
    }
    
    /** 북마크 삭제 */
    @Transactional
    public boolean removeBookmark(Long userId, Long policyId) {
        int result = policyInteractionMapper.deleteBookmark(userId, policyId);
        return result > 0;
    }
    
    /** 사용자의 북마크 목록 조회 (정책 정보 포함) */
    public List<BookmarkWithPolicyDTO> getUserBookmarks(Long userId) {
        return policyInteractionMapper.selectBookmarksByUserId(userId);
    }
    
    // ────────────────────────────────────────
    // 신청 관련
    // ────────────────────────────────────────
    
    /** 정책 신청 등록 */
    @Transactional
    public boolean addApplication(Long userId, Long policyId) {
        // 이미 신청했는지 확인
        UserPolicyApplicationVO existing = policyInteractionMapper.selectApplication(userId, policyId);
        if (existing != null) {
            log.info("이미 신청한 정책입니다. userId: {}, policyId: {}", userId, policyId);
            return false;
        }
        
        UserPolicyApplicationVO application = UserPolicyApplicationVO.builder()
                .userId(userId)
                .policyId(policyId)
                .applicationUrl(null)  // 선택적 필드로 null 처리
                .isApplied(false)      // 신청 등록 시 기본값 false
                .benefitStatus("PENDING") // 기본값: 처리 중
                .build();
                
        int result = policyInteractionMapper.insertApplication(application);
        
        return result > 0;
    }
    
    /** 사용자의 신청 목록 조회 (정책 정보 포함) */
    public List<ApplicationWithPolicyDTO> getUserApplications(Long userId) {
        return policyInteractionMapper.selectApplicationsByUserId(userId);
    }
    
    /** 정책 신청 완료 처리 (false -> true) */
    @Transactional
    public boolean completeApplication(Long userId, Long policyId) {
        // 신청 기록이 있는지 확인
        UserPolicyApplicationVO existing = policyInteractionMapper.selectApplication(userId, policyId);
        if (existing == null) {
            log.info("신청 기록이 없습니다. userId: {}, policyId: {}", userId, policyId);
            return false;
        }
        
        // 이미 완료된 신청인지 확인
        if (Boolean.TRUE.equals(existing.getIsApplied())) {
            log.info("이미 완료된 신청입니다. userId: {}, policyId: {}", userId, policyId);
            return false;
        }
        
        int result = policyInteractionMapper.updateApplicationToComplete(userId, policyId);
        
        // 실제 신청 완료 시 사용자 벡터 갱신 (가중치: 0.7)
        if (result > 0) {
            updateUserVectorWithLinearInterpolation(userId, policyId, 0.7);
        }
        
        return result > 0;
    }
    
    /** 정책 신청 기록 삭제 */
    @Transactional
    public boolean removeApplication(Long userId, Long policyId) {
        // 신청 기록이 있는지 확인
        UserPolicyApplicationVO existing = policyInteractionMapper.selectApplication(userId, policyId);
        if (existing == null) {
            log.info("삭제할 신청 기록이 없습니다. userId: {}, policyId: {}", userId, policyId);
            return false;
        }
        
        // 완료된 신청도 삭제 가능하도록 변경
        log.info("신청 기록을 삭제합니다. userId: {}, policyId: {}, isApplied: {}", 
                userId, policyId, existing.getIsApplied());
        
        int result = policyInteractionMapper.deleteApplication(userId, policyId);
        return result > 0;
    }
    
    /** 미완료 신청 정책 하나 조회 (is_applied = false) */
    public ApplicationWithPolicyDTO getIncompleteApplication(Long userId) {
        return policyInteractionMapper.findIncompleteApplication(userId);
    }
    
    /** 혜택 수령 상태 업데이트 */
    @Transactional
    public boolean updateBenefitStatus(Long userId, Long policyId, String benefitStatus) {
        // 신청 기록이 있는지 확인
        UserPolicyApplicationVO existing = policyInteractionMapper.selectApplication(userId, policyId);
        if (existing == null) {
            log.info("신청 기록이 없습니다. userId: {}, policyId: {}", userId, policyId);
            return false;
        }
        
        // 유효한 상태값인지 확인
        if (!benefitStatus.equals("RECEIVED") && !benefitStatus.equals("PENDING") && !benefitStatus.equals("NOT_ELIGIBLE")) {
            log.error("잘못된 혜택 상태입니다. benefitStatus: {}", benefitStatus);
            return false;
        }
        
        int result = policyInteractionMapper.updateBenefitStatus(userId, policyId, benefitStatus);
        log.info("혜택 상태 업데이트 완료 - userId: {}, policyId: {}, benefitStatus: {}", userId, policyId, benefitStatus);
        
        return result > 0;
    }
    
    // ────────────────────────────────────────
    // 사용자 벡터 갱신 관련
    // ────────────────────────────────────────
    
    /**
     * 선형보간법을 사용하여 사용자 벡터 갱신
     * 공식: newVector = (1 - t) * currentVector + t * policyVector
     * @param userId 사용자 ID
     * @param policyId 정책 ID
     * @param weight 행동 가중치 (조회: 0.1, 북마크: 0.3, 신청: 0.7)
     */
    private void updateUserVectorWithLinearInterpolation(Long userId, Long policyId, double weight) {
        try {
            // 1. 정책 벡터 조회
            PolicyVectorVO policyVector = policyMapper.findByPolicyId(policyId);
            if (policyVector == null) {
                log.warn("[사용자 벡터] 정책 벡터를 찾을 수 없음 - 정책 ID: {}", policyId);
                return;
            }
            
            // 2. 사용자 벡터 조회 또는 초기화
            UserVectorVO userVector = userPolicyMapper.findUserVectorByUserId(userId);
            if (userVector == null) {
                userVector = UserVectorUtil.createInitialUserVector(userId);
                log.info("[사용자 벡터] 초기 벡터 생성 - userId: {}", userId);
            }
            
            // 3. 선형보간법 적용
            // t는 가중치 * 학습률
            double t = weight * 0.1; // 학습률 0.1 적용
            applyLinearInterpolation(userVector, policyVector, t);
            
            // 4. DB 저장
            if (userVector.getId() == null) {
                userPolicyMapper.saveUserVector(userVector);
                log.info("[사용자 벡터] 신규 생성 완료 - userId: {}", userId);
            } else {
                userPolicyMapper.updateUserVector(userVector);
                log.info("[사용자 벡터] 업데이트 완료 - userId: {}", userId);
            }
            
        } catch (Exception e) {
            log.error("[사용자 벡터] 갱신 실패 - userId: {}, policyId: {}, 오류: {}", 
                    userId, policyId, e.getMessage());
        }
    }
    
    /**
     * 선형보간법 적용: result = (1 - t) * userVector + t * policyVector
     * @param userVector 갱신할 사용자 벡터
     * @param policyVector 참조할 정책 벡터
     * @param t 보간 계수 (weight * learningRate)
     */
    private void applyLinearInterpolation(UserVectorVO userVector, PolicyVectorVO policyVector, double t) {
        BigDecimal oneMinusT = BigDecimal.valueOf(1 - t);
        BigDecimal tValue = BigDecimal.valueOf(t);
        
        // 혜택 금액 차원
        BigDecimal newBenefit = userVector.getVecBenefitAmount()
            .multiply(oneMinusT)
            .add(policyVector.getVecBenefitAmount().multiply(tValue))
            .setScale(4, RoundingMode.HALF_UP);
        
        // 마감일 차원
        BigDecimal newDeadline = userVector.getVecDeadline()
            .multiply(oneMinusT)
            .add(policyVector.getVecDeadline().multiply(tValue))
            .setScale(4, RoundingMode.HALF_UP);
        
        // 조회수 차원
        BigDecimal newViews = userVector.getVecViews()
            .multiply(oneMinusT)
            .add(policyVector.getVecViews().multiply(tValue))
            .setScale(4, RoundingMode.HALF_UP);
        
        userVector.setVecBenefitAmount(newBenefit);
        userVector.setVecDeadline(newDeadline);
        userVector.setVecViews(newViews);
        
        log.debug("[선형보간] t: {}, 결과: [{}, {}, {}]", 
            t, newBenefit, newDeadline, newViews);
    }
    
    // ────────────────────────────────────────
    // 리뷰 관련
    // ────────────────────────────────────────
    
    /** 리뷰 작성 */
    @Transactional
    public void addReview(Long userId, Long policyId, String benefitStatus, String content) {
        // 욕설 필터링 검사
        if (profanityFilter.containsProfanity(content)) {
            log.warn("욕설이 포함된 리뷰 작성 시도 - userId: {}, policyId: {}", userId, policyId);
            throw ReviewException.profanityDetected();
        }
        
        // NOT_ELIGIBLE 리뷰는 신청 기록 없어도 OK
        if (!benefitStatus.equals("NOT_ELIGIBLE")) {
            UserPolicyApplicationVO application = policyInteractionMapper.selectApplication(userId, policyId);
            if (application == null || !Boolean.TRUE.equals(application.getIsApplied())) {
                log.info("신청을 완료하지 않은 정책입니다. userId: {}, policyId: {}", userId, policyId);
                throw ReviewException.notApplied();
            }
        }
        
        // 이미 해당 혜택 상태로 리뷰를 작성했는지 확인
        UserPolicyReviewVO existing = policyInteractionMapper.selectReviewByUserAndPolicy(userId, policyId, benefitStatus);
        if (existing != null) {
            log.info("이미 해당 혜택 상태로 리뷰를 작성한 정책입니다. userId: {}, policyId: {}, benefitStatus: {}", userId, policyId, benefitStatus);
            throw ReviewException.alreadyReviewed();
        }
        
        // 유효성 검사
        if (!benefitStatus.equals("RECEIVED") && !benefitStatus.equals("PENDING") && !benefitStatus.equals("NOT_ELIGIBLE")) {
            log.error("잘못된 혜택 상태입니다. benefitStatus: {}", benefitStatus);
            throw ReviewException.invalidBenefitStatus();
        }
        
        UserPolicyReviewVO review = UserPolicyReviewVO.builder()
                .userId(userId)
                .policyId(policyId)
                .likeCount(0) // 초기값 0
                .benefitStatus(benefitStatus)
                .content(content)
                .build();
                
        int result = policyInteractionMapper.insertReview(review);
        if (result <= 0) {
            throw new RuntimeException("리뷰 작성에 실패했습니다.");
        }
    }
    
    /** 리뷰 수정 */
    @Transactional
    public void updateReview(Long userId, Long policyId, String benefitStatus, String content) {
        // 욕설 필터링 검사
        if (profanityFilter.containsProfanity(content)) {
            log.warn("욕설이 포함된 리뷰 수정 시도 - userId: {}, policyId: {}", userId, policyId);
            throw ReviewException.profanityDetected();
        }
        
        // 리뷰가 존재하는지 확인
        UserPolicyReviewVO existing = policyInteractionMapper.selectReviewByUserAndPolicy(userId, policyId, benefitStatus);
        if (existing == null) {
            log.info("수정할 리뷰가 없습니다. userId: {}, policyId: {}, benefitStatus: {}", userId, policyId, benefitStatus);
            throw ReviewException.reviewNotFound();
        }
        
        UserPolicyReviewVO review = UserPolicyReviewVO.builder()
                .userId(userId)
                .policyId(policyId)
                .benefitStatus(benefitStatus)
                .content(content)
                .build();
                
        int result = policyInteractionMapper.updateReview(review);
        if (result <= 0) {
            throw new RuntimeException("리뷰 수정에 실패했습니다.");
        }
    }
    
    /** 리뷰 삭제 */
    @Transactional
    public boolean deleteReview(Long userId, Long policyId, String benefitStatus) {
        // 리뷰가 존재하는지 확인
        UserPolicyReviewVO existing = policyInteractionMapper.selectReviewByUserAndPolicy(userId, policyId, benefitStatus);
        if (existing == null) {
            log.info("삭제할 리뷰가 없습니다. userId: {}, policyId: {}, benefitStatus: {}", userId, policyId, benefitStatus);
            return false;
        }
        
        int result = policyInteractionMapper.deleteReview(userId, policyId, benefitStatus);
        return result > 0;
    }
    
    /** 내 리뷰 조회 */
    public UserPolicyReviewVO getMyReview(Long userId, Long policyId, String benefitStatus) {
        return policyInteractionMapper.selectReviewByUserAndPolicy(userId, policyId, benefitStatus);
    }
    
    /** 정책별 리뷰 목록 조회 (실시간 좋아요 수 포함) */
    public List<ReviewWithUserDTO> getPolicyReviews(Long policyId) {
        return getPolicyReviews(policyId, null);
    }
    
    /** 정책별 리뷰 목록 조회 (실시간 좋아요 수 및 사용자 좋아요 상태 포함) */
    public List<ReviewWithUserDTO> getPolicyReviews(Long policyId, Long currentUserId) {
        List<ReviewWithUserDTO> reviews = policyInteractionMapper.selectReviewsByPolicyId(policyId);
        
        // 각 리뷰에 실시간 좋아요 수 및 사용자 좋아요 상태 적용
        reviews.forEach(review -> {
            // 이름 마스킹 처리
            if (review.getUserName() != null) {
                review.setUserName(NameMaskingUtil.maskName(review.getUserName()));
            }
            
            // Redis에서 실시간 좋아요 수로 업데이트
            Long redisLikeCount = redisUtil.getLikeCount(review.getReviewId());
            review.setLikeCount(redisLikeCount.intValue());
            
            // 현재 사용자의 좋아요 상태 설정 (로그인한 사용자만)
            if (currentUserId != null) {
                boolean isLiked = redisUtil.isUserLikedReview(currentUserId, review.getReviewId());
                review.setIsLikedByCurrentUser(isLiked);
            } else {
                review.setIsLikedByCurrentUser(null); // 비로그인 사용자는 null
            }
        });
        
        return reviews;
    }
    
    /** 정책별 리뷰 수 조회 */
    public Integer getPolicyReviewCount(Long policyId) {
        Integer reviewCount = policyInteractionMapper.selectReviewCountByPolicyId(policyId);
        return reviewCount != null ? reviewCount : 0;
    }
    
    /** 사용자가 작성한 모든 리뷰 조회 */
    public List<ReviewWithPolicyDTO> getUserReviews(Long userId) {
        List<ReviewWithPolicyDTO> reviews = policyInteractionMapper.selectReviewsByUserId(userId);
        // 이름 마스킹 처리
        reviews.forEach(review -> {
            if (review.getUserName() != null) {
                review.setUserName(NameMaskingUtil.maskName(review.getUserName()));
            }
        });
        return reviews;
    }
    
    // ────────────────────────────────────────
    // 좋아요 시스템 관련 (Redis 기반) - 간소화
    // ────────────────────────────────────────
    
    /** 리뷰 좋아요 추가 (하이브리드: Redis + DB 동기화) */
    @Transactional
    public boolean addReviewLike(Long userId, Long reviewId) {
        // 1. Redis에 즉시 반영
        boolean redisSuccess = redisUtil.addLikeToReview(userId, reviewId);
        
        if (redisSuccess) {
            // 2. DB에도 동기화 (백그라운드)
            try {
                policyInteractionMapper.incrementReviewLikeCount(reviewId);
                log.info("좋아요 DB 동기화 완료 - reviewId: {}", reviewId);
            } catch (Exception e) {
                log.error("좋아요 DB 동기화 실패 - reviewId: {}, 오류: {}", reviewId, e.getMessage());
                // Redis는 성공했으므로 true 반환 (사용자 경험 우선)
            }
        }
        
        return redisSuccess;
    }
    
    /** 리뷰 좋아요 취소 (하이브리드: Redis + DB 동기화) */
    @Transactional
    public boolean removeReviewLike(Long userId, Long reviewId) {
        // 1. Redis에서 즉시 제거
        boolean redisSuccess = redisUtil.removeLikeFromReview(userId, reviewId);
        
        if (redisSuccess) {
            // 2. DB에도 동기화 (백그라운드)
            try {
                policyInteractionMapper.decrementReviewLikeCount(reviewId);
                log.info("좋아요 취소 DB 동기화 완료 - reviewId: {}", reviewId);
            } catch (Exception e) {
                log.error("좋아요 취소 DB 동기화 실패 - reviewId: {}, 오류: {}", reviewId, e.getMessage());
                // Redis는 성공했으므로 true 반환 (사용자 경험 우선)
            }
        }
        
        return redisSuccess;
    }
    
    /** 리뷰의 좋아요 수 조회 */
    public Long getReviewLikeCount(Long reviewId) {
        return redisUtil.getLikeCount(reviewId);
    }
    
    /** 사용자의 특정 리뷰 좋아요 상태 확인 */
    public boolean isUserLikedReview(Long userId, Long reviewId) {
        return redisUtil.isUserLikedReview(userId, reviewId);
    }
    
    // ────────────────────────────────────────
    // 관리자 리뷰 관련
    // ────────────────────────────────────────
    
    /** 전체 리뷰 조회 (관리자 전용) */
    public List<ReviewWithUserDTO> getAllReviews() {
        log.debug("[PolicyInteractionService] 전체 리뷰 조회 시작");
        
        try {
            List<ReviewWithUserDTO> reviews = policyInteractionMapper.selectAllReviewsWithUser();
            
            // 각 리뷰에 실시간 좋아요 수 적용
            reviews.forEach(review -> {
                Long redisLikeCount = redisUtil.getLikeCount(review.getReviewId());
                review.setLikeCount(redisLikeCount.intValue());
            });
            
            log.debug("[PolicyInteractionService] 전체 리뷰 조회 완료: {}개", reviews.size());
            return reviews;
        } catch (Exception e) {
            log.error("[PolicyInteractionService] 전체 리뷰 조회 실패", e);
            throw new RuntimeException("전체 리뷰 조회에 실패했습니다.", e);
        }
    }
    
    /** 정책별 모든 리뷰 삭제 (관리자 전용) */
    @Transactional
    public boolean deleteReviewsByPolicyId(Long policyId) {
        log.info("[PolicyInteractionService] 정책별 리뷰 삭제 시작 - policyId: {}", policyId);
        
        try {
            int deletedCount = policyInteractionMapper.deleteReviewsByPolicyId(policyId);
            log.info("[PolicyInteractionService] 정책별 리뷰 삭제 완료 - policyId: {}, 삭제된 리뷰 수: {}", policyId, deletedCount);
            return deletedCount > 0;
        } catch (Exception e) {
            log.error("[PolicyInteractionService] 정책별 리뷰 삭제 실패 - policyId: {}", policyId, e);
            throw new RuntimeException("정책별 리뷰 삭제에 실패했습니다.", e);
        }
    }

    /** 개별 리뷰 삭제 (관리자 전용) */
    @Transactional
    public boolean deleteSingleReview(Long reviewId) {
        log.info("[PolicyInteractionService] 개별 리뷰 삭제 시작 - reviewId: {}", reviewId);
        
        try {
            int deletedCount = policyInteractionMapper.deleteSingleReview(reviewId);
            log.info("[PolicyInteractionService] 개별 리뷰 삭제 완료 - reviewId: {}, 삭제된 리뷰 수: {}", reviewId, deletedCount);
            return deletedCount > 0;
        } catch (Exception e) {
            log.error("[PolicyInteractionService] 개별 리뷰 삭제 실패 - reviewId: {}", reviewId, e);
            throw new RuntimeException("개별 리뷰 삭제에 실패했습니다.", e);
        }
    }
    
}
