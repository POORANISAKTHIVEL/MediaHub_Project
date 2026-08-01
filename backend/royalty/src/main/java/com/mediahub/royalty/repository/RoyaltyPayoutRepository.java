package com.mediahub.royalty.repository;

import com.mediahub.royalty.exception.ResourceNotFoundException;
import com.mediahub.royalty.model.RoyaltyPayout;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class RoyaltyPayoutRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public void save(RoyaltyPayout payout) {
        if (payout.getPayoutID() == 0) {
            entityManager.persist(payout);
        } else {
            entityManager.merge(payout);
        }
    }

    // API 12 — Get all payouts
    public List<RoyaltyPayout> findAll() {
        TypedQuery<RoyaltyPayout> query = entityManager.createQuery(
                "SELECT p FROM RoyaltyPayout p", RoyaltyPayout.class);
        return query.getResultList();
    }

    // API 13 — Get payout by ID
    public RoyaltyPayout findById(int payoutID) {
        RoyaltyPayout payout = entityManager.find(RoyaltyPayout.class, payoutID);
        if (payout == null) {
            throw new ResourceNotFoundException("Payout not found with ID: " + payoutID);
        }
        return payout;
    }

    // API 14, 15 — Update status
    @Transactional
    public int updateStatus(int payoutID, String status) {
        return entityManager.createQuery(
                "UPDATE RoyaltyPayout p SET p.status = :status WHERE p.payoutID = :payoutID")
                .setParameter("status", status)
                .setParameter("payoutID", payoutID)
                .executeUpdate();
    }

    // API 16 — Delete payout
    @Transactional
    public int delete(int payoutID) {
        try {
            return entityManager.createQuery(
                    "DELETE FROM RoyaltyPayout p WHERE p.payoutID = :payoutID")
                    .setParameter("payoutID", payoutID)
                    .executeUpdate();
        } catch (RuntimeException ex) {
            return 0;
        }
    }

    // Check status before delete
    public String findStatusById(int payoutID) {
        try {
            TypedQuery<String> query = entityManager.createQuery(
                    "SELECT p.status FROM RoyaltyPayout p WHERE p.payoutID = :payoutID",
                    String.class);
            return query.setParameter("payoutID", payoutID).getSingleResult();
        } catch (NoResultException ex) {
            throw new ResourceNotFoundException("Payout not found with ID: " + payoutID);
        }
    }
}
