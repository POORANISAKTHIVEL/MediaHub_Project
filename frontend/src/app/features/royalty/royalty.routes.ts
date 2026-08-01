import { Routes } from '@angular/router';
import { permissionGuard } from '../../core/auth/auth.guard';
import { RoyaltyDashboard } from './royalty-dashboard';
import { RoyaltyRules } from './royalty-rules';
import { RoyaltyStatements } from './royalty-statements';
import { StatementDetail } from './statement-detail';
import { RoyaltyPayouts } from './royalty-payouts';

export const ROYALTY_ROUTES: Routes = [
  {
    path: '',
    canActivate: [permissionGuard],
    data: { permissions: ['royalty:view'] },
    children: [
      { path: '', component: RoyaltyDashboard },
      { path: 'rules', component: RoyaltyRules },
      { path: 'statements', component: RoyaltyStatements },
      { path: 'statements/:id', component: StatementDetail },
      { path: 'payouts', component: RoyaltyPayouts }
    ]
  }
];
