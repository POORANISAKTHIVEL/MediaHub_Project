import { Routes } from '@angular/router';
import { permissionGuard } from '../../core/auth/auth.guard';
import { PlansManagement } from './plans-management';
import { SubscriptionsList } from './subscriptions-list';
import { SubscriptionDetail } from './subscription-detail';
import { SubscriptionHistoryPage } from './subscription-history';
import { PlanCatalog } from './plan-catalog';
import { MySubscription } from './my-subscription';

export const SUBSCRIPTION_ROUTES: Routes = [
  { path: 'catalog', component: PlanCatalog, canActivate: [permissionGuard], data: { permissions: ['plan:view'] } },
  { path: 'my', component: MySubscription, canActivate: [permissionGuard], data: { permissions: ['subscription:view'] } },
  {
    path: '',
    canActivate: [permissionGuard],
    data: { permissions: ['plan:configure', 'subscription:manage'] },
    children: [
      { path: 'plans', component: PlansManagement },
      { path: 'history', component: SubscriptionHistoryPage }
    ]
  },
  {
    
    path: 'subscriptions',
    canActivate: [permissionGuard],
    data: { permissions: ['subscription:manage', 'subscription:view'] },
    children: [
      { path: '', component: SubscriptionsList },
      { path: ':id', component: SubscriptionDetail }
    ]
  }
];
