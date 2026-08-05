import { Component, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { SubscriptionClient } from '../../core/api/subscription-client';
import { UserSubscription, SubscriptionHistory } from '../../core/models/subscription.models';
import { IamClient } from '../../core/api/iam-client';
import { IamUser } from '../../core/models/iam.models';
import { StatusBadge } from '../../shared/components/status-badge';
import { LoadingSpinner } from '../../shared/components/loading-spinner';
import { ToastService } from '../../shared/services/toast.service';
import { ConfirmService } from '../../shared/services/confirm.service';
import { AuthService } from '../../core/auth/auth.service';

@Component({
  selector: 'app-subscription-detail',
  imports: [RouterLink, StatusBadge, LoadingSpinner],
  templateUrl: './subscription-detail.html'
})
export class SubscriptionDetail implements OnInit {
  auth = inject(AuthService);
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private subscription = inject(SubscriptionClient);
  private iam = inject(IamClient);
  private toast = inject(ToastService);
  private confirm = inject(ConfirmService);

  loading = signal(true);
  sub = signal<UserSubscription | null>(null);
  timeline = signal<SubscriptionHistory[]>([]);
  totalSubscriptions = signal(0);
  users = signal<IamUser[]>([]);

  ngOnInit() {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.iam.getAllUsers().subscribe(u => this.users.set(u));
    this.subscription.fetchPlans().subscribe();
    this.subscription.getSubscriptionById(id).subscribe(s => {
      this.sub.set(s ?? null);
      this.loading.set(false);
      if (s) {
        this.subscription.subscriptionCountForUser(s.userId).subscribe(count => this.totalSubscriptions.set(count));
        this.subscription.fetchHistories().subscribe(rows => {
          this.timeline.set(rows.filter(h => h.subscriptionId === id).sort((a, b) => b.changeDate.localeCompare(a.changeDate)));
        });
      }
    });
  }

  userName(userId: number): string {
    return this.users().find(u => u.userId === userId)?.name ?? ('User #' + userId);
  }

  userEmail(userId: number): string {
    return this.users().find(u => u.userId === userId)?.email ?? '—';
  }

  userRole(userId: number): string {
    return this.users().find(u => u.userId === userId)?.roleType ?? '—';
  }

  planName(planId: number): string {
    return this.subscription.planName(planId);
  }

  async suspend() {
    const s = this.sub();
    if (!s) return;
    const ok = await this.confirm.ask(`Suspend this subscription for ${this.userName(s.userId)}?`, 'Suspend', true);
    if (!ok) return;
    this.subscription.suspendSubscription(s.subscriptionId).subscribe({
      next: () => {
        this.toast.ok('Subscription suspended');
        this.sub.set({ ...s, status: 'Suspended' });
      },
      error: (err) => this.toast.warn(err?.error?.message ?? 'Unable to suspend')
    });
  }

  reactivate() {
    const s = this.sub();
    if (!s) return;
    this.subscription.reactivateSubscription(s.subscriptionId).subscribe({
      next: () => { this.toast.ok('Subscription reactivated'); this.sub.set({ ...s, status: 'Active' }); },
      error: (err) => this.toast.warn(err?.error?.message ?? 'Unable to reactivate')
    });
  }
}
