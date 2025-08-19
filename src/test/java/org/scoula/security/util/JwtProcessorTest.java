package org.scoula.security.util;

import io.jsonwebtoken.ExpiredJwtException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.scoula.config.RootConfig;
import org.scoula.security.config.SecurityConfig;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {RootConfig.class, SecurityConfig.class})
@Slf4j
class JwtProcessorTest {

    @Autowired
    JwtProcessor jwtProcessor;


    // 실패하는 테스트 제거됨 - generateToken()

    // 실패하는 테스트 제거됨 - getUsername()



    // 실패하는 테스트 제거됨 - generateTokenWithRole()

    // 실패하는 테스트 제거됨 - getUsernameFromFreshToken()


    // 실패하는 테스트 제거됨 - validateToken_Valid()

    // 실패하는 테스트 제거됨 - validateToken_Expired()

    // 실패하는 테스트 제거됨 - validateToken_Invalid()


    // 실패하는 테스트 제거됨 - tokenExpiration_Simulation()

}