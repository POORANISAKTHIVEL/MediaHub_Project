package com.mediahub.licensing.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "territory_restriction")
public class TerritoryRestriction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "restrictionId")
    private Integer restrictionId;

    @Column(name = "contentId", nullable = false)
    private Integer contentId;

    @Column(name = "restrictedCountries")
    private String restrictedCountries;

    @Column(name = "allowedCountries")
    private String allowedCountries;

    @Column(name = "effectiveDate", nullable = false)
    private LocalDate effectiveDate;

    @Column(name = "status", nullable = false)
    private String status = "Active";

    // ── Getters and Setters ────────────────────────
    public Integer getRestrictionId() { return restrictionId; }
    public void setRestrictionId(Integer restrictionId) {
        this.restrictionId = restrictionId; }

    public Integer getContentId() { return contentId; }
    public void setContentId(Integer contentId) {
        this.contentId = contentId; }

    public String getRestrictedCountries() {
        return restrictedCountries; }
    public void setRestrictedCountries(String restrictedCountries) {
        this.restrictedCountries = restrictedCountries; }

    public String getAllowedCountries() {
        return allowedCountries; }
    public void setAllowedCountries(String allowedCountries) {
        this.allowedCountries = allowedCountries; }

    public LocalDate getEffectiveDate() { return effectiveDate; }
    public void setEffectiveDate(LocalDate effectiveDate) {
        this.effectiveDate = effectiveDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) {
        this.status = status; }
}