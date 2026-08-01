import { Routes } from '@angular/router';
import { permissionGuard } from '../../core/auth/auth.guard';
import { CreatorsList } from './creators-list';
import { CreatorProfile } from './creator-profile';

export const CREATORS_ROUTES: Routes = [
  {
    path: '',
    canActivate: [permissionGuard],
    data: { permissions: ['content:read'] },
    children: [
      { path: '', component: CreatorsList },
      { path: ':id', component: CreatorProfile }
    ]
  }
];
