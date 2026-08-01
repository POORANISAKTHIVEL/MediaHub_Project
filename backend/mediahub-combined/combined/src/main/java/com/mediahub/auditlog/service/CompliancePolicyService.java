package com.mediahub.auditlog.service;

import com.mediahub.auditlog.dto.PageResponse;
import com.mediahub.auditlog.entity.CompliancePolicy;
import com.mediahub.auditlog.repository.CompliancePolicyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CompliancePolicyService {

    @Autowired private CompliancePolicyRepository policyRepository;

    // ── GET all policies — paginated ───────────────────────────────────────
    public PageResponse<CompliancePolicy> getAllPolicies(int page, int size) {
        Pageable pageable = PageRequest.of(page, size,
            Sort.by("createdAt").descending());
        return PageResponse.of(policyRepository.findAll(pageable));
    }

    // ── GET active policies — paginated ────────────────────────────────────
    public PageResponse<CompliancePolicy> getActivePolicies(
            int page, int size) {
        Pageable pageable = PageRequest.of(page, size,
            Sort.by("createdAt").descending());
        return PageResponse.of(
            policyRepository.findByIsActiveTrue(pageable));
    }

    // ── GET single policy ──────────────────────────────────────────────────
    public CompliancePolicy getPolicyById(Long policyId) {
        return policyRepository.findById(policyId)
            .orElseThrow(() -> new RuntimeException("POLICY_NOT_FOUND"));
    }

    // ── POST create policy ─────────────────────────────────────────────────
    public CompliancePolicy createPolicy(CompliancePolicy policy) {
        if (policyRepository.findByPolicyName(
                policy.getPolicyName()).isPresent()) {
            throw new RuntimeException("POLICY_NAME_ALREADY_EXISTS");
        }
        return policyRepository.save(policy);
    }
}