package org.scoula.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.*;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import javax.sql.DataSource;
import java.util.concurrent.Executor;

/**
 * 🌱 Root Application Context 설정 클래스
 * - Spring Framework의 최상위(Root) 애플리케이션 컨텍스트를 설정하는 클래스
 * - 웹 계층과 무관한 비즈니스 로직, 서비스, 데이터 액세스 계층의 Bean들을 관리
 */
@Slf4j
@Configuration
@PropertySources({
        @PropertySource("classpath:application.properties"), // 공통 설정
        @PropertySource("classpath:application-${spring.profiles.active}.properties") // 환경별 설정
})
@MapperScan(basePackages = {
        "org.scoula.member.mapper",  // 회원 매퍼 스캔
        "org.scoula.policy.mapper",
        "org.scoula.userPolicy.mapper",
        "org.scoula.policy.mapper",
        "org.scoula.codef.mapper",
        "org.scoula.push.mapper", // push 매퍼 스캔 추가
        "org.scoula.policyInteraction.mapper", // 정책 상호작용 매퍼 스캔 추가
        "org.scoula.asset.mapper",
        "org.scoula.external.gpt.mapper" // GPT 프롬프트 매퍼 스캔 추가
})
@ComponentScan(basePackages = {
        "org.scoula.member.service",
        "org.scoula.policy.service",
        "org.scoula.external",
        "org.scoula.scheduler", // 스케줄러 패키지 추가
        "org.scoula.config", // config
        "org.scoula.userPolicy.service",
        "org.scoula.codef",
        "org.scoula.push.service", // push 서비스 스캔 추가
        "org.scoula.push.config", // push config 스캔 추가
        "org.scoula.policyInteraction.service", // 정책 상호작용 서비스 스캔 추가
        "org.scoula.policy.util", // 정책 데이터 홀더 스캔 추가
        "org.scoula.asset",
        "org.scoula.userPolicy.util" // 사용자 정책 유틸리티 스캔 추가
})
@EnableScheduling // 스케줄링 기능 활성화
@EnableRetry // 재시도(@Retryable) 기능 활성화
@EnableAsync // 비동기(@Async) 기능 활성화
public class RootConfig {

    // 현재는 기본 설정만 있는 상태
    // 프로젝트 발전에 따라 다음과 같은 빈들을 추가할 수 있습니다:
    // - 데이터베이스 설정
    // - 트랜잭션 관리
    // - 보안 설정
    // - 외부 API 클라이언트
    // - 캐시 설정
    // - 스케줄링 설정


    @Value("${jdbc.driver}")
    String driver;
    @Value("${jdbc.url}")
    String url;
    @Value("${jdbc.username}")
    String username;
    @Value("${jdbc.password}")
    String password;
    @Autowired
    ApplicationContext applicationContext;


    /**
     * HikariCP 커넥션 풀을 사용한 DataSource 빈 생성
     *
     * @return 설정된 DataSource 객체
     */
    @Bean
    public DataSource dataSource() {
        // HikariCP 설정 객체 생성
        HikariConfig config = new HikariConfig();

        // 데이터베이스 연결 정보 설정
        config.setDriverClassName(driver);          // JDBC 드라이버 클래스
        config.setJdbcUrl(url);                    // 데이터베이스 URL
        config.setUsername(username);              // 사용자명
        config.setPassword(password);              // 비밀번호

        // 커넥션 풀 추가 설정 (선택사항)
        config.setMaximumPoolSize(10);             // 최대 커넥션 수
        config.setMinimumIdle(5);                  // 최소 유지 커넥션 수
        config.setConnectionTimeout(30000);       // 연결 타임아웃 (30초)
        config.setIdleTimeout(600000);            // 유휴 타임아웃 (10분)

        // HikariDataSource 생성 및 반환
        HikariDataSource dataSource = new HikariDataSource(config);
        return dataSource;
    }

    /**
     * SqlSessionFactory 빈 등록
     * - MyBatis의 핵심 팩토리 객체를 스프링 컨테이너에 등록
     *
     * @param dataSource 위 dataSource() 메서드에서 등록된 bean이 주입됨
     */
    @Bean
    public SqlSessionFactory sqlSessionFactory(DataSource dataSource) throws Exception {
        SqlSessionFactoryBean sqlSessionFactory = new SqlSessionFactoryBean();

        // MyBatis 설정 파일 위치 지정
        sqlSessionFactory.setConfigLocation(applicationContext.getResource("classpath:/mybatis-config.xml"));

        // 데이터베이스 연결 설정
        sqlSessionFactory.setDataSource(dataSource);

        return sqlSessionFactory.getObject();
    }

    /**
     * 트랜잭션 매니저 설정
     * - 데이터베이스 트랜잭션을 스프링이 관리하도록 설정
     */
    @Bean
    public DataSourceTransactionManager transactionManager() {
        DataSourceTransactionManager manager = new DataSourceTransactionManager(dataSource());
        return manager;
    }

    /**
     * FCM 전송 전용 비동기 스레드풀 설정
     * - Firebase Cloud Messaging 전송을 위한 전용 ThreadPoolTaskExecutor
     */
    @Bean(name = "fcmTaskExecutor")
    public Executor fcmTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        // 스레드풀 크기 설정
        executor.setCorePoolSize(8);           // 기본 스레드 수 (동시 FCM 전송 수)
        executor.setMaxPoolSize(16);           // 최대 스레드 수 (피크 시간대 대응)
        executor.setQueueCapacity(500);        // 대기 큐 크기 (대기 중인 FCM 전송 수)
        
        // 스레드 설정
        executor.setThreadNamePrefix("FCM-Async-");  // 스레드 이름 (디버깅 용이)
        executor.setKeepAliveSeconds(60);            // 유휴 스레드 유지 시간 (60초)
        executor.setWaitForTasksToCompleteOnShutdown(true);  // 종료 시 대기 중인 작업 완료
        executor.setAwaitTerminationSeconds(60);     // 종료 대기 시간 (60초)
        
        // 스레드풀 초기화
        executor.initialize();
        
        log.info("🚀 [FCM 스레드풀] 초기화 완료 - Core: {}, Max: {}, Queue: {}", 
                executor.getCorePoolSize(), executor.getMaxPoolSize(), executor.getQueueCapacity());
        
        return executor;
    }

}
