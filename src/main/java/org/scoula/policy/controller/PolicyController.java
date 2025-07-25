package org.scoula.policy.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scoula.policy.dto.PolicyDetailDTO;
import org.scoula.policy.service.PolicyService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/policy")
@RequiredArgsConstructor
@Slf4j                             // 로깅 기능
@Api(
        tags = "정책 관리",                    // 그룹 이름 (필수)
        description = "정책 CRUD API",        // 상세 설명
        value = "PolicyController"              // 컨트롤러 식별자
)
public class PolicyController {

    private final PolicyService policyService;

    /**
     * 정책 수집 및 저장 API
     * 이 API는 외부에서 정책 데이터를 수집하고, 이를 데이터베이스에 저장하는 기능을 제공합니다.
     * 정책 데이터는 외부 API를 통해 수집되며, 수집된 데이터는 YouthPolicyVO 객체로 변환되어 저장됩니다.
     * POST: http://localhost:8080/admin/policy/sync
     * @return ResponseEntity
     *         - 200 OK: 정책 데이터 수집 및 저장 성공시 "정책 수집 및 저장 완료!" 메시지 반환
     *         - 400 Bad Request: 잘못된 요청 데이터 (예: 외부 API 호출 실패 등)
     *         - 500 Internal Server Error: 서버 내부 오류 발생 시
     */
    @ApiOperation(value = "정책 수집 및 저장", notes = "정책 수집 및 저장을 수행하는 API")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "성공적으로 요청이 처리되었습니다."),
            @ApiResponse(code = 400, message = "잘못된 요청입니다."),
            @ApiResponse(code = 500, message = "서버에서 오류가 발생했습니다.")
    })
    @GetMapping(value = "/sync", produces = "text/plain;charset=UTF-8")
    public String syncPolicyData() {
        System.out.println("test!!");
        policyService.fetchAndSaveAllPolicies();
        return "정책 수집 및 저장 완료!";
    }

    /**
     * 정책 ID로 정책 조회 API
     * 이 API는 정책 ID를 기반으로 특정 정책을 조회하는 기능을 제공합니다.
     * 정책 ID는 URL 경로 변수로 전달되며, 해당 ID에 해당하는 정책 정보를 반환합니다.
     * GET: http://localhost:8080/admin/policy/{policyId}
     * @return ResponseEntity
     *        - 200 OK: 정책 조회 성공시 PolicyDetailDTO 객체 반환
     *        - 400 Bad Request: 잘못된 요청 데이터 (예: 존재하지 않는 정책 ID 등)
     *        - 500 Internal Server Error: 서버 내부 오류 발생 시
     */
    @ApiOperation(value = "정책 ID로 정책 조회", notes = "정책 ID를 기반으로 특정 정책을 조회하는 API")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "성공적으로 요청이 처리되었습니다.", response = PolicyDetailDTO.class),
            @ApiResponse(code = 400, message = "잘못된 요청입니다."),
            @ApiResponse(code = 500, message = "서버에서 오류가 발생했습니다.")
    })
    @GetMapping("/{policyId}")
    public PolicyDetailDTO getPolicyById(@PathVariable String policyId) {
        return policyService.getPolicyById(policyId);
    }
}
