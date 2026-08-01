package com.mediahub.editorial.repository;

import com.mediahub.editorial.model.EditorialReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EditorialReviewRepository
        extends JpaRepository<EditorialReview, Integer> {

    // ✅ ROYALTY VALIDATION — check if a content item has an approved review
    boolean existsByContentIDAndDecision(int contentID, String decision);
}
