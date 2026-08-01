package com.mediahub.auditlog.repository;

import com.mediahub.auditlog.entity.CompliancePolicy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CompliancePolicyRepository
        extends JpaRepository<CompliancePolicy, Long> {
    Optional<CompliancePolicy> findByPolicyName(String policyName);
    Page<CompliancePolicy> findByIsActiveTrue(Pageable pageable);
    List<CompliancePolicy> findByIsActiveTrue();
    Page<CompliancePolicy> findByPolicyType(
        CompliancePolicy.PolicyType policyType, Pageable pageable);
}