import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { SubscriptionClient } from '../../core/api/subscription-client';
import { SubscriptionHistory as HistoryModel } from '../../core/models/subscription.models';
import { IamClient } from '../../core/api/iam-client';
import { IamUser } from '../../core/models/iam.models';
import { LoadingSpinner } from '../../shared/components/loading-spinner';
import { Pagination } from '../../shared/components/pagination';
import { FitRowsDirective } from '../../shared/directives/fit-rows.directive';

@Component({
  selector: 'app-subscription-history',
  imports: [RouterLink, LoadingSpinner, Pagination, FitRowsDirective],
  templateUrl: './subscription-history.html'
})
export class SubscriptionHistoryPage implements OnInit {
  private subscription = inject(SubscriptionClient);
  private iam = inject(IamClient);

  loading = signal(true);
  rows = signal<HistoryModel[]>([]);
  users = signal<IamUser[]>([]);

  page = signal(0);
  pageSize = signal(10);
  totalPages = computed(() => Math.max(1, Math.ceil(this.rows().length / this.pageSize())));
  pagedRows = computed(() => this.rows().slice(this.page() * this.pageSize(), (this.page() + 1) * this.pageSize()));

  onRowsThatFit(n: number) {
    if (n === this.pageSize()) return;
    this.pageSize.set(n);
    this.page.set(0);
  }

  ngOnInit() {
    this.iam.getAllUsers().subscribe(u => this.users.set(u));
    this.subscription.fetchPlans().subscribe();
    this.subscription.fetchHistories().subscribe(rows => {
      this.rows.set(rows);
      this.loading.set(false);
    });
  }

  userName(userId: number): string {
    return this.users().find(u => u.userId === userId)?.name ?? ('User #' + userId);
  }

  planName(planId?: number): string {
    return planId ? this.subscription.planName(planId) : '—';
  }

  /** Amount is derived, not stored — matches the plan price at the "to" side of the
   *  change, minus the "from" side for Upgrade/Downgrade so it reads as a delta. */
  amount(h: HistoryModel): number {
    const toPrice = this.subscription.planPrice(h.toPlanId);
    if (h.changeType === 'Cancellation') return 0;
    if (h.changeType === 'Upgrade' || h.changeType === 'Downgrade') {
      const fromPrice = h.fromPlanId ? this.subscription.planPrice(h.fromPlanId) : 0;
      return toPrice - fromPrice;
    }
    return toPrice;
  }

  amountLabel(h: HistoryModel): string {
    if (h.changeType === 'Cancellation') return '$0.00';
    const amt = this.amount(h);
    const sign = amt > 0 ? '+' : amt < 0 ? '-' : '';
    return `${sign}$${Math.abs(amt).toFixed(2)}`;
  }

  fromToLabel(h: HistoryModel): string {
    if (h.changeType === 'Cancellation') return `${this.planName(h.fromPlanId)} → —`;
    if (h.changeType === 'New') return `— → ${this.planName(h.toPlanId)}`;
    if (h.fromPlanId && h.fromPlanId !== h.toPlanId) return `${this.planName(h.fromPlanId)} → ${this.planName(h.toPlanId)}`;
    return this.planName(h.toPlanId);
  }

  eventClass(changeType: string): string {
    switch (changeType) {
      case 'Upgrade': return 'b-blue';
      case 'Downgrade': return 'b-amber';
      case 'Renewal': return 'b-green';
      case 'Cancellation': return 'b-red';
      case 'New': return 'b-violet';
      default: return 'b-blue';
    }
  }

  formatDate(iso: string): string {
    return new Date(iso).toLocaleDateString('en-US', { month: 'short', day: '2-digit', year: 'numeric' });
  }
}
