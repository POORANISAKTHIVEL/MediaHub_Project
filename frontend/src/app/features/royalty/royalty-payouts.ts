import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RoyaltyClient } from '../../core/api/royalty-client';
import { RoyaltyPayout } from '../../core/models/royalty.models';
import { StatusBadge } from '../../shared/components/status-badge';
import { LoadingSpinner } from '../../shared/components/loading-spinner';
import { RowMenu, RowMenuItem } from '../../shared/components/row-menu';
import { Pagination } from '../../shared/components/pagination';
import { FitRowsDirective } from '../../shared/directives/fit-rows.directive';
import { ToastService } from '../../shared/services/toast.service';
import { AuthService } from '../../core/auth/auth.service';

@Component({
  selector: 'app-royalty-payouts',
  imports: [FormsModule, StatusBadge, LoadingSpinner, RowMenu, Pagination, FitRowsDirective],
  templateUrl: './royalty-payouts.html'
})
export class RoyaltyPayouts implements OnInit {
  private royalty = inject(RoyaltyClient);
  private toast = inject(ToastService);
  auth = inject(AuthService);

  loading = signal(true);
  payouts = signal<RoyaltyPayout[]>([]);

  page = signal(0);
  pageSize = signal(10);
  totalPages = computed(() => Math.max(1, Math.ceil(this.payouts().length / this.pageSize())));
  pagedPayouts = computed(() => this.payouts().slice(this.page() * this.pageSize(), (this.page() + 1) * this.pageSize()));

  onRowsThatFit(n: number) {
    if (n === this.pageSize()) return;
    this.pageSize.set(n);
    this.page.set(0);
  }

  creating = signal(false);
  form: { statementID: number; creatorID: number; amount: number; method: RoyaltyPayout['method'] } =
    { statementID: 0, creatorID: 0, amount: 0, method: 'BankTransfer' };

  failing = signal<RoyaltyPayout | null>(null);
  failReason = '';

  viewing = signal<RoyaltyPayout | null>(null);

  ngOnInit() {
    this.load();
  }

  load() {
    this.loading.set(true);
    this.royalty.getAllPayouts().subscribe(rows => {
      this.payouts.set(rows);
      this.loading.set(false);
    });
  }

  openCreate() {
    this.form = { statementID: 0, creatorID: 0, amount: 0, method: 'BankTransfer' };
    this.creating.set(true);
  }

  create() {
    if (!this.form.statementID || !this.form.creatorID || this.form.amount <= 0) return;
    this.royalty.createPayout(this.form).subscribe(() => {
      this.toast.ok('Payout created');
      this.creating.set(false);
      this.load();
    });
  }

  markProcessed(p: RoyaltyPayout) {
    this.royalty.markProcessed(p.payoutID).subscribe(() => { this.toast.ok('Payout marked Processed'); this.load(); });
  }

  openFail(p: RoyaltyPayout) {
    this.failReason = '';
    this.failing.set(p);
  }

  confirmFail() {
    const p = this.failing();
    if (!p || !this.failReason.trim()) return;
    this.royalty.markFailed(p.payoutID, this.failReason).subscribe(() => {
      this.toast.ok('Payout marked Failed');
      this.failing.set(null);
      this.load();
    });
  }

  retry(p: RoyaltyPayout) {
    this.royalty.retryPayout(p.payoutID).subscribe({
      next: () => { this.toast.ok('Payout retry scheduled'); this.load(); },
      error: (err) => this.toast.warn(err?.error?.message ?? 'Unable to retry payout')
    });
  }

  menuFor(p: RoyaltyPayout): RowMenuItem[] {
    const items: RowMenuItem[] = [{ label: 'View', action: 'view' }];
    if (!this.auth.hasPermission('royalty:approve')) return items;
    if (p.status === 'Pending') items.push({ label: 'Mark Processed', action: 'processed' }, { label: 'Mark Failed', action: 'failed' });
    if (p.status === 'Failed') items.push({ label: 'Retry', action: 'retry' });
    return items;
  }

  onAction(action: string, p: RoyaltyPayout) {
    if (action === 'view') this.viewing.set(p);
    if (action === 'processed') this.markProcessed(p);
    if (action === 'failed') this.openFail(p);
    if (action === 'retry') this.retry(p);
  }
}
