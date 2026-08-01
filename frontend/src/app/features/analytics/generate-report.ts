import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { AnalyticsClient } from '../../core/api/analytics-client';
import { ReportResponse } from '../../core/models/analytics.models';
import { ToastService } from '../../shared/services/toast.service';

@Component({
  selector: 'app-generate-report',
  imports: [FormsModule, RouterLink],
  templateUrl: './generate-report.html'
})
export class GenerateReport {
  private analytics = inject(AnalyticsClient);
  private toast = inject(ToastService);

  form = {
    reportType: 'Full Platform Summary',
    fromDate: new Date(Date.now() - 27 * 86400000).toISOString().slice(0, 10),
    toDate: new Date().toISOString().slice(0, 10),
    format: 'XLSX'
  };

  generating = signal(false);
  result = signal<ReportResponse | null>(null);

  generate() {
    this.generating.set(true);
    this.analytics.generateReport(this.form).subscribe(report => {
      this.generating.set(false);
      this.result.set(report);
      this.toast.ok('Report generated');
    });
  }

  download() {
    const r = this.result();
    if (r) this.analytics.downloadReport(r);
  }
}
