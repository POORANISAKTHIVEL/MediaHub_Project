package com.mediahub.licensing.repository;

import com.mediahub.licensing.entity.TerritoryRestriction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TerritoryRestrictionRepository
        extends JpaRepository<TerritoryRestriction, Integer> {

	// Changed from RestrictionStatus to String
    List<TerritoryRestriction> findByContentIdAndStatus(
        Integer contentId, String status);

    List<TerritoryRestriction> findByContentId(Integer contentId);
}