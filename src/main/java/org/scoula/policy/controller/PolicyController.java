package org.scoula.policy.controller;

import lombok.RequiredArgsConstructor;
import org.scoula.policy.service.PolicyService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/policy")
@RequiredArgsConstructor
public class PolicyController {

    private final PolicyService policyService;

    @GetMapping(value = "/sync", produces = "text/plain;charset=UTF-8")
    public String syncPolicyData() {
        System.out.println("test!!");
        policyService.fetchAndSaveAllPolicies();
        return "정책 수집 및 저장 완료!";
    }

}
