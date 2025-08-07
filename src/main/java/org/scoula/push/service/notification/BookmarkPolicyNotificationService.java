package org.scoula.push.service.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scoula.member.mapper.MemberMapper;
import org.scoula.policy.domain.YouthPolicyPeriodVO;
import org.scoula.policy.domain.YouthPolicyVO;
import org.scoula.policy.mapper.PolicyMapper;
import org.scoula.policyInteraction.domain.YouthPolicyBookmarkVO;
import org.scoula.policyInteraction.mapper.PolicyInteractionMapper;
import org.scoula.push.service.subscription.UserNotificationService;
import org.scoula.security.account.domain.MemberVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookmarkPolicyNotificationService {

    private final PolicyInteractionMapper policyInteractionMapper;
    private final PolicyMapper policyMapper;
    private final MemberMapper memberMapper;
    private final UserNotificationService userNotificationService;

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

    /**
     * 모든 북마크된 정책에 대해 신청일/마감일 알림 체크 및 즉시 발송
     */
    @Transactional
    public void checkAndSendBookmarkNotifications() {
        log.info("[북마크 알림] 북마크된 정책 알림 체크 및 발송 시작");
        
        // 북마크 알림을 구독한 사용자의 북마크만 조회 (최적화)
        List<YouthPolicyBookmarkVO> allBookmarks = policyInteractionMapper.getBookmarksWithActiveSubscription();
        log.info("[북마크 알림] 총 {}개의 북마크 발견", allBookmarks.size());

        LocalDate today = LocalDate.now();

        for (YouthPolicyBookmarkVO bookmark : allBookmarks) {
            try {
                processPolicyNotification(bookmark, today);
            } catch (Exception e) {
                log.error("[북마크 알림] 정책 ID {} 처리 중 오류: {}", bookmark.getPolicyId(), e.getMessage());
            }
        }
        
        log.info("[북마크 알림] 북마크된 정책 알림 체크 및 발송 완료");
    }

    /**
     * 특정 정책에 대한 알림 처리
     */
    private void processPolicyNotification(YouthPolicyBookmarkVO bookmark, LocalDate today) {
        Long policyId = bookmark.getPolicyId();
        Long userId = bookmark.getUserId();

        // 정책 정보 조회
        YouthPolicyVO policy = policyMapper.findYouthPolicyById(policyId);
        if (policy == null) {
            log.warn("[북마크 알림] 정책을 찾을 수 없음 - 정책 ID: {}", policyId);
            return;
        }

        // 정책 기간 정보 조회
        YouthPolicyPeriodVO period = policyMapper.findYouthPolicyPeriodByPolicyId(policyId);
        if (period == null || period.getApplyPeriod() == null) {
            log.debug("[북마크 알림] 신청 기간 정보 없음 - 정책 ID: {}", policyId);
            return;
        }

        // 신청 기간 파싱
        PolicyPeriod policyPeriod = parseApplyPeriod(period.getApplyPeriod());
        if (policyPeriod == null) {
            log.warn("[북마크 알림] 신청 기간 파싱 실패 - 정책 ID: {}, 기간: {}", policyId, period.getApplyPeriod());
            return;
        }

        // 알림 조건 체크 및 즉시 발송
        checkAndSendNotification(userId, policy, policyPeriod, today);
    }

    /**
     * 알림 조건 체크 및 즉시 발송
     */
    private void checkAndSendNotification(Long userId, YouthPolicyVO policy, PolicyPeriod period, LocalDate today) {
        String policyTitle = policy.getTitle();
        Long policyId = policy.getId();

        // 사용자 정보 조회
        MemberVO member = memberMapper.findByUserId(userId);
        if (member == null) {
            log.warn("[북마크 알림] 사용자 정보를 찾을 수 없음 - userId: {}", userId);
            return;
        }
        String displayName = getDisplayName(member);

        // 1. 신청 시작일 당일 체크
        if (today.equals(period.getStartDate())) {
            String title = "[북마크] 정책 신청이 시작됐어요!";
            String message = String.format("%s님, '%s' 정책 신청이 오늘부터 시작됩니다! 놓치지 마세요", displayName, policyTitle);
            String targetUrl = "/policy/" + policyId;
            
            userNotificationService.createAndSendBookmarkNotification(userId, title, message, targetUrl);
            log.info("[북마크 알림] 신청 시작 알림 발송 - 사용자: {}, 정책: {}", displayName, policyTitle);
            return;
        }

        // 2. 마감일 3일 전부터 당일까지 체크
        long daysUntilDeadline = ChronoUnit.DAYS.between(today, period.getEndDate());
        
        if (daysUntilDeadline >= 0 && daysUntilDeadline <= 3) {
            String title = getDeadlineNotificationTitle(daysUntilDeadline);
            String message = getDeadlineNotificationMessage(policyTitle, daysUntilDeadline, displayName);
            String targetUrl = "/policy/" + policyId;
            
            userNotificationService.createAndSendBookmarkNotification(userId, title, message, targetUrl);
            log.info("[북마크 알림] 마감 {}일 전 알림 발송 - 사용자: {}, 정책: {}", daysUntilDeadline, displayName, policyTitle);
        }
    }

    /**
     * 마감일 알림 제목 생성
     */
    private String getDeadlineNotificationTitle(long daysUntilDeadline) {
        return switch ((int) daysUntilDeadline) {
            case 0 -> "[북마크] 마감 당일!";
            case 1 -> "[북마크] 마감 하루 전!";
            case 2 -> "[북마크] 마감 이틀 전!";
            case 3 -> "[북마크] 마감 3일 전!";
            default -> "[북마크] 마감 임박!";
        };
    }

    /**
     * 마감일 알림 메시지 생성
     */
    private String getDeadlineNotificationMessage(String policyTitle, long daysUntilDeadline, String displayName) {
        return switch ((int) daysUntilDeadline) {
            case 0 -> String.format("%s님, '%s' 정책이 오늘 마감됩니다! 지금 바로 신청하세요!", displayName, policyTitle);
            case 1 -> String.format("%s님, '%s' 정책 마감이 하루 남았습니다! 서둘러 신청하세요!", displayName, policyTitle);
            case 2 -> String.format("%s님, '%s' 정책 마감이 이틀 남았습니다! 준비하세요!", displayName, policyTitle);
            case 3 -> String.format("%s님, '%s' 정책 마감이 3일 남았습니다! 미리 준비하세요!", displayName, policyTitle);
            default -> String.format("%s님, '%s' 정책 마감이 임박했습니다! 서둘러 신청하세요!", displayName, policyTitle);
        };
    }

    /**
     * 신청 기간 문자열 파싱 (예: "20250403 ~ 20250422")
     */
    private PolicyPeriod parseApplyPeriod(String applyPeriod) {
        try {
            if (applyPeriod == null || applyPeriod.trim().isEmpty()) {
                return null;
            }

            // "20250403 ~ 20250422" 형식 파싱
            String[] parts = applyPeriod.split("~");
            if (parts.length != 2) {
                return null;
            }

            String startDateStr = parts[0].trim().replaceAll("[^0-9]", "");
            String endDateStr = parts[1].trim().replaceAll("[^0-9]", "");

            if (startDateStr.length() != 8 || endDateStr.length() != 8) {
                return null;
            }

            LocalDate startDate = LocalDate.parse(startDateStr, DATE_FORMAT);
            LocalDate endDate = LocalDate.parse(endDateStr, DATE_FORMAT);

            return new PolicyPeriod(startDate, endDate);

        } catch (Exception e) {
            log.warn("[북마크 알림] 날짜 파싱 오류: {}, 입력값: {}", e.getMessage(), applyPeriod);
            return null;
        }
    }

    /**
     * 표시용 사용자명 반환 (name 우선, 없으면 loginId)
     */
    private String getDisplayName(MemberVO member) {
        if (member.getName() != null && !member.getName().trim().isEmpty()) {
            return member.getName();
        } else if (member.getLoginId() != null && !member.getLoginId().trim().isEmpty()) {
            return member.getLoginId();
        }
        return "사용자" + member.getUserId(); // fallback
    }

    /**
     * 정책 기간 내부 클래스
     */
    private static class PolicyPeriod {
        private final LocalDate startDate;
        private final LocalDate endDate;

        public PolicyPeriod(LocalDate startDate, LocalDate endDate) {
            this.startDate = startDate;
            this.endDate = endDate;
        }

        public LocalDate getStartDate() {
            return startDate;
        }

        public LocalDate getEndDate() {
            return endDate;
        }
    }
}