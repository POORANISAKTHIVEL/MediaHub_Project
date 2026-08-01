import { Component, computed, effect, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { IamClient } from '../../core/api/iam-client';
import { Permission, Role } from '../../core/models/iam.models';
import { ToastService } from '../../shared/services/toast.service';
import { ConfirmService } from '../../shared/services/confirm.service';
import { LoadingSpinner } from '../../shared/components/loading-spinner';
import { Pagination } from '../../shared/components/pagination';

const PAGE_SIZE = 6;

@Component({
  selector: 'app-roles-list',
  imports: [RouterLink, FormsModule, LoadingSpinner, Pagination],
  templateUrl: './roles-list.html'
})
export class RolesList implements OnInit {
  private iam = inject(IamClient);
  private toast = inject(ToastService);
  private confirm = inject(ConfirmService);

  loading = signal(true);
  roles = signal<Role[]>([]);
  permissions = signal<Permission[]>([]);
  activeRoleId = signal<number | null>(null);
  activeRolePermissionTypes = signal<string[]>([]);
  loadingRolePermissions = signal(false);
  permPage = signal(0);
  pageSize = PAGE_SIZE;

  activeRole = computed(() => this.roles().find(r => r.roleId === this.activeRoleId()) ?? null);

  constructor() {
    effect(() => {
      const roleId = this.activeRoleId();
      if (roleId == null) { this.activeRolePermissionTypes.set([]); return; }
      this.loadingRolePermissions.set(true);
      this.iam.getPermissionsForRole(roleId).subscribe(types => {
        this.activeRolePermissionTypes.set(types);
        this.loadingRolePermissions.set(false);
      });
    });
  }

  pagedPermissions = computed(() => {
    const start = this.permPage() * this.pageSize;
    return this.permissions().slice(start, start + this.pageSize);
  });
  totalPermPages = computed(() => Math.max(1, Math.ceil(this.permissions().length / this.pageSize)));

  creatingRole = signal(false);
  newRoleType = '';
  renaming = signal<Role | null>(null);
  renameValue = '';

  ngOnInit() {
    this.load();
  }

  load() {
    this.loading.set(true);
    this.iam.getAllRoles().subscribe(roles => {
      this.roles.set(roles);
      if (!this.activeRoleId() && roles.length) this.activeRoleId.set(roles[0].roleId);
      this.loading.set(false);
    });
    this.iam.getAllPermissions().subscribe(p => this.permissions.set(p));
  }

  select(role: Role) {
    this.activeRoleId.set(role.roleId);
    this.permPage.set(0);
  }

  hasPermission(p: Permission): boolean {
    return this.activeRolePermissionTypes().includes(p.permissionType);
  }

  togglePermission(p: Permission) {
    const role = this.activeRole();
    if (!role) return;
    const granted = this.activeRolePermissionTypes().includes(p.permissionType);
    const request$ = granted
      ? this.iam.revokePermissionFromRole(role.roleId, p.permissionId)
      : this.iam.assignPermissionToRole(role.roleId, p.permissionId);

    request$.subscribe({
      next: () => {
        this.activeRolePermissionTypes.update(types =>
          granted ? types.filter(t => t !== p.permissionType) : [...types, p.permissionType]
        );
        this.toast.ok(`${p.permissionType} ${granted ? 'revoked from' : 'granted to'} ${role.roleType}`);
      },
      error: (err) => this.toast.warn(err?.error?.message ?? 'Unable to update permission')
    });
  }

  createRole() {
    if (!this.newRoleType.trim()) return;
    this.iam.createRole(this.newRoleType.trim()).subscribe(() => {
      this.toast.ok('Role created successfully');
      this.creatingRole.set(false);
      this.newRoleType = '';
      this.load();
    });
  }

  openRename(role: Role) {
    this.renaming.set(role);
    this.renameValue = role.roleType;
  }

  saveRename() {
    const role = this.renaming();
    if (!role || !this.renameValue.trim()) return;
    this.iam.renameRole(role.roleId, this.renameValue.trim()).subscribe(() => {
      this.toast.ok('Role updated successfully');
      this.renaming.set(null);
      this.load();
    });
  }

  async deleteRole(role: Role) {
    const ok = await this.confirm.ask(`Delete role "${role.roleType}"? Permissions will be unlinked automatically.`, 'Delete', true);
    if (!ok) return;
    this.iam.deleteRole(role.roleId).subscribe(() => {
      this.toast.ok('Role deleted. Permissions unlinked automatically.');
      if (this.activeRoleId() === role.roleId) this.activeRoleId.set(null);
      this.load();
    });
  }
}
