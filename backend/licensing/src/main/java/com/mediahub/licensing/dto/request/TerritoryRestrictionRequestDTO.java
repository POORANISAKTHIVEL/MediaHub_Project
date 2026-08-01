package com.mediahub.licensing.dto.request;

import java.time.LocalDate;

public class TerritoryRestrictionRequestDTO {

    private Integer contentId;
    private String restrictedCountries;
    private String allowedCountries;
    private LocalDate effectiveDate;

    public Integer getContentId() { return contentId; }
    public void setContentId(Integer contentId) { this.contentId = contentId; }

    public String getRestrictedCountries() { return restrictedCountries; }
    public void setRestrictedCountries(String restrictedCountries) { this.restrictedCountries = restrictedCountries; }

    public String getAllowedCountries() { return allowedCountries; }
    public void setAllowedCountries(String allowedCountries) { this.allowedCountries = allowedCountries; }

    public LocalDate getEffectiveDate() { return effectiveDate; }
    public void setEffectiveDate(LocalDate effectiveDate) { this.effectiveDate = effectiveDate; }
}
