import { Component, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AnalyticsClient } from '../../core/api/analytics-client';
import { ReportResponse } from '../../core/models/analytics.models';
import { LoadingSpinner } from '../../shared/components/loading-spinner';
import { ToastService } from '../../shared/services/toast.service';
import { ConfirmService } from '../../shared/services/confirm.service';

@Component({
  selector: 'app-report-detail',
  imports: [RouterLink, LoadingSpinner],
  templateUrl: './report-detail.html'
})
export class ReportDetail implements OnInit {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private analytics = inject(AnalyticsClient);
  private toast = inject(ToastService);
  private confirm = inject(ConfirmService);

  loading = signal(true);
  report = signal<ReportResponse | null>(null);

  ngOnInit() {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.analytics.getReport(id).subscribe(r => {
      this.report.set(r ?? null);
      this.loading.set(false);
    });
  }

  download() {
    const r = this.report();
    if (r) this.analytics.downloadReport(r);
  }

  async remove() {
    const r = this.report();
    if (!r) return;
    const ok = await this.confirm.ask(`Delete report "${r.reportName}" (#${r.reportId})?`, 'Delete', true);
    if (!ok) return;
    this.analytics.deleteReport(r.reportId).subscribe(() => {
      this.toast.ok('Report deleted successfully');
      this.router.navigate(['/analytics/reports']);
    });
  }
}
