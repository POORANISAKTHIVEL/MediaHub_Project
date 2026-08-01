package com.mediahub.licensing.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity 
@Table(name = "license_agreement")
public class LicenseAgreement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "licenseId")
    private Integer licenseId;

    @Column(name = "contentId", nullable = false)
    private Integer contentId;

    @Column(name = "licensorId", nullable = false)
    private Integer licensorId;

    @Column(name = "licenseeRef", nullable = false)
    private String licenseeRef;

    @Column(name = "territory", nullable = false)
    private String territory;

    @Column(name = "rightsType", nullable = false)
    private String rightsType;

    @Column(name = "startDate", nullable = false)
    private LocalDate startDate;

    @Column(name = "endDate", nullable = false)
    private LocalDate endDate;

    @Column(name = "licenseFee", nullable = false)
    private BigDecimal licenseFee;

    @Column(name = "status", nullable = false)
    private String status = "Active";

    // ── Getters and Setters ────────────────────
    public Integer getLicenseId() { return licenseId; }
    public void setLicenseId(Integer licenseId) {
        this.licenseId = licenseId; }

    public Integer getContentId() { return contentId; }
    public void setContentId(Integer contentId) {
        this.contentId = contentId; }

    public Integer getLicensorId() { return licensorId; }
    public void setLicensorId(Integer licensorId) {
        this.licensorId = licensorId; }

    public String getLicenseeRef() { return licenseeRef; }
    public void setLicenseeRef(String licenseeRef) {
        this.licenseeRef = licenseeRef; }

    public String getTerritory() { return territory; }
    public void setTerritory(String territory) {
        this.territory = territory; }

    public String getRightsType() { return rightsType; }
    public void setRightsType(String rightsType) {
        this.rightsType = rightsType; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate; }

    public BigDecimal getLicenseFee() { return licenseFee; }
    public void setLicenseFee(BigDecimal licenseFee) {
        this.licenseFee = licenseFee; }

    public String getStatus() { return status; }
    public void setStatus(String status) {
        this.status = status; }
}