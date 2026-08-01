package com.mediahub.licensing.controller;

import com.mediahub.licensing.dto.request.TerritoryRestrictionRequestDTO;
import com.mediahub.licensing.dto.response.TerritoryRestrictionResponseDTO;
import com.mediahub.licensing.service.TerritoryRestrictionService;
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

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TerritoryRestrictionControllerTest {

    @Mock
    private TerritoryRestrictionService service;

    @InjectMocks
    private TerritoryRestrictionController controller;

    private TerritoryRestrictionRequestDTO requestDTO;
    private TerritoryRestrictionResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        requestDTO = new TerritoryRestrictionRequestDTO();
        requestDTO.setContentId(100);
        requestDTO.setRestrictedCountries("CN,RU");
        requestDTO.setAllowedCountries("US,CA");
        requestDTO.setEffectiveDate(LocalDate.now());

        responseDTO = new TerritoryRestrictionResponseDTO();
        responseDTO.setRestrictionId(1);
        responseDTO.setContentId(100);
        responseDTO.setRestrictedCountries("CN,RU");
        responseDTO.setAllowedCountries("US,CA");
        responseDTO.setEffectiveDate(LocalDate.now());
        responseDTO.setStatus("Active");
    }

    // ─────────────────────────────────────────────────────────────
    // POST create
    // ─────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("create (POST /createTerritoryRestriction/v1.0)")
    class Create {

        @Test
        @DisplayName("TC83 - should return 201 when restriction created successfully")
        void create_returns201() {
            when(service.createRestriction(any())).thenReturn(responseDTO);

            ResponseEntity<?> response = controller.create(requestDTO);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        }

        @Test
        @DisplayName("TC84 - should return success message in body on create")
        void create_returnsSuccessMessage() {
            when(service.createRestriction(any())).thenReturn(responseDTO);

            ResponseEntity<?> response = controller.create(requestDTO);

            assertThat(response.getBody().toString()).contains("Territory rule created successfully");
        }

        @Test
        @DisplayName("TC85 - should call service.createRestriction once")
        void create_callsServiceOnce() {
            when(service.createRestriction(any())).thenReturn(responseDTO);

            controller.create(requestDTO);

            verify(service, times(1)).createRestriction(any());
        }

        @Test
        @DisplayName("TC86 - should propagate exception when service throws on create")
        void create_serviceThrows_propagatesException() {
            when(service.createRestriction(any())).thenThrow(new RuntimeException("DB error"));

            assertThatThrownBy(() -> controller.create(requestDTO))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("DB error");
        }

        @Test
        @DisplayName("TC87 - should pass request DTO directly to service")
        void create_passesRequestDtoToService() {
            when(service.createRestriction(any())).thenReturn(responseDTO);

            controller.create(requestDTO);

            verify(service).createRestriction(requestDTO);
        }

        @Test
        @DisplayName("TC88 - should return non-null body on create")
        void create_responseBodyNotNull() {
            when(service.createRestriction(any())).thenReturn(responseDTO);

            ResponseEntity<?> response = controller.create(requestDTO);

            assertThat(response.getBody()).isNotNull();
        }
    }

    // ─────────────────────────────────────────────────────────────
    // GET by content ID
    // ─────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("getByContent (GET /getTerritoryRestriction/v1.0/{contentId})")
    class GetByContent {

        @Test
        @DisplayName("TC89 - should return 200 with restrictions for contentId")
        void getByContent_returns200WithList() {
            when(service.getByContentId(100)).thenReturn(List.of(responseDTO));

            ResponseEntity<List<TerritoryRestrictionResponseDTO>> response = controller.getByContent(100);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).hasSize(1);
        }

        @Test
        @DisplayName("TC90 - should return 200 with empty list when no restrictions")
        void getByContent_emptyList_returns200() {
            when(service.getByContentId(999)).thenReturn(Collections.emptyList());

            ResponseEntity<List<TerritoryRestrictionResponseDTO>> response = controller.getByContent(999);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isEmpty();
        }

        @Test
        @DisplayName("TC91 - should return correct restrictedCountries in body")
        void getByContent_returnsRestrictedCountries() {
            when(service.getByContentId(100)).thenReturn(List.of(responseDTO));

            ResponseEntity<List<TerritoryRestrictionResponseDTO>> response = controller.getByContent(100);

            assertThat(response.getBody().get(0).getRestrictedCountries()).isEqualTo("CN,RU");
        }

        @Test
        @DisplayName("TC92 - should return correct allowedCountries in body")
        void getByContent_returnsAllowedCountries() {
            when(service.getByContentId(100)).thenReturn(List.of(responseDTO));

            ResponseEntity<List<TerritoryRestrictionResponseDTO>> response = controller.getByContent(100);

            assertThat(response.getBody().get(0).getAllowedCountries()).isEqualTo("US,CA");
        }

        @Test
        @DisplayName("TC93 - should call service with correct contentId")
        void getByContent_callsServiceWithCorrectId() {
            when(service.getByContentId(55)).thenReturn(Collections.emptyList());

            controller.getByContent(55);

            verify(service).getByContentId(55);
        }

        @Test
        @DisplayName("TC94 - should return multiple restrictions when available")
        void getByContent_multipleRestrictions() {
            TerritoryRestrictionResponseDTO second = new TerritoryRestrictionResponseDTO();
            second.setRestrictionId(2);
            second.setContentId(100);
            second.setStatus("Active");
            second.setEffectiveDate(LocalDate.now());

            when(service.getByContentId(100)).thenReturn(Arrays.asList(responseDTO, second));

            ResponseEntity<List<TerritoryRestrictionResponseDTO>> response = controller.getByContent(100);

            assertThat(response.getBody()).hasSize(2);
        }
    }

    // ─────────────────────────────────────────────────────────────
    // PUT update
    // ─────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("update (PUT /updateTerritoryRestriction/v1.0/{id})")
    class Update {

        @Test
        @DisplayName("TC95 - should return 200 when restriction updated successfully")
        void update_returns200() {
            when(service.updateRestriction(eq(1), any())).thenReturn(responseDTO);

            ResponseEntity<?> response = controller.update(1, requestDTO);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        }

        @Test
        @DisplayName("TC96 - should return success message on update")
        void update_returnsSuccessMessage() {
            when(service.updateRestriction(eq(1), any())).thenReturn(responseDTO);

            ResponseEntity<?> response = controller.update(1, requestDTO);

            assertThat(response.getBody().toString()).contains("Territory rule updated successfully");
        }

        @Test
        @DisplayName("TC97 - should propagate exception when service throws on update")
        void update_serviceThrows_propagatesException() {
            when(service.updateRestriction(eq(999), any()))
                    .thenThrow(new RuntimeException("Restriction not found"));

            assertThatThrownBy(() -> controller.update(999, requestDTO))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Restriction not found");
        }

        @Test
        @DisplayName("TC98 - should call service.updateRestriction with correct ID")
        void update_callsServiceWithCorrectId() {
            when(service.updateRestriction(eq(3), any())).thenReturn(responseDTO);

            controller.update(3, requestDTO);

            verify(service).updateRestriction(eq(3), any());
        }

        @Test
        @DisplayName("TC99 - should return non-null body on update")
        void update_responseBodyNotNull() {
            when(service.updateRestriction(eq(1), any())).thenReturn(responseDTO);

            ResponseEntity<?> response = controller.update(1, requestDTO);

            assertThat(response.getBody()).isNotNull();
        }

        @Test
        @DisplayName("TC100 - should call service exactly once on update")
        void update_callsServiceOnce() {
            when(service.updateRestriction(eq(1), any())).thenReturn(responseDTO);

            controller.update(1, requestDTO);

            verify(service, times(1)).updateRestriction(eq(1), any());
        }
    }
}
