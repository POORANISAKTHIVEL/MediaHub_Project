import { Component, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { SubscriptionClient } from '../../core/api/subscription-client';
import { SubscriptionPlan } from '../../core/models/subscription.models';
import { StatusBadge } from '../../shared/components/status-badge';
import { LoadingSpinner } from '../../shared/components/loading-spinner';
import { RowMenu, RowMenuItem } from '../../shared/components/row-menu';
import { ToastService } from '../../shared/services/toast.service';

@Component({
  selector: 'app-plans-management',
  imports: [FormsModule, RouterLink, StatusBadge, LoadingSpinner, RowMenu],
  templateUrl: './plans-management.html'
})
export class PlansManagement implements OnInit {
  private subscription = inject(SubscriptionClient);
  private toast = inject(ToastService);

  loading = signal(true);
  plans = signal<SubscriptionPlan[]>([]);

  creating = signal(false);
  editing = signal<SubscriptionPlan | null>(null);
  form: { name: string; price: number; billingCycle: SubscriptionPlan['billingCycle']; contentAccessLevel: string; maxDevices: number; downloadAllowed: number } =
    { name: '', price: 0, billingCycle: 'Monthly', contentAccessLevel: '', maxDevices: 1, downloadAllowed: 0 };

  ngOnInit() {
    this.load();
  }

  load() {
    this.loading.set(true);
    this.subscription.fetchPlans().subscribe(rows => {
      this.plans.set(rows);
      this.loading.set(false);
    });
  }

  openCreate() {
    this.form = { name: '', price: 0, billingCycle: 'Monthly', contentAccessLevel: '', maxDevices: 1, downloadAllowed: 0 };
    this.creating.set(true);
  }

  openEdit(p: SubscriptionPlan) {
    this.form = { name: p.name, price: p.price, billingCycle: p.billingCycle, contentAccessLevel: p.contentAccessLevel ?? '', maxDevices: p.maxDevices ?? 1, downloadAllowed: p.downloadAllowed ?? 0 };
    this.editing.set(p);
  }

  save() {
    if (!this.form.name.trim()) return;
    const editing = this.editing();
    if (editing) {
      this.subscription.updatePlan(editing.planId, this.form).subscribe(() => {
        this.toast.ok('Plan updated successfully');
        this.editing.set(null);
        this.load();
      });
    } else {
      this.subscription.createPlan(this.form).subscribe({
        next: () => { this.toast.ok('Plan created successfully'); this.creating.set(false); this.load(); },
        error: (err) => this.toast.warn(err?.error?.message ?? 'Unable to create plan')
      });
    }
  }

  toggleStatus(p: SubscriptionPlan) {
    const next = p.status === 'Active' ? 'Discontinued' : 'Active';
    this.subscription.updatePlan(p.planId, { status: next } as any).subscribe({
      next: () => {
        this.toast.ok(`Plan ${next === 'Active' ? 'activated' : 'discontinued'}`);
        this.load();
      },
      error: (err) => this.toast.warn(err?.error?.message ?? 'Unable to update plan status')
    });
  }

  menuFor(p: SubscriptionPlan): RowMenuItem[] {
    return [
      { label: 'Edit', action: 'edit' },
      { label: p.status === 'Active' ? 'Deactivate' : 'Activate', action: 'toggle' }
    ];
  }

  onAction(action: string, p: SubscriptionPlan) {
    if (action === 'edit') this.openEdit(p);
    if (action === 'toggle') this.toggleStatus(p);
  }
}
