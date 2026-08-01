package com.mediahub.licensing.service;

import com.mediahub.licensing.dto.request.TerritoryRestrictionRequestDTO;
import com.mediahub.licensing.dto.response.TerritoryRestrictionResponseDTO;
import com.mediahub.licensing.entity.TerritoryRestriction;
import com.mediahub.licensing.repository.TerritoryRestrictionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    public TerritoryRestrictionResponseDTO createRestriction(TerritoryRestrictionRequestDTO dto) {
        log.info("Creating Territory Restriction");
        TerritoryRestriction entity = toEntity(dto);
        entity.setStatus("Active");
        
        TerritoryRestriction saved =
        repo.save(entity);
        log.info("Territory Restriction Created Successfully");

        NotificationRequestDTO notification = new NotificationRequestDTO();

        notification.setUserId(101L);

        notification.setContentId(
                saved.getContentId());

        notification.setMessage(
                "Territory restriction created for Content "
                + saved.getContentId());

        notification.setCategory("LICENSE");

        executor.sendNotification(notification);

        return toResponseDTO(saved);
    }

    public List<TerritoryRestrictionResponseDTO> getByContentId(
        Integer contentId) {

        log.info("Fetching Territory Restrictions");

        return repo.findByContentIdAndStatus(
                        contentId,
                        "Active")
                .stream()
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

        existing.setRestrictedCountries(
                dto.getRestrictedCountries());

        existing.setAllowedCountries(
                dto.getAllowedCountries());

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
}
