package org.scoula.member.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.scoula.security.account.domain.AuthVO;
import org.scoula.security.account.domain.MemberVO;

import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberDTO {
  private String userId;
  private String loginId;
  private String email;
  private int point;
  private Date createdAt;
  private String name;

  // authList 제거!
  // private List<String> authList;

  public static MemberDTO of(MemberVO m) {
    return MemberDTO.builder()
            .loginId(m.getLoginId())
            .email(m.getEmail())
            .point(m.getPoint())
            .createdAt(m.getCreatedAt())
            .name(m.getName())
            .build();
  }
}
