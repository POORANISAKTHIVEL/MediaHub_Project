package com.mediahub.contentcatalog.repository;

import com.mediahub.contentcatalog.entity.ContentAsset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ContentAssetRepository extends JpaRepository<ContentAsset, Integer> {

    // ✅ ROYALTY INTEGRATION — fetch all content for a given creator
    List<ContentAsset> findByCreatorId(int creatorId);
}