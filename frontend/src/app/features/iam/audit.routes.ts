import { Routes } from '@angular/router';
import { permissionGuard } from '../../core/auth/auth.guard';
import { AuditLog } from './audit-log';

export const AUDIT_ROUTES: Routes = [
  { path: '', component: AuditLog, canActivate: [permissionGuard], data: { permissions: ['audit:read'] } }
];
