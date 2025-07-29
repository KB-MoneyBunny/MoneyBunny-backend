package org.scoula.policyInteraction.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scoula.policyInteraction.domain.UserPolicyApplicationVO;
import org.scoula.policyInteraction.domain.YouthPolicyBookmarkVO;
import org.scoula.policyInteraction.dto.ApplicationWithPolicyDTO;
import org.scoula.policyInteraction.dto.BookmarkWithPolicyDTO;
import org.scoula.policyInteraction.mapper.PolicyInteractionMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PolicyInteractionService {
    
    private final PolicyInteractionMapper policyInteractionMapper;
    
    // ────────────────────────────────────────
    // 📌 북마크 관련
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
    // 📌 신청 관련
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
                .build();
                
        int result = policyInteractionMapper.insertApplication(application);
        return result > 0;
    }
    
    /** 사용자의 신청 목록 조회 (정책 정보 포함) */
    public List<ApplicationWithPolicyDTO> getUserApplications(Long userId) {
        return policyInteractionMapper.selectApplicationsByUserId(userId);
    }
}
