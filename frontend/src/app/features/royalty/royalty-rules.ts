import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RoyaltyClient } from '../../core/api/royalty-client';
import { RoyaltyRule } from '../../core/models/royalty.models';
import { StatusBadge } from '../../shared/components/status-badge';
import { LoadingSpinner } from '../../shared/components/loading-spinner';
import { RowMenu, RowMenuItem } from '../../shared/components/row-menu';
import { Pagination } from '../../shared/components/pagination';
import { FitRowsDirective } from '../../shared/directives/fit-rows.directive';
import { ToastService } from '../../shared/services/toast.service';
import { ConfirmService } from '../../shared/services/confirm.service';
import { AuthService } from '../../core/auth/auth.service';
import { formatDate } from '../../shared/utils/date-format';

@Component({
  selector: 'app-royalty-rules',
  imports: [FormsModule, StatusBadge, LoadingSpinner, RowMenu, Pagination, FitRowsDirective],
  templateUrl: './royalty-rules.html'
})
export class RoyaltyRules implements OnInit {
  auth = inject(AuthService);
  private royalty = inject(RoyaltyClient);
  private toast = inject(ToastService);
  private confirm = inject(ConfirmService);

  formatDate = formatDate;

  loading = signal(true);
  rules = signal<RoyaltyRule[]>([]);

  page = signal(0);
  pageSize = signal(10);
  totalPages = computed(() => Math.max(1, Math.ceil(this.rules().length / this.pageSize())));
  pagedRules = computed(() => this.rules().slice(this.page() * this.pageSize(), (this.page() + 1) * this.pageSize()));

  onRowsThatFit(n: number) {
    if (n === this.pageSize()) return;
    this.pageSize.set(n);
    this.page.set(0);
  }

  creating = signal(false);
  editing = signal<RoyaltyRule | null>(null);
  viewing = signal<RoyaltyRule | null>(null);
  form: { creatorTier: string; revenueSharePercent: number; minimumPayoutThreshold: number; payoutFrequency: RoyaltyRule['payoutFrequency']; effectiveDate: string } =
    { creatorTier: '', revenueSharePercent: 50, minimumPayoutThreshold: 25, payoutFrequency: 'Monthly', effectiveDate: '' };

  ngOnInit() {
    this.load();
  }

  load() {
    this.loading.set(true);
    this.royalty.getAllRules().subscribe(rows => {
      this.rules.set(rows);
      this.loading.set(false);
    });
  }

  menuFor(r: RoyaltyRule): RowMenuItem[] {
    const items: RowMenuItem[] = [{ label: 'View', action: 'view' }];
    if (!this.auth.hasPermission('royalty:approve')) return items;
    items.push({ label: 'Edit', action: 'edit' });
    if (r.status === 'Active') items.push({ label: 'Deactivate', action: 'deactivate' });
    else items.push({ label: 'Delete', action: 'delete' });
    return items;
  }

  onAction(action: string, r: RoyaltyRule) {
    if (action === 'view') this.viewing.set(r);
    if (action === 'edit') this.openEdit(r);
    if (action === 'deactivate') this.deactivate(r);
    if (action === 'delete') this.remove(r);
  }

  openCreate() {
    this.form = { creatorTier: '', revenueSharePercent: 50, minimumPayoutThreshold: 25, payoutFrequency: 'Monthly', effectiveDate: '' };
    this.creating.set(true);
  }

  openEdit(r: RoyaltyRule) {
    this.form = { creatorTier: r.creatorTier, revenueSharePercent: r.revenueSharePercent, minimumPayoutThreshold: r.minimumPayoutThreshold, payoutFrequency: r.payoutFrequency, effectiveDate: r.effectiveDate };
    this.editing.set(r);
  }

  create() {
    if (!this.form.creatorTier.trim() || !this.form.effectiveDate) return;
    this.royalty.createRule(this.form).subscribe(() => {
      this.toast.ok('Royalty rule created');
      this.creating.set(false);
      this.load();
    });
  }

  save() {
    const r = this.editing();
    if (!r || !this.form.creatorTier.trim() || !this.form.effectiveDate) return;
    this.royalty.updateRule(r.ruleID, this.form).subscribe({
      next: () => {
        this.toast.ok('Royalty rule updated');
        this.editing.set(null);
        this.load();
      },
      error: (err) => this.toast.warn(err?.error?.message ?? 'Unable to update rule')
    });
  }

  async deactivate(r: RoyaltyRule) {
    const ok = await this.confirm.ask(`Deactivate rule for "${r.creatorTier}"? Future calculations will stop using it.`, 'Deactivate');
    if (!ok) return;
    this.royalty.deactivateRule(r.ruleID).subscribe(() => { this.toast.ok('Rule deactivated'); this.load(); });
  }

  async remove(r: RoyaltyRule) {
    const ok = await this.confirm.ask(`Delete rule for "${r.creatorTier}"?`, 'Delete', true);
    if (!ok) return;
    this.royalty.deleteRule(r.ruleID).subscribe({
      next: () => { this.toast.ok('Rule deleted'); this.load(); },
      error: (err) => this.toast.warn(err?.error?.message ?? 'Unable to delete rule')
    });
  }
}
