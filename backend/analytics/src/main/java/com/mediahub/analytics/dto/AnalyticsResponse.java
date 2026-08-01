package com.mediahub.analytics.dto;

public class AnalyticsResponse {

    private int totalSubscriptions;
    private int activeSubscriptions;
    private int failedPayouts;
    private int totalRevenue;

    public AnalyticsResponse(int totalSubscriptions,
                              int activeSubscriptions,
                              int failedPayouts,
                              int totalRevenue) {
        this.totalSubscriptions = totalSubscriptions;
        this.activeSubscriptions = activeSubscriptions;
        this.failedPayouts       = failedPayouts;
        this.totalRevenue        = totalRevenue;
    }

    public int getTotalSubscriptions() { return totalSubscriptions; }
    public int getActiveSubscriptions() { return activeSubscriptions; }
    public int getFailedPayouts()       { return failedPayouts; }
    public int getTotalRevenue()        { return totalRevenue; }
}
