package com.mediahub.licensing.service;

import com.mediahub.licensing.dto.request.LicenseAgreementRequestDTO;
import com.mediahub.licensing.dto.response.LicenseAgreementResponseDTO;
import com.mediahub.licensing.dto.response.LicenseExpiringSoonResponseDTO;
import com.mediahub.licensing.entity.LicenseAgreement;
import com.mediahub.licensing.repository.LicenseAgreementRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.mediahub.licensing.executor.LicenseNotificationExecutor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import com.mediahub.licensing.executor.ContentValidationExecutor;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LicenseAgreementServiceTest {

    @Mock
    private LicenseAgreementRepository repo;

    @Mock
    private LicenseNotificationExecutor executor;

    @Mock
    private ContentValidationExecutor contentValidationExecutor;

    @InjectMocks
    private LicenseAgreementService service;

    private LicenseAgreement sampleEntity;
    private LicenseAgreementRequestDTO sampleRequest;

    @BeforeEach
    void setUp() {
        sampleEntity = new LicenseAgreement();
        sampleEntity.setLicenseId(1);
        sampleEntity.setContentId(100);
        sampleEntity.setLicensorId(200);
        sampleEntity.setLicenseeRef("LIC-001");
        sampleEntity.setTerritory("US");
        sampleEntity.setRightsType("Streaming");
        sampleEntity.setStartDate(LocalDate.now());
        sampleEntity.setEndDate(LocalDate.now().plusDays(30));
        sampleEntity.setLicenseFee(new BigDecimal("5000.00"));
        sampleEntity.setStatus("Active");

        sampleRequest = new LicenseAgreementRequestDTO();
        sampleRequest.setContentId(100);
        sampleRequest.setLicensorId(200);
        sampleRequest.setLicenseeRef("LIC-001");
        sampleRequest.setTerritory("US");
        sampleRequest.setRightsType("Streaming");
        sampleRequest.setStartDate(LocalDate.now());
        sampleRequest.setEndDate(LocalDate.now().plusDays(30));
        sampleRequest.setLicenseFee(new BigDecimal("5000.00"));
    }

    // ─────────────────────────────────────────────────────────────
    // CREATE
    // ─────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("createLicense")
    class CreateLicense {

        @Test
        @DisplayName("TC01 - should create license and return response DTO")
        void createLicense_success() {
            
            when(contentValidationExecutor.validateContent(anyInt()))
                        .thenReturn(true);

            when(repo.save(any(LicenseAgreement.class))).thenReturn(sampleEntity);

            LicenseAgreementResponseDTO result = service.createLicense(sampleRequest, 1L);

            assertThat(result).isNotNull();
            assertThat(result.getLicenseId()).isEqualTo(1);
            assertThat(result.getStatus()).isEqualTo("Active");
            verify(repo, times(1)).save(any(LicenseAgreement.class));
        }

        @Test
        @DisplayName("TC02 - should always set status to Active on create")
        void createLicense_setsStatusActive() {
            
            when(contentValidationExecutor.validateContent(anyInt()))
                        .thenReturn(true);

            when(repo.save(any(LicenseAgreement.class))).thenAnswer(inv -> {
                LicenseAgreement saved = inv.getArgument(0);
                assertThat(saved.getStatus()).isEqualTo("Active");
                saved.setLicenseId(1);
                return saved;
            });

            service.createLicense(sampleRequest, 1L);
            verify(repo).save(any(LicenseAgreement.class));
        }

        @Test
        @DisplayName("TC03 - should map all request fields to entity correctly")
        void createLicense_mapsAllFields() {
            
            when(contentValidationExecutor.validateContent(anyInt()))
                        .thenReturn(true);

            when(repo.save(any(LicenseAgreement.class))).thenAnswer(inv -> {
                LicenseAgreement e = inv.getArgument(0);
                assertThat(e.getContentId()).isEqualTo(100);
                assertThat(e.getLicensorId()).isEqualTo(200);
                assertThat(e.getLicenseeRef()).isEqualTo("LIC-001");
                assertThat(e.getTerritory()).isEqualTo("US");
                assertThat(e.getRightsType()).isEqualTo("Streaming");
                assertThat(e.getLicenseFee()).isEqualByComparingTo("5000.00");
                return sampleEntity;
            });

            service.createLicense(sampleRequest, 1L);
        }

        @Test
        @DisplayName("TC04 - should propagate repository exception on save failure")
        void createLicense_repoThrows_propagates() {
            
            when(contentValidationExecutor.validateContent(anyInt()))
                        .thenReturn(true);

            when(repo.save(any())).thenThrow(new RuntimeException("DB error"));

            assertThatThrownBy(() -> service.createLicense(sampleRequest, 1L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("DB error");
        }

        @Test
        @DisplayName("TC05 - should return DTO with same content/licensor IDs as request")
        void createLicense_responseDtoHasCorrectIds() {
            
            when(contentValidationExecutor.validateContent(anyInt()))
                        .thenReturn(true);

            when(repo.save(any())).thenReturn(sampleEntity);

            LicenseAgreementResponseDTO result = service.createLicense(sampleRequest, 1L);

            assertThat(result.getContentId()).isEqualTo(sampleRequest.getContentId());
            assertThat(result.getLicensorId()).isEqualTo(sampleRequest.getLicensorId());
        }

        @Test
        @DisplayName("TC06 - should return DTO with correct license fee")
        void createLicense_responseDtoHasCorrectFee() {
            
            when(contentValidationExecutor.validateContent(anyInt()))
                        .thenReturn(true);

            when(repo.save(any())).thenReturn(sampleEntity);

            LicenseAgreementResponseDTO result = service.createLicense(sampleRequest, 1L);

            assertThat(result.getLicenseFee()).isEqualByComparingTo("5000.00");
        }

        @Test
        @DisplayName("TC07 - should throw exception when content does not exist")
        void createLicense_contentNotFound() {

            when(contentValidationExecutor.validateContent(anyInt()))
                    .thenReturn(false);

            assertThatThrownBy(() ->
                    service.createLicense(sampleRequest, 1L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining(
                            "Content not found in Content Catalog");

            verify(repo, never()).save(any());
        }
    }

    // ─────────────────────────────────────────────────────────────
    // GET ALL
    // ─────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("getAllLicenses")
    class GetAllLicenses {

        @Test
        @DisplayName("TC07 - should return all licenses as DTOs")
        void getAllLicenses_returnsAll() {
            LicenseAgreement second = buildEntity(2, "UK", "Active");
            when(repo.findAll()).thenReturn(Arrays.asList(sampleEntity, second));

            List<LicenseAgreementResponseDTO> result = service.getAllLicenses();

            assertThat(result).hasSize(2);
            assertThat(result.get(0).getLicenseId()).isEqualTo(1);
            assertThat(result.get(1).getLicenseId()).isEqualTo(2);
        }

        @Test
        @DisplayName("TC08 - should return empty list when no licenses exist")
        void getAllLicenses_emptyRepo_returnsEmptyList() {
            when(repo.findAll()).thenReturn(Collections.emptyList());

            List<LicenseAgreementResponseDTO> result = service.getAllLicenses();

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("TC09 - should call findAll exactly once")
        void getAllLicenses_callsRepoOnce() {
            when(repo.findAll()).thenReturn(Collections.emptyList());

            service.getAllLicenses();

            verify(repo, times(1)).findAll();
        }

        @Test
        @DisplayName("TC10 - should map territory from entity to DTO")
        void getAllLicenses_mapsTerritory() {
            sampleEntity.setTerritory("CA");
            when(repo.findAll()).thenReturn(List.of(sampleEntity));

            List<LicenseAgreementResponseDTO> result = service.getAllLicenses();

            assertThat(result.get(0).getTerritory()).isEqualTo("CA");
        }
    }

    // ─────────────────────────────────────────────────────────────
    // GET BY STATUS
    // ─────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("getByStatus")
    class GetByStatus {

        @Test
        @DisplayName("TC11 - should return Active licenses")
        void getByStatus_active_returnsActiveLicenses() {
            when(repo.findByStatus("Active")).thenReturn(List.of(sampleEntity));

            List<LicenseAgreementResponseDTO> result = service.getByStatus("Active");

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getStatus()).isEqualTo("Active");
        }

        @Test
        @DisplayName("TC12 - should return Expired licenses")
        void getByStatus_expired_returnsExpiredLicenses() {
            LicenseAgreement expired = buildEntity(2, "US", "Expired");
            when(repo.findByStatus("Expired")).thenReturn(List.of(expired));

            List<LicenseAgreementResponseDTO> result = service.getByStatus("Expired");

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getStatus()).isEqualTo("Expired");
        }

        @Test
        @DisplayName("TC13 - should return empty list for unknown status")
        void getByStatus_unknownStatus_returnsEmpty() {
            when(repo.findByStatus("Unknown")).thenReturn(Collections.emptyList());

            List<LicenseAgreementResponseDTO> result = service.getByStatus("Unknown");

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("TC14 - should pass exact status string to repository")
        void getByStatus_passesExactStatusToRepo() {
            when(repo.findByStatus("Terminated")).thenReturn(Collections.emptyList());

            service.getByStatus("Terminated");

            verify(repo).findByStatus("Terminated");
        }

        @Test
        @DisplayName("TC15 - should return multiple licenses with same status")
        void getByStatus_multipleResults() {
            LicenseAgreement second = buildEntity(2, "UK", "Active");
            when(repo.findByStatus("Active")).thenReturn(Arrays.asList(sampleEntity, second));

            List<LicenseAgreementResponseDTO> result = service.getByStatus("Active");

            assertThat(result).hasSize(2);
        }
    }

    // ─────────────────────────────────────────────────────────────
    // GET BY ID
    // ─────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("getById")
    class GetById {

        @Test
        @DisplayName("TC16 - should return DTO when license exists")
        void getById_exists_returnsDTO() {
            when(repo.findById(1)).thenReturn(Optional.of(sampleEntity));

            Optional<LicenseAgreementResponseDTO> result = service.getById(1);

            assertThat(result).isPresent();
            assertThat(result.get().getLicenseId()).isEqualTo(1);
        }

        @Test
        @DisplayName("TC17 - should return empty Optional when license not found")
        void getById_notFound_returnsEmpty() {
            when(repo.findById(999)).thenReturn(Optional.empty());

            Optional<LicenseAgreementResponseDTO> result = service.getById(999);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("TC18 - should map all fields correctly for getById")
        void getById_mapsAllFields() {
            when(repo.findById(1)).thenReturn(Optional.of(sampleEntity));

            Optional<LicenseAgreementResponseDTO> result = service.getById(1);

            assertThat(result).isPresent();
            LicenseAgreementResponseDTO dto = result.get();
            assertThat(dto.getLicenseeRef()).isEqualTo("LIC-001");
            assertThat(dto.getRightsType()).isEqualTo("Streaming");
            assertThat(dto.getTerritory()).isEqualTo("US");
        }

        @Test
        @DisplayName("TC19 - should call findById with correct ID")
        void getById_callsRepoWithCorrectId() {
            when(repo.findById(42)).thenReturn(Optional.empty());

            service.getById(42);

            verify(repo).findById(42);
        }

        @Test
        @DisplayName("TC20 - should return DTO with correct status")
        void getById_returnsCorrectStatus() {
            sampleEntity.setStatus("Expired");
            when(repo.findById(1)).thenReturn(Optional.of(sampleEntity));

            Optional<LicenseAgreementResponseDTO> result = service.getById(1);

            assertThat(result.get().getStatus()).isEqualTo("Expired");
        }
    }

    // ─────────────────────────────────────────────────────────────
    // GET EXPIRING SOON
    // ─────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("getExpiringSoon")
    class GetExpiringSoon {

        @Test
        @DisplayName("TC21 - should return expiring soon DTOs")
        void getExpiringSoon_returnsResults() {
            sampleEntity.setEndDate(LocalDate.now().plusDays(3));
            when(repo.findExpiringSoon(any(), any())).thenReturn(List.of(sampleEntity));

            List<LicenseExpiringSoonResponseDTO> result = service.getExpiringSoon();

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getLicenseId()).isEqualTo(1);
        }

        @Test
        @DisplayName("TC22 - should return empty list when none expiring soon")
        void getExpiringSoon_empty() {
            when(repo.findExpiringSoon(any(), any())).thenReturn(Collections.emptyList());

            List<LicenseExpiringSoonResponseDTO> result = service.getExpiringSoon();

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("TC23 - should compute correct daysRemaining for a license expiring in 5 days")
        void getExpiringSoon_daysRemainingIsCorrect() {
            sampleEntity.setEndDate(LocalDate.now().plusDays(5));
            when(repo.findExpiringSoon(any(), any())).thenReturn(List.of(sampleEntity));

            List<LicenseExpiringSoonResponseDTO> result = service.getExpiringSoon();

            assertThat(result.get(0).getDaysRemaining()).isEqualTo(5L);
        }

        @Test
        @DisplayName("TC24 - should include territory in expiring soon DTO")
        void getExpiringSoon_includesTerritory() {
            sampleEntity.setEndDate(LocalDate.now().plusDays(2));
            sampleEntity.setTerritory("DE");
            when(repo.findExpiringSoon(any(), any())).thenReturn(List.of(sampleEntity));

            List<LicenseExpiringSoonResponseDTO> result = service.getExpiringSoon();

            assertThat(result.get(0).getTerritory()).isEqualTo("DE");
        }

        @Test
        @DisplayName("TC25 - should include correct endDate in expiring soon DTO")
        void getExpiringSoon_includesEndDate() {
            LocalDate targetDate = LocalDate.now().plusDays(4);
            sampleEntity.setEndDate(targetDate);
            when(repo.findExpiringSoon(any(), any())).thenReturn(List.of(sampleEntity));

            List<LicenseExpiringSoonResponseDTO> result = service.getExpiringSoon();

            assertThat(result.get(0).getEndDate()).isEqualTo(targetDate);
        }

        @Test
        @DisplayName("TC26 - should query repo with today and seven days later")
        void getExpiringSoon_queriesWithCorrectDateRange() {
            when(repo.findExpiringSoon(any(), any())).thenReturn(Collections.emptyList());

            service.getExpiringSoon();

            verify(repo).findExpiringSoon(
                    argThat(d -> !d.isAfter(LocalDate.now())),
                    argThat(d -> d.isAfter(LocalDate.now()))
            );
        }
    }

    // ─────────────────────────────────────────────────────────────
    // UPDATE
    // ─────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("updateLicense")
    class UpdateLicense {

        @Test
        @DisplayName("TC27 - should update Active license successfully")
        void updateLicense_active_success() {
            sampleEntity.setStatus("Active");
            when(repo.findById(1)).thenReturn(Optional.of(sampleEntity));
            when(repo.save(any())).thenReturn(sampleEntity);

            LicenseAgreementRequestDTO update = new LicenseAgreementRequestDTO();
            update.setTerritory("JP");
            update.setRightsType("Broadcast");
            update.setEndDate(LocalDate.now().plusDays(60));

            LicenseAgreementResponseDTO result = service.updateLicense(1, update);

            assertThat(result).isNotNull();
            verify(repo).save(any());
        }

        @Test
        @DisplayName("TC28 - should throw RuntimeException when license ID not found")
        void updateLicense_notFound_throwsException() {
            when(repo.findById(999)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.updateLicense(999, sampleRequest))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("License not found");
        }

        @Test
        @DisplayName("TC29 - should throw exception when updating Expired license")
        void updateLicense_expiredStatus_throwsException() {
            sampleEntity.setStatus("Expired");
            when(repo.findById(1)).thenReturn(Optional.of(sampleEntity));

            assertThatThrownBy(() -> service.updateLicense(1, sampleRequest))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Cannot update Expired or Terminated license");
        }

        @Test
        @DisplayName("TC30 - should throw exception when updating Terminated license")
        void updateLicense_terminatedStatus_throwsException() {
            sampleEntity.setStatus("Terminated");
            when(repo.findById(1)).thenReturn(Optional.of(sampleEntity));

            assertThatThrownBy(() -> service.updateLicense(1, sampleRequest))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Cannot update Expired or Terminated license");
        }

        @Test
        @DisplayName("TC31 - should update territory field on entity before saving")
        void updateLicense_updatesTerritory() {
            sampleEntity.setStatus("Active");
            when(repo.findById(1)).thenReturn(Optional.of(sampleEntity));
            when(repo.save(any())).thenAnswer(inv -> inv.getArgument(0));

            LicenseAgreementRequestDTO update = new LicenseAgreementRequestDTO();
            update.setTerritory("FR");
            update.setRightsType("Streaming");
            update.setEndDate(LocalDate.now().plusDays(10));

            service.updateLicense(1, update);

            verify(repo).save(argThat(e -> "FR".equals(e.getTerritory())));
        }

        @Test
        @DisplayName("TC32 - should update endDate field on entity before saving")
        void updateLicense_updatesEndDate() {
            sampleEntity.setStatus("Active");
            LocalDate newEnd = LocalDate.now().plusDays(90);
            when(repo.findById(1)).thenReturn(Optional.of(sampleEntity));
            when(repo.save(any())).thenAnswer(inv -> inv.getArgument(0));

            LicenseAgreementRequestDTO update = new LicenseAgreementRequestDTO();
            update.setTerritory("US");
            update.setRightsType("Streaming");
            update.setEndDate(newEnd);

            service.updateLicense(1, update);

            verify(repo).save(argThat(e -> newEnd.equals(e.getEndDate())));
        }

        @Test
        @DisplayName("TC33 - should not save when license is in Expired state")
        void updateLicense_expired_doesNotCallSave() {
            sampleEntity.setStatus("Expired");
            when(repo.findById(1)).thenReturn(Optional.of(sampleEntity));

            assertThatThrownBy(() -> service.updateLicense(1, sampleRequest));
            verify(repo, never()).save(any());
        }

        @Test
        @DisplayName("TC34 - should not save when license is in Terminated state")
        void updateLicense_terminated_doesNotCallSave() {
            sampleEntity.setStatus("Terminated");
            when(repo.findById(1)).thenReturn(Optional.of(sampleEntity));

            assertThatThrownBy(() -> service.updateLicense(1, sampleRequest));
            verify(repo, never()).save(any());
        }
    }

    // ─────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────
    private LicenseAgreement buildEntity(int id, String territory, String status) {
        LicenseAgreement e = new LicenseAgreement();
        e.setLicenseId(id);
        e.setContentId(100);
        e.setLicensorId(200);
        e.setLicenseeRef("LIC-00" + id);
        e.setTerritory(territory);
        e.setRightsType("Streaming");
        e.setStartDate(LocalDate.now());
        e.setEndDate(LocalDate.now().plusDays(30));
        e.setLicenseFee(new BigDecimal("1000.00"));
        e.setStatus(status);
        return e;
    }
}
