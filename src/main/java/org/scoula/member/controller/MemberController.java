package org.scoula.member.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scoula.common.util.RedisUtil;
import org.scoula.common.util.UploadFiles;
import org.scoula.member.dto.*;
import org.scoula.security.service.MailService;
import org.scoula.member.service.MemberService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.scoula.security.util.PasswordValidator;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/member")
@Api(tags = "회원 정보 API", description = "회원가입, 중복확인, 아바타 이미지 관련 API")
public class MemberController {

  private final MemberService service;

  // ID 중복 체크
  @ApiOperation(
          value = "ID 중복 체크",
          notes = "회원가입 시 사용자의 ID(로그인ID)가 이미 존재하는지 확인합니다."
  )
  @GetMapping("/checkusername/{username}")
  public ResponseEntity<Boolean> checkUsername(@PathVariable String username) {
    return ResponseEntity.ok().body(service.checkDuplicate(username));
  }

  // 회원가입
  @ApiOperation(
          value = "회원가입",
          notes = "신규 사용자의 회원가입을 처리합니다. 사용자 정보와 비밀번호를 포함한 데이터를 전송해야 합니다."
  )
  @PostMapping("/join")
  public ResponseEntity<?> join(@RequestBody MemberJoinDTO member) {
    // 비밀번호 정규식 유효성 검사
    if (!PasswordValidator.isValid(member.getPassword())) {
      return ResponseEntity.badRequest().body("비밀번호 형식이 올바르지 않습니다.");
    }

    // 회원가입 처리
    MemberDTO joined = service.join(member);
    return ResponseEntity.ok(joined);
  }


  // 아바타 이미지 다운로드
  @ApiOperation(
          value = "아바타 이미지 다운로드",
          notes = "사용자의 아바타 이미지를 다운로드합니다. 등록된 이미지가 없는 경우 기본 이미지를 제공합니다."
  )
  @GetMapping("/{username}/avatar")
  public void getAvatar(@PathVariable String username, HttpServletResponse response) {
    String avatarPath = "c:/upload/avatar/" + username + ".png";
    File file = new File(avatarPath);

    if(!file.exists()) {
      // 아바타가 없는 경우 기본 이미지 사용
      file = new File("C:/upload/avatar/unknown.png");
    }

    UploadFiles.downloadImage(response, file);
  }

  // 이메일 중복 체크
  @ApiOperation(
          value = "이메일 중복 체크",
          notes = "회원가입 또는 아이디 찾기 시, 입력한 이메일이 이미 가입되어 있는지 확인합니다."
  )
  @GetMapping("/check-email")
  public ResponseEntity<Boolean> checkEmail(@RequestParam("email") String email) {
    boolean exists = service.isEmailExists(email);
    return ResponseEntity.ok(exists);
  }

  @ApiOperation(value = "내 프로필 조회", notes = "로그인된 사용자의 username, name, email 반환")
  @GetMapping("/information")
  public ResponseEntity<MemberDTO> get(Authentication auth) {
    // auth.getName() 으로 username 꺼내서 기존 service.get() 호출
    MemberDTO dto = service.get(auth.getName());

    return ResponseEntity.ok(dto);
  }

  // 프로필 이미지 아이디 변경
  @ApiOperation(
          value = "프로필 이미지 아이디 변경",
          notes = "로그인된 사용자의 프로필 이미지 아이디(0~3)만 숫자 하나로 변경합니다."
  )
  @ApiImplicitParams({
          @ApiImplicitParam(name = "imageId", value = "프로필 이미지 아이디(0~3)", required = true, dataType = "int", paramType = "path", example = "2")
  })
  @PatchMapping("/profile-image/{imageId}")
  public ResponseEntity<MemberDTO> updateProfileImageByPath(
          @ApiIgnore Authentication auth,     // 스웨거 문서에서 숨김
          @PathVariable int imageId
  ) {
    if (imageId < 0 || imageId > 3) {
      return ResponseEntity.badRequest().build();
    }
    MemberDTO updated = service.updateProfileImage(auth.getName(), imageId);
    return ResponseEntity.ok(updated);
  }



}