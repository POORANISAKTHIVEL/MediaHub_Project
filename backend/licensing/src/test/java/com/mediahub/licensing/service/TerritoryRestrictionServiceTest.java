package com.mediahub.licensing.service;

import com.mediahub.licensing.dto.request.TerritoryRestrictionRequestDTO;
import com.mediahub.licensing.dto.response.TerritoryRestrictionResponseDTO;
import com.mediahub.licensing.entity.TerritoryRestriction;
import com.mediahub.licensing.repository.TerritoryRestrictionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.mediahub.licensing.executor.LicenseNotificationExecutor;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TerritoryRestrictionServiceTest {

    @Mock
    private TerritoryRestrictionRepository repo;

    @Mock
    private LicenseNotificationExecutor executor;

    @InjectMocks
    private TerritoryRestrictionService service;

    private TerritoryRestriction sampleEntity;
    private TerritoryRestrictionRequestDTO sampleRequest;

    @BeforeEach
    void setUp() {
        sampleEntity = new TerritoryRestriction();
        sampleEntity.setRestrictionId(1);
        sampleEntity.setContentId(100);
        sampleEntity.setRestrictedCountries("CN,RU");
        sampleEntity.setAllowedCountries("US,CA,UK");
        sampleEntity.setEffectiveDate(LocalDate.now());
        sampleEntity.setStatus("Active");

        sampleRequest = new TerritoryRestrictionRequestDTO();
        sampleRequest.setContentId(100);
        sampleRequest.setRestrictedCountries("CN,RU");
        sampleRequest.setAllowedCountries("US,CA,UK");
        sampleRequest.setEffectiveDate(LocalDate.now());
    }

    // ─────────────────────────────────────────────────────────────
    // CREATE
    // ─────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("createRestriction")
    class CreateRestriction {

        @Test
        @DisplayName("TC35 - should create restriction and return response DTO")
        void createRestriction_success() {
            when(repo.save(any(TerritoryRestriction.class))).thenReturn(sampleEntity);

            TerritoryRestrictionResponseDTO result = service.createRestriction(sampleRequest, 1L);

            assertThat(result).isNotNull();
            assertThat(result.getRestrictionId()).isEqualTo(1);
            verify(repo, times(1)).save(any());
        }

        @Test
        @DisplayName("TC36 - should always set status to Active on create")
        void createRestriction_setsStatusActive() {
            when(repo.save(any())).thenAnswer(inv -> {
                TerritoryRestriction saved = inv.getArgument(0);
                assertThat(saved.getStatus()).isEqualTo("Active");
                saved.setRestrictionId(1);
                return saved;
            });

            service.createRestriction(sampleRequest, 1L);
            verify(repo).save(any());
        }

        @Test
        @DisplayName("TC37 - should map contentId from request to entity")
        void createRestriction_mapsContentId() {
            when(repo.save(any())).thenAnswer(inv -> {
                TerritoryRestriction e = inv.getArgument(0);
                assertThat(e.getContentId()).isEqualTo(100);
                return sampleEntity;
            });

            service.createRestriction(sampleRequest, 1L);
        }

        @Test
        @DisplayName("TC38 - should map restrictedCountries from request to entity")
        void createRestriction_mapsRestrictedCountries() {
            when(repo.save(any())).thenAnswer(inv -> {
                TerritoryRestriction e = inv.getArgument(0);
                assertThat(e.getRestrictedCountries()).isEqualTo("CN,RU");
                return sampleEntity;
            });

            service.createRestriction(sampleRequest, 1L);
        }

        @Test
        @DisplayName("TC39 - should map allowedCountries from request to entity")
        void createRestriction_mapsAllowedCountries() {
            when(repo.save(any())).thenAnswer(inv -> {
                TerritoryRestriction e = inv.getArgument(0);
                assertThat(e.getAllowedCountries()).isEqualTo("US,CA,UK");
                return sampleEntity;
            });

            service.createRestriction(sampleRequest, 1L);
        }

        @Test
        @DisplayName("TC40 - should propagate repository exception on save failure")
        void createRestriction_repoThrows_propagates() {
            when(repo.save(any())).thenThrow(new RuntimeException("DB error"));

            assertThatThrownBy(() -> service.createRestriction(sampleRequest, 1L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("DB error");
        }

        @Test
        @DisplayName("TC41 - should return DTO with correct status Active")
        void createRestriction_responseHasStatusActive() {
            when(repo.save(any())).thenReturn(sampleEntity);

            TerritoryRestrictionResponseDTO result = service.createRestriction(sampleRequest, 1L);

            assertThat(result.getStatus()).isEqualTo("Active");
        }

        @Test
        @DisplayName("TC42 - should return DTO with correct effectiveDate")
        void createRestriction_responseHasEffectiveDate() {
            LocalDate today = LocalDate.now();
            sampleEntity.setEffectiveDate(today);
            when(repo.save(any())).thenReturn(sampleEntity);

            TerritoryRestrictionResponseDTO result = service.createRestriction(sampleRequest, 1L);

            assertThat(result.getEffectiveDate()).isEqualTo(today);
        }
    }

    // ─────────────────────────────────────────────────────────────
    // GET BY CONTENT ID
    // ─────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("getByContentId")
    class GetByContentId {

        @Test
        @DisplayName("TC43 - should return active restrictions for content ID")
        void getByContentId_returnsActiveRestrictions() {
            when(repo.findByContentIdAndStatus(100, "Active")).thenReturn(List.of(sampleEntity));

            List<TerritoryRestrictionResponseDTO> result = service.getByContentId(100, true);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getContentId()).isEqualTo(100);
        }

        @Test
        @DisplayName("TC44 - should return empty list when content has no active restrictions")
        void getByContentId_noRestrictions_returnsEmpty() {
            when(repo.findByContentIdAndStatus(999, "Active")).thenReturn(Collections.emptyList());

            List<TerritoryRestrictionResponseDTO> result = service.getByContentId(999, true);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("TC45 - should always filter by Active status")
        void getByContentId_alwaysFiltersActiveStatus() {
            when(repo.findByContentIdAndStatus(100, "Active")).thenReturn(Collections.emptyList());

            service.getByContentId(100, true);

            verify(repo).findByContentIdAndStatus(100, "Active");
            verify(repo, never()).findByContentIdAndStatus(eq(100), eq("Inactive"));
        }

        @Test
        @DisplayName("TC46 - should return multiple restrictions for same content")
        void getByContentId_multipleRestrictions() {
            TerritoryRestriction second = new TerritoryRestriction();
            second.setRestrictionId(2);
            second.setContentId(100);
            second.setStatus("Active");
            second.setEffectiveDate(LocalDate.now());

            when(repo.findByContentIdAndStatus(100, "Active"))
                    .thenReturn(Arrays.asList(sampleEntity, second));

            List<TerritoryRestrictionResponseDTO> result = service.getByContentId(100, true);

            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("TC47 - should map restrictedCountries to DTO")
        void getByContentId_mapsRestrictedCountries() {
            when(repo.findByContentIdAndStatus(100, "Active")).thenReturn(List.of(sampleEntity));

            List<TerritoryRestrictionResponseDTO> result = service.getByContentId(100, true);

            assertThat(result.get(0).getRestrictedCountries()).isEqualTo("CN,RU");
        }

        @Test
        @DisplayName("TC48 - should map allowedCountries to DTO")
        void getByContentId_mapsAllowedCountries() {
            when(repo.findByContentIdAndStatus(100, "Active")).thenReturn(List.of(sampleEntity));

            List<TerritoryRestrictionResponseDTO> result = service.getByContentId(100, true);

            assertThat(result.get(0).getAllowedCountries()).isEqualTo("US,CA,UK");
        }
    }

    // ─────────────────────────────────────────────────────────────
    // UPDATE
    // ─────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("updateRestriction")
    class UpdateRestriction {

        @Test
        @DisplayName("TC49 - should update restriction successfully")
        void updateRestriction_success() {
            when(repo.findById(1)).thenReturn(Optional.of(sampleEntity));
            when(repo.save(any())).thenReturn(sampleEntity);

            TerritoryRestrictionRequestDTO update = new TerritoryRestrictionRequestDTO();
            update.setRestrictedCountries("IN,PK");
            update.setAllowedCountries("AU,NZ");

            TerritoryRestrictionResponseDTO result = service.updateRestriction(1, update);

            assertThat(result).isNotNull();
            verify(repo).save(any());
        }

        @Test
        @DisplayName("TC50 - should throw RuntimeException when restriction not found")
        void updateRestriction_notFound_throwsException() {
            when(repo.findById(999)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.updateRestriction(999, sampleRequest))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Restriction not found");
        }

        @Test
        @DisplayName("TC51 - should update restrictedCountries on entity before saving")
        void updateRestriction_updatesRestrictedCountries() {
            when(repo.findById(1)).thenReturn(Optional.of(sampleEntity));
            when(repo.save(any())).thenAnswer(inv -> inv.getArgument(0));

            TerritoryRestrictionRequestDTO update = new TerritoryRestrictionRequestDTO();
            update.setRestrictedCountries("IN");
            update.setAllowedCountries("AU");

            service.updateRestriction(1, update);

            verify(repo).save(argThat(e -> "IN".equals(e.getRestrictedCountries())));
        }

        @Test
        @DisplayName("TC52 - should update allowedCountries on entity before saving")
        void updateRestriction_updatesAllowedCountries() {
            when(repo.findById(1)).thenReturn(Optional.of(sampleEntity));
            when(repo.save(any())).thenAnswer(inv -> inv.getArgument(0));

            TerritoryRestrictionRequestDTO update = new TerritoryRestrictionRequestDTO();
            update.setRestrictedCountries("CN");
            update.setAllowedCountries("NZ");

            service.updateRestriction(1, update);

            verify(repo).save(argThat(e -> "NZ".equals(e.getAllowedCountries())));
        }

        @Test
        @DisplayName("TC53 - should not save when restriction is not found")
        void updateRestriction_notFound_doesNotSave() {
            when(repo.findById(999)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.updateRestriction(999, sampleRequest));
            verify(repo, never()).save(any());
        }

        @Test
        @DisplayName("TC54 - should propagate repository exception on save failure during update")
        void updateRestriction_saveThrows_propagates() {
            when(repo.findById(1)).thenReturn(Optional.of(sampleEntity));
            when(repo.save(any())).thenThrow(new RuntimeException("Save failed"));

            assertThatThrownBy(() -> service.updateRestriction(1, sampleRequest))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Save failed");
        }

        @Test
        @DisplayName("TC55 - returned DTO should reflect updated restrictedCountries")
        void updateRestriction_responseDtoReflectsUpdate() {
            TerritoryRestriction updatedEntity = new TerritoryRestriction();
            updatedEntity.setRestrictionId(1);
            updatedEntity.setContentId(100);
            updatedEntity.setRestrictedCountries("IN,PK");
            updatedEntity.setAllowedCountries("AU");
            updatedEntity.setEffectiveDate(LocalDate.now());
            updatedEntity.setStatus("Active");

            when(repo.findById(1)).thenReturn(Optional.of(sampleEntity));
            when(repo.save(any())).thenReturn(updatedEntity);

            TerritoryRestrictionRequestDTO update = new TerritoryRestrictionRequestDTO();
            update.setRestrictedCountries("IN,PK");
            update.setAllowedCountries("AU");

            TerritoryRestrictionResponseDTO result = service.updateRestriction(1, update);

            assertThat(result.getRestrictedCountries()).isEqualTo("IN,PK");
        }
    }
}
