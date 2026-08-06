import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { SubscriptionClient } from '../../core/api/subscription-client';
import { UserSubscription } from '../../core/models/subscription.models';
import { IamClient } from '../../core/api/iam-client';
import { IamUser } from '../../core/models/iam.models';
import { AuthService } from '../../core/auth/auth.service';
import { StatusBadge } from '../../shared/components/status-badge';
import { LoadingSpinner } from '../../shared/components/loading-spinner';
import { RowMenu, RowMenuItem } from '../../shared/components/row-menu';
import { Pagination } from '../../shared/components/pagination';
import { FitRowsDirective } from '../../shared/directives/fit-rows.directive';

@Component({
  selector: 'app-subscriptions-list',
  imports: [RouterLink, StatusBadge, LoadingSpinner, RowMenu, Pagination, FitRowsDirective],
  templateUrl: './subscriptions-list.html'
})
export class SubscriptionsList implements OnInit {
  private subscription = inject(SubscriptionClient);
  private iam = inject(IamClient);
  private router = inject(Router);
  auth = inject(AuthService);

  loading = signal(true);
  rows = signal<UserSubscription[]>([]);
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
    this.load();
    this.iam.getAllUsers().subscribe(u => this.users.set(u));
    this.subscription.fetchPlans().subscribe();
  }

  load() {
    this.loading.set(true);
    this.subscription.fetchSubscriptions().subscribe(rows => {
      this.rows.set(rows);
      this.loading.set(false);
    });
  }

  userName(userId: number): string {
    return this.users().find(u => u.userId === userId)?.name ?? ('User #' + userId);
  }

  planName(planId: number): string {
    return this.subscription.planName(planId);
  }

  menuFor(_s: UserSubscription): RowMenuItem[] {
    return [{ label: 'View Details', action: 'view' }];
  }

  onRowAction(action: string, s: UserSubscription) {
    if (action === 'view') this.router.navigate(['/subscription/subscriptions', s.subscriptionId]);
  }
}
