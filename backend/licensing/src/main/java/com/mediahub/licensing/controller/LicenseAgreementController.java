package com.mediahub.licensing.controller;
 
import com.mediahub.licensing.dto.request.LicenseAgreementRequestDTO;
import com.mediahub.licensing.dto.response.LicenseAgreementResponseDTO;
import com.mediahub.licensing.dto.response.LicenseExpiringSoonResponseDTO;
import com.mediahub.licensing.service.LicenseAgreementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
 
import java.util.List;
 
@RestController
@RequestMapping("/mediaHub/contentLicensing")
public class LicenseAgreementController {
 
    @Autowired
    private LicenseAgreementService service;
 
    @PreAuthorize("hasAuthority('license:manage')")
    @PostMapping("/createLicense/v1.0")
    public ResponseEntity<?> create(
            @RequestBody LicenseAgreementRequestDTO dto) {
 
        service.createLicense(dto);
 
        return ResponseEntity.status(201)
                .body("{\"message\": \"License created successfully\"}");
    }
 
    @PreAuthorize("hasAuthority('license:manage')")
    @GetMapping("/getAllLicenses/v1.0")
    public ResponseEntity<List<LicenseAgreementResponseDTO>> getAll(
            @RequestParam(required = false) String status) {
 
        if (status != null) {
            return ResponseEntity.ok(service.getByStatus(status));
        }
 
        return ResponseEntity.ok(service.getAllLicenses());
    }
 
    @PreAuthorize("hasAuthority('license:manage')")
    @GetMapping("/getExpiringSoonLicenses/v1.0")
    public ResponseEntity<List<LicenseExpiringSoonResponseDTO>>
    expiringSoon() {
 
        return ResponseEntity.ok(service.getExpiringSoon());
    }
 
    @PreAuthorize("hasAuthority('license:manage')")
    @GetMapping("/getLicense/v1.0/{id}")
    public ResponseEntity<LicenseAgreementResponseDTO> getById(
            @PathVariable Integer id) {
 
        return service.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
 
    @PreAuthorize("hasAuthority('license:manage')")
    @PutMapping("/updateLicense/v1.0/{id}")
    public ResponseEntity<?> update(
            @PathVariable Integer id,
            @RequestBody LicenseAgreementRequestDTO dto) {
 
        service.updateLicense(id, dto);
 
        return ResponseEntity.ok(
                "{\"message\": \"License updated successfully\"}");
    }
 
    // ==========================
    // ANALYTICS API
    // ==========================
 
    @PreAuthorize("hasAuthority('license:manage')")
    @GetMapping("/analytics/v1.0")
    public ResponseEntity<?> getLicenseAnalytics() {
        return ResponseEntity.ok(
                service.getLicenseAnalytics());
    }

    // ✅ ROYALTY VALIDATION ENDPOINT
    @PreAuthorize("hasAuthority('license:manage')")
    @GetMapping("/validateLicensor/{licensorId}")
    public ResponseEntity<Boolean> validateLicensor(
            @PathVariable Integer licensorId) {
        return ResponseEntity.ok(service.validateLicensor(licensorId));
    }
}