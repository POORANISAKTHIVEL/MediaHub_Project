export interface ReportResponse {
  reportId: number;
  reportName: string;
  generatedDate: string;
  totalContents: number;
  activeSubscriptions?: number;
  totalRevenue?: number;
  activeLicenses?: number;
  contentStatusBreakdown?: { label: string; count: number }[];
  contentTypeBreakdown?: { label: string; count: number }[];
  /** Frontend-only fields — reportType/format/from/to aren't persisted by the backend, they're
   *  merged in client-side purely so the UI reflects what the user picked when generating. */
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
