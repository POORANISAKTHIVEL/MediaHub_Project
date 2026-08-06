import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { IamClient } from '../../core/api/iam-client';
import { Permission } from '../../core/models/iam.models';
import { FilterChip } from '../../shared/components/filter-chip';
import { LoadingSpinner } from '../../shared/components/loading-spinner';
import { Pagination } from '../../shared/components/pagination';
import { FitRowsDirective } from '../../shared/directives/fit-rows.directive';
import { ToastService } from '../../shared/services/toast.service';
import { ConfirmService } from '../../shared/services/confirm.service';

const MODULE_OPTIONS = ['All', 'Content', 'Subscription', 'Licensing', 'Editorial', 'Notification', 'Analytics', 'IAM', 'Royalty'];
const PAGE_SIZE = 8;

@Component({
  selector: 'app-permissions-list',
  imports: [RouterLink, FormsModule, FilterChip, LoadingSpinner, Pagination, FitRowsDirective],
  templateUrl: './permissions-list.html'
})
export class PermissionsList implements OnInit {
  private iam = inject(IamClient);
  private toast = inject(ToastService);
  private confirm = inject(ConfirmService);

  loading = signal(true);
  allPermissions = signal<Permission[]>([]);
  rolesHoldingMap = signal<Record<string, string[]>>({});
  searchTerm = signal('');
  moduleFilter = signal('');
  moduleOptions = MODULE_OPTIONS;
  page = signal(0);
  pageSize = signal(PAGE_SIZE);

  rows = computed(() => {
    const term = this.searchTerm().trim().toLowerCase();
    return this.allPermissions()
      .filter(p => !this.moduleFilter() || this.iam.moduleOf(p.permissionType) === this.moduleFilter())
      .filter(p => !term || p.permissionType.toLowerCase().includes(term));
  });
  totalPages = computed(() => Math.max(1, Math.ceil(this.rows().length / this.pageSize())));
  pagedRows = computed(() => {
    const start = this.page() * this.pageSize();
    return this.rows().slice(start, start + this.pageSize());
  });

  onRowsThatFit(n: number) {
    if (n === this.pageSize()) return;
    this.pageSize.set(n);
    this.page.set(0);
  }

  creating = signal(false);
  newType = '';
  editing = signal<Permission | null>(null);
  editType = '';

  ngOnInit() {
    this.load();
  }

  load() {
    this.loading.set(true);
    this.iam.getAllPermissions().subscribe(p => {
      this.allPermissions.set(p);
      this.loading.set(false);
    });
    this.iam.getRolesHoldingMap().subscribe(m => this.rolesHoldingMap.set(m));
  }

  moduleOf(p: Permission) { return this.iam.moduleOf(p.permissionType); }
  rolesHolding(p: Permission) { return (this.rolesHoldingMap()[p.permissionType] ?? []).join(', '); }

  setModuleFilter(v: string) {
    this.moduleFilter.set(v);
    this.page.set(0);
  }

  onSearchChange(v: string) {
    this.searchTerm.set(v);
    this.page.set(0);
  }

  createPermission() {
    if (!this.newType.trim()) return;
    this.iam.createPermission(this.newType.trim()).subscribe(() => {
      this.toast.ok('Permission created successfully');
      this.creating.set(false);
      this.newType = '';
      this.load();
    });
  }

  openEdit(p: Permission) {
    this.editing.set(p);
    this.editType = p.permissionType;
  }

  saveEdit() {
    const p = this.editing();
    if (!p || !this.editType.trim()) return;
    this.iam.updatePermission(p.permissionId, this.editType.trim()).subscribe(() => {
      this.toast.ok('Permission updated successfully');
      this.editing.set(null);
      this.load();
    });
  }

  async deletePermission(p: Permission) {
    const ok = await this.confirm.ask(`Delete permission "${p.permissionType}"?`, 'Delete', true);
    if (!ok) return;
    this.iam.deletePermission(p.permissionId).subscribe(() => {
      this.toast.ok('Permission deleted');
      this.load();
    });
  }
}
