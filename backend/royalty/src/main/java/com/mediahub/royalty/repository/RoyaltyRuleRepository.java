package com.mediahub.royalty.repository;

import com.mediahub.royalty.exception.ResourceNotFoundException;
import com.mediahub.royalty.model.RoyaltyRule;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class RoyaltyRuleRepository {

    @PersistenceContext
    private EntityManager entityManager;

    // API 1 — Save new rule
    @Transactional
    public void save(RoyaltyRule rule) {
        if (rule.getRuleID() == 0) {
            entityManager.persist(rule);
        } else {
            entityManager.merge(rule);
        }
    }

    // API 2 — Get all rules
    public List<RoyaltyRule> findAll() {
        TypedQuery<RoyaltyRule> query = entityManager.createQuery(
                "SELECT r FROM RoyaltyRule r", RoyaltyRule.class);
        return query.getResultList();
    }

    // API 3 — Get rule by ID
    public RoyaltyRule findById(int ruleID) {
        RoyaltyRule rule = entityManager.find(RoyaltyRule.class, ruleID);
        if (rule == null) {
            throw new ResourceNotFoundException(
                    "Royalty rule not found with ID: " + ruleID);
        }
        return rule;
    }

    // API 4 — Update status
    @Transactional
    public int updateStatus(int ruleID, String status) {
        return entityManager.createQuery(
                "UPDATE RoyaltyRule r SET r.status = :status WHERE r.ruleID = :ruleID")
                .setParameter("status", status)
                .setParameter("ruleID", ruleID)
                .executeUpdate();
    }

    // API 5 — Delete rule
    @Transactional
    public int delete(int ruleID) {
        try {
            return entityManager.createQuery(
                    "DELETE FROM RoyaltyRule r WHERE r.ruleID = :ruleID")
                    .setParameter("ruleID", ruleID)
                    .executeUpdate();
        } catch (RuntimeException ex) {
            return 0;
        }
    }

    // Check status before delete
    public String findStatusById(int ruleID) {
        try {
            TypedQuery<String> query = entityManager.createQuery(
                    "SELECT r.status FROM RoyaltyRule r WHERE r.ruleID = :ruleID",
                    String.class);
            return query.setParameter("ruleID", ruleID).getSingleResult();
        } catch (NoResultException ex) {
            throw new ResourceNotFoundException(
                    "Royalty rule not found with ID: " + ruleID);
        }
    }
}
