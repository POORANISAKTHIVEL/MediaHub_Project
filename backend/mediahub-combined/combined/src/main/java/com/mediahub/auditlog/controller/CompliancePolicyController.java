package com.mediahub.auditlog.controller;

import com.mediahub.auditlog.dto.PageResponse;
import com.mediahub.auditlog.entity.CompliancePolicy;
import com.mediahub.auditlog.service.CompliancePolicyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/mediaHub/auditlog/policies")
@PreAuthorize("hasRole('ADMIN')")
public class CompliancePolicyController {

    @Autowired private CompliancePolicyService policyService;

    // GET all policies — paginated
    @GetMapping("/getAllPolicies/v1.0")
    public ResponseEntity<Map<String, Object>> getAllPolicies(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {
        PageResponse<CompliancePolicy> result =
            policyService.getAllPolicies(page, size);
        return ResponseEntity.ok(Map.of(
            "message",       "Policies retrieved",
            "data",          result.getData(),
            "currentPage",   result.getCurrentPage(),
            "totalPages",    result.getTotalPages(),
            "totalElements", result.getTotalElements()));
    }

    // GET active policies — paginated
    @GetMapping("/getActivePolicies/v1.0")
    public ResponseEntity<Map<String, Object>> getActivePolicies(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {
        PageResponse<CompliancePolicy> result =
            policyService.getActivePolicies(page, size);
        return ResponseEntity.ok(Map.of(
            "message",       "Active policies retrieved",
            "data",          result.getData(),
            "currentPage",   result.getCurrentPage(),
            "totalPages",    result.getTotalPages(),
            "totalElements", result.getTotalElements()));
    }

    // GET single policy
    @GetMapping("/getPolicy/v1/{policyId}")
    public ResponseEntity<Map<String, Object>> getPolicy(
            @PathVariable Long policyId) {
        return ResponseEntity.ok(Map.of(
            "message", "Policy retrieved",
            "data",    policyService.getPolicyById(policyId)));
    }

    // POST create policy
    @PostMapping("/createPolicy/v1.0")
    public ResponseEntity<Map<String, Object>> createPolicy(
            @RequestBody CompliancePolicy policy) {
        return ResponseEntity.status(201).body(Map.of(
            "message", "Policy created successfully",
            "data",    policyService.createPolicy(policy)));
    }
}