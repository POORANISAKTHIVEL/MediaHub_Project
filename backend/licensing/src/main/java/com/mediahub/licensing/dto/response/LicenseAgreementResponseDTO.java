package com.mediahub.licensing.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public class LicenseAgreementResponseDTO {

    private Integer licenseId;
    private Integer contentId;
    private Integer licensorId;
    private String licenseeRef;
    private String territory;
    private String rightsType;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal licenseFee;
    private String status;

    public Integer getLicenseId() { return licenseId; }
    public void setLicenseId(Integer licenseId) { this.licenseId = licenseId; }

    public Integer getContentId() { return contentId; }
    public void setContentId(Integer contentId) { this.contentId = contentId; }

    public Integer getLicensorId() { return licensorId; }
    public void setLicensorId(Integer licensorId) { this.licensorId = licensorId; }

    public String getLicenseeRef() { return licenseeRef; }
    public void setLicenseeRef(String licenseeRef) { this.licenseeRef = licenseeRef; }

    public String getTerritory() { return territory; }
    public void setTerritory(String territory) { this.territory = territory; }

    public String getRightsType() { return rightsType; }
    public void setRightsType(String rightsType) { this.rightsType = rightsType; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public BigDecimal getLicenseFee() { return licenseFee; }
    public void setLicenseFee(BigDecimal licenseFee) { this.licenseFee = licenseFee; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
