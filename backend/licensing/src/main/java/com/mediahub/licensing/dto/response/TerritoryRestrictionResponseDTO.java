package com.mediahub.licensing.dto.response;

import java.time.LocalDate;

public class TerritoryRestrictionResponseDTO {

    private Integer restrictionId;
    private Integer contentId;
    private String restrictedCountries;
    private String allowedCountries;
    private LocalDate effectiveDate;
    private String status;

    public Integer getRestrictionId() { return restrictionId; }
    public void setRestrictionId(Integer restrictionId) { this.restrictionId = restrictionId; }

    public Integer getContentId() { return contentId; }
    public void setContentId(Integer contentId) { this.contentId = contentId; }

    public String getRestrictedCountries() { return restrictedCountries; }
    public void setRestrictedCountries(String restrictedCountries) { this.restrictedCountries = restrictedCountries; }

    public String getAllowedCountries() { return allowedCountries; }
    public void setAllowedCountries(String allowedCountries) { this.allowedCountries = allowedCountries; }

    public LocalDate getEffectiveDate() { return effectiveDate; }
    public void setEffectiveDate(LocalDate effectiveDate) { this.effectiveDate = effectiveDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
