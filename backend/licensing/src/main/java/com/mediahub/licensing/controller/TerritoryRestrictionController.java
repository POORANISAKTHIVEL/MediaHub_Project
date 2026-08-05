package com.mediahub.licensing.controller;

import com.mediahub.licensing.client.AuditClient;
import com.mediahub.licensing.dto.request.TerritoryRestrictionRequestDTO;
import com.mediahub.licensing.dto.response.TerritoryRestrictionResponseDTO;
import com.mediahub.licensing.service.TerritoryRestrictionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/mediaHub/contentLicensing")
public class TerritoryRestrictionController {

    @Autowired
    private TerritoryRestrictionService service;

    @Autowired
    private AuditClient auditClient;

    private static String actorRole(Authentication authentication) {
        return authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .filter(a -> a.startsWith("ROLE_"))
            .map(a -> a.substring(5))
            .findFirst()
            .orElse("UNKNOWN");
    }

    @PreAuthorize("hasAuthority('license:manage')")
    @PostMapping("/createTerritoryRestriction/v1.0")
    public ResponseEntity<?> create(@RequestBody TerritoryRestrictionRequestDTO dto, Authentication authentication) {
        service.createRestriction(dto, Long.valueOf(authentication.getName()));
        auditClient.log("TERRITORY_RESTRICTION_CREATED", "LICENSING", Long.valueOf(authentication.getName()),
            actorRole(authentication), "TerritoryRestriction", String.valueOf(dto.getContentId()),
            "Created territory restriction for content " + dto.getContentId(), "LOW");
        return ResponseEntity.status(201).body("{\"message\": \"Territory rule created successfully\"}");
    }

    // Editorial needs read access too — Publication Calendar checks for an active territory
    // restriction before it will let a schedule be created.
    @PreAuthorize("hasAnyAuthority('license:manage','editorial:manage')")
    @GetMapping("/getTerritoryRestriction/v1.0/{contentId}")
    public ResponseEntity<List<TerritoryRestrictionResponseDTO>> getByContent(
            @PathVariable Integer contentId,
            @RequestParam(defaultValue = "true") boolean activeOnly) {
        return ResponseEntity.ok(service.getByContentId(contentId, activeOnly));
    }

    @PreAuthorize("hasAuthority('license:manage')")
    @PutMapping("/updateTerritoryRestriction/v1.0/{id}")
    public ResponseEntity<?> update(
            @PathVariable Integer id,
            @RequestBody TerritoryRestrictionRequestDTO dto,
            Authentication authentication) {
        service.updateRestriction(id, dto);
        auditClient.log("TERRITORY_RESTRICTION_UPDATED", "LICENSING", Long.valueOf(authentication.getName()),
            actorRole(authentication), "TerritoryRestriction", id.toString(),
            "Updated territory restriction", "LOW");
        return ResponseEntity.ok("{\"message\": \"Territory rule updated successfully\"}");
    }
}
