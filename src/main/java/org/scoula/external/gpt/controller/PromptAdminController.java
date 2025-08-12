package org.scoula.external.gpt.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scoula.external.gpt.domain.PromptCalculationRuleVO;
import org.scoula.external.gpt.domain.PromptConditionVO;
import org.scoula.external.gpt.domain.PromptExampleVO;
import org.scoula.external.gpt.domain.PromptConditionType;
import org.scoula.external.gpt.mapper.PromptConditionMapper;
import org.scoula.external.gpt.service.PromptBuilderService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/api/admin/prompt", produces = "application/json; charset=UTF-8")
@RequiredArgsConstructor
@Slf4j
@Api(
    tags = "프롬프트 관리자 API",
    description = "GPT 프롬프트 조건, 예시, 계산 규칙 관리 API",
    value = "PromptAdminController"
)
public class PromptAdminController {

    private final PromptConditionMapper promptConditionMapper;
    private final PromptBuilderService promptBuilderService;

    // ========== 조건 관리 API ==========

    @GetMapping("/conditions")
    @ApiOperation(value = "조건 목록 조회", notes = "모든 프롬프트 조건을 조회합니다.")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "조건 목록 조회 성공"),
        @ApiResponse(code = 500, message = "서버 내부 오류")
    })
    public ResponseEntity<List<PromptConditionVO>> getConditions() {
        log.info("프롬프트 조건 목록 조회 요청");
        List<PromptConditionVO> conditions = promptConditionMapper.findAllConditions();
        return ResponseEntity.ok(conditions);
    }

    @PostMapping("/conditions")
    @ApiOperation(value = "조건 추가", notes = "새로운 프롬프트 조건을 추가합니다.")
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = "조건 추가 성공"),
        @ApiResponse(code = 400, message = "잘못된 요청 데이터"),
        @ApiResponse(code = 500, message = "서버 내부 오류")
    })
    public ResponseEntity<PromptConditionVO> createCondition(@RequestBody PromptConditionVO condition) {
        log.info("프롬프트 조건 추가 요청: {}", condition);
        promptConditionMapper.insertCondition(condition);
        return ResponseEntity.status(201).body(condition);
    }

    @PutMapping("/conditions/{id}")
    @ApiOperation(value = "조건 수정", notes = "기존 프롬프트 조건을 수정합니다.")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "조건 수정 성공"),
        @ApiResponse(code = 404, message = "조건을 찾을 수 없음"),
        @ApiResponse(code = 500, message = "서버 내부 오류")
    })
    public ResponseEntity<PromptConditionVO> updateCondition(@PathVariable Long id, @RequestBody PromptConditionVO condition) {
        log.info("프롬프트 조건 수정 요청 ID: {}, 데이터: {}", id, condition);
        condition.setId(id);
        promptConditionMapper.updateCondition(condition);
        return ResponseEntity.ok(condition);
    }

    @DeleteMapping("/conditions/{id}")
    @ApiOperation(value = "조건 삭제", notes = "프롬프트 조건을 삭제합니다.")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "조건 삭제 성공"),
        @ApiResponse(code = 404, message = "조건을 찾을 수 없음"),
        @ApiResponse(code = 500, message = "서버 내부 오류")
    })
    public ResponseEntity<Void> deleteCondition(@PathVariable Long id) {
        log.info("프롬프트 조건 삭제 요청 ID: {}", id);
        promptConditionMapper.deleteCondition(id);
        return ResponseEntity.ok().build();
    }

    // ========== 예시 관리 API ==========

    @GetMapping("/examples")
    @ApiOperation(value = "예시 목록 조회", notes = "모든 프롬프트 예시를 조회합니다.")
    public ResponseEntity<List<PromptExampleVO>> getExamples() {
        log.info("프롬프트 예시 목록 조회 요청");
        List<PromptExampleVO> examples = promptConditionMapper.findAllExamples();
        return ResponseEntity.ok(examples);
    }

    @PostMapping("/examples")
    @ApiOperation(value = "예시 추가", notes = "새로운 프롬프트 예시를 추가합니다.")
    public ResponseEntity<PromptExampleVO> createExample(@RequestBody PromptExampleVO example) {
        log.info("프롬프트 예시 추가 요청: {}", example);
        promptConditionMapper.insertExample(example);
        return ResponseEntity.status(201).body(example);
    }

    @PutMapping("/examples/{id}")
    @ApiOperation(value = "예시 수정", notes = "기존 프롬프트 예시를 수정합니다.")
    public ResponseEntity<PromptExampleVO> updateExample(@PathVariable Long id, @RequestBody PromptExampleVO example) {
        log.info("프롬프트 예시 수정 요청 ID: {}, 데이터: {}", id, example);
        example.setId(id);
        promptConditionMapper.updateExample(example);
        return ResponseEntity.ok(example);
    }

    @DeleteMapping("/examples/{id}")
    @ApiOperation(value = "예시 삭제", notes = "프롬프트 예시를 삭제합니다.")
    public ResponseEntity<Void> deleteExample(@PathVariable Long id) {
        log.info("프롬프트 예시 삭제 요청 ID: {}", id);
        promptConditionMapper.deleteExample(id);
        return ResponseEntity.ok().build();
    }

    // ========== 계산 규칙 관리 API ==========

    @GetMapping("/calculation-rules")
    @ApiOperation(value = "계산 규칙 목록 조회", notes = "모든 프롬프트 계산 규칙을 조회합니다.")
    public ResponseEntity<List<PromptCalculationRuleVO>> getCalculationRules() {
        log.info("프롬프트 계산 규칙 목록 조회 요청");
        List<PromptCalculationRuleVO> rules = promptConditionMapper.findAllCalculationRules();
        return ResponseEntity.ok(rules);
    }

    @PostMapping("/calculation-rules")
    @ApiOperation(value = "계산 규칙 추가", notes = "새로운 프롬프트 계산 규칙을 추가합니다.")
    public ResponseEntity<PromptCalculationRuleVO> createCalculationRule(@RequestBody PromptCalculationRuleVO rule) {
        log.info("프롬프트 계산 규칙 추가 요청: {}", rule);
        promptConditionMapper.insertCalculationRule(rule);
        return ResponseEntity.status(201).body(rule);
    }

    @PutMapping("/calculation-rules/{id}")
    @ApiOperation(value = "계산 규칙 수정", notes = "기존 프롬프트 계산 규칙을 수정합니다.")
    public ResponseEntity<PromptCalculationRuleVO> updateCalculationRule(@PathVariable Long id, @RequestBody PromptCalculationRuleVO rule) {
        log.info("프롬프트 계산 규칙 수정 요청 ID: {}, 데이터: {}", id, rule);
        rule.setId(id);
        promptConditionMapper.updateCalculationRule(rule);
        return ResponseEntity.ok(rule);
    }

    @DeleteMapping("/calculation-rules/{id}")
    @ApiOperation(value = "계산 규칙 삭제", notes = "프롬프트 계산 규칙을 삭제합니다.")
    public ResponseEntity<Void> deleteCalculationRule(@PathVariable Long id) {
        log.info("프롬프트 계산 규칙 삭제 요청 ID: {}", id);
        promptConditionMapper.deleteCalculationRule(id);
        return ResponseEntity.ok().build();
    }

    // ========== 유틸리티 API ==========

    @GetMapping(value = "/preview", produces = "text/plain; charset=UTF-8")
    @ApiOperation(value = "프롬프트 미리보기", notes = "현재 설정으로 생성되는 완전한 프롬프트를 미리 확인합니다.")
    public ResponseEntity<String> previewPrompt(@RequestParam String sampleContent) {
        log.info("프롬프트 미리보기 요청: {}", sampleContent);
        
        // PromptBuilderService를 사용하여 완전한 프롬프트 생성
        String fullPrompt = promptBuilderService.buildPromptOptimized(sampleContent);
        
        return ResponseEntity.ok()
                .contentType(MediaType.valueOf("text/plain; charset=UTF-8"))
                .body(fullPrompt);
    }
}