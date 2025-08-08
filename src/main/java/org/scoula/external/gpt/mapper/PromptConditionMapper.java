package org.scoula.external.gpt.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.scoula.external.gpt.domain.PromptCalculationRuleVO;
import org.scoula.external.gpt.domain.PromptConditionVO;
import org.scoula.external.gpt.domain.PromptExampleVO;
import org.scoula.external.gpt.domain.PromptConditionType;

import java.util.List;

@Mapper
public interface PromptConditionMapper {

    // ========== 조건 관련 메서드 ==========
    
    // 모든 조건 조회
    List<PromptConditionVO> findAllConditions();

    // 타입별 조건 조회
    List<PromptConditionVO> findConditionsByType(@Param("type") PromptConditionType type);

    // 조건 추가
    int insertCondition(PromptConditionVO condition);

    // 조건 업데이트
    int updateCondition(PromptConditionVO condition);

    // 조건 삭제
    int deleteCondition(@Param("id") Long id);
    
    // ========== 계산 규칙 관련 메서드 ==========
    
    // 모든 계산 규칙 조회
    List<PromptCalculationRuleVO> findAllCalculationRules();
    
    // 계산 규칙 추가
    int insertCalculationRule(PromptCalculationRuleVO rule);
    
    // 계산 규칙 업데이트
    int updateCalculationRule(PromptCalculationRuleVO rule);
    
    // 계산 규칙 삭제
    int deleteCalculationRule(@Param("id") Long id);
    
    // ========== 예시 관련 메서드 ==========
    
    // 모든 예시 조회
    List<PromptExampleVO> findAllExamples();
    
    // 타입별 예시 조회
    List<PromptExampleVO> findExamplesByType(@Param("type") PromptConditionType type);
    
    // 예시 추가
    int insertExample(PromptExampleVO example);
    
    // 예시 업데이트
    int updateExample(PromptExampleVO example);
    
    // 예시 삭제
    int deleteExample(@Param("id") Long id);
}