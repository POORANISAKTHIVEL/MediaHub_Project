package com.mediahub.licensing.dto.response;

import java.time.LocalDate;

public class LicenseExpiringSoonResponseDTO {

    private Integer licenseId;
    private String territory;
    private LocalDate endDate;
    private Long daysRemaining;

    // Getters and Setters
    public Integer getLicenseId() { return licenseId; }
    public void setLicenseId(Integer licenseId) {
        this.licenseId = licenseId; }

    public String getTerritory() { return territory; }
    public void setTerritory(String territory) {
        this.territory = territory; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate; }

    public Long getDaysRemaining() { return daysRemaining; }
    public void setDaysRemaining(Long daysRemaining) {
        this.daysRemaining = daysRemaining; }
}