package com.mediahub.licensing.controller;

import com.mediahub.licensing.dto.request.LicenseAgreementRequestDTO;
import com.mediahub.licensing.dto.response.LicenseAgreementResponseDTO;
import com.mediahub.licensing.dto.response.LicenseExpiringSoonResponseDTO;
import com.mediahub.licensing.service.LicenseAgreementService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LicenseAgreementControllerTest {

    @Mock
    private LicenseAgreementService service;

    @InjectMocks
    private LicenseAgreementController controller;

    private LicenseAgreementRequestDTO requestDTO;
    private LicenseAgreementResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        requestDTO = new LicenseAgreementRequestDTO();
        requestDTO.setContentId(100);
        requestDTO.setLicensorId(200);
        requestDTO.setLicenseeRef("LIC-001");
        requestDTO.setTerritory("US");
        requestDTO.setRightsType("Streaming");
        requestDTO.setStartDate(LocalDate.now());
        requestDTO.setEndDate(LocalDate.now().plusDays(30));
        requestDTO.setLicenseFee(new BigDecimal("5000.00"));

        responseDTO = new LicenseAgreementResponseDTO();
        responseDTO.setLicenseId(1);
        responseDTO.setContentId(100);
        responseDTO.setLicensorId(200);
        responseDTO.setLicenseeRef("LIC-001");
        responseDTO.setTerritory("US");
        responseDTO.setRightsType("Streaming");
        responseDTO.setStartDate(LocalDate.now());
        responseDTO.setEndDate(LocalDate.now().plusDays(30));
        responseDTO.setLicenseFee(new BigDecimal("5000.00"));
        responseDTO.setStatus("Active");
    }

    // ─────────────────────────────────────────────────────────────
    // POST create
    // ─────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("create (POST /createLicense/v1.0)")
    class Create {

        @Test
        @DisplayName("TC56 - should return 201 when license created successfully")
        void create_returns201() {
            when(service.createLicense(any())).thenReturn(responseDTO);

            ResponseEntity<?> response = controller.create(requestDTO);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        }

        @Test
        @DisplayName("TC57 - should return success message in body on create")
        void create_returnsSuccessMessage() {
            when(service.createLicense(any())).thenReturn(responseDTO);

            ResponseEntity<?> response = controller.create(requestDTO);

            assertThat(response.getBody().toString()).contains("License created successfully");
        }

        @Test
        @DisplayName("TC58 - should call service.createLicense once")
        void create_callsServiceOnce() {
            when(service.createLicense(any())).thenReturn(responseDTO);

            controller.create(requestDTO);

            verify(service, times(1)).createLicense(any());
        }

        @Test
        @DisplayName("TC59 - should propagate exception when service throws")
        void create_serviceThrows_propagatesException() {
            when(service.createLicense(any())).thenThrow(new RuntimeException("DB error"));

            assertThatThrownBy(() -> controller.create(requestDTO))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("DB error");
        }

        @Test
        @DisplayName("TC60 - should pass request DTO directly to service")
        void create_passesRequestDtoToService() {
            when(service.createLicense(any())).thenReturn(responseDTO);

            controller.create(requestDTO);

            verify(service).createLicense(requestDTO);
        }

        @Test
        @DisplayName("TC61 - should return non-null body on create")
        void create_responseBodyNotNull() {
            when(service.createLicense(any())).thenReturn(responseDTO);

            ResponseEntity<?> response = controller.create(requestDTO);

            assertThat(response.getBody()).isNotNull();
        }
    }

    // ─────────────────────────────────────────────────────────────
    // GET all
    // ─────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("getAll (GET /getAllLicenses/v1.0)")
    class GetAll {

        @Test
        @DisplayName("TC62 - should return 200 with all licenses when no status param")
        void getAll_noStatus_returns200WithList() {
            when(service.getAllLicenses()).thenReturn(List.of(responseDTO));

            ResponseEntity<List<LicenseAgreementResponseDTO>> response = controller.getAll(null);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).hasSize(1);
        }

        @Test
        @DisplayName("TC63 - should return empty list when no licenses")
        void getAll_noStatus_emptyRepo() {
            when(service.getAllLicenses()).thenReturn(Collections.emptyList());

            ResponseEntity<List<LicenseAgreementResponseDTO>> response = controller.getAll(null);

            assertThat(response.getBody()).isEmpty();
        }

        @Test
        @DisplayName("TC64 - should call getByStatus when status param is provided")
        void getAll_withStatus_callsGetByStatus() {
            when(service.getByStatus("Active")).thenReturn(List.of(responseDTO));

            controller.getAll("Active");

            verify(service).getByStatus("Active");
            verify(service, never()).getAllLicenses();
        }

        @Test
        @DisplayName("TC65 - should call getAllLicenses when status param is null")
        void getAll_nullStatus_callsGetAll() {
            when(service.getAllLicenses()).thenReturn(Collections.emptyList());

            controller.getAll(null);

            verify(service).getAllLicenses();
            verify(service, never()).getByStatus(any());
        }

        @Test
        @DisplayName("TC66 - should return filtered list when status is Expired")
        void getAll_expiredStatus_returnsFiltered() {
            LicenseAgreementResponseDTO expired = new LicenseAgreementResponseDTO();
            expired.setLicenseId(2);
            expired.setStatus("Expired");
            when(service.getByStatus("Expired")).thenReturn(List.of(expired));

            ResponseEntity<List<LicenseAgreementResponseDTO>> response = controller.getAll("Expired");

            assertThat(response.getBody()).hasSize(1);
            assertThat(response.getBody().get(0).getStatus()).isEqualTo("Expired");
        }

        @Test
        @DisplayName("TC67 - should return multiple licenses when getAllLicenses returns many")
        void getAll_multipleLicenses() {
            LicenseAgreementResponseDTO second = new LicenseAgreementResponseDTO();
            second.setLicenseId(2);
            when(service.getAllLicenses()).thenReturn(Arrays.asList(responseDTO, second));

            ResponseEntity<List<LicenseAgreementResponseDTO>> response = controller.getAll(null);

            assertThat(response.getBody()).hasSize(2);
        }
    }

    // ─────────────────────────────────────────────────────────────
    // GET expiring soon
    // ─────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("expiringSoon (GET /getExpiringSoonLicenses/v1.0)")
    class ExpiringSoon {

        @Test
        @DisplayName("TC68 - should return 200 with expiring soon list")
        void expiringSoon_returns200WithList() {
            LicenseExpiringSoonResponseDTO dto = buildExpiringSoonDTO(1, "US", 3L);
            when(service.getExpiringSoon()).thenReturn(List.of(dto));

            ResponseEntity<List<LicenseExpiringSoonResponseDTO>> response = controller.expiringSoon();

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).hasSize(1);
        }

        @Test
        @DisplayName("TC69 - should return daysRemaining correctly")
        void expiringSoon_returnsDaysRemaining() {
            LicenseExpiringSoonResponseDTO dto = buildExpiringSoonDTO(1, "US", 5L);
            when(service.getExpiringSoon()).thenReturn(List.of(dto));

            ResponseEntity<List<LicenseExpiringSoonResponseDTO>> response = controller.expiringSoon();

            assertThat(response.getBody().get(0).getDaysRemaining()).isEqualTo(5L);
        }

        @Test
        @DisplayName("TC70 - should return empty list when none expiring soon")
        void expiringSoon_emptyList() {
            when(service.getExpiringSoon()).thenReturn(Collections.emptyList());

            ResponseEntity<List<LicenseExpiringSoonResponseDTO>> response = controller.expiringSoon();

            assertThat(response.getBody()).isEmpty();
        }

        @Test
        @DisplayName("TC71 - should call service.getExpiringSoon exactly once")
        void expiringSoon_callsServiceOnce() {
            when(service.getExpiringSoon()).thenReturn(Collections.emptyList());

            controller.expiringSoon();

            verify(service, times(1)).getExpiringSoon();
        }

        private LicenseExpiringSoonResponseDTO buildExpiringSoonDTO(int id, String territory, long days) {
            LicenseExpiringSoonResponseDTO dto = new LicenseExpiringSoonResponseDTO();
            dto.setLicenseId(id);
            dto.setTerritory(territory);
            dto.setEndDate(LocalDate.now().plusDays(days));
            dto.setDaysRemaining(days);
            return dto;
        }
    }

    // ─────────────────────────────────────────────────────────────
    // GET by ID
    // ─────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("getById (GET /getLicense/v1.0/{id})")
    class GetById {

        @Test
        @DisplayName("TC72 - should return 200 with DTO when license found")
        void getById_found_returns200() {
            when(service.getById(1)).thenReturn(Optional.of(responseDTO));

            ResponseEntity<LicenseAgreementResponseDTO> response = controller.getById(1);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody().getLicenseId()).isEqualTo(1);
        }

        @Test
        @DisplayName("TC73 - should return 404 when license not found")
        void getById_notFound_returns404() {
            when(service.getById(999)).thenReturn(Optional.empty());

            ResponseEntity<LicenseAgreementResponseDTO> response = controller.getById(999);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        @DisplayName("TC74 - should return territory in response body")
        void getById_returnsTerritory() {
            when(service.getById(1)).thenReturn(Optional.of(responseDTO));

            ResponseEntity<LicenseAgreementResponseDTO> response = controller.getById(1);

            assertThat(response.getBody().getTerritory()).isEqualTo("US");
        }

        @Test
        @DisplayName("TC75 - should return status in response body")
        void getById_returnsStatus() {
            when(service.getById(1)).thenReturn(Optional.of(responseDTO));

            ResponseEntity<LicenseAgreementResponseDTO> response = controller.getById(1);

            assertThat(response.getBody().getStatus()).isEqualTo("Active");
        }

        @Test
        @DisplayName("TC76 - should call service with correct ID")
        void getById_callsServiceWithCorrectId() {
            when(service.getById(7)).thenReturn(Optional.empty());

            controller.getById(7);

            verify(service).getById(7);
        }

        @Test
        @DisplayName("TC77 - should return null body on 404")
        void getById_notFound_hasNullBody() {
            when(service.getById(999)).thenReturn(Optional.empty());

            ResponseEntity<LicenseAgreementResponseDTO> response = controller.getById(999);

            assertThat(response.getBody()).isNull();
        }
    }

    // ─────────────────────────────────────────────────────────────
    // PUT update
    // ─────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("update (PUT /updateLicense/v1.0/{id})")
    class Update {

        @Test
        @DisplayName("TC78 - should return 200 when license updated successfully")
        void update_returns200() {
            when(service.updateLicense(eq(1), any())).thenReturn(responseDTO);

            ResponseEntity<?> response = controller.update(1, requestDTO);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        }

        @Test
        @DisplayName("TC79 - should return success message on update")
        void update_returnsSuccessMessage() {
            when(service.updateLicense(eq(1), any())).thenReturn(responseDTO);

            ResponseEntity<?> response = controller.update(1, requestDTO);

            assertThat(response.getBody().toString()).contains("License updated successfully");
        }

        @Test
        @DisplayName("TC80 - should propagate exception when service throws on update")
        void update_serviceThrows_propagatesException() {
            when(service.updateLicense(eq(1), any()))
                    .thenThrow(new RuntimeException("Cannot update Expired or Terminated license"));

            assertThatThrownBy(() -> controller.update(1, requestDTO))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Cannot update Expired or Terminated license");
        }

        @Test
        @DisplayName("TC81 - should call service.updateLicense with correct ID")
        void update_callsServiceWithCorrectId() {
            when(service.updateLicense(eq(5), any())).thenReturn(responseDTO);

            controller.update(5, requestDTO);

            verify(service).updateLicense(eq(5), any());
        }

        @Test
        @DisplayName("TC82 - should not call service when exception is thrown before save")
        void update_licenseNotFound_throws() {
            when(service.updateLicense(eq(999), any()))
                    .thenThrow(new RuntimeException("License not found"));

            assertThatThrownBy(() -> controller.update(999, requestDTO))
                    .hasMessageContaining("License not found");
        }
    }
}
