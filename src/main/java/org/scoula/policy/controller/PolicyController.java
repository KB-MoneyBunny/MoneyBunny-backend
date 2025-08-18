package org.scoula.policy.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scoula.policy.dto.PolicyDetailDTO;
import org.scoula.policy.service.PolicyService;
import org.scoula.security.account.domain.CustomUser;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

@RestController
@RequestMapping("/api/policy")
@RequiredArgsConstructor
@Slf4j                             // 로깅 기능
@Api(
        tags = "정책 관리 API",                    // 그룹 이름 (필수)
        description = "정책 CRUD API",        // 상세 설명
        value = "PolicyController"              // 컨트롤러 식별자
)
public class PolicyController {

    private final PolicyService policyService;

    @ApiOperation(value = "정책 수집 및 저장", notes = "정책 수집 및 저장을 수행하는 API")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "성공적으로 요청이 처리되었습니다."),
            @ApiResponse(code = 400, message = "잘못된 요청입니다."),
            @ApiResponse(code = 500, message = "서버에서 오류가 발생했습니다.")
    })
    @GetMapping(value = "/sync", produces = "text/plain;charset=UTF-8")
    public String syncPolicyData() {
        policyService.fetchAndSaveAllPolicies();
        return "정책 수집 및 저장 완료!";
    }

    @ApiOperation(value = "사용자용 정책 상세 조회", notes = "정책 상세 정보를 조회하며, 사용자 조회 기록을 Redis에 저장하고 벡터를 갱신합니다")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "성공적으로 요청이 처리되었습니다.", response = PolicyDetailDTO.class),
            @ApiResponse(code = 404, message = "정책을 찾을 수 없습니다."),
            @ApiResponse(code = 500, message = "서버에서 오류가 발생했습니다.")
    })
    @GetMapping("/detail/{policyId}")
    public ResponseEntity<PolicyDetailDTO> getPolicyDetail(
            @PathVariable String policyId,
            @ApiIgnore @AuthenticationPrincipal CustomUser customUser) {

        // customUser가 null이면 userId도 null로 처리
        Long userId = null;
        if (customUser != null && customUser.getMember() != null) {
            userId = customUser.getMember().getUserId();
        }

        try {
            // userId가 null이면 tracking 로직(조회수 증가·벡터 갱신)을 생략하도록 서비스 구현
            PolicyDetailDTO policyDetail =
                    policyService.getPolicyDetailWithTracking(policyId, userId);

            if (policyDetail == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(policyDetail);

        } catch (IllegalArgumentException e) {
            log.error("잘못된 정책 ID 형식 - policyId: {}", policyId);
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("정책 상세 조회 실패 - policyId: {}, 오류: {}", policyId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

}
