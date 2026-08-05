import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { IamClient } from '../../core/api/iam-client';
import { IamUser } from '../../core/models/iam.models';
import { StatusBadge } from '../../shared/components/status-badge';
import { FilterChip } from '../../shared/components/filter-chip';
import { RowMenu, RowMenuItem } from '../../shared/components/row-menu';
import { LoadingSpinner } from '../../shared/components/loading-spinner';
import { Pagination } from '../../shared/components/pagination';
import { ToastService } from '../../shared/services/toast.service';

const ROLE_OPTIONS = ['All', 'admin', 'subscriber', 'creator', 'rightsManager', 'editorial', 'revenueAnalyst'];
const STATUS_OPTIONS = ['All', 'active', 'suspended'];

@Component({
  selector: 'app-users-list',
  imports: [RouterLink, FormsModule, StatusBadge, FilterChip, RowMenu, LoadingSpinner, Pagination],
  templateUrl: './users-list.html'
})
export class UsersList implements OnInit {
  private iam = inject(IamClient);
  private toast = inject(ToastService);

  loading = signal(true);
  allUsers = signal<IamUser[]>([]);
  searchTerm = signal('');
  roleFilter = signal('');
  statusFilter = signal('');
  roleOptions = ROLE_OPTIONS;
  statusOptions = STATUS_OPTIONS;

  onSearchChange(value: string) {
    this.searchTerm.set(value);
    this.page.set(0);
  }

  page = signal(0);
  pageSize = 10;
  totalPages = computed(() => Math.max(1, Math.ceil(this.rows().length / this.pageSize)));
  pagedRows = computed(() => this.rows().slice(this.page() * this.pageSize, (this.page() + 1) * this.pageSize));

  onRoleFilterChange(value: string) {
    this.roleFilter.set(value);
    this.page.set(0);
  }

  onStatusFilterChange(value: string) {
    this.statusFilter.set(value);
    this.page.set(0);
  }

  editing = signal<IamUser | null>(null);
  viewing = signal<IamUser | null>(null);
  confirmingSuspend = signal<IamUser | null>(null);
  confirmingDeactivate = signal<IamUser | null>(null);
  editName = '';
  editPhone = '';
  editCountry = '';
  suspendReason = '';
  deactivateReason = '';

  rows = computed(() => {
    const term = this.searchTerm().trim().toLowerCase();
    return this.allUsers()
      .filter(u => u.status === 'active' || u.status === 'suspended')
      .filter(u => !this.roleFilter() || u.roleType === this.roleFilter())
      .filter(u => !this.statusFilter() || u.status === this.statusFilter())
      .filter(u => !term || u.name.toLowerCase().includes(term) || u.email.toLowerCase().includes(term) || String(u.userId).includes(term));
  });

  ngOnInit() {
    this.load();
  }

  load() {
    this.loading.set(true);
    this.iam.getAllUsers().subscribe(users => {
      this.allUsers.set(users);
      this.loading.set(false);
    });
  }

  menuFor(u: IamUser): RowMenuItem[] {
    const items: RowMenuItem[] = [
      { label: 'View', action: 'view' },
      { label: 'Edit', action: 'edit' }
    ];
    if (u.status === 'active') items.push({ label: 'Suspend', action: 'suspend' }, { label: 'Deactivate', action: 'deactivate' });
    if (u.status === 'suspended') items.push({ label: 'Activate', action: 'activate' }, { label: 'Deactivate', action: 'deactivate' });
    if (u.status === 'inactive') items.push({ label: 'Activate', action: 'activate' });
    return items;
  }

  onAction(action: string, u: IamUser) {
    if (action === 'view') this.viewing.set(u);
    if (action === 'edit') { this.editing.set(u); this.editName = u.name; this.editPhone = u.phone ?? ''; this.editCountry = u.country ?? ''; }
    if (action === 'suspend') { this.confirmingSuspend.set(u); this.suspendReason = ''; }
    if (action === 'deactivate') { this.confirmingDeactivate.set(u); this.deactivateReason = ''; }
    if (action === 'activate') this.activate(u);
  }

  saveEdit() {
    const u = this.editing();
    if (!u) return;
    this.iam.updateUser(u.userId, { name: this.editName, phone: this.editPhone, country: this.editCountry }).subscribe(() => {
      this.toast.ok('User updated successfully');
      this.editing.set(null);
      this.load();
    });
  }

  confirmSuspend() {
    const u = this.confirmingSuspend();
    if (!u || !this.suspendReason.trim()) return;
    this.iam.suspendUser(u.userId, this.suspendReason).subscribe(() => {
      this.toast.ok('User suspended successfully');
      this.confirmingSuspend.set(null);
      this.load();
    });
  }

  confirmDeactivate() {
    const u = this.confirmingDeactivate();
    if (!u || !this.deactivateReason.trim()) return;
    this.iam.deactivateUser(u.userId, this.deactivateReason).subscribe(() => {
      this.toast.ok('User deactivated successfully');
      this.confirmingDeactivate.set(null);
      this.load();
    });
  }

  activate(u: IamUser) {
    this.iam.activateUser(u.userId).subscribe(() => {
      this.toast.ok('User activated successfully');
      this.load();
    });
  }

  initials(name: string): string {
    return name.split(' ').map(p => p[0]).join('').slice(0, 2).toUpperCase();
  }
}
