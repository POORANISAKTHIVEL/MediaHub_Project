import { Routes } from '@angular/router';
import { permissionGuard } from '../../core/auth/auth.guard';
import { LicensingList } from './licensing-list';
import { LicensingExpiring } from './licensing-expiring';
import { TerritoryRestrictions } from './territory-restrictions';
import { LicenseDetail } from './license-detail';
import { LicenseEdit } from './license-edit';

export const LICENSING_ROUTES: Routes = [
  {
    path: '',
    canActivate: [permissionGuard],
    data: { permissions: ['license:manage'] },
    children: [
      { path: '', component: LicensingList },
      { path: 'expiring', component: LicensingExpiring },
      { path: 'territory', component: TerritoryRestrictions },
      { path: ':id/edit', component: LicenseEdit },
      { path: ':id', component: LicenseDetail }
    ]
  }
];
