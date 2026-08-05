import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, forkJoin, of } from 'rxjs';
import { delay, map, switchMap } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import { MockStore, mockOf, mockError } from './mock-store';
import { AuditEvent, IamUser, ModuleSource, PageResponse, Permission, Role, Severity, UserStatus } from '../models/iam.models';
import { SEED_USERS, ROLE_PERMISSIONS } from '../auth/seed-users';

const ROLE_SEED: Role[] = [
  { roleId: 1, roleType: 'subscriber' },
  { roleId: 2, roleType: 'creator' },
  { roleId: 3, roleType: 'editorial' },
  { roleId: 4, roleType: 'rightsManager' },
  { roleId: 5, roleType: 'revenueAnalyst' },
  { roleId: 6, roleType: 'admin' }
];

const PERMISSION_MODULE: Record<string, string> = {
  'content:read': 'Content', 'content:write': 'Content', 'content:publish': 'Content', 'content:delete': 'Content',
  'royalty:view': 'Royalty', 'royalty:approve': 'Royalty',
  'plan:configure': 'Subscription', 'plan:view': 'Subscription', 'subscription:view': 'Subscription', 'subscription:manage': 'Subscription',
  'role:manage': 'IAM', 'permission:manage': 'IAM', 'user:suspend': 'IAM', 'user:manage': 'IAM', 'audit:read': 'IAM',
  'report:view': 'Analytics', 'license:manage': 'Licensing', 'editorial:manage': 'Editorial',
  'notification:view': 'Notification', 'notification:send': 'Notification', 'notification:update': 'Notification', 'notification:analytics': 'Notification'
};

const PERMISSION_SEED: Permission[] = Object.keys(PERMISSION_MODULE).map((type, i) => ({ permissionId: i + 1, permissionType: type }));

function rolesHolding(permissionType: string): string {
  return ROLE_SEED.filter(r => ROLE_PERMISSIONS[r.roleType as keyof typeof ROLE_PERMISSIONS]?.includes(permissionType))
    .map(r => r.roleType).join(', ');
}

const USER_SEED: IamUser[] = SEED_USERS.map(u => ({
  userId: u.userId, name: u.name, email: u.email, phone: u.phone, country: u.country,
  status: u.status, role: ROLE_SEED.find(r => r.roleId === u.roleId)!, roleType: u.roleType
}));

let auditSeq = 1;
const AUDIT_SEED: AuditEvent[] = [
  { eventId: auditSeq++, eventType: 'USER_UPDATED', moduleSource: 'IAM', performedBy: 1, performedByRole: 'admin', targetEntityType: 'User', targetEntityId: '2', severity: 'LOW', status: 'SUCCESS', description: 'Updated user contact details', createdAt: '2026-07-27T08:41:00' },
  { eventId: auditSeq++, eventType: 'USER_SUSPENDED', moduleSource: 'IAM', performedBy: 1, performedByRole: 'admin', targetEntityType: 'User', targetEntityId: '4', severity: 'MEDIUM', status: 'SUCCESS', description: 'Suspended user account', createdAt: '2026-07-27T08:39:00' },
  { eventId: auditSeq++, eventType: 'CONTENT_CREATED', moduleSource: 'CONTENT', performedBy: 3, performedByRole: 'editorial', targetEntityType: 'ContentAsset', targetEntityId: '10482', severity: 'LOW', status: 'SUCCESS', description: 'Created new content asset', createdAt: '2026-07-27T08:12:00' },
  { eventId: auditSeq++, eventType: 'LICENSE_UPDATED', moduleSource: 'LICENSING', performedBy: 5, performedByRole: 'rightsManager', targetEntityType: 'LicenseAgreement', targetEntityId: '221', severity: 'LOW', status: 'SUCCESS', description: 'Updated license terms', createdAt: '2026-07-27T07:58:00' },
  { eventId: auditSeq++, eventType: 'STATEMENT_FINALISED', moduleSource: 'ROYALTY', performedBy: 6, performedByRole: 'revenueAnalyst', targetEntityType: 'RoyaltyStatement', targetEntityId: '88', severity: 'MEDIUM', status: 'SUCCESS', description: 'Finalised royalty statement', createdAt: '2026-07-26T11:40:00' },
  { eventId: auditSeq++, eventType: 'REVIEW_APPROVED', moduleSource: 'EDITORIAL', performedBy: 4, performedByRole: 'editorial', targetEntityType: 'EditorialReview', targetEntityId: '55', severity: 'LOW', status: 'SUCCESS', description: 'Approved content for publication', createdAt: '2026-07-26T10:15:00' }
];

@Injectable({ providedIn: 'root' })
export class IamClient {
  private http = inject(HttpClient);
  private base = `${environment.apiBaseUrl}/mediaHub/iam`;

  private users = new MockStore<IamUser>(USER_SEED, 'userId');
  private roles = new MockStore<Role>(ROLE_SEED, 'roleId');
  private permissions = new MockStore<Permission>(PERMISSION_SEED, 'permissionId');
  private auditEvents = new MockStore<AuditEvent>(AUDIT_SEED, 'eventId');

  moduleOf(permissionType: string): string {
    return PERMISSION_MODULE[permissionType] ?? 'Other';
  }
  rolesHolding(permissionType: string): string {
    return rolesHolding(permissionType);
  }

  /** Map of permissionType -> comma-joined role names currently holding it. In mock mode this
   *  mirrors the static ROLE_PERMISSIONS matrix; in real mode it's built from the live
   *  role/permission assignments in the database, since roles-list.ts lets admins change those
   *  at runtime and the Permissions page's "Held by roles" column needs to reflect that. */
  getRolesHoldingMap(): Observable<Record<string, string[]>> {
    if (!environment.useMockAuth) {
      return this.getAllRoles().pipe(
        switchMap(roles => {
          if (!roles.length) return of({} as Record<string, string[]>);
          return forkJoin(roles.map(r => this.getPermissionsForRole(r.roleId).pipe(map(types => ({ role: r.roleType, types }))))).pipe(
            map(results => {
              const byPermission: Record<string, string[]> = {};
              for (const { role, types } of results) {
                for (const t of types) {
                  (byPermission[t] ??= []).push(role);
                }
              }
              return byPermission;
            })
          );
        })
      );
    }
    const byPermission: Record<string, string[]> = {};
    for (const role of ROLE_SEED) {
      for (const t of (ROLE_PERMISSIONS[role.roleType as keyof typeof ROLE_PERMISSIONS] ?? [])) {
        (byPermission[t] ??= []).push(role.roleType);
      }
    }
    return mockOf(byPermission);
  }

  // ---- Users ----
  getAllUsers(): Observable<IamUser[]> {
    if (!environment.useMockAuth) {
      return this.http.get<{ data: IamUser[] }>(`${this.base}/users/getAllUsers/v1.0`)
        .pipe(map(r => r.data.map(u => ({ ...u, roleType: u.role?.roleType ?? u.roleType }))));
    }
    return mockOf(this.users.all());
  }

  updateUser(userId: number, patch: { name?: string; phone?: string; country?: string }): Observable<{ message: string }> {
    if (!environment.useMockAuth) return this.http.put<{ message: string }>(`${this.base}/users/updateUser/v1/${userId}`, patch);
    const u = this.users.update(userId, patch);
    return u ? mockOf({ message: 'User updated successfully' }) : mockError(404, 'USER_NOT_FOUND');
  }

  suspendUser(userId: number, reason: string): Observable<{ message: string }> {
    if (!environment.useMockAuth) return this.http.post<{ message: string }>(`${this.base}/users/suspendUser/v1/${userId}`, { reason });
    const u = this.users.find(x => x.userId === userId);
    if (!u) return mockError(404, 'USER_NOT_FOUND');
    if (u.status === 'suspended') return mockError(409, 'ALREADY_SUSPENDED');
    this.users.update(userId, { status: 'suspended' as UserStatus });
    return mockOf({ message: 'User suspended successfully' });
  }

  activateUser(userId: number): Observable<{ message: string }> {
    if (!environment.useMockAuth) return this.http.post<{ message: string }>(`${this.base}/users/activateUser/v1/${userId}`, { activatedBy: 1 });
    const u = this.users.find(x => x.userId === userId);
    if (!u) return mockError(404, 'USER_NOT_FOUND');
    if (u.status === 'active') return mockError(400, 'ALREADY_ACTIVE');
    this.users.update(userId, { status: 'active' as UserStatus });
    return mockOf({ message: 'User activated successfully' });
  }

  deactivateUser(userId: number, reason: string): Observable<{ message: string }> {
    if (!environment.useMockAuth) return this.http.delete<{ message: string }>(`${this.base}/users/deleteUser/v1/${userId}`, { body: { reason } });
    const u = this.users.find(x => x.userId === userId);
    if (!u) return mockError(404, 'USER_NOT_FOUND');
    this.users.update(userId, { status: 'inactive' as UserStatus });
    return mockOf({ message: 'User deactivated successfully' });
  }

  // ---- Roles ----
  getAllRoles(): Observable<Role[]> {
    if (!environment.useMockAuth) return this.http.get<{ data: Role[] }>(`${this.base}/roles/getAllRoles/v1.0`).pipe(map(r => r.data));
    return mockOf(this.roles.all());
  }

  createRole(roleType: string): Observable<{ message: string }> {
    if (!environment.useMockAuth) return this.http.post<{ message: string }>(`${this.base}/roles/createRole/v1.0`, { roleType });
    this.roles.insert({ roleType } as any);
    return mockOf({ message: 'Role created successfully' });
  }

  renameRole(roleId: number, roleType: string): Observable<{ message: string }> {
    if (!environment.useMockAuth) return this.http.put<{ message: string }>(`${this.base}/roles/updateRole/v1/${roleId}`, { roleType });
    this.roles.update(roleId, { roleType });
    return mockOf({ message: 'Role updated successfully' });
  }

  deleteRole(roleId: number): Observable<{ message: string }> {
    if (!environment.useMockAuth) return this.http.delete<{ message: string }>(`${this.base}/roles/deleteRole/v1/${roleId}`);
    this.roles.remove(roleId);
    return mockOf({ message: 'Role deleted. Permissions unlinked automatically.' });
  }

  // ---- Permissions ----
  getAllPermissions(): Observable<Permission[]> {
    if (!environment.useMockAuth) return this.http.get<{ data: Permission[] }>(`${this.base}/permissions/getAllPermissions/v1.0`).pipe(map(r => r.data));
    return mockOf(this.permissions.all());
  }

  createPermission(permissionType: string): Observable<{ message: string }> {
    if (!environment.useMockAuth) return this.http.post<{ message: string }>(`${this.base}/permissions/createPermission/v1.0`, { permissionType });
    this.permissions.insert({ permissionType } as any);
    return mockOf({ message: 'Permission created successfully' });
  }

  updatePermission(permissionId: number, permissionType: string): Observable<{ message: string }> {
    if (!environment.useMockAuth) return this.http.put<{ message: string }>(`${this.base}/permissions/updatePermission/v1/${permissionId}`, { permissionType });
    this.permissions.update(permissionId, { permissionType });
    return mockOf({ message: 'Permission updated successfully' });
  }

  deletePermission(permissionId: number): Observable<{ message: string }> {
    if (!environment.useMockAuth) return this.http.delete<{ message: string }>(`${this.base}/permissions/deletePermission/v1/${permissionId}`);
    this.permissions.remove(permissionId);
    return mockOf({ message: 'Permission deleted successfully' });
  }

  // ---- Audit events (paginated, per backend auditlog module) ----
  // Real backend exposes one filter dimension at a time (by user OR module OR severity, or
  // unfiltered) — unlike the mock, which can combine filters freely on the in-memory array.
  // Priority when multiple filters are set: userId > moduleSource > severity > none.
  getAuditEvents(page: number, size: number, filters: { userId?: number; moduleSource?: ModuleSource; severity?: Severity } = {}): Observable<PageResponse<AuditEvent>> {
    if (!environment.useMockAuth) {
      const auditBase = `${environment.apiBaseUrl}/mediaHub/auditlog/events`;
      let url: string;
      if (filters.userId) url = `${auditBase}/getByUser/v1/${filters.userId}?page=${page}&size=${size}`;
      else if (filters.moduleSource) url = `${auditBase}/getByModule/v1/${filters.moduleSource}?page=${page}&size=${size}`;
      else if (filters.severity) url = `${auditBase}/getBySeverity/v1/${filters.severity}?page=${page}&size=${size}`;
      else url = `${auditBase}/getAllEvents/v1.0?page=${page}&size=${size}`;

      return this.http.get<{ data: AuditEvent[]; currentPage: number; totalPages: number; totalElements: number }>(url).pipe(
        map(r => ({
          data: r.data,
          currentPage: r.currentPage,
          totalPages: r.totalPages,
          totalElements: r.totalElements,
          pageSize: size,
          isFirst: r.currentPage === 0,
          isLast: r.currentPage >= r.totalPages - 1
        }))
      );
    }

    let rows = this.auditEvents.all();
    if (filters.userId) rows = rows.filter(e => e.performedBy === filters.userId);
    if (filters.moduleSource) rows = rows.filter(e => e.moduleSource === filters.moduleSource);
    if (filters.severity) rows = rows.filter(e => e.severity === filters.severity);
    rows = [...rows].sort((a, b) => b.createdAt.localeCompare(a.createdAt));

    const totalElements = rows.length;
    const totalPages = Math.max(1, Math.ceil(totalElements / size));
    const data = rows.slice(page * size, page * size + size);
    return mockOf({ data, currentPage: page, totalPages, totalElements, pageSize: size, isFirst: page === 0, isLast: page >= totalPages - 1 });
  }

  // Fetches every audit event in one call so the Audit Log page can filter by user ID, action
  // and module together and paginate client-side — the backend only supports one filter
  // dimension at a time, which isn't enough for combined search.
  getAllAuditEvents(): Observable<AuditEvent[]> {
    if (!environment.useMockAuth) {
      const url = `${environment.apiBaseUrl}/mediaHub/auditlog/events/getAllEvents/v1.0?page=0&size=5000`;
      return this.http.get<{ data: AuditEvent[] }>(url).pipe(map(r => r.data));
    }
    return mockOf([...this.auditEvents.all()].sort((a, b) => b.createdAt.localeCompare(a.createdAt)));
  }

  // ---- Role <-> Permission assignments ----
  getPermissionsForRole(roleId: number): Observable<string[]> {
    if (!environment.useMockAuth) {
      return this.http.get<{ permissions: string[] }>(`${this.base}/roles/getPermissions/v1/${roleId}`).pipe(map(r => r.permissions));
    }
    const role = this.roles.find(r => r.roleId === roleId);
    return mockOf(role ? (ROLE_PERMISSIONS[role.roleType as keyof typeof ROLE_PERMISSIONS] ?? []) : []);
  }

  assignPermissionToRole(roleId: number, permissionId: number): Observable<{ message: string }> {
    if (!environment.useMockAuth) return this.http.post<{ message: string }>(`${this.base}/roles/assignPermission/v1/${roleId}`, { permissionId });
    return mockOf({ message: 'Permission assigned successfully' });
  }

  revokePermissionFromRole(roleId: number, permissionId: number): Observable<{ message: string }> {
    if (!environment.useMockAuth) return this.http.delete<{ message: string }>(`${this.base}/roles/revokePermission/v1/${roleId}/${permissionId}`);
    return mockOf({ message: 'Permission revoked successfully' });
  }
}
