import { Component, inject, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AnalyticsClient } from '../../core/api/analytics-client';
import { StatCard } from '../../shared/components/stat-card';
import { LoadingSpinner } from '../../shared/components/loading-spinner';

@Component({
  selector: 'app-analytics-dashboard',
  imports: [RouterLink, StatCard, LoadingSpinner],
  templateUrl: './analytics-dashboard.html'
})
export class AnalyticsDashboard implements OnInit {
  private analytics = inject(AnalyticsClient);

  loading = signal(true);
  data = signal<any>(null);

  ngOnInit() {
    this.analytics.getDashboard().subscribe(d => {
      this.data.set(d);
      this.loading.set(false);
    });
  }

  barPct(count: number, breakdown: { label: string; count: number }[]): number {
    const max = Math.max(...breakdown.map(b => b.count), 1);
    return Math.round((count / max) * 100);
  }
}
