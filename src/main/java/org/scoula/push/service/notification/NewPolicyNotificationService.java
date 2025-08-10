package org.scoula.push.service.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scoula.member.mapper.MemberMapper;
import org.scoula.policy.domain.YouthPolicyVO;
import org.scoula.policy.mapper.PolicyMapper;
import org.scoula.push.domain.SubscriptionVO;
import org.scoula.push.mapper.SubscriptionMapper;
import org.scoula.push.service.subscription.UserNotificationService;
import org.scoula.security.account.domain.MemberVO;
import org.scoula.userPolicy.dto.SearchResultDTO;
import org.scoula.userPolicy.service.UserPolicyService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 신규 정책 알림 서비스
 * 당일 생성된 정책 중 사용자 조건에 맞는 것들에 대해 알림 발송
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NewPolicyNotificationService {

    private final PolicyMapper policyMapper;
    private final SubscriptionMapper subscriptionMapper;
    private final MemberMapper memberMapper;
    private final UserPolicyService userPolicyService;
    private final UserNotificationService userNotificationService;

    /**
     * 신규 정책 알림 처리 메인 메서드
     * 오후 6시에 실행되어 당일 생성된 정책들을 처리
     */
    public void processNewPolicyAlerts() {
        log.info("[신규 정책 알림] 처리 시작");
        
        try {
            // 1. 오늘 생성된 신규 정책들 조회
            List<YouthPolicyVO> todayNewPolicies = policyMapper.findTodayNewPolicies();
            
            if (todayNewPolicies.isEmpty()) {
                log.info("[신규 정책 알림] 오늘 생성된 신규 정책 없음");
                return;
            }
            
            log.info("[신규 정책 알림] 오늘 생성된 신규 정책 {}건 발견", todayNewPolicies.size());
            
            // 2. 신규 정책 알림 구독자들 조회
            List<SubscriptionVO> newPolicySubscribers = getNewPolicySubscribers();
            
            if (newPolicySubscribers.isEmpty()) {
                log.info("[신규 정책 알림] 신규 정책 알림 구독자 없음");
                return;
            }
            
            log.info("[신규 정책 알림] 신규 정책 알림 구독자 {}명 발견", newPolicySubscribers.size());
            
            // 3. 각 구독자별로 매칭되는 신규 정책 확인 및 알림 발송
            int totalNotificationsSent = 0;
            for (SubscriptionVO subscriber : newPolicySubscribers) {
                int sentCount = processNewPolicyAlertsForUser(subscriber.getUserId(), todayNewPolicies);
                totalNotificationsSent += sentCount;
            }
            
            log.info("[신규 정책 알림] 처리 완료 - 총 {}건의 알림 발송", totalNotificationsSent);
            
        } catch (Exception e) {
            log.error("[신규 정책 알림] 처리 중 오류 발생", e);
        }
    }

    /**
     * 특정 사용자에 대해 신규 정책 알림 처리
     */
    private int processNewPolicyAlertsForUser(Long userId, List<YouthPolicyVO> newPolicies) {
        try {
            // 사용자 정보 조회
            MemberVO member = memberMapper.findByUserId(userId);
            if (member == null || member.getLoginId() == null) {
                log.warn("[신규 정책 알림] 사용자를 찾을 수 없음 - userId: {}", userId);
                return 0;
            }
            
            String loginId = member.getLoginId();  // searchMatchingPolicy에 사용할 username 파라미터
            String displayName = getDisplayName(member);  // 알림 메시지에 표시할 이름
            
            // 기존 매칭 로직을 활용하여 사용자에게 맞는 정책들 조회 (username = loginId)
            List<SearchResultDTO> userMatchingPolicies = userPolicyService.searchMatchingPolicy(loginId);
            
            if (userMatchingPolicies == null || userMatchingPolicies.isEmpty()) {
                log.debug("[신규 정책 알림] 사용자에게 맞는 정책 없음 - userId: {}", userId);
                return 0;
            }
            
            // 매칭되는 정책 ID 집합 생성
            Set<Long> matchingPolicyIds = userMatchingPolicies.stream()
                    .map(SearchResultDTO::getPolicyId)
                    .collect(Collectors.toSet());
            
            // 신규 정책 중 사용자에게 매칭되는 것들만 알림 발송
            int sentCount = 0;
            for (YouthPolicyVO newPolicy : newPolicies) {
                if (matchingPolicyIds.contains(newPolicy.getId())) {
                    sendNewPolicyNotification(userId, newPolicy, displayName);
                    sentCount++;
                }
            }
            
            if (sentCount > 0) {
                log.info("[신규 정책 알림] 사용자 {}({})에게 {}건의 신규 정책 알림 발송", 
                         displayName, userId, sentCount);
            }
            
            return sentCount;
            
        } catch (Exception e) {
            log.error("[신규 정책 알림] 사용자별 처리 중 오류 - userId: {}, 오류: {}", userId, e.getMessage());
            return 0;
        }
    }

    /**
     * 신규 정책 알림 발송
     */
    private void sendNewPolicyNotification(Long userId, YouthPolicyVO policy, String displayName) {
        try {
            String title = "[신규 정책] " + displayName + "님의 맞춤형 정책이 생겼어요!";
            String message = String.format("[%s]\n%s", 
                    policy.getTitle(),
                    policy.getPolicyBenefitDescription() != null ? 
                            policy.getPolicyBenefitDescription() : policy.getDescription());
            String targetUrl = "/policy/" + policy.getId();
            
            userNotificationService.createAndSendNewPolicyNotification(userId, title, message, targetUrl);
            
            log.debug("[신규 정책 알림] 개별 알림 발송 완료 - 사용자: {}, 정책: {}", displayName, policy.getTitle());
            
        } catch (Exception e) {
            log.error("[신규 정책 알림] 개별 알림 발송 실패 - 사용자: {}, 정책: {}, 오류: {}", 
                      displayName, policy.getTitle(), e.getMessage());
        }
    }

    /**
     * 신규 정책 알림 구독자들 조회
     */
    private List<SubscriptionVO> getNewPolicySubscribers() {
        try {
            return subscriptionMapper.findActiveByNotificationType("NEW_POLICY");
        } catch (Exception e) {
            log.error("[신규 정책 알림] 구독자 조회 중 오류", e);
            return List.of();
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
}