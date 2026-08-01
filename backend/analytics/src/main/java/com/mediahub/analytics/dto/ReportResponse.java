package com.mediahub.analytics.dto;

public class ReportResponse {

    private Long   reportId;
    private String reportName;
    private String generatedDate;
    private int    totalContents;

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

    public void setReportId(Long reportId)           { this.reportId = reportId; }
    public void setReportName(String reportName)     { this.reportName = reportName; }
    public void setGeneratedDate(String date)        { this.generatedDate = date; }
    public void setTotalContents(int totalContents)  { this.totalContents = totalContents; }
}
