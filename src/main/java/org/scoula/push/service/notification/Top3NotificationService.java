package org.scoula.push.service.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scoula.member.mapper.MemberMapper;
import org.scoula.push.domain.SubscriptionVO;
import org.scoula.push.mapper.SubscriptionMapper;
import org.scoula.push.service.subscription.UserNotificationService;
import org.scoula.security.account.domain.MemberVO;
import org.scoula.userPolicy.dto.SearchResultDTO;
import org.scoula.userPolicy.service.UserPolicyService;
import org.springframework.stereotype.Service;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

/**
 * TOP3 정책 추천 알림 서비스
 * 매주 일요일 오후 3시에 사용자별 맞춤 TOP3 정책을 알림으로 발송
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class Top3NotificationService {

    private final SubscriptionMapper subscriptionMapper;
    private final UserPolicyService userPolicyService;
    private final MemberMapper memberMapper;
    private final UserNotificationService userNotificationService;

    /**
     * TOP3 구독자들에게 개인화된 정책 추천 알림 발송
     */
    public void sendTop3Notifications() {
        log.info("[TOP3 알림] 발송 시작");

        try {
            // 1. TOP3 알림 구독자 조회
            List<SubscriptionVO> top3Subscribers = subscriptionMapper.findActiveByNotificationType("TOP3");
            
            if (top3Subscribers.isEmpty()) {
                log.info("[TOP3 알림] 구독자가 없습니다.");
                return;
            }

            log.info("[TOP3 알림] 구독자 수: {}", top3Subscribers.size());
            
            int successCount = 0;
            int failCount = 0;

            // 2. 각 구독자별로 개인화된 TOP3 정책 알림 발송
            for (SubscriptionVO subscriber : top3Subscribers) {
                try {
                    boolean sent = sendPersonalizedTop3Notification(subscriber);
                    if (sent) {
                        successCount++;
                    } else {
                        failCount++;
                    }
                } catch (Exception e) {
                    log.error("[TOP3 알림] 사용자별 알림 발송 실패 - userId: {}, 오류: {}", 
                            subscriber.getUserId(), e.getMessage());
                    failCount++;
                }
            }

            log.info("[TOP3 알림] 발송 완료 - 성공: {}, 실패: {}", successCount, failCount);

        } catch (Exception e) {
            log.error("[TOP3 알림] 전체 발송 실패: {}", e.getMessage());
        }
    }

    /**
     * 개별 사용자에게 개인화된 TOP3 정책 알림 발송
     */
    private boolean sendPersonalizedTop3Notification(SubscriptionVO subscriber) {
        try {
            // 1. 사용자 정보 조회
            MemberVO member = memberMapper.findByUserId(subscriber.getUserId());
            if (member == null) {
                log.warn("[TOP3 알림] 사용자 정보를 찾을 수 없음 - userId: {}", subscriber.getUserId());
                return false;
            }

            String username = member.getLoginId();
            String displayName = member.getName() != null ? member.getName() : username;

            // 2. 사용자 맞춤 정책 조회
            List<SearchResultDTO> matchingPolicies = userPolicyService.searchMatchingPolicy(username);
            
            if (matchingPolicies == null || matchingPolicies.isEmpty()) {
                log.info("[TOP3 알림] 사용자에게 맞는 정책이 없음 - 조건 설정 유도 알림 발송 - userId: {}", subscriber.getUserId());
                
                // 조건 미설정 사용자에게 설정 유도 알림 발송
                String title = "[TOP3] 맞춤 정책 추천 설정";
                String message = String.format("%s님, 맞춤 정책 추천을 받으려면 조건을 설정해주세요!", displayName);
                String targetUrl = "/policy";
                
                // UserNotificationService를 통한 통합 알림 발송 (여러 토큰 지원)
                userNotificationService.createAndSendTop3Notification(subscriber.getUserId(), title, message, targetUrl);
                
                log.info("[TOP3 알림] 조건 설정 유도 알림 발송 완료 - userId: {}", subscriber.getUserId());
                return true; // 성공으로 처리
            }

            // 3. TOP3 정책 선택 (상위 3개, 지원금 0원 포함)
            List<SearchResultDTO> top3Policies = matchingPolicies.stream()
                    .limit(3)
                    .toList();

            if (top3Policies.size() < 3) {
                log.info("[TOP3 알림] 정책이 3개 미만 - userId: {}, 정책 수: {}", 
                        subscriber.getUserId(), top3Policies.size());
                return false;
            }

            // 4. 총 지원금액 계산 (null인 경우 0원으로 처리)
            long totalAmount = top3Policies.stream()
                    .mapToLong(policy -> policy.getPolicyBenefitAmount() != null ? policy.getPolicyBenefitAmount() : 0L)
                    .sum();

            // 5. 알림 메시지 생성
            String formattedAmount = NumberFormat.getNumberInstance(Locale.KOREA).format(totalAmount);
            String title = "[TOP3] 맞춤 정책 추천";
            String message = String.format("%s님, 최대 %s원 지원받을 수 있는 TOP3 정책을 확인하세요!", 
                    displayName, formattedAmount);
            String targetUrl = "/policy/top3";

            // 6. UserNotificationService를 통한 통합 알림 발송 (여러 토큰 지원)
            userNotificationService.createAndSendTop3Notification(subscriber.getUserId(), title, message, targetUrl);

            log.info("[TOP3 알림] 발송 성공 - userId: {}, 총 지원금액: {}원", 
                    subscriber.getUserId(), formattedAmount);
            
            return true;

        } catch (Exception e) {
            log.error("[TOP3 알림] 개별 발송 실패 - userId: {}, 오류: {}", 
                    subscriber.getUserId(), e.getMessage());
            return false;
        }
    }
}