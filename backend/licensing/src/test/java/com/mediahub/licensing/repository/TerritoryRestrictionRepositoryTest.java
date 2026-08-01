package com.mediahub.licensing.repository;

import com.mediahub.licensing.entity.TerritoryRestriction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb2;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
class TerritoryRestrictionRepositoryTest {

    @Autowired
    private TerritoryRestrictionRepository repo;

    private TerritoryRestriction activeRestriction;
    private TerritoryRestriction inactiveRestriction;

    @BeforeEach
    void setUp() {
        repo.deleteAll();

        activeRestriction = new TerritoryRestriction();
        activeRestriction.setContentId(100);
        activeRestriction.setRestrictedCountries("CN,RU");
        activeRestriction.setAllowedCountries("US,CA");
        activeRestriction.setEffectiveDate(LocalDate.now());
        activeRestriction.setStatus("Active");

        inactiveRestriction = new TerritoryRestriction();
        inactiveRestriction.setContentId(100);
        inactiveRestriction.setRestrictedCountries("IN");
        inactiveRestriction.setAllowedCountries("AU");
        inactiveRestriction.setEffectiveDate(LocalDate.now().minusDays(30));
        inactiveRestriction.setStatus("Inactive");

        repo.save(activeRestriction);
        repo.save(inactiveRestriction);
    }

    // ─────────────────────────────────────────────────────────────
    // BASIC CRUD
    // ─────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("Basic CRUD")
    class BasicCrud {

        @Test
        @DisplayName("TC112 - should save restriction and generate ID")
        void save_generatesId() {
            TerritoryRestriction entity = buildEntity(200, "Active");
            TerritoryRestriction saved = repo.save(entity);

            assertThat(saved.getRestrictionId()).isNotNull().isPositive();
        }

        @Test
        @DisplayName("TC113 - should find restriction by ID")
        void findById_returnsSavedEntity() {
            Optional<TerritoryRestriction> found = repo.findById(activeRestriction.getRestrictionId());

            assertThat(found).isPresent();
            assertThat(found.get().getRestrictedCountries()).isEqualTo("CN,RU");
        }

        @Test
        @DisplayName("TC114 - should return empty Optional for non-existent ID")
        void findById_notFound_returnsEmpty() {
            Optional<TerritoryRestriction> found = repo.findById(999999);

            assertThat(found).isEmpty();
        }

        @Test
        @DisplayName("TC115 - should return all saved restrictions")
        void findAll_returnsAll() {
            List<TerritoryRestriction> all = repo.findAll();

            assertThat(all).hasSize(2);
        }

        @Test
        @DisplayName("TC116 - should delete restriction by ID")
        void deleteById_removesRestriction() {
            repo.deleteById(activeRestriction.getRestrictionId());

            assertThat(repo.findById(activeRestriction.getRestrictionId())).isEmpty();
        }
    }

    // ─────────────────────────────────────────────────────────────
    // FIND BY CONTENT ID AND STATUS
    // ─────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("findByContentIdAndStatus")
    class FindByContentIdAndStatus {

        @Test
        @DisplayName("TC117 - should return Active restrictions for contentId 100")
        void findByContentIdAndStatus_active_returnsActiveOnly() {
            List<TerritoryRestriction> result =
                    repo.findByContentIdAndStatus(100, "Active");

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getStatus()).isEqualTo("Active");
        }

        @Test
        @DisplayName("TC118 - should return Inactive restrictions for contentId 100")
        void findByContentIdAndStatus_inactive_returnsInactiveOnly() {
            List<TerritoryRestriction> result =
                    repo.findByContentIdAndStatus(100, "Inactive");

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getStatus()).isEqualTo("Inactive");
        }

        @Test
        @DisplayName("TC119 - should return empty list for unknown contentId")
        void findByContentIdAndStatus_unknownContentId_returnsEmpty() {
            List<TerritoryRestriction> result =
                    repo.findByContentIdAndStatus(999, "Active");

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("TC120 - should return empty list for contentId with no Active restrictions")
        void findByContentIdAndStatus_noActiveForContent_returnsEmpty() {
            TerritoryRestriction inactiveOnly = buildEntity(300, "Inactive");
            repo.save(inactiveOnly);

            List<TerritoryRestriction> result =
                    repo.findByContentIdAndStatus(300, "Active");

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("TC121 - should return multiple Active restrictions for same contentId")
        void findByContentIdAndStatus_multipleActive() {
            TerritoryRestriction second = buildEntity(100, "Active");
            second.setRestrictedCountries("DE,FR");
            repo.save(second);

            List<TerritoryRestriction> result =
                    repo.findByContentIdAndStatus(100, "Active");

            assertThat(result).hasSizeGreaterThanOrEqualTo(2);
        }

        @Test
        @DisplayName("TC122 - should not mix results from different contentIds")
        void findByContentIdAndStatus_isolatesContentId() {
            TerritoryRestriction otherContent = buildEntity(500, "Active");
            repo.save(otherContent);

            List<TerritoryRestriction> result =
                    repo.findByContentIdAndStatus(100, "Active");

            result.forEach(r -> assertThat(r.getContentId()).isEqualTo(100));
        }
    }

    // ─────────────────────────────────────────────────────────────
    // UPDATE
    // ─────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("Update operations")
    class Update {

        @Test
        @DisplayName("TC123 - should persist updated restrictedCountries")
        void update_persistsRestrictedCountries() {
            activeRestriction.setRestrictedCountries("KP,IR");
            repo.save(activeRestriction);

            TerritoryRestriction found = repo.findById(activeRestriction.getRestrictionId()).orElseThrow();
            assertThat(found.getRestrictedCountries()).isEqualTo("KP,IR");
        }

        @Test
        @DisplayName("TC124 - should persist updated allowedCountries")
        void update_persistsAllowedCountries() {
            activeRestriction.setAllowedCountries("JP,KR");
            repo.save(activeRestriction);

            TerritoryRestriction found = repo.findById(activeRestriction.getRestrictionId()).orElseThrow();
            assertThat(found.getAllowedCountries()).isEqualTo("JP,KR");
        }

        @Test
        @DisplayName("TC125 - should persist updated status")
        void update_persistsStatus() {
            activeRestriction.setStatus("Inactive");
            repo.save(activeRestriction);

            TerritoryRestriction found = repo.findById(activeRestriction.getRestrictionId()).orElseThrow();
            assertThat(found.getStatus()).isEqualTo("Inactive");
        }
    }

    private TerritoryRestriction buildEntity(int contentId, String status) {
        TerritoryRestriction e = new TerritoryRestriction();
        e.setContentId(contentId);
        e.setRestrictedCountries("XX");
        e.setAllowedCountries("YY");
        e.setEffectiveDate(LocalDate.now());
        e.setStatus(status);
        return e;
    }
}
