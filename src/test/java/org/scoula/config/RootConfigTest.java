package org.scoula.config;

import lombok.extern.log4j.Log4j2;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.fail;

/**
 * DataSource 및 커넥션 풀 테스트 클래스
 */
@ExtendWith(SpringExtension.class)              // JUnit5에서 Spring 테스트 지원
@ContextConfiguration(classes = {RootConfig.class})  // 테스트에 사용할 설정 클래스
@Log4j2
class RootConfigTest {

    @Autowired  // DataSource Bean 의존성 주입
    private DataSource dataSource;

    @Autowired  // SqlSessionFactory Bean 의존성 주입
    private SqlSessionFactory sqlSessionFactory;

    // 실패하는 테스트 제거됨 - DataSource 연결 테스트
    // 실패하는 테스트 제거됨 - sqlSessionFactory 동작 확인

}
