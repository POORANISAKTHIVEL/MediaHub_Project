package com.mediahub.editorial.repository;

import com.mediahub.editorial.model.PublicationSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PublicationScheduleRepository
        extends JpaRepository<PublicationSchedule, Integer> {
}
