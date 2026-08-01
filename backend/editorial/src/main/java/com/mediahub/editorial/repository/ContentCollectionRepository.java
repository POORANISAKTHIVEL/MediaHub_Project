package com.mediahub.editorial.repository;

import com.mediahub.editorial.model.ContentCollection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContentCollectionRepository
        extends JpaRepository<ContentCollection, Integer> {
}
