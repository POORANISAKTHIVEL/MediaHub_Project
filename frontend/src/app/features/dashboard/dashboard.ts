import { Component, inject, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../core/auth/auth.service';
import { AnalyticsClient } from '../../core/api/analytics-client';
import { RoyaltyClient } from '../../core/api/royalty-client';
import { StatCard } from '../../shared/components/stat-card';
import { LoadingSpinner } from '../../shared/components/loading-spinner';
import { ToastService } from '../../shared/services/toast.service';

@Component({
  selector: 'app-dashboard',
  imports: [RouterLink, StatCard, LoadingSpinner],
  templateUrl: './dashboard.html'
})
export class Dashboard implements OnInit {
  auth = inject(AuthService);
  private analytics = inject(AnalyticsClient);
  private royalty = inject(RoyaltyClient);
  private toast = inject(ToastService);

  loading = signal(true);
  data = signal<any>(null);

  // Not covered by the aggregated /analytics/dashboard payload — matches the same real
  // getAllStatements()/getAllPayouts() calls the Royalty Dashboard itself uses.
  draftStatements = signal(0);
  pendingPayouts = signal(0);
  statementStatusBreakdown = signal<{ label: string; count: number }[]>([]);

  ngOnInit() {
    this.analytics.getDashboard().subscribe({
      next: d => { this.data.set(d); this.loading.set(false); },
      error: () => this.loading.set(false)
    });

    if (this.auth.hasPermission('royalty:view')) {
      this.royalty.getAllStatements().subscribe(rows => {
        this.draftStatements.set(rows.filter(r => r.status === 'Draft').length);
        const counts: Record<string, number> = {};
        rows.forEach(r => counts[r.status] = (counts[r.status] ?? 0) + 1);
        this.statementStatusBreakdown.set(Object.entries(counts).map(([label, count]) => ({ label, count })));
      });
      this.royalty.getAllPayouts().subscribe(rows => {
        this.pendingPayouts.set(rows.filter(p => p.status === 'Pending').length);
      });
    }
  }

  barPct(count: number, breakdown: { label: string; count: number }[]): number {
    const max = Math.max(...breakdown.map(b => b.count), 1);
    return Math.round((count / max) * 100);
  }

  export() {
    const d = this.data();
    if (!d) return;
    const canContent = this.auth.hasPermission('content:read');
    const rows: string[][] = [
      ['Metric', 'Value'],
      ...(canContent ? [['Total Content', String(d.contentCatalogAnalytics?.totalContents ?? '')]] : []),
      ...(this.auth.hasPermission('subscription:manage') ? [['Active Subscriptions', String(d.subscriptionAnalytics?.activeSubscriptions ?? '')]] : []),
      ...(canContent ? [['Pending Reviews', String(d.editorialAnalytics?.pendingReviews ?? '')]] : []),
      ...(canContent ? [['Approved Reviews', String(d.editorialAnalytics?.approvedReviews ?? '')]] : []),
      ...(this.auth.hasPermission('royalty:view') ? [
        ['Total Revenue', String(d.revenueAnalytics?.totalRevenue ?? '')],
        ['Royalty Payable', String(d.revenueAnalytics?.totalRoyaltyAmount ?? '')],
        ['Draft Statements', String(this.draftStatements())],
        ['Pending Payouts', String(this.pendingPayouts())]
      ] : []),
      ...(canContent ? [
        [],
        ['Content by Status', 'Count'],
        ...(d.contentCatalogAnalytics?.contentStatusBreakdown ?? []).map((b: any) => [b.label, String(b.count)]),
        [],
        ['Content by Type', 'Count'],
        ...(d.contentCatalogAnalytics?.contentTypeBreakdown ?? []).map((b: any) => [b.label, String(b.count)]),
        ...(this.auth.hasPermission('license:manage') ? [[], ['Licenses expiring < 30d', String(d.licensingAnalytics?.expiringSoonLicenses ?? '')]] : [])
      ] : []),
      ...(this.auth.hasPermission('royalty:view') ? [
        [],
        ['Statements by Status', 'Count'],
        ...this.statementStatusBreakdown().map(b => [b.label, String(b.count)])
      ] : [])
    ].map(row => row ?? []);

    const csv = rows.map(row => row.map(cell => `"${(cell ?? '').replace(/"/g, '""')}"`).join(',')).join('\n');
    const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `mediahub-dashboard-${new Date().toISOString().slice(0, 10)}.csv`;
    document.body.appendChild(a);
    a.click();
    a.remove();
    URL.revokeObjectURL(url);
    this.toast.ok('Dashboard exported');
  }
}
