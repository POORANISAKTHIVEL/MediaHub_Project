package com.mediahub.licensing.service;
 
import com.mediahub.licensing.dto.request.LicenseAgreementRequestDTO;
import com.mediahub.licensing.dto.response.LicenseAgreementResponseDTO;
import com.mediahub.licensing.dto.response.LicenseExpiringSoonResponseDTO;
import com.mediahub.licensing.entity.LicenseAgreement;
import com.mediahub.licensing.repository.LicenseAgreementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import com.mediahub.licensing.executor.ContentValidationExecutor;
import com.mediahub.licensing.dto.request.NotificationRequestDTO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.mediahub.licensing.executor.LicenseNotificationExecutor;
 
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
 
@Service
public class LicenseAgreementService {
    
private static final Logger log = LoggerFactory.getLogger(LicenseAgreementService.class);
    
    @Autowired
    private LicenseNotificationExecutor executor;

    @Autowired
    private LicenseAgreementRepository repo;

   @Autowired
   private ContentValidationExecutor contentValidationExecutor;

    
 
public LicenseAgreementResponseDTO createLicense(LicenseAgreementRequestDTO dto, Long actorUserId) {
 
    log.info("Creating License");
 
    //  CONTENT CATALOG VALIDATION
    Boolean contentExists =
                contentValidationExecutor.validateContent(
                        dto.getContentId());
 
    if (contentExists == null
            || !contentExists) {
 
        log.error(
                "Content not found in Content Catalog : {}",
                dto.getContentId());
 
        throw new RuntimeException(
                "Content not found in Content Catalog");
    }
 
    LicenseAgreement entity = toEntity(dto);
 
    entity.setStatus("Active");
 
    LicenseAgreement saved = repo.save(entity);
 
    log.info("License Created Successfully");
 
    NotificationRequestDTO notification =
            new NotificationRequestDTO();
 
    // Notify the rights manager who created the license, not the manually-typed
    // licensorId field (which is often not a real user account at all).
    notification.setUserId(actorUserId);
 
    notification.setLicenseId(
            saved.getLicenseId());
 
    notification.setContentId(
            saved.getContentId());
 
    notification.setExpiryDate(
            saved.getEndDate());
 
    notification.setMessage(
            "License created successfully for Content "
                    + saved.getContentId());
 
    notification.setCategory("LICENSE");
 
    executor.sendNotification(notification, currentAuthHeader());
 
    return toResponseDTO(saved);
}
 
    public List<LicenseAgreementResponseDTO> getAllLicenses() {

        log.info("Fetching All Licenses");

        return repo.findAll()
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
   }
 
   public List<LicenseAgreementResponseDTO> getByStatus(String status) {

    log.info("Fetching Licenses with Status : {}", status);

    return repo.findByStatus(status)
            .stream()
            .map(this::toResponseDTO)
            .collect(Collectors.toList());
   }
 
    public Optional<LicenseAgreementResponseDTO> getById(Integer id) {

        Optional<LicenseAgreement> license = repo.findById(id);

        if (license.isEmpty()) {
                log.error("License Not Found");
        }

        return license.map(this::toResponseDTO);
    }
 
    public List<LicenseExpiringSoonResponseDTO> getExpiringSoon() {

    log.info("Fetching Expiring Soon Licenses");

    LocalDate today = LocalDate.now();
    LocalDate sevenDaysLater = today.plusDays(7);

    return repo.findExpiringSoon(today, sevenDaysLater)
            .stream()
            .map(this::toExpiringSoonDTO)
            .collect(Collectors.toList());
}
 
    public LicenseAgreementResponseDTO updateLicense(
        Integer id,
        LicenseAgreementRequestDTO dto) {

        log.info("Updating License Id : {}", id);

        LicenseAgreement existing = repo.findById(id)
                .orElseThrow(() -> {

                        log.error("License Not Found");

                        return new RuntimeException("License not found");
                });

        if (existing.getStatus().equals("Expired")
                || existing.getStatus().equals("Terminated")) {

                log.error("Cannot update Expired or Terminated License");

                throw new RuntimeException(
                        "Cannot update Expired or Terminated license");
        }

        existing.setTerritory(dto.getTerritory());
        existing.setRightsType(dto.getRightsType());
        existing.setEndDate(dto.getEndDate());
        existing.setStatus(dto.getStatus());

        LicenseAgreement updated = repo.save(existing);

        log.info("License Updated Successfully");

        return toResponseDTO(updated);
   }
 
    // ✅ ROYALTY VALIDATION — check if a creator has an active license
    public boolean validateLicensor(Integer licensorId) {
        log.info("Validating licensor ID: {}", licensorId);
        boolean hasActiveLicense = repo.existsByLicensorIdAndStatus(licensorId, "Active");
        log.info("Licensor {} has active license: {}", licensorId, hasActiveLicense);
        return hasActiveLicense;
    }

    // ==========================
    // ANALYTICS METHOD
    // ==========================
    public Map<String, Object> getLicenseAnalytics() {
 
        List<LicenseAgreement> licenses = repo.findAll();
 
        int totalLicenses = licenses.size();
 
        long activeLicenses = licenses.stream()
                .filter(l -> "Active".equalsIgnoreCase(l.getStatus()))
                .count();
 
        long expiredLicenses = licenses.stream()
                .filter(l -> "Expired".equalsIgnoreCase(l.getStatus()))
                .count();
 
        long terminatedLicenses = licenses.stream()
                .filter(l -> "Terminated".equalsIgnoreCase(l.getStatus()))
                .count();
 
        long exclusiveLicenses = licenses.stream()
                .filter(l -> "Exclusive".equalsIgnoreCase(l.getRightsType()))
                .count();
 
        long nonExclusiveLicenses = licenses.stream()
                .filter(l -> "Non-Exclusive".equalsIgnoreCase(l.getRightsType()))
                .count();
 
        long expiringSoonLicenses = getExpiringSoon().size();
 
        Map<String, Object> response = new HashMap<>();
 
        response.put("message", "License analytics retrieved successfully");
        response.put("totalLicenses", totalLicenses);
        response.put("activeLicenses", activeLicenses);
        response.put("expiredLicenses", expiredLicenses);
        response.put("terminatedLicenses", terminatedLicenses);
        response.put("exclusiveLicenses", exclusiveLicenses);
        response.put("nonExclusiveLicenses", nonExclusiveLicenses);
        response.put("expiringSoonLicenses", expiringSoonLicenses);
 
        return response;
    }
 
    private LicenseAgreement toEntity(LicenseAgreementRequestDTO dto) {
 
        LicenseAgreement entity = new LicenseAgreement();
 
        entity.setContentId(dto.getContentId());
        entity.setLicensorId(dto.getLicensorId());
        entity.setLicenseeRef(dto.getLicenseeRef());
        entity.setTerritory(dto.getTerritory());
        entity.setRightsType(dto.getRightsType());
        entity.setStartDate(dto.getStartDate());
        entity.setEndDate(dto.getEndDate());
        entity.setLicenseFee(dto.getLicenseFee());
 
        return entity;
    }
 
    private LicenseAgreementResponseDTO toResponseDTO(
            LicenseAgreement entity) {
 
        LicenseAgreementResponseDTO dto =
                new LicenseAgreementResponseDTO();
 
        dto.setLicenseId(entity.getLicenseId());
        dto.setContentId(entity.getContentId());
        dto.setLicensorId(entity.getLicensorId());
        dto.setLicenseeRef(entity.getLicenseeRef());
        dto.setTerritory(entity.getTerritory());
        dto.setRightsType(entity.getRightsType());
        dto.setStartDate(entity.getStartDate());
        dto.setEndDate(entity.getEndDate());
        dto.setLicenseFee(entity.getLicenseFee());
        dto.setStatus(entity.getStatus());
 
        return dto;
    }
 
    private LicenseExpiringSoonResponseDTO toExpiringSoonDTO(
            LicenseAgreement entity) {
 
        LicenseExpiringSoonResponseDTO dto =
                new LicenseExpiringSoonResponseDTO();
 
        dto.setLicenseId(entity.getLicenseId());
        dto.setTerritory(entity.getTerritory());
        dto.setEndDate(entity.getEndDate());
 
        long days = ChronoUnit.DAYS.between(
                LocalDate.now(),
                entity.getEndDate());
 
        dto.setDaysRemaining(days);

        return dto;
    }

    // executor.sendNotification runs @Async on a different thread, where
    // RequestContextHolder is empty — so the header must be read here, on the request thread.
    private String currentAuthHeader() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attrs != null ? attrs.getRequest().getHeader(HttpHeaders.AUTHORIZATION) : null;
    }
}