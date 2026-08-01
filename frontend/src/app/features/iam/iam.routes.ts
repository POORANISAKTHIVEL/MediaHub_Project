import { Routes } from '@angular/router';
import { permissionGuard } from '../../core/auth/auth.guard';
import { UsersList } from './users-list';
import { RolesList } from './roles-list';
import { PermissionsList } from './permissions-list';

export const IAM_ROUTES: Routes = [
  {
    path: '',
    canActivate: [permissionGuard],
    data: { permissions: ['user:manage', 'role:manage', 'permission:manage'] },
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'users' },
      { path: 'users', component: UsersList },
      { path: 'roles', component: RolesList },
      { path: 'permissions', component: PermissionsList }
    ]
  }
];
