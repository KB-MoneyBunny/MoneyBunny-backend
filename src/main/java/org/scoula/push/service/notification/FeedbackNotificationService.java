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
                String.format("💰 %s님의 이번 주 소비 리포트", displayName), 
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
        
        // 마무리 메시지
        message.append("\n\n");
        if (comparison.getIsIncrease()) {
            message.append("다음 주는 조금 더 절약해보세요! 💪");
        } else {
            message.append("훌륭한 절약입니다! 계속 유지해보세요! 🎉");
        }
        
        return message.toString();
    }
    
    /**
     * 주간 지출 비교 메시지 생성
     */
    private String createWeeklyComparisonMessage(WeeklySpendingComparison comparison) {
        NumberFormat formatter = NumberFormat.getNumberInstance(Locale.KOREA);
        
        if (comparison.getChangePercentage() == 0) {
            return String.format("📊 이번 주 지출: %s원\n지난주와 동일한 수준이에요", 
                    formatter.format(comparison.getThisWeekAmount()));
        }
        
        String changeIcon = comparison.getIsIncrease() ? "📈" : "📉";
        String changeVerb = comparison.getIsIncrease() ? "증가" : "절약";
        
        return String.format("%s 지난주 대비 %.1f%% %s했어요!\n(이번 주: %s원)", 
                changeIcon,
                comparison.getChangePercentage(),
                changeVerb,
                formatter.format(comparison.getThisWeekAmount()));
    }
    
    /**
     * 요일별 지출 피크 메시지 생성
     */
    private String createDayOfWeekPeakMessage(DayOfWeekPeak peak) {
        String dayEmoji = getDayEmoji(peak.getDayOfWeek());
        String contextMessage = getContextMessage(peak);
        
        return String.format("📅 %s%s에 가장 많이 소비하시는군요%s", 
                dayEmoji, peak.getDayName(), contextMessage);
    }
    
    /**
     * 요일별 이모지 반환
     */
    private String getDayEmoji(int dayOfWeek) {
        return switch (dayOfWeek) {
            case 1, 7 -> "🌴 "; // 주말
            case 2 -> "🌅 "; // 월요일
            case 3 -> "💼 "; // 화요일
            case 4 -> "⚡ "; // 수요일
            case 5 -> "🚀 "; // 목요일
            case 6 -> "🎉 "; // 금요일
            default -> "";
        };
    }
    
    /**
     * 요일별 상황에 맞는 추가 메시지
     */
    private String getContextMessage(DayOfWeekPeak peak) {
        if (peak.isWeekend()) {
            return " (주말 여가 시간이군요! 🛍️)";
        } else if (peak.getDayOfWeek() == 6) { // 금요일
            return " (불금의 힘이군요! 🍻)";
        } else if (peak.getDayOfWeek() == 2) { // 월요일
            return " (월요병과 함께하는 소비? 😅)";
        } else {
            return " (평일 소비 패턴을 체크해보세요 📋)";
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