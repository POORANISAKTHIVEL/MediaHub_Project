import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, forkJoin, map, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import { MockStore, mockOf } from './mock-store';
import { ReportResponse } from '../models/analytics.models';
import { ContentClient } from './content-client';
import { SubscriptionClient } from './subscription-client';
import { RoyaltyClient } from './royalty-client';
import { LicensingClient } from './licensing-client';
import { NotificationClient } from './notification-client';

let reportSeq = 1;
const REPORT_SEED: ReportResponse[] = [];

@Injectable({ providedIn: 'root' })
export class AnalyticsClient {
  private http = inject(HttpClient);
  private base = `${environment.apiBaseUrl}/analytics/mediaHub`;
  private content = inject(ContentClient);
  private subscription = inject(SubscriptionClient);
  private royalty = inject(RoyaltyClient);
  private licensing = inject(LicensingClient);
  private notification = inject(NotificationClient);

  private reports = new MockStore<ReportResponse>(REPORT_SEED, 'reportId');

  /** Real backend has no "list all reports" endpoint — only generate/get-by-id/delete/download.
   *  Track reports generated (or looked up by id) this session so Reports List has something to
   *  show; a fresh page load with no prior activity will show an empty list even though older
   *  reports still exist and are reachable directly by id. */
  private realReportsCache: ReportResponse[] = [];

  getDashboard(): Observable<any> {
    if (!environment.useMockAnalytics) return this.http.get(`${this.base}/analytics/dashboard`);
    return forkJoin({
      content: this.content.fetchContents(),
      plans: this.subscription.fetchSubscriptions(),
      statements: this.royalty.getAllStatements(),
      licenses: this.licensing.getAllLicenses(),
    }).pipe(map(({ content, plans, statements, licenses }) => {
      const statusCounts: Record<string, number> = {};
      content.forEach(c => statusCounts[c.status] = (statusCounts[c.status] ?? 0) + 1);
      const typeCounts: Record<string, number> = {};
      content.forEach(c => typeCounts[c.type] = (typeCounts[c.type] ?? 0) + 1);
      return {
        contentCatalogAnalytics: {
          totalContents: content.length,
          contentStatusBreakdown: Object.entries(statusCounts).map(([label, count]) => ({ label, count })),
          contentTypeBreakdown: Object.entries(typeCounts).map(([label, count]) => ({ label, count }))
        },
        subscriptionAnalytics: {
          totalSubscriptions: plans.length,
          activeSubscriptions: plans.filter(p => p.status === 'Active').length
        },
        revenueAnalytics: {
          totalRevenue: statements.reduce((s, r) => s + r.totalRevenue, 0),
          totalRoyaltyAmount: statements.reduce((s, r) => s + r.royaltyAmount, 0)
        },
        licensingAnalytics: {
          totalLicenses: licenses.length,
          activeLicenses: licenses.filter(l => l.status === 'Active').length
        }
      };
    }));
  }

  /** Real ReportController.generateReport() takes no request body at all — it always builds the
   *  same analytics snapshot server-side and ignores reportType/fromDate/toDate/format entirely.
   *  Those fields are merged into the returned object client-side purely so the UI reflects what
   *  the user asked for; the underlying report content is the same regardless of what's picked. */
  generateReport(options: { reportType: string; fromDate: string; toDate: string; format: string }): Observable<ReportResponse> {
    if (!environment.useMockAnalytics) {
      return this.http.post<ReportResponse>(`${this.base}/reports/generate`, {}).pipe(
        map(row => ({ ...row, ...options })),
        tap(row => this.realReportsCache.push(row))
      );
    }
    return this.content.fetchContents().pipe(map(rows => {
      const row: ReportResponse = {
        reportId: reportSeq++,
        reportName: 'MediaHub Analytics Report',
        generatedDate: new Date().toISOString().slice(0, 10),
        totalContents: rows.length,
        reportType: options.reportType,
        format: options.format,
        fromDate: options.fromDate,
        toDate: options.toDate
      };
      this.reports.insert(row as any);
      return row;
    }));
  }

  getReports(): Observable<ReportResponse[]> {
    if (!environment.useMockAnalytics) return mockOf(this.realReportsCache);
    return mockOf(this.reports.all());
  }

  getReport(id: number): Observable<ReportResponse | undefined> {
    if (!environment.useMockAnalytics) {
      return this.http.get<ReportResponse>(`${this.base}/reports/${id}`).pipe(
        tap(row => { if (!this.realReportsCache.some(r => r.reportId === row.reportId)) this.realReportsCache.push(row); })
      );
    }
    return mockOf(this.reports.find(r => r.reportId === id));
  }

  deleteReport(id: number): Observable<string> {
    if (!environment.useMockAnalytics) {
      return this.http.delete(`${this.base}/reports/${id}`, { responseType: 'text' }).pipe(
        tap(() => { this.realReportsCache = this.realReportsCache.filter(r => r.reportId !== id); })
      );
    }
    this.reports.remove(id);
    return mockOf('Report deleted successfully');
  }

  /** Real backend streams actual .xlsx bytes from GET /reports/download/{id}. The mock layer can't
   *  generate real XLSX client-side, so it builds an equivalent CSV summary instead — functionally
   *  the same action, different file format under the hood. */
  downloadReport(report: ReportResponse) {
    if (!environment.useMockAnalytics) {
      this.http.get(`${this.base}/reports/download/${report.reportId}`, { responseType: 'blob' }).subscribe(blob => {
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `MediaHub_Analytics_Report_${report.reportId}.xlsx`;
        document.body.appendChild(a);
        a.click();
        a.remove();
        URL.revokeObjectURL(url);
      });
      return;
    }
    const rows = [
      ['Report ID', String(report.reportId)],
      ['Report Name', report.reportName],
      ['Generated Date', report.generatedDate],
      ['Report Type', report.reportType ?? ''],
      ['Period', `${report.fromDate ?? ''} to ${report.toDate ?? ''}`],
      ['Total Contents', String(report.totalContents)]
    ];
    const csv = rows.map(r => r.map(c => `"${c.replace(/"/g, '""')}"`).join(',')).join('\n');
    const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `MediaHub_Analytics_Report_${report.reportId}.csv`;
    document.body.appendChild(a);
    a.click();
    a.remove();
    URL.revokeObjectURL(url);
  }
}
