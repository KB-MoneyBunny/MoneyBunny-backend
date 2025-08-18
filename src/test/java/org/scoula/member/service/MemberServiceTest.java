package org.scoula.member.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.scoula.member.dto.MemberDTO;
import org.scoula.member.dto.MemberJoinDTO;
import org.scoula.member.mapper.MemberMapper;
import org.scoula.security.account.domain.MemberVO;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Date;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MemberService 단위 테스트")
class MemberServiceTest {

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private MemberMapper memberMapper;

    @InjectMocks
    private MemberServiceImpl memberService;

    private String username;
    private String email;
    private String password;
    private MemberVO memberVO;
    private MemberJoinDTO memberJoinDTO;
    private MemberDTO memberDTO;

    @BeforeEach
    void setUp() {
        username = "testuser";
        email = "test@example.com";
        password = "Password123!";

        memberVO = MemberVO.builder()
                .userId(1L)
                .loginId(username)
                .email(email)
                .password("encodedPassword")
                .name("테스트사용자")
                .profileImageId(1)
                .createdAt(new Date())
                .build();

        memberJoinDTO = MemberJoinDTO.builder()
                .loginId(username)
                .email(email)
                .password(password)
                .name("테스트사용자")
                .build();

        memberDTO = MemberDTO.builder()
                .userId("1")
                .loginId(username)
                .email(email)
                .name("테스트사용자")
                .profileImageId(1)
                .createdAt(new Date())
                .build();
    }

    // ====================================
    // ID 중복 체크 테스트
    // ====================================

    @Test
    @DisplayName("ID 중복 체크 - 중복됨")
    void checkDuplicate_Exists() {
        // Given
        when(memberMapper.findByUsername(username)).thenReturn(memberVO);

        // When
        boolean result = memberService.checkDuplicate(username);

        // Then
        assertTrue(result);
        verify(memberMapper).findByUsername(username);
    }

    @Test
    @DisplayName("ID 중복 체크 - 중복 안됨")
    void checkDuplicate_NotExists() {
        // Given
        when(memberMapper.findByUsername(username)).thenReturn(null);

        // When
        boolean result = memberService.checkDuplicate(username);

        // Then
        assertFalse(result);
        verify(memberMapper).findByUsername(username);
    }

    // ====================================
    // 회원 정보 조회 테스트
    // ====================================

    @Test
    @DisplayName("회원 정보 조회 - 성공")
    void get_Success() {
        // Given
        when(memberMapper.get(username)).thenReturn(memberVO);

        // When
        MemberDTO result = memberService.get(username);

        // Then
        assertNotNull(result);
        assertEquals(username, result.getLoginId());
        assertEquals(email, result.getEmail());
        verify(memberMapper).get(username);
    }

    @Test
    @DisplayName("회원 정보 조회 - 회원 없음")
    void get_MemberNotFound() {
        // Given
        when(memberMapper.get(username)).thenReturn(null);

        // When & Then
        assertThrows(NoSuchElementException.class, () -> memberService.get(username));
        verify(memberMapper).get(username);
    }

    // ====================================
    // 사용자명으로 회원 조회 테스트
    // ====================================

    @Test
    @DisplayName("사용자명으로 회원 조회 - 성공")
    void findByUsername_Success() {
        // Given
        when(memberMapper.findByUsername(username)).thenReturn(memberVO);

        // When
        MemberVO result = memberService.findByUsername(username);

        // Then
        assertNotNull(result);
        assertEquals(username, result.getLoginId());
        verify(memberMapper).findByUsername(username);
    }

    // ====================================
    // 회원 가입 테스트
    // ====================================

    @Test
    @DisplayName("회원 가입 - 성공")
    void join_Success() {
        // Given
        when(memberMapper.getByEmail(email)).thenReturn(null);
        when(passwordEncoder.encode(password)).thenReturn("encodedPassword");
        when(memberMapper.get(username)).thenReturn(memberVO);

        // When
        MemberDTO result = memberService.join(memberJoinDTO);

        // Then
        assertNotNull(result);
        assertEquals(username, result.getLoginId());
        verify(memberMapper).getByEmail(email);
        verify(passwordEncoder).encode(password);
        verify(memberMapper).insert(any(MemberVO.class));
        verify(memberMapper).get(username);
    }

    @Test
    @DisplayName("회원 가입 - 이메일 중복")
    void join_DuplicateEmail() {
        // Given
        when(memberMapper.getByEmail(email)).thenReturn(memberVO);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> memberService.join(memberJoinDTO));
        verify(memberMapper).getByEmail(email);
        verify(memberMapper, never()).insert(any());
    }

    @Test
    @DisplayName("회원 가입 - 유효하지 않은 이메일")
    void join_InvalidEmail() {
        // Given
        MemberJoinDTO invalidEmailDto = MemberJoinDTO.builder()
                .loginId(username)
                .email("invalid-email")
                .password(password)
                .name("테스트사용자")
                .build();

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> memberService.join(invalidEmailDto));
        verify(memberMapper, never()).insert(any());
    }

    @Test
    @DisplayName("회원 가입 - 비밀번호 너무 짧음")
    void join_PasswordTooShort() {
        // Given
        MemberJoinDTO shortPasswordDto = MemberJoinDTO.builder()
                .loginId(username)
                .email(email)
                .password("123")
                .name("테스트사용자")
                .build();

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> memberService.join(shortPasswordDto));
        verify(memberMapper, never()).insert(any());
    }

    @Test
    @DisplayName("회원 가입 - 비밀번호 복잡성 요구사항 미충족")
    void join_PasswordComplexityNotMet() {
        // Given
        MemberJoinDTO simplePasswordDto = MemberJoinDTO.builder()
                .loginId(username)
                .email(email)
                .password("password123")  // 특수문자 없음
                .name("테스트사용자")
                .build();

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> memberService.join(simplePasswordDto));
        verify(memberMapper, never()).insert(any());
    }

    // ====================================
    // 가입 정보 유효성 검사 테스트
    // ====================================

    @Test
    @DisplayName("가입 정보 유효성 검사 - 성공")
    void validateJoinInfo_Success() {
        // Given
        when(memberMapper.getByEmail(email)).thenReturn(null);

        // When & Then
        assertDoesNotThrow(() -> memberService.validateJoinInfo(memberJoinDTO));
        verify(memberMapper).getByEmail(email);
    }

    @Test
    @DisplayName("가입 정보 유효성 검사 - null 이메일")
    void validateJoinInfo_NullEmail() {
        // Given
        MemberJoinDTO nullEmailDto = MemberJoinDTO.builder()
                .loginId(username)
                .email(null)
                .password(password)
                .name("테스트사용자")
                .build();

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> memberService.validateJoinInfo(nullEmailDto));
    }

    @Test
    @DisplayName("가입 정보 유효성 검사 - null 비밀번호")
    void validateJoinInfo_NullPassword() {
        // Given
        MemberJoinDTO nullPasswordDto = MemberJoinDTO.builder()
                .loginId(username)
                .email(email)
                .password(null)
                .name("테스트사용자")
                .build();

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> memberService.validateJoinInfo(nullPasswordDto));
    }

    // ====================================
    // 이메일 존재 여부 확인 테스트
    // ====================================

    @Test
    @DisplayName("이메일 존재 여부 확인 - 존재함")
    void isEmailExists_Exists() {
        // Given
        when(memberMapper.getByEmail(email)).thenReturn(memberVO);

        // When
        boolean result = memberService.isEmailExists(email);

        // Then
        assertTrue(result);
        verify(memberMapper).getByEmail(email);
    }

    @Test
    @DisplayName("이메일 존재 여부 확인 - 존재하지 않음")
    void isEmailExists_NotExists() {
        // Given
        when(memberMapper.getByEmail(email)).thenReturn(null);

        // When
        boolean result = memberService.isEmailExists(email);

        // Then
        assertFalse(result);
        verify(memberMapper).getByEmail(email);
    }

    // ====================================
    // 프로필 이미지 업데이트 테스트
    // ====================================

    @Test
    @DisplayName("프로필 이미지 업데이트 - 성공")
    void updateProfileImage_Success() {
        // Given
        int profileImageId = 2;
        when(memberMapper.updateProfileImage(username, profileImageId)).thenReturn(1);
        when(memberMapper.findByUsername(username)).thenReturn(memberVO);

        // When
        MemberDTO result = memberService.updateProfileImage(username, profileImageId);

        // Then
        assertNotNull(result);
        assertEquals(username, result.getLoginId());
        verify(memberMapper).updateProfileImage(username, profileImageId);
        verify(memberMapper).findByUsername(username);
    }

    // ====================================
    // 비밀번호 복잡성 검증 테스트
    // ====================================

    @Test
    @DisplayName("비밀번호 복잡성 검증 - 영문만 있는 경우")
    void validatePassword_OnlyLetters() {
        // Given
        MemberJoinDTO onlyLettersDto = MemberJoinDTO.builder()
                .loginId(username)
                .email(email)
                .password("abcdefgh")
                .name("테스트사용자")
                .build();

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> memberService.validateJoinInfo(onlyLettersDto));
    }

    @Test
    @DisplayName("비밀번호 복잡성 검증 - 숫자만 있는 경우")
    void validatePassword_OnlyNumbers() {
        // Given
        MemberJoinDTO onlyNumbersDto = MemberJoinDTO.builder()
                .loginId(username)
                .email(email)
                .password("12345678")
                .name("테스트사용자")
                .build();

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> memberService.validateJoinInfo(onlyNumbersDto));
    }

    @Test
    @DisplayName("비밀번호 복잡성 검증 - 특수문자만 있는 경우")
    void validatePassword_OnlySpecialChars() {
        // Given
        MemberJoinDTO onlySpecialDto = MemberJoinDTO.builder()
                .loginId(username)
                .email(email)
                .password("!@#$%^&*")
                .name("테스트사용자")
                .build();

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> memberService.validateJoinInfo(onlySpecialDto));
    }
}