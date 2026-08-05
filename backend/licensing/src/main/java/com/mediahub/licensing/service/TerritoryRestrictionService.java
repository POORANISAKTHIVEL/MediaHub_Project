package com.mediahub.licensing.service;

import com.mediahub.licensing.dto.request.TerritoryRestrictionRequestDTO;
import com.mediahub.licensing.dto.response.TerritoryRestrictionResponseDTO;
import com.mediahub.licensing.entity.TerritoryRestriction;
import com.mediahub.licensing.repository.TerritoryRestrictionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.mediahub.licensing.executor.LicenseNotificationExecutor;
import com.mediahub.licensing.dto.request.NotificationRequestDTO;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TerritoryRestrictionService {

    private static final Logger log = LoggerFactory.getLogger(TerritoryRestrictionService.class);

    @Autowired
    private TerritoryRestrictionRepository repo;

    @Autowired
    private LicenseNotificationExecutor executor;

    public TerritoryRestrictionResponseDTO createRestriction(TerritoryRestrictionRequestDTO dto, Long actorUserId) {
        log.info("Creating Territory Restriction");
        TerritoryRestriction entity = toEntity(dto);
        entity.setStatus("Active");
        
        TerritoryRestriction saved =
        repo.save(entity);
        log.info("Territory Restriction Created Successfully");

        NotificationRequestDTO notification = new NotificationRequestDTO();

        // Notify the rights manager who created the restriction — this used to be hardcoded
        // to a fake userId (101) that no real account maps to, so the notification went nowhere.
        notification.setUserId(actorUserId);

        notification.setContentId(
                saved.getContentId());

        notification.setMessage(
                "Territory restriction created for Content "
                + saved.getContentId());

        notification.setCategory("LICENSE");

        executor.sendNotification(notification, currentAuthHeader());

        return toResponseDTO(saved);
    }

    public List<TerritoryRestrictionResponseDTO> getByContentId(
        Integer contentId, boolean activeOnly) {

        log.info("Fetching Territory Restrictions");

        List<TerritoryRestriction> rows = activeOnly
                ? repo.findByContentIdAndStatus(contentId, "Active")
                : repo.findByContentId(contentId);

        return rows.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
   }

    public TerritoryRestrictionResponseDTO updateRestriction(
        Integer id,
        TerritoryRestrictionRequestDTO dto) {

        TerritoryRestriction existing = repo.findById(id)
                .orElseThrow(() -> {

                    log.error("Restriction Not Found");

                    return new RuntimeException(
                            "Restriction not found");
                });

        // Only overwrite fields the caller actually sent — a status-only toggle shouldn't
        // null out restrictedCountries/allowedCountries just because they weren't in that payload.
        if (dto.getRestrictedCountries() != null) {
            existing.setRestrictedCountries(dto.getRestrictedCountries());
        }
        if (dto.getAllowedCountries() != null) {
            existing.setAllowedCountries(dto.getAllowedCountries());
        }
        if (dto.getStatus() != null) {
            existing.setStatus(dto.getStatus());
        }

        return toResponseDTO(repo.save(existing));
   }

    private TerritoryRestriction toEntity(TerritoryRestrictionRequestDTO dto) {
        TerritoryRestriction entity = new TerritoryRestriction();
        entity.setContentId(dto.getContentId());
        entity.setRestrictedCountries(dto.getRestrictedCountries());
        entity.setAllowedCountries(dto.getAllowedCountries());
        entity.setEffectiveDate(dto.getEffectiveDate());
        return entity;
    }

    private TerritoryRestrictionResponseDTO toResponseDTO(TerritoryRestriction entity) {
        TerritoryRestrictionResponseDTO dto = new TerritoryRestrictionResponseDTO();
        dto.setRestrictionId(entity.getRestrictionId());
        dto.setContentId(entity.getContentId());
        dto.setRestrictedCountries(entity.getRestrictedCountries());
        dto.setAllowedCountries(entity.getAllowedCountries());
        dto.setEffectiveDate(entity.getEffectiveDate());
        dto.setStatus(entity.getStatus());
        return dto;
    }

    // executor.sendNotification runs @Async on a different thread, where
    // RequestContextHolder is empty — so the header must be read here, on the request thread.
    private String currentAuthHeader() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attrs != null ? attrs.getRequest().getHeader(HttpHeaders.AUTHORIZATION) : null;
    }
}
