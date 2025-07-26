package org.scoula.policyInteraction.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scoula.policyInteraction.domain.UserPolicyApplicationVO;
import org.scoula.policyInteraction.domain.YouthPolicyBookmarkVO;
import org.scoula.policyInteraction.dto.ApplicationRequestDto;
import org.scoula.policyInteraction.dto.BookmarkRequestDto;
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
    public boolean addBookmark(BookmarkRequestDto dto) {
        // 이미 북마크 되어있는지 확인
        YouthPolicyBookmarkVO existing = policyInteractionMapper.selectBookmark(dto.getUserId(), dto.getPolicyId());
        if (existing != null) {
            log.info("이미 북마크된 정책입니다. userId: {}, policyId: {}", dto.getUserId(), dto.getPolicyId());
            return false;
        }
        
        YouthPolicyBookmarkVO bookmark = YouthPolicyBookmarkVO.builder()
                .userId(dto.getUserId())
                .policyId(dto.getPolicyId())
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
    
    /** 북마크 여부 확인 */
    public boolean isBookmarked(Long userId, Long policyId) {
        YouthPolicyBookmarkVO bookmark = policyInteractionMapper.selectBookmark(userId, policyId);
        return bookmark != null;
    }
    
    /** 사용자의 북마크 목록 조회 */
    public List<YouthPolicyBookmarkVO> getUserBookmarks(Long userId) {
        return policyInteractionMapper.selectBookmarksByUserId(userId);
    }
    
    // ────────────────────────────────────────
    // 📌 신청 관련
    // ────────────────────────────────────────
    
    /** 정책 신청 등록 */
    @Transactional
    public boolean addApplication(ApplicationRequestDto dto) {
        // 이미 신청했는지 확인
        UserPolicyApplicationVO existing = policyInteractionMapper.selectApplication(dto.getUserId(), dto.getPolicyId());
        if (existing != null) {
            log.info("이미 신청한 정책입니다. userId: {}, policyId: {}", dto.getUserId(), dto.getPolicyId());
            return false;
        }
        
        UserPolicyApplicationVO application = UserPolicyApplicationVO.builder()
                .userId(dto.getUserId())
                .policyId(dto.getPolicyId())
                .applicationUrl(dto.getApplicationUrl())
                .build();
                
        int result = policyInteractionMapper.insertApplication(application);
        return result > 0;
    }
    
    /** 신청 여부 확인 */
    public boolean isApplied(Long userId, Long policyId) {
        UserPolicyApplicationVO application = policyInteractionMapper.selectApplication(userId, policyId);
        return application != null;
    }
    
    /** 사용자의 신청 목록 조회 */
    public List<UserPolicyApplicationVO> getUserApplications(Long userId) {
        return policyInteractionMapper.selectApplicationsByUserId(userId);
    }
}
