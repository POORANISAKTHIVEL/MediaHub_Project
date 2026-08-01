import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { IamClient } from '../../core/api/iam-client';
import { AuditEvent, IamUser, ModuleSource, PageResponse } from '../../core/models/iam.models';
import { FilterChip } from '../../shared/components/filter-chip';
import { StatusBadge } from '../../shared/components/status-badge';
import { Pagination } from '../../shared/components/pagination';
import { LoadingSpinner } from '../../shared/components/loading-spinner';

const MODULES: ModuleSource[] = ['IAM', 'CONTENT', 'SUBSCRIPTION', 'EDITORIAL', 'LICENSING', 'ROYALTY', 'ANALYTICS', 'NOTIFICATION', 'SYSTEM'];

@Component({
  selector: 'app-audit-log',
  imports: [FilterChip, StatusBadge, Pagination, LoadingSpinner],
  templateUrl: './audit-log.html'
})
export class AuditLog implements OnInit {
  private iam = inject(IamClient);

  loading = signal(true);
  page = signal<PageResponse<AuditEvent> | null>(null);
  currentPage = signal(0);
  pageSize = 10;

  users = signal<IamUser[]>([]);
  userFilter = signal('');
  moduleFilter = signal('');
  userOptions = computed(() => ['All', ...this.users().map(u => u.name)]);
  moduleOptions = ['All', ...MODULES];

  ngOnInit() {
    this.iam.getAllUsers().subscribe(u => this.users.set(u));
    this.load();
  }

  load() {
    this.loading.set(true);
    const userId = this.userFilter() ? this.users().find(u => u.name === this.userFilter())?.userId : undefined;
    const moduleSource = this.moduleFilter() as ModuleSource | undefined;
    this.iam.getAuditEvents(this.currentPage(), this.pageSize, { userId, moduleSource }).subscribe(res => {
      this.page.set(res);
      this.loading.set(false);
    });
  }

  onUserChange(v: string) { this.userFilter.set(v); this.currentPage.set(0); this.load(); }
  onModuleChange(v: string) { this.moduleFilter.set(v); this.currentPage.set(0); this.load(); }
  onPageChange(p: number) { this.currentPage.set(p); this.load(); }

  performerName(userId: number): string {
    return this.users().find(u => u.userId === userId)?.name ?? ('User #' + userId);
  }
}
