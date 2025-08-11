package org.scoula.member.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.scoula.security.account.domain.AuthVO;
import org.scoula.security.account.domain.MemberVO;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface MemberMapper {
  MemberVO get(String username);                    // 회원 조회 (권한 포함)
  MemberVO findByUsername(String username);         // ID 중복 체크용 조회
  MemberVO findByUserId(@Param("userId") Long userId);  // 사용자 ID로 조회
  int insert(MemberVO member);                      // 회원정보 저장
  MemberVO getByEmail(String email);                // 이메일 중복 검사
  int updateProfileImage(@Param("username") String username,
                         @Param("profileImageId") int profileImageId);

}
