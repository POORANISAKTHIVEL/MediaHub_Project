import { Component, inject, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { SubscriptionClient } from '../../core/api/subscription-client';
import { UserSubscription } from '../../core/models/subscription.models';
import { AuthService } from '../../core/auth/auth.service';
import { StatusBadge } from '../../shared/components/status-badge';
import { LoadingSpinner } from '../../shared/components/loading-spinner';
import { ToastService } from '../../shared/services/toast.service';
import { ConfirmService } from '../../shared/services/confirm.service';

@Component({
  selector: 'app-my-subscription',
  imports: [RouterLink, StatusBadge, LoadingSpinner],
  templateUrl: './my-subscription.html'
})
export class MySubscription implements OnInit {
  private subscription = inject(SubscriptionClient);
  private auth = inject(AuthService);
  private toast = inject(ToastService);
  private confirm = inject(ConfirmService);

  loading = signal(true);
  sub = signal<UserSubscription | null>(null);

  ngOnInit() {
    this.subscription.fetchPlans().subscribe();
    const userId = this.auth.currentUser()?.userId;
    if (!userId) { this.loading.set(false); return; }
    this.subscription.fetchSubscriptionForUser(userId).subscribe(s => {
      this.sub.set(s ?? null);
      this.loading.set(false);
    });
  }

  planName(planId: number): string {
    return this.subscription.planName(planId);
  }

  async cancel() {
    const s = this.sub();
    if (!s) return;
    const ok = await this.confirm.ask('Cancel your subscription? You will lose access at the end of the current billing period.', 'Cancel Subscription', true);
    if (!ok) return;
    this.subscription.cancelSubscription(s.subscriptionId).subscribe(() => {
      this.toast.ok('Subscription cancelled successfully');
      this.sub.set({ ...s, status: 'Cancelled' });
    });
  }
}
