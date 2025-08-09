package org.scoula.push.service.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scoula.codef.service.FeedbackAnalysisService;
import org.scoula.member.mapper.MemberMapper;
import org.scoula.push.domain.SubscriptionVO;
import org.scoula.push.dto.feedback.DayOfWeekPeak;
import org.scoula.push.dto.feedback.WeeklySpendingComparison;
import org.scoula.push.mapper.SubscriptionMapper;
import org.scoula.push.service.subscription.UserNotificationService;
import org.scoula.security.account.domain.MemberVO;
import org.springframework.stereotype.Service;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Slf4j
public class FeedbackNotificationService {
    
    private final FeedbackAnalysisService feedbackAnalysisService;
    private final UserNotificationService userNotificationService;
    private final SubscriptionMapper subscriptionMapper;
    private final MemberMapper memberMapper;
    
    /**
     * 사용자에게 주간 소비 리포트 알림 전송
     * @param userId 사용자 ID
     */
    public void sendWeeklyConsumptionReport(Long userId) {
        log.info("[피드백알림] 주간 소비 리포트 전송 시작: userId={}", userId);
        
        try {
            // 사용자 정보 조회
            MemberVO member = memberMapper.findByUserId(userId);
            if (member == null) {
                log.warn("[피드백알림] 사용자를 찾을 수 없음: userId={}", userId);
                return;
            }
            
            String displayName = getDisplayName(member);
            
            // 카드 데이터 보유 여부 확인
            if (!feedbackAnalysisService.hasCardData(userId)) {
                log.info("[피드백알림] 카드 데이터 없음, 알림 건너뜀: userId={}", userId);
                return;
            }
            
            // 1. 주간 지출 비교 분석
            WeeklySpendingComparison weeklyComparison = feedbackAnalysisService.analyzeWeeklySpending(userId);
            
            // 2. 요일별 지출 피크 분석
            DayOfWeekPeak dayOfWeekPeak = feedbackAnalysisService.analyzeDayOfWeekPeak(userId);
            
            // 3. 통합 피드백 메시지 생성 (사용자 이름 포함)
            String message = createFeedbackMessage(displayName, weeklyComparison, dayOfWeekPeak);
            
            // 4. 알림 전송
            userNotificationService.createAndSendFeedbackNotification(
                userId, 
                String.format("[피드백] %s님의 이번 주 소비 리포트", displayName), 
                message, 
                null // targetUrl 없음
            );
            
            log.info("[피드백알림] 주간 소비 리포트 전송 완료: userId={}", userId);
            
        } catch (Exception e) {
            log.error("[피드백알림] 주간 소비 리포트 전송 실패: userId={}, error={}", userId, e.getMessage(), e);
        }
    }
    
    /**
     * 모든 FEEDBACK 구독자에게 주간 소비 리포트 발송 (스케줄러용)
     */
    public void sendWeeklyConsumptionReportToAll() {
        log.info("[피드백알림] 전체 사용자 주간 소비 리포트 발송 시작");
        
        try {
            // FEEDBACK 타입 알림을 구독하고 있는 모든 활성 사용자 조회
            List<SubscriptionVO> feedbackSubscriptions = subscriptionMapper.findActiveByNotificationType("FEEDBACK");
            
            if (feedbackSubscriptions.isEmpty()) {
                log.info("[피드백알림] FEEDBACK 알림 구독자가 없어 작업을 종료합니다");
                return;
            }
            
            log.info("[피드백알림] FEEDBACK 알림 대상 사용자 수: {}", feedbackSubscriptions.size());
            
            int successCount = 0;
            int errorCount = 0;
            
            // 각 구독자에게 개별적으로 피드백 알림 발송
            for (SubscriptionVO subscription : feedbackSubscriptions) {
                try {
                    Long userId = subscription.getUserId();
                    log.debug("[피드백알림] 피드백 알림 처리 중: userId={}", userId);
                    
                    // 개별 사용자에게 주간 소비 리포트 발송
                    sendWeeklyConsumptionReport(userId);
                    successCount++;
                    
                } catch (Exception e) {
                    errorCount++;
                    log.error("[피드백알림] 사용자 {}의 피드백 알림 발송 실패: {}", 
                            subscription.getUserId(), e.getMessage(), e);
                }
            }
            
            log.info("[피드백알림] 전체 사용자 주간 소비 리포트 발송 완료 - 성공: {}, 실패: {}", successCount, errorCount);
            
        } catch (Exception e) {
            log.error("[피드백알림] 전체 사용자 주간 소비 리포트 발송 중 오류 발생: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 주간 지출 비교와 요일별 피크를 통합한 피드백 메시지 생성
     */
    private String createFeedbackMessage(String displayName, WeeklySpendingComparison comparison, DayOfWeekPeak peak) {
        StringBuilder message = new StringBuilder();
        
        // 주간 지출 비교 메시지
        String weeklyMessage = createWeeklyComparisonMessage(comparison);
        message.append(weeklyMessage);
        
        // 요일별 피크 메시지 (데이터가 있는 경우만)
        if (peak.getDayOfWeek() > 0 && peak.getTotalAmount() > 0) {
            message.append("\n");
            String peakMessage = createDayOfWeekPeakMessage(peak);
            message.append(peakMessage);
        }
        
        
        return message.toString();
    }
    
    /**
     * 주간 지출 비교 메시지 생성
     */
    private String createWeeklyComparisonMessage(WeeklySpendingComparison comparison) {
        NumberFormat formatter = NumberFormat.getNumberInstance(Locale.KOREA);
        
        if (comparison.getChangePercentage() == 0) {
            return String.format("이번 주 지출: %s원\n지난주와 동일한 수준이에요", 
                    formatter.format(comparison.getThisWeekAmount()));
        }
        
        String changeVerb = comparison.getIsIncrease() ? "증가" : "절약";
        
        return String.format("지난주 대비 %.1f%% %s했어요!\n(이번 주: %s원)", 
                comparison.getChangePercentage(),
                changeVerb,
                formatter.format(comparison.getThisWeekAmount()));
    }
    
    /**
     * 요일별 지출 피크 메시지 생성
     */
    private String createDayOfWeekPeakMessage(DayOfWeekPeak peak) {
        String contextMessage = getContextMessage(peak);
        
        return String.format("%s에 가장 많이 소비하시는군요%s", 
                peak.getDayName(), contextMessage);
    }
    
    
    /**
     * 요일별 상황에 맞는 추가 메시지
     */
    private String getContextMessage(DayOfWeekPeak peak) {
        if (peak.isWeekend()) {
            return "\n(여가 시간을 잘 보내셨나요?)";
        } else if (peak.getDayOfWeek() == 6) { // 금요일
            return "\n(불금의 힘인가요?)";
        } else if (peak.getDayOfWeek() == 2) { // 월요일
            return "\n(월요병이 지갑을 열게 하나요?)";
        } else {
            return "\n(평일 중간의 스트레스 해소용일까요?)";
        }
    }
    
    /**
     * 표시할 사용자 이름 결정 (NewPolicyNotificationService와 동일한 로직)
     */
    private String getDisplayName(MemberVO member) {
        if (member.getName() != null && !member.getName().trim().isEmpty()) {
            return member.getName();
        } else if (member.getLoginId() != null && !member.getLoginId().trim().isEmpty()) {
            return member.getLoginId();
        }
        return "사용자" + member.getUserId(); // fallback
    }
}