import { Component, inject, OnInit, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { AnalyticsClient } from '../../core/api/analytics-client';
import { ReportResponse } from '../../core/models/analytics.models';
import { LoadingSpinner } from '../../shared/components/loading-spinner';
import { EmptyState } from '../../shared/components/empty-state';
import { RowMenu, RowMenuItem } from '../../shared/components/row-menu';
import { ToastService } from '../../shared/services/toast.service';
import { ConfirmService } from '../../shared/services/confirm.service';

@Component({
  selector: 'app-reports-list',
  imports: [RouterLink, LoadingSpinner, EmptyState, RowMenu],
  templateUrl: './reports-list.html'
})
export class ReportsList implements OnInit {
  private analytics = inject(AnalyticsClient);
  private toast = inject(ToastService);
  private confirm = inject(ConfirmService);
  private router = inject(Router);

  loading = signal(true);
  reports = signal<ReportResponse[]>([]);

  ngOnInit() {
    this.load();
  }

  load() {
    this.loading.set(true);
    this.analytics.getReports().subscribe(rows => {
      this.reports.set(rows);
      this.loading.set(false);
    });
  }

  menuFor(_r: ReportResponse): RowMenuItem[] {
    return [{ label: 'Download', action: 'download' }, { label: 'View', action: 'view' }, { label: 'Delete', action: 'delete' }];
  }

  onAction(action: string, r: ReportResponse) {
    if (action === 'download') this.analytics.downloadReport(r);
    if (action === 'view') this.router.navigate(['/analytics/reports', r.reportId]);
    if (action === 'delete') this.remove(r);
  }

  async remove(r: ReportResponse) {
    const ok = await this.confirm.ask(`Delete report "${r.reportName}" (#${r.reportId})?`, 'Delete', true);
    if (!ok) return;
    this.analytics.deleteReport(r.reportId).subscribe(() => {
      this.toast.ok('Report deleted successfully');
      this.load();
    });
  }
}
