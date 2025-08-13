package org.scoula.security.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.scoula.security.filter.AuthenticationErrorFilter;
import org.scoula.security.filter.JwtAuthenticationFilter;
import org.scoula.security.filter.JwtUsernamePasswordAuthenticationFilter;
import org.scoula.security.handler.CustomAccessDeniedHandler;
import org.scoula.security.handler.CustomAuthenticationEntryPoint;
import org.scoula.security.handler.LoginFailureHandler;
import org.scoula.security.handler.LoginSuccessHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.filter.CorsFilter;

@Slf4j
@Configuration
@EnableWebSecurity
@MapperScan(basePackages = {"org.scoula.security.account.mapper"})
@ComponentScan(basePackages = {"org.scoula.security"})
@RequiredArgsConstructor
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final UserDetailsService userDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final AuthenticationErrorFilter authenticationErrorFilter;
    private final CustomAccessDeniedHandler accessDeniedHandler;
    private final CustomAuthenticationEntryPoint authenticationEntryPoint;
    private final LoginSuccessHandler loginSuccessHandler;
    private final LoginFailureHandler loginFailureHandler;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    public CharacterEncodingFilter encodingFilter() {
        CharacterEncodingFilter encodingFilter = new CharacterEncodingFilter();
        encodingFilter.setEncoding("UTF-8");
        encodingFilter.setForceEncoding(true);
        return encodingFilter;
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManager() throws Exception {
        return super.authenticationManager();
    }

    @Bean
    public JwtUsernamePasswordAuthenticationFilter jwtUsernamePasswordAuthenticationFilter() throws Exception {
        JwtUsernamePasswordAuthenticationFilter filter =
                new JwtUsernamePasswordAuthenticationFilter(authenticationManager(), loginSuccessHandler, loginFailureHandler);
        filter.setFilterProcessesUrl("/api/auth/login");
        return filter;
    }

    /**
     * HTTP 보안 설정 메서드 (웹 애플리케이션의 보안 정책을 상세하게 구성)
     * @param http HttpSecurity 객체
     * @throws Exception 설정 중 발생할 수 있는 예외
     */
    @Override
    public void configure(HttpSecurity http) throws Exception {

        http
                .addFilterBefore(encodingFilter(), CsrfFilter.class)// 한글 인코딩 필터 설정
                .addFilterBefore(authenticationErrorFilter, UsernamePasswordAuthenticationFilter.class) // 인증 에러 필터
                .addFilterAt(jwtUsernamePasswordAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class) // 로그인 필터
                .addFilterBefore(jwtAuthenticationFilter, JwtUsernamePasswordAuthenticationFilter.class)  // JWT 인증 필터

                // 예외 처리 설정
                .exceptionHandling()
                .authenticationEntryPoint(authenticationEntryPoint)  // 401 에러 처리
                .accessDeniedHandler(accessDeniedHandler);           // 403 에러 처리


        //  HTTP 보안 설정
        http.httpBasic().disable()      // 기본 HTTP 인증 비활성화
                .csrf().disable()           // CSRF 보호 비활성화 (REST API에서는 불필요)
                .formLogin().disable()      // 폼 로그인 비활성화 (JSON 기반 API 사용)
                .sessionManagement()        // 세션 관리 설정
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS);  // 무상태 모드


        http
                .authorizeRequests() // 경로별 접근 권한 설정

                // 공통/프리플라이트
                .antMatchers(HttpMethod.OPTIONS).permitAll()

                // 인증/로그인
                .antMatchers("/api/auth/**").permitAll()

                // 인증 후 회원 가입 및 회원정보 조회
                .antMatchers("/api/member/**").permitAll()

                // 외부 연동(Codef)🎵
                .antMatchers("/codef/**").authenticated()

                // 자산 🎵
                .antMatchers("/api/asset/**").authenticated()

                // 게스트 정책 검색
                .antMatchers("/api/guestPolicy/**").permitAll()

                // 정책 상호작용 - 미완료 신청 조회 🎵
                .antMatchers("/api/policy-interaction/application/incomplete").authenticated()

                // 정책 상세/공유 URL (비로그인 허용)
                .antMatchers(HttpMethod.GET, "/api/policy/*").permitAll() // 공유 URL 로그인 X
                .antMatchers(HttpMethod.GET, "/api/policy/detail/**").permitAll()

                // 정책 리뷰(비로그인 허용)
                .antMatchers(HttpMethod.GET, "/api/policy-interaction/review/*/list").permitAll() // 💪(상일) 정책 리뷰 목록 조회 허용

                // 정책 API
                .antMatchers("/api/policy/**").authenticated() // 정책 API 임시 허용
                // 푸시 알림
                .antMatchers("/api/push/**").authenticated()
                // 사용자 정책
                .antMatchers("/api/userPolicy/**").authenticated() // 사용자 정책 API 임시 허용

                .anyRequest().authenticated(); // 현재는 모든 접근 허용 (개발 단계)
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        log.info("configure AuthenticationManagerBuilder");
        auth.userDetailsService(userDetailsService)
                .passwordEncoder(passwordEncoder());
    }

    @Override
    public void configure(WebSecurity web) {
        web.ignoring().antMatchers(
                "/assets/**",
//                "/*",
//                "/admin/policy/**",
                "/api/admin/prompt/**",
                "/policy/*/reviews", // 💪(상일) 정책 리뷰 페이지 허용

                // swagger 관련
                "/swagger-ui.html", "/webjars/**",
                "/swagger-resources/**", "/v2/api-docs"

        );
    }

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.addAllowedOriginPattern("*");
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }

}
