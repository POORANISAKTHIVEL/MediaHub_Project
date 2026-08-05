import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { IamClient } from '../../core/api/iam-client';
import { AuditEvent, IamUser, ModuleSource } from '../../core/models/iam.models';
import { FilterChip } from '../../shared/components/filter-chip';
import { Pagination } from '../../shared/components/pagination';
import { LoadingSpinner } from '../../shared/components/loading-spinner';

const MODULES: ModuleSource[] = ['IAM', 'CONTENT', 'SUBSCRIPTION', 'EDITORIAL', 'LICENSING', 'ROYALTY', 'ANALYTICS', 'NOTIFICATION', 'SYSTEM'];

@Component({
  selector: 'app-audit-log',
  imports: [FormsModule, FilterChip, Pagination, LoadingSpinner],
  templateUrl: './audit-log.html'
})
export class AuditLog implements OnInit {
  private iam = inject(IamClient);

  loading = signal(true);
  events = signal<AuditEvent[]>([]);
  users = signal<IamUser[]>([]);

  actionFilter = signal('');
  moduleFilter = signal('');
  moduleOptions = ['All', ...MODULES];

  currentPage = signal(0);
  pageSize = 10;

  filteredEvents = computed(() => {
    const action = this.actionFilter().trim().toLowerCase();
    const module = this.moduleFilter();
    return this.events().filter(e =>
      (!action || e.eventType.toLowerCase().includes(action)) &&
      (!module || e.moduleSource === module)
    );
  });

  totalElements = computed(() => this.filteredEvents().length);
  totalPages = computed(() => Math.max(1, Math.ceil(this.totalElements() / this.pageSize)));

  pagedEvents = computed(() => {
    const start = this.currentPage() * this.pageSize;
    return this.filteredEvents().slice(start, start + this.pageSize);
  });

  ngOnInit() {
    this.iam.getAllUsers().subscribe(u => this.users.set(u));
    this.iam.getAllAuditEvents().subscribe(rows => {
      this.events.set(rows);
      this.loading.set(false);
    });
  }

  onActionChange(v: string) { this.actionFilter.set(v); this.currentPage.set(0); }
  onModuleChange(v: string) { this.moduleFilter.set(v); this.currentPage.set(0); }
  onPageChange(p: number) { this.currentPage.set(p); }

  performerName(userId: number): string {
    return this.users().find(u => u.userId === userId)?.name ?? ('User #' + userId);
  }

  formatTimestamp(iso: string): string {
    return new Date(iso).toLocaleString('en-US', {
      month: 'short', day: 'numeric', year: 'numeric',
      hour: 'numeric', minute: '2-digit', second: '2-digit', hour12: true
    });
  }

  export() {
    const rows: string[][] = [
      ['Action Taken', 'User Name', 'Service Module', 'IP Address', 'Timestamp'],
      ...this.filteredEvents().map(e => [e.eventType, this.performerName(e.performedBy), e.moduleSource, e.ipAddress ?? '—', this.formatTimestamp(e.createdAt)])
    ];
    const csv = rows.map(row => row.map(cell => `"${(cell ?? '').replace(/"/g, '""')}"`).join(',')).join('\n');
    const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `mediahub-audit-log-${new Date().toISOString().slice(0, 10)}.csv`;
    document.body.appendChild(a);
    a.click();
    a.remove();
    URL.revokeObjectURL(url);
  }
}
