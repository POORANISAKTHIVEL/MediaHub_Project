package com.mediahub.royalty.repository;

import com.mediahub.royalty.exception.ResourceNotFoundException;
import com.mediahub.royalty.model.RoyaltyStatement;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class RoyaltyStatementRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public void save(RoyaltyStatement statement) {
        if (statement.getStatementID() == 0) {
            entityManager.persist(statement);
        } else {
            entityManager.merge(statement);
        }
    }

    public List<RoyaltyStatement> findAll() {
        TypedQuery<RoyaltyStatement> query = entityManager.createQuery(
                "SELECT s FROM RoyaltyStatement s", RoyaltyStatement.class);
        return query.getResultList();
    }

    public RoyaltyStatement findById(int statementID) {
        RoyaltyStatement statement = entityManager.find(RoyaltyStatement.class, statementID);
        if (statement == null) {
            throw new ResourceNotFoundException(
                    "Statement not found with ID: " + statementID);
        }
        return statement;
    }

    @Transactional
    public int updateStatus(int statementID, String status) {
        return entityManager.createQuery(
                "UPDATE RoyaltyStatement s SET s.status = :status WHERE s.statementID = :statementID")
                .setParameter("status", status)
                .setParameter("statementID", statementID)
                .executeUpdate();
    }

    public String findStatusById(int statementID) {
        try {
            TypedQuery<String> query = entityManager.createQuery(
                    "SELECT s.status FROM RoyaltyStatement s WHERE s.statementID = :statementID",
                    String.class);
            return query.setParameter("statementID", statementID).getSingleResult();
        } catch (NoResultException ex) {
            throw new ResourceNotFoundException(
                    "Statement not found with ID: " + statementID);
        }
    }
}
