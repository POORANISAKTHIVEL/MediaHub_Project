import { Component, inject, OnInit, signal } from '@angular/core';
import { Router } from '@angular/router';
import { SubscriptionClient } from '../../core/api/subscription-client';
import { SubscriptionPlan } from '../../core/models/subscription.models';
import { AuthService } from '../../core/auth/auth.service';
import { LoadingSpinner } from '../../shared/components/loading-spinner';
import { ToastService } from '../../shared/services/toast.service';

@Component({
  selector: 'app-plan-catalog',
  imports: [LoadingSpinner],
  templateUrl: './plan-catalog.html'
})
export class PlanCatalog implements OnInit {
  private subscription = inject(SubscriptionClient);
  private auth = inject(AuthService);
  private toast = inject(ToastService);
  private router = inject(Router);

  loading = signal(true);
  plans = signal<SubscriptionPlan[]>([]);

  ngOnInit() {
    this.subscription.fetchPlans().subscribe(rows => {
      this.plans.set(rows.filter(p => p.status === 'Active'));
      this.loading.set(false);
    });
  }

  subscribe(p: SubscriptionPlan) {
    const userId = this.auth.currentUser()?.userId;
    if (!userId) return;
    const start = new Date().toISOString().slice(0, 10);
    const end = new Date(Date.now() + 30 * 86400000).toISOString().slice(0, 10);
    this.subscription.createSubscription({ userId, planId: p.planId, startDate: start, endDate: end, renewalType: 'AutoRenew' }).subscribe({
      next: () => { this.toast.ok(`Subscribed to ${p.name}`); this.router.navigate(['/subscription/my']); },
      error: (err) => this.toast.warn(err?.error?.message ?? 'You already have an active subscription')
    });
  }
}
