import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { AuthService } from '../../core/auth/auth.service';
import { NotificationClient } from '../../core/api/notification-client';
import { AppNotification } from '../../core/models/notification.models';
import { FilterChip } from '../../shared/components/filter-chip';
import { LoadingSpinner } from '../../shared/components/loading-spinner';
import { EmptyState } from '../../shared/components/empty-state';
import { ToastService } from '../../shared/services/toast.service';

const CATEGORY_OPTIONS = ['All', 'CONTENT', 'SUBSCRIPTION', 'ROYALTY', 'LICENSE', 'EDITORIAL'];
const CATEGORY_ICON: Record<string, string> = { CONTENT: '🎞', SUBSCRIPTION: '◈', ROYALTY: '$', LICENSE: '⚖', EDITORIAL: '✎' };

@Component({
  selector: 'app-notifications-list',
  imports: [FilterChip, LoadingSpinner, EmptyState],
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

  rows = computed(() => this.all()
    .filter(n => this.tab() === 'All' || n.status === 'UNREAD')
    .filter(n => !this.categoryFilter() || n.category === this.categoryFilter())
  );

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
