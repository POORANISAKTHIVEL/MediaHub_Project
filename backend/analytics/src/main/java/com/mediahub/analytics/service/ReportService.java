package com.mediahub.analytics.service;

import com.mediahub.analytics.dto.ReportResponse;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Service
public class ReportService {

    private final Map<Long, ReportResponse> reportStore = new HashMap<>();
    private Long reportCounter = 1L;

    public ReportResponse generateReport(Map<String, Object> analyticsData) {

        // Extract content count from content catalog analytics
        int totalContents = 0;
        Object contentAnalytics = analyticsData.get("contentCatalogAnalytics");
        if (contentAnalytics instanceof Map) {
            Object tc = ((Map<?, ?>) contentAnalytics).get("totalContents");
            if (tc != null) {
                totalContents = Integer.parseInt(tc.toString());
            }
        }

        // Extract total subscriptions from subscription analytics
        int totalSubscriptions = 0;
        Object subAnalytics = analyticsData.get("subscriptionAnalytics");
        if (subAnalytics instanceof Map) {
            Object ts = ((Map<?, ?>) subAnalytics).get("totalSubscriptions");
            if (ts != null) {
                totalSubscriptions = Integer.parseInt(ts.toString());
            }
        }

        // Extract total audit events from IAM analytics
        long totalAuditEvents = 0;
        Object iamAnalytics = analyticsData.get("iamAuditAnalytics");
        if (iamAnalytics instanceof Map) {
            Object te = ((Map<?, ?>) iamAnalytics).get("totalAuditEvents");
            if (te != null) {
                totalAuditEvents = Long.parseLong(te.toString());
            }
        }

        ReportResponse report = new ReportResponse(
                reportCounter++,
                "MediaHub Analytics Report",
                LocalDate.now().toString(),
                totalContents
        );

        reportStore.put(report.getReportId(), report);
        return report;
    }

    public ReportResponse getReportById(Long id) {
        return reportStore.get(id);
    }

    public String deleteReport(Long id) {
        if (reportStore.remove(id) != null) {
            return "Report deleted successfully";
        }
        return "Report not found";
    }

    public byte[] downloadReport(Long id) throws Exception {

        ReportResponse report = reportStore.get(id);
        if (report == null) {
            throw new RuntimeException("Report not found with ID: " + id);
        }

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {

            Sheet sheet = workbook.createSheet("MediaHub Analytics Report");

            // Header row
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Field");
            header.createCell(1).setCellValue("Value");

            // Data rows
            Row row0 = sheet.createRow(1);
            row0.createCell(0).setCellValue("Report ID");
            row0.createCell(1).setCellValue(report.getReportId());

            Row row1 = sheet.createRow(2);
            row1.createCell(0).setCellValue("Report Name");
            row1.createCell(1).setCellValue(report.getReportName());

            Row row2 = sheet.createRow(3);
            row2.createCell(0).setCellValue("Generated Date");
            row2.createCell(1).setCellValue(report.getGeneratedDate());

            Row row3 = sheet.createRow(4);
            row3.createCell(0).setCellValue("Total Contents");
            row3.createCell(1).setCellValue(report.getTotalContents());

            // Auto-size columns
            sheet.autoSizeColumn(0);
            sheet.autoSizeColumn(1);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        }
    }
}
