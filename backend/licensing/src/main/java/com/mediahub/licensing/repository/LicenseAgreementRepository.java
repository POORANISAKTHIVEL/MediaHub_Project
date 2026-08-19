package com.mediahub.licensing.repository;

import com.mediahub.licensing.entity.LicenseAgreement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface LicenseAgreementRepository
        extends JpaRepository<LicenseAgreement, Integer> {

    List<LicenseAgreement> findByStatus(String status);

    @Query("SELECT l FROM LicenseAgreement l " +
           "WHERE l.status = 'Active' " +
           "AND l.endDate BETWEEN :today " +
           "AND :sevenDaysLater")
    List<LicenseAgreement> findExpiringSoon(
        @Param("today") LocalDate today,
        @Param("sevenDaysLater") LocalDate sevenDaysLater);

    @Query("SELECT l FROM LicenseAgreement l " +
           "WHERE l.status = 'Active' " +
           "AND l.endDate < :today")
    List<LicenseAgreement> findExpiredLicenses(
        @Param("today") LocalDate today);

    // ✅ ROYALTY VALIDATION — check if creator has an active license
    boolean existsByLicensorIdAndStatus(Integer licensorId, String status);

    // ✅ ROYALTY VALIDATION — check if a specific content has an active license
    boolean existsByContentIdAndStatus(Integer contentId, String status);
}