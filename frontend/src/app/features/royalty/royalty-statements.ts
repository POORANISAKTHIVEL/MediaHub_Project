import { Component, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { RoyaltyClient } from '../../core/api/royalty-client';
import { RoyaltyStatement } from '../../core/models/royalty.models';
import { StatusBadge } from '../../shared/components/status-badge';
import { LoadingSpinner } from '../../shared/components/loading-spinner';
import { RowMenu, RowMenuItem } from '../../shared/components/row-menu';
import { ToastService } from '../../shared/services/toast.service';

@Component({
  selector: 'app-royalty-statements',
  imports: [FormsModule, StatusBadge, LoadingSpinner, RowMenu],
  templateUrl: './royalty-statements.html'
})
export class RoyaltyStatements implements OnInit {
  private royalty = inject(RoyaltyClient);
  private toast = inject(ToastService);
  private router = inject(Router);

  loading = signal(true);
  statements = signal<RoyaltyStatement[]>([]);

  generating = signal(false);
  form = { creatorID: 0, period: '', totalViews: 0, totalRevenue: 0 };

  ngOnInit() {
    this.load();
  }

  load() {
    this.loading.set(true);
    this.royalty.getAllStatements().subscribe(rows => {
      this.statements.set(rows);
      this.loading.set(false);
    });
  }

  view(s: RoyaltyStatement) {
    this.router.navigate(['/royalty/statements', s.statementID]);
  }

  openGenerate() {
    this.form = { creatorID: 0, period: '', totalViews: 0, totalRevenue: 0 };
    this.generating.set(true);
  }

  generate() {
    if (!this.form.creatorID || !this.form.period.trim()) return;
    this.royalty.generateStatement(this.form).subscribe({
      next: () => { this.toast.ok('Statement generated'); this.generating.set(false); this.load(); },
      error: (err) => this.toast.warn(err?.error?.message ?? 'Unable to generate statement')
    });
  }

  finalise(s: RoyaltyStatement) {
    this.royalty.finaliseStatement(s.statementID).subscribe({
      next: () => { this.toast.ok('Statement finalised'); this.load(); },
      error: (err) => this.toast.warn(err?.error?.message ?? 'Unable to finalise statement')
    });
  }

  markPaid(s: RoyaltyStatement) {
    this.royalty.markPaid(s.statementID).subscribe({
      next: () => { this.toast.ok('Statement marked as Paid'); this.load(); },
      error: (err) => this.toast.warn(err?.error?.message ?? 'Unable to mark as paid')
    });
  }

  menuFor(s: RoyaltyStatement): RowMenuItem[] {
    const items: RowMenuItem[] = [{ label: 'View', action: 'view' }];
    if (s.status === 'Draft') items.push({ label: 'Finalise', action: 'finalise' });
    if (s.status === 'Finalised') items.push({ label: 'Mark Paid', action: 'markPaid' });
    return items;
  }

  onAction(action: string, s: RoyaltyStatement) {
    if (action === 'view') this.view(s);
    if (action === 'finalise') this.finalise(s);
    if (action === 'markPaid') this.markPaid(s);
  }
}
