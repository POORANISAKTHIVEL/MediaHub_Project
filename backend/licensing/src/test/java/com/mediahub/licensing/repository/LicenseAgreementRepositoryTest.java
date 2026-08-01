package com.mediahub.licensing.repository;

import com.mediahub.licensing.entity.LicenseAgreement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
class LicenseAgreementRepositoryTest {

    @Autowired
    private LicenseAgreementRepository repo;

    private LicenseAgreement active;
    private LicenseAgreement expired;

    @BeforeEach
    void setUp() {
        repo.deleteAll();

        active = new LicenseAgreement();
        active.setContentId(100);
        active.setLicensorId(200);
        active.setLicenseeRef("LIC-A");
        active.setTerritory("US");
        active.setRightsType("Streaming");
        active.setStartDate(LocalDate.now().minusDays(10));
        active.setEndDate(LocalDate.now().plusDays(30));
        active.setLicenseFee(new BigDecimal("5000.00"));
        active.setStatus("Active");

        expired = new LicenseAgreement();
        expired.setContentId(101);
        expired.setLicensorId(201);
        expired.setLicenseeRef("LIC-E");
        expired.setTerritory("UK");
        expired.setRightsType("Broadcast");
        expired.setStartDate(LocalDate.now().minusDays(60));
        expired.setEndDate(LocalDate.now().minusDays(10));
        expired.setLicenseFee(new BigDecimal("3000.00"));
        expired.setStatus("Expired");

        repo.save(active);
        repo.save(expired);
    }

    // ─────────────────────────────────────────────────────────────
    // BASIC CRUD
    // ─────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("Basic CRUD")
    class BasicCrud {

        @Test
        @DisplayName("TC96 - should save a new license and generate ID")
        void save_generatesId() {
            LicenseAgreement entity = buildEntity("LIC-NEW", "Active",
                    LocalDate.now().plusDays(20));
            LicenseAgreement saved = repo.save(entity);

            assertThat(saved.getLicenseId()).isNotNull().isPositive();
        }

        @Test
        @DisplayName("TC97 - should find license by ID after saving")
        void findById_returnsSavedEntity() {
            Optional<LicenseAgreement> found = repo.findById(active.getLicenseId());

            assertThat(found).isPresent();
            assertThat(found.get().getLicenseeRef()).isEqualTo("LIC-A");
        }

        @Test
        @DisplayName("TC98 - should return empty Optional for non-existent ID")
        void findById_notFound_returnsEmpty() {
            Optional<LicenseAgreement> found = repo.findById(999999);

            assertThat(found).isEmpty();
        }

        @Test
        @DisplayName("TC99 - should return all saved licenses")
        void findAll_returnsAllLicenses() {
            List<LicenseAgreement> all = repo.findAll();

            assertThat(all).hasSize(2);
        }

        @Test
        @DisplayName("TC100 - should delete license by ID")
        void deleteById_removesLicense() {
            repo.deleteById(active.getLicenseId());

            assertThat(repo.findById(active.getLicenseId())).isEmpty();
        }
    }

    // ─────────────────────────────────────────────────────────────
    // FIND BY STATUS
    // ─────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("findByStatus")
    class FindByStatus {

        @Test
        @DisplayName("TC101 - should return only Active licenses")
        void findByStatus_active_returnsOnlyActive() {
            List<LicenseAgreement> result = repo.findByStatus("Active");

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getStatus()).isEqualTo("Active");
        }

        @Test
        @DisplayName("TC102 - should return only Expired licenses")
        void findByStatus_expired_returnsOnlyExpired() {
            List<LicenseAgreement> result = repo.findByStatus("Expired");

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getStatus()).isEqualTo("Expired");
        }

        @Test
        @DisplayName("TC103 - should return empty list for Terminated when none exist")
        void findByStatus_terminated_returnsEmpty() {
            List<LicenseAgreement> result = repo.findByStatus("Terminated");

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("TC104 - should return multiple Active licenses when more than one exist")
        void findByStatus_multipleActive() {
            LicenseAgreement second = buildEntity("LIC-B", "Active",
                    LocalDate.now().plusDays(15));
            repo.save(second);

            List<LicenseAgreement> result = repo.findByStatus("Active");

            assertThat(result).hasSizeGreaterThanOrEqualTo(2);
        }
    }

    // ─────────────────────────────────────────────────────────────
    // FIND EXPIRING SOON
    // ─────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("findExpiringSoon")
    class FindExpiringSoon {

        @Test
        @DisplayName("TC105 - should return Active licenses expiring within 7 days")
        void findExpiringSoon_returnsActiveLicensesInWindow() {
            LicenseAgreement expiringSoon = buildEntity("LIC-X", "Active",
                    LocalDate.now().plusDays(5));
            repo.save(expiringSoon);

            List<LicenseAgreement> result = repo.findExpiringSoon(
                    LocalDate.now(), LocalDate.now().plusDays(7));

            assertThat(result).isNotEmpty();
            assertThat(result).allMatch(l -> l.getStatus().equals("Active"));
        }

        @Test
        @DisplayName("TC106 - should not return licenses expiring after 7 days")
        void findExpiringSoon_excludesLicensesBeyondWindow() {
            List<LicenseAgreement> result = repo.findExpiringSoon(
                    LocalDate.now(), LocalDate.now().plusDays(7));

            result.forEach(l ->
                assertThat(l.getEndDate()).isBeforeOrEqualTo(LocalDate.now().plusDays(7))
            );
        }

        @Test
        @DisplayName("TC107 - should not return already-expired licenses")
        void findExpiringSoon_excludesAlreadyExpired() {
            List<LicenseAgreement> result = repo.findExpiringSoon(
                    LocalDate.now(), LocalDate.now().plusDays(7));

            result.forEach(l ->
                assertThat(l.getEndDate()).isAfterOrEqualTo(LocalDate.now())
            );
        }

        @Test
        @DisplayName("TC108 - should return empty list when no licenses expiring in window")
        void findExpiringSoon_noneInWindow_returnsEmpty() {
            List<LicenseAgreement> result = repo.findExpiringSoon(
                    LocalDate.now().minusDays(5), LocalDate.now().minusDays(1));

            assertThat(result).isEmpty();
        }
    }

    // ─────────────────────────────────────────────────────────────
    // UPDATE
    // ─────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("Update operations")
    class Update {

        @Test
        @DisplayName("TC109 - should persist updated territory")
        void update_persistsTerritory() {
            active.setTerritory("JP");
            repo.save(active);

            LicenseAgreement found = repo.findById(active.getLicenseId()).orElseThrow();
            assertThat(found.getTerritory()).isEqualTo("JP");
        }

        @Test
        @DisplayName("TC110 - should persist updated status")
        void update_persistsStatus() {
            active.setStatus("Terminated");
            repo.save(active);

            LicenseAgreement found = repo.findById(active.getLicenseId()).orElseThrow();
            assertThat(found.getStatus()).isEqualTo("Terminated");
        }

        @Test
        @DisplayName("TC111 - should persist updated endDate")
        void update_persistsEndDate() {
            LocalDate newEnd = LocalDate.now().plusDays(90);
            active.setEndDate(newEnd);
            repo.save(active);

            LicenseAgreement found = repo.findById(active.getLicenseId()).orElseThrow();
            assertThat(found.getEndDate()).isEqualTo(newEnd);
        }
    }

    private LicenseAgreement buildEntity(String ref, String status, LocalDate endDate) {
        LicenseAgreement e = new LicenseAgreement();
        e.setContentId(100);
        e.setLicensorId(200);
        e.setLicenseeRef(ref);
        e.setTerritory("US");
        e.setRightsType("Streaming");
        e.setStartDate(LocalDate.now().minusDays(5));
        e.setEndDate(endDate);
        e.setLicenseFee(new BigDecimal("1000.00"));
        e.setStatus(status);
        return e;
    }
}
