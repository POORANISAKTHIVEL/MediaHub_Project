import { Component, inject, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { SubscriptionClient } from '../../core/api/subscription-client';
import { SubscriptionHistory as HistoryModel } from '../../core/models/subscription.models';
import { IamClient } from '../../core/api/iam-client';
import { IamUser } from '../../core/models/iam.models';
import { LoadingSpinner } from '../../shared/components/loading-spinner';

@Component({
  selector: 'app-subscription-history',
  imports: [RouterLink, LoadingSpinner],
  templateUrl: './subscription-history.html'
})
export class SubscriptionHistoryPage implements OnInit {
  private subscription = inject(SubscriptionClient);
  private iam = inject(IamClient);

  loading = signal(true);
  rows = signal<HistoryModel[]>([]);
  users = signal<IamUser[]>([]);

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
}
