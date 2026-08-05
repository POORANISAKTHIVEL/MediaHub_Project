package com.mediahub.analytics.service;

import com.mediahub.analytics.dto.ReportResponse;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReportService {

    private final Map<Long, ReportResponse> reportStore = new HashMap<>();
    private Long reportCounter = 1L;

    public ReportResponse generateReport(Map<String, Object> analyticsData) {

        // The dashboard already assembles all of this from the same 7-module analyticsData map
        // (see AnalyticsService#getAnalytics) — the report was only ever pulling totalContents
        // out of it and discarding the rest, so downloads never had subscriptions/revenue/
        // licenses/breakdowns even though the dashboard clearly has the data.
        Map<?, ?> contentAnalytics = asMap(analyticsData.get("contentCatalogAnalytics"));
        Map<?, ?> subAnalytics     = asMap(analyticsData.get("subscriptionAnalytics"));
        Map<?, ?> revenueAnalytics = asMap(analyticsData.get("revenueAnalytics"));
        Map<?, ?> licensingAnalytics = asMap(analyticsData.get("licensingAnalytics"));

        int totalContents = intOf(contentAnalytics, "totalContents");
        int activeSubscriptions = intOf(subAnalytics, "activeSubscriptions");
        double totalRevenue = doubleOf(revenueAnalytics, "totalRevenue");
        int activeLicenses = intOf(licensingAnalytics, "activeLicenses");

        List<Map<String, Object>> statusBreakdown = listOf(contentAnalytics, "contentStatusBreakdown");
        List<Map<String, Object>> typeBreakdown = listOf(contentAnalytics, "contentTypeBreakdown");

        ReportResponse report = new ReportResponse(
                reportCounter++,
                "MediaHub Analytics Report",
                LocalDate.now().toString(),
                totalContents
        );
        report.setActiveSubscriptions(activeSubscriptions);
        report.setTotalRevenue(totalRevenue);
        report.setActiveLicenses(activeLicenses);
        report.setContentStatusBreakdown(statusBreakdown);
        report.setContentTypeBreakdown(typeBreakdown);

        reportStore.put(report.getReportId(), report);
        return report;
    }

    private Map<?, ?> asMap(Object value) {
        return value instanceof Map ? (Map<?, ?>) value : Collections.emptyMap();
    }

    private int intOf(Map<?, ?> map, String key) {
        Object v = map.get(key);
        return v != null ? Integer.parseInt(v.toString()) : 0;
    }

    private double doubleOf(Map<?, ?> map, String key) {
        Object v = map.get(key);
        return v != null ? Double.parseDouble(v.toString()) : 0.0;
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> listOf(Map<?, ?> map, String key) {
        Object v = map.get(key);
        return v instanceof List ? (List<Map<String, Object>>) v : Collections.emptyList();
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

            Row row4 = sheet.createRow(5);
            row4.createCell(0).setCellValue("Active Subscriptions");
            row4.createCell(1).setCellValue(report.getActiveSubscriptions());

            Row row5 = sheet.createRow(6);
            row5.createCell(0).setCellValue("Total Revenue");
            row5.createCell(1).setCellValue(report.getTotalRevenue());

            Row row6 = sheet.createRow(7);
            row6.createCell(0).setCellValue("Active Licenses");
            row6.createCell(1).setCellValue(report.getActiveLicenses());

            int nextRow = 9;
            nextRow = writeBreakdown(sheet, nextRow, "Content by Status", report.getContentStatusBreakdown());
            nextRow = writeBreakdown(sheet, nextRow, "Content by Type", report.getContentTypeBreakdown());

            // Auto-size columns
            sheet.autoSizeColumn(0);
            sheet.autoSizeColumn(1);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        }
    }

    private int writeBreakdown(Sheet sheet, int startRow, String title, List<Map<String, Object>> breakdown) {
        Row titleRow = sheet.createRow(startRow);
        titleRow.createCell(0).setCellValue(title);
        int row = startRow + 1;
        if (breakdown != null) {
            for (Map<String, Object> entry : breakdown) {
                Row r = sheet.createRow(row++);
                r.createCell(0).setCellValue(String.valueOf(entry.get("label")));
                Object count = entry.get("count");
                r.createCell(1).setCellValue(count != null ? Double.parseDouble(count.toString()) : 0);
            }
        }
        return row + 1;
    }
}
