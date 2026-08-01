package com.mediahub.licensing.controller;

import com.mediahub.licensing.dto.request.TerritoryRestrictionRequestDTO;
import com.mediahub.licensing.dto.response.TerritoryRestrictionResponseDTO;
import com.mediahub.licensing.service.TerritoryRestrictionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/mediaHub/contentLicensing")
public class TerritoryRestrictionController {

    @Autowired
    private TerritoryRestrictionService service;

    @PreAuthorize("hasAuthority('license:manage')")
    @PostMapping("/createTerritoryRestriction/v1.0")
    public ResponseEntity<?> create(@RequestBody TerritoryRestrictionRequestDTO dto) {
        service.createRestriction(dto);
        return ResponseEntity.status(201).body("{\"message\": \"Territory rule created successfully\"}");
    }

    @PreAuthorize("hasAuthority('license:manage')")
    @GetMapping("/getTerritoryRestriction/v1.0/{contentId}")
    public ResponseEntity<List<TerritoryRestrictionResponseDTO>> getByContent(
            @PathVariable Integer contentId) {
        return ResponseEntity.ok(service.getByContentId(contentId));
    }

    @PreAuthorize("hasAuthority('license:manage')")
    @PutMapping("/updateTerritoryRestriction/v1.0/{id}")
    public ResponseEntity<?> update(
            @PathVariable Integer id,
            @RequestBody TerritoryRestrictionRequestDTO dto) {
        service.updateRestriction(id, dto);
        return ResponseEntity.ok("{\"message\": \"Territory rule updated successfully\"}");
    }
}
