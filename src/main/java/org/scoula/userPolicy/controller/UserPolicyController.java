package org.scoula.userPolicy.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scoula.policy.domain.YouthPolicyVO;
import org.scoula.security.account.domain.CustomUser;
import org.scoula.userPolicy.dto.SearchRequestDTO;
import org.scoula.userPolicy.dto.SearchResultDTO;
import org.scoula.userPolicy.dto.UserPolicyDTO;
import org.scoula.userPolicy.service.UserPolicyService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import springfox.documentation.annotations.ApiIgnore;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequestMapping("/api/userPolicy")
@RequiredArgsConstructor
@Slf4j                             // 로깅 기능
@Api(
        tags = "유저 정책 조건 관리 API",                    // 그룹 이름 (필수)
        description = "유저 정책 조건 CRUD API",        // 상세 설명
        value = "UserPolicyController"              // 컨트롤러 식별자
)
public class UserPolicyController {

    private final UserPolicyService userPolicyService;

    /**
     * 사용자 정책 조건 조회 API
     * GET: http://localhost:8080/api/userPolicy
     * @return ResponseEntity
     *         - 200 OK: 사용자 정책 조건 조회 성공시 UserPolicyDTO 반환
     *         - 404 Not Found: 해당 사용자의 정책 조건을 찾을 수 없음
     *         - 500 Internal Server Error: 서버 내부 오류 발생 시
     */
    @ApiOperation(value = "사용자 정책 조건 조회", notes = "사용자의 정책 조건을 조회하는 API")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "성공적으로 요청이 처리되었습니다.", response = UserPolicyDTO.class),
            @ApiResponse(code = 404, message = "리소스를 찾을 수 없습니다."),
            @ApiResponse(code = 500, message = "서버에서 오류가 발생했습니다.")
    })
    @GetMapping("")
    public ResponseEntity<UserPolicyDTO> getUserPolicyCondition(@ApiIgnore @AuthenticationPrincipal CustomUser customUser) {
        String username = customUser.getUsername();
        UserPolicyDTO userPolicyDTO = userPolicyService.getUserPolicyCondition(username);
        if (userPolicyDTO == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(userPolicyDTO);
    }

    /**
     * 사용자 정책 조건 저장 API
     * POST: http://localhost:8080/api/userPolicy
     * @return ResponseEntity
     *         - 200 OK: 사용자 정책 조건 생성 성공시 사용자 정책 DTO 반환
     *         - 400 Bad Request: 잘못된 요청 데이터 (조건 누락 등)
     *         - 500 Internal Server Error: 서버 내부 오류 발생 시
     */
    @ApiOperation(value = "사용자 정책 조건 생성", notes = "새로운 사용자 정책 조건을 생성하는 API")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "성공적으로 요청이 처리되었습니다.", response = UserPolicyDTO.class),
            @ApiResponse(code = 400, message = "잘못된 요청입니다."),
            @ApiResponse(code = 500, message = "서버에서 오류가 발생했습니다.")
    })
    @PostMapping("")
    public ResponseEntity<Void> saveUserPolicyCondition(@ApiIgnore @AuthenticationPrincipal CustomUser customUser, @RequestBody UserPolicyDTO userPolicyDTO) {
        String username = customUser.getUsername();
        userPolicyService.saveUserPolicyCondition(username,userPolicyDTO);
        return ResponseEntity.ok().build();
    }

    /**
     * 사용자 정책 조건 수정 API
     * PUT: http://localhost:8080/api/userPolicy
     * @return ResponseEntity
     *         - 200 OK: 사용자 정책 조건 수정 성공시 사용자 정책 DTO 반환
     *         - 400 Bad Request: 잘못된 요청 데이터 (조건 누락 등)
     *         - 404 Not Found: 해당 사용자의 정책 조건을 찾을 수 없음
     *         - 500 Internal Server Error: 서버 내부 오류 발생 시
     */
    @ApiOperation(value = "사용자 정책 조건 수정", notes = "기존 사용자 정책 조건을 수정하는 API")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "성공적으로 요청이 처리되었습니다.", response = UserPolicyDTO.class),
            @ApiResponse(code = 400, message = "잘못된 요청입니다."),
            @ApiResponse(code = 404, message = "리소스를 찾을 수 없습니다."),
            @ApiResponse(code = 500, message = "서버에서 오류가 발생했습니다.")
    })
    @PutMapping("")
    public ResponseEntity<Void> updateUserPolicyCondition(@AuthenticationPrincipal CustomUser customUser, @RequestBody UserPolicyDTO userPolicyDTO) {
        String username = customUser.getUsername();
        userPolicyService.updateUserPolicyCondition(username, userPolicyDTO);
        return ResponseEntity.ok().build();
    }


    /**
     * 사용자 정책 조건에 맞는 정책 검색 API
     * POST: http://localhost:8080/api/userPolicy/search
     * @return ResponseEntity
     *         - 200 OK: 정책 검색 성공시 SearchResultDTO 리스트 반환
     *         - 400 Bad Request: 잘못된 요청 데이터 (검색 조건 누락 등)
     *         - 500 Internal Server Error: 서버 내부 오류 발생 시
     */
    @ApiOperation(value = "사용자 정책 조건에 맞는 정책 검색", notes = "사용자 정책 조건에 맞는 정책을 검색하는 API")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "성공적으로 요청이 처리되었습니다.", response = SearchResultDTO.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "잘못된 요청입니다."),
            @ApiResponse(code = 500, message = "서버에서 오류가 발생했습니다.")
    })
    @PostMapping("/search")
    public ResponseEntity<List<SearchResultDTO>> searchFilteredPolicy(@ApiIgnore @AuthenticationPrincipal CustomUser customUser, @RequestBody SearchRequestDTO searchRequestDTO) {
        String username = customUser.getUsername();
        List<SearchResultDTO> searchResultDTO=userPolicyService.searchFilteredPolicy(username, searchRequestDTO);
        return ResponseEntity.ok(searchResultDTO);
    }


}
