import { Component, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RoyaltyClient } from '../../core/api/royalty-client';
import { RoyaltyPayout } from '../../core/models/royalty.models';
import { StatusBadge } from '../../shared/components/status-badge';
import { LoadingSpinner } from '../../shared/components/loading-spinner';
import { RowMenu, RowMenuItem } from '../../shared/components/row-menu';
import { ToastService } from '../../shared/services/toast.service';

@Component({
  selector: 'app-royalty-payouts',
  imports: [FormsModule, StatusBadge, LoadingSpinner, RowMenu],
  templateUrl: './royalty-payouts.html'
})
export class RoyaltyPayouts implements OnInit {
  private royalty = inject(RoyaltyClient);
  private toast = inject(ToastService);

  loading = signal(true);
  payouts = signal<RoyaltyPayout[]>([]);

  creating = signal(false);
  form: { statementID: number; creatorID: number; amount: number; method: RoyaltyPayout['method'] } =
    { statementID: 0, creatorID: 0, amount: 0, method: 'BankTransfer' };

  failing = signal<RoyaltyPayout | null>(null);
  failReason = '';

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
    if (p.status === 'Pending') return [{ label: 'Mark Processed', action: 'processed' }, { label: 'Mark Failed', action: 'failed' }];
    if (p.status === 'Failed') return [{ label: 'Retry', action: 'retry' }];
    return [];
  }

  onAction(action: string, p: RoyaltyPayout) {
    if (action === 'processed') this.markProcessed(p);
    if (action === 'failed') this.openFail(p);
    if (action === 'retry') this.retry(p);
  }
}
