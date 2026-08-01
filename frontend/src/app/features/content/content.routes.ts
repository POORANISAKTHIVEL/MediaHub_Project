import { Routes } from '@angular/router';
import { permissionGuard } from '../../core/auth/auth.guard';
import { ContentList } from './content-list';
import { ContentDetail } from './content-detail';
import { ContentForm } from './content-form';
import { TagManagement } from './tag-management';

export const CONTENT_ROUTES: Routes = [
  {
    path: '',
    canActivate: [permissionGuard],
    data: { permissions: ['content:read'] },
    children: [
      { path: '', component: ContentList },
      { path: 'tags', component: TagManagement },
      { path: 'create', component: ContentForm, canActivate: [permissionGuard], data: { permissions: ['content:write'] } },
      { path: ':id/edit', component: ContentForm, canActivate: [permissionGuard], data: { permissions: ['content:write'] } },
      { path: ':id', component: ContentDetail }
    ]
  }
];
