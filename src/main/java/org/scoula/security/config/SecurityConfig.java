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
        filter.setFilterProcessesUrl("/api/member/login");
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
                .antMatchers(HttpMethod.OPTIONS).permitAll()
                .antMatchers("/codef/**").permitAll()  // ✅ 요 줄 추가!!
                .antMatchers("/api/member/**").permitAll()
                .antMatchers("/api/policy/**").authenticated() // 정책 API 임시 허용
                .antMatchers("/api/push/**").authenticated()
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
                "/*",
                "/admin/policy/sync",
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

//    // Spring Security 검사를 우회할 경로 설정
//    @Override
//    public void configure(WebSecurity web) throws Exception {
//        web.ignoring().antMatchers(
//                "/assets/**",      // 정적 리소스
//                "/*",              // 루트 경로의 파일들
//                "/api/member/**",   // 회원 관련 공개 API
//                "/api/userPolicy/**", // 사용자 정책 API 임시 허용
//
//                // 정책 수집 테스트용 경로 추가
//                "/admin/policy/sync",
//
//                // Swagger 관련 URL은 보안에서 제외
//                "/swagger-ui.html", "/webjars/**",
//                "/swagger-resources/**", "/v2/api-docs"
//        );
//    }
}
