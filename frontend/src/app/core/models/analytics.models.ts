export interface ReportResponse {
  reportId: number;
  reportName: string;
  generatedDate: string;
  totalContents: number;
  /** Frontend-only fields — the real ReportResponse only has the four fields above.
   *  reportType/format/from/to would need backend additions to persist for real. */
  reportType?: string;
  format?: string;
  fromDate?: string;
  toDate?: string;
}

export interface DashboardAnalytics {
  contentCatalogAnalytics: {
    totalContents: number;
    contentStatusBreakdown: { label: string; count: number }[];
    contentTypeBreakdown: { label: string; count: number }[];
  };
  subscriptionAnalytics: Record<string, any>;
  editorialAnalytics: Record<string, any>;
  licensingAnalytics: Record<string, any>;
  revenueAnalytics: Record<string, any>;
  notificationAnalytics: Record<string, any>;
  iamAuditAnalytics: Record<string, any>;
}
