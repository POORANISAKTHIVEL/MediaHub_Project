package com.mediahub.contentcatalog.repository;

import com.mediahub.contentcatalog.entity.ContentTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ContentTagRepository extends JpaRepository<ContentTag, Integer> {
    List<ContentTag> findByContentId(int contentId);
}