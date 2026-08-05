import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { AuthService } from '../../core/auth/auth.service';
import { NotificationClient } from '../../core/api/notification-client';
import { AppNotification } from '../../core/models/notification.models';
import { FilterChip } from '../../shared/components/filter-chip';
import { LoadingSpinner } from '../../shared/components/loading-spinner';
import { EmptyState } from '../../shared/components/empty-state';
import { Pagination } from '../../shared/components/pagination';
import { ToastService } from '../../shared/services/toast.service';

const CATEGORY_OPTIONS = ['All', 'CONTENT', 'SUBSCRIPTION', 'ROYALTY', 'LICENSE', 'EDITORIAL'];
const CATEGORY_ICON: Record<string, string> = { CONTENT: '🎞', SUBSCRIPTION: '◈', ROYALTY: '$', LICENSE: '⚖', EDITORIAL: '✎' };

/** Roles whose notifications feed is narrowed to categories relevant to their own work — e.g. a
 *  creator cares about what happens to their content (editorial decisions, publication/territory,
 *  royalty), not platform-wide subscription billing events that happen to share their account. */
const ROLE_CATEGORY_ALLOWLIST: Record<string, string[]> = {
  creator: ['CONTENT', 'EDITORIAL', 'LICENSE', 'ROYALTY'],
  subscriber: ['SUBSCRIPTION', 'CONTENT']
};

@Component({
  selector: 'app-notifications-list',
  imports: [FilterChip, LoadingSpinner, EmptyState, Pagination],
  templateUrl: './notifications-list.html'
})
export class NotificationsList implements OnInit {
  private auth = inject(AuthService);
  private notif = inject(NotificationClient);
  private toast = inject(ToastService);

  loading = signal(true);
  all = signal<AppNotification[]>([]);
  tab = signal<'All' | 'Unread'>('All');
  categoryFilter = signal('');
  categoryOptions = CATEGORY_OPTIONS;

  private allowedCategories = computed(() => ROLE_CATEGORY_ALLOWLIST[this.auth.roleType() ?? ''] ?? null);

  rows = computed(() => {
    const allowed = this.allowedCategories();
    return this.all()
      .filter(n => !allowed || allowed.includes(n.category))
      .filter(n => this.tab() === 'All' || n.status === 'UNREAD')
      .filter(n => !this.categoryFilter() || n.category === this.categoryFilter());
  });

  // Only offer the categories this role can actually see in the filter dropdown.
  visibleCategoryOptions = computed(() => {
    const allowed = this.allowedCategories();
    return allowed ? ['All', ...this.categoryOptions.slice(1).filter(c => allowed.includes(c))] : this.categoryOptions;
  });

  page = signal(0);
  pageSize = 10;
  totalPages = computed(() => Math.max(1, Math.ceil(this.rows().length / this.pageSize)));
  pagedRows = computed(() => this.rows().slice(this.page() * this.pageSize, (this.page() + 1) * this.pageSize));

  selectTab(t: 'All' | 'Unread') {
    this.tab.set(t);
    this.page.set(0);
  }

  onCategoryFilterChange(value: string) {
    this.categoryFilter.set(value);
    this.page.set(0);
  }

  ngOnInit() {
    this.load();
  }

  load() {
    this.loading.set(true);
    const userId = this.auth.currentUser()?.userId ?? 1;
    this.notif.getAllForUser(userId).subscribe({
      next: rows => { this.all.set(rows); this.loading.set(false); },
      error: () => { this.all.set([]); this.loading.set(false); }
    });
  }

  icon(n: AppNotification): string {
    return CATEGORY_ICON[n.category] ?? '◇';
  }

  markRead(n: AppNotification) {
    if (n.status !== 'UNREAD') return;
    this.notif.updateStatus(n.notificationId, 'READ').subscribe(() => this.load());
  }

  dismiss(n: AppNotification) {
    this.notif.updateStatus(n.notificationId, 'DISMISSED').subscribe({
      next: () => { this.toast.ok('Notification dismissed'); this.load(); },
      error: (err) => this.toast.warn(err?.error?.message ?? 'Unable to dismiss notification')
    });
  }
}
