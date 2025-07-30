package org.scoula.security.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scoula.security.account.domain.CustomUser;
import org.scoula.security.account.domain.MemberVO;
import org.scoula.security.account.mapper.UserDetailsMapper;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Slf4j
@Component      // Spring Bean으로 등록
@RequiredArgsConstructor   // final 필드에 대한 생성자 자동 생성
public class CustomUserDetailsService implements UserDetailsService {

    private final UserDetailsMapper mapper;  // MyBatis 매퍼 주입

    // loadUserByUsername() : 사용자 이름(username)을 이용해 사용자 정보를 조회하는 서비스
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info(">>> 로그인 시도: username={}", username);

        MemberVO vo = mapper.get(username);

        if (vo == null) {
            log.warn(">>> 로그인 실패: 존재하지 않는 사용자 - {}", username);
            throw new UsernameNotFoundException(username + "은 없는 id입니다.");
        }

//        log.info(">>> 로그인 대상: {}", vo.getLoginId());  // 또는 username, but pw 틀렸을 경우에도 로그 출력됨!

        return new CustomUser(vo);
    }


}
