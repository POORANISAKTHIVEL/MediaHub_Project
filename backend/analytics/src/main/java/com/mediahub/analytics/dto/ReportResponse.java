package com.mediahub.analytics.dto;

import java.util.List;
import java.util.Map;

public class ReportResponse {

    private Long   reportId;
    private String reportName;
    private String generatedDate;
    private int    totalContents;
    private int    activeSubscriptions;
    private double totalRevenue;
    private int    activeLicenses;
    private List<Map<String, Object>> contentStatusBreakdown;
    private List<Map<String, Object>> contentTypeBreakdown;

    public ReportResponse() {}

    public ReportResponse(Long reportId,
                          String reportName,
                          String generatedDate,
                          int totalContents) {
        this.reportId      = reportId;
        this.reportName    = reportName;
        this.generatedDate = generatedDate;
        this.totalContents = totalContents;
    }

    public Long   getReportId()      { return reportId; }
    public String getReportName()    { return reportName; }
    public String getGeneratedDate() { return generatedDate; }
    public int    getTotalContents() { return totalContents; }
    public int    getActiveSubscriptions() { return activeSubscriptions; }
    public double getTotalRevenue()  { return totalRevenue; }
    public int    getActiveLicenses() { return activeLicenses; }
    public List<Map<String, Object>> getContentStatusBreakdown() { return contentStatusBreakdown; }
    public List<Map<String, Object>> getContentTypeBreakdown()   { return contentTypeBreakdown; }

    public void setReportId(Long reportId)           { this.reportId = reportId; }
    public void setReportName(String reportName)     { this.reportName = reportName; }
    public void setGeneratedDate(String date)        { this.generatedDate = date; }
    public void setTotalContents(int totalContents)  { this.totalContents = totalContents; }
    public void setActiveSubscriptions(int activeSubscriptions) { this.activeSubscriptions = activeSubscriptions; }
    public void setTotalRevenue(double totalRevenue) { this.totalRevenue = totalRevenue; }
    public void setActiveLicenses(int activeLicenses) { this.activeLicenses = activeLicenses; }
    public void setContentStatusBreakdown(List<Map<String, Object>> contentStatusBreakdown) { this.contentStatusBreakdown = contentStatusBreakdown; }
    public void setContentTypeBreakdown(List<Map<String, Object>> contentTypeBreakdown)     { this.contentTypeBreakdown = contentTypeBreakdown; }
}
