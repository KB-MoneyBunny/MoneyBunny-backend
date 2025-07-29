package org.scoula.security.account.mapper;

import org.apache.ibatis.annotations.Param;
import org.scoula.security.account.domain.MemberVO;

public interface UserDetailsMapper {
    /**
     * 사용자명으로 사용자 정보와 권한 정보를 조회
     * @param username 사용자 ID
     * @return 사용자 정보 및 권한 목록
     */
    public MemberVO get(String username);

//    MemberVO getWithAuthByLoginId(String loginId);

    MemberVO findByLoginIdAndEmail(@Param("loginId") String loginId,
                                   @Param("email") String email);  // 이메일 인증용
    boolean resetPassword(@Param("loginId") String loginId, @Param("password") String password); // 비밀번호 재설정
    MemberVO findByEmail(@Param("email") String email);
}