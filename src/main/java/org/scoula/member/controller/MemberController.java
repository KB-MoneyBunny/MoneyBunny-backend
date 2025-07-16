package org.scoula.member.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scoula.common.util.UploadFiles;
import org.scoula.member.dto.MemberDTO;
import org.scoula.member.dto.MemberJoinDTO;
import org.scoula.member.service.MemberService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.File;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/member")
public class MemberController {
  final MemberService service;

  // ID 중복 체크 API
  @GetMapping("/checkusername/{username}")
  public ResponseEntity<Boolean> checkUsername(@PathVariable String username) {
    return ResponseEntity.ok().body(service.checkDuplicate(username));
  }

  // 회원가입 API
  @PostMapping("")
  public ResponseEntity<MemberDTO> join(MemberJoinDTO member) {
    return ResponseEntity.ok(service.join(member));
  }


  // 아바타 이미지 다운로드 API
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
}