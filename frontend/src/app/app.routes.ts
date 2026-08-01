import { Routes } from '@angular/router';
import { authGuard, guestGuard } from './core/auth/auth.guard';
import { AppShell } from './layout/app-shell';
import { Login } from './features/auth/login';
import { Register } from './features/auth/register';
import { Dashboard } from './features/dashboard/dashboard';
import { Profile } from './features/profile/profile';
import { Forbidden } from './features/misc/forbidden';
import { NotFound } from './features/misc/not-found';

export const routes: Routes = [
  { path: 'login', component: Login, canActivate: [guestGuard] },
  { path: 'register', component: Register, canActivate: [guestGuard] },
  {
    path: '',
    component: AppShell,
    canActivate: [authGuard],
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'dashboard' },
      { path: 'dashboard', component: Dashboard },
      { path: 'profile', component: Profile },
      { path: 'forbidden', component: Forbidden },
      {
        path: 'content',
        loadChildren: () => import('./features/content/content.routes').then(m => m.CONTENT_ROUTES)
      },
      {
        path: 'creators',
        loadChildren: () => import('./features/content/creators.routes').then(m => m.CREATORS_ROUTES)
      },
      {
        path: 'editorial',
        loadChildren: () => import('./features/editorial/editorial.routes').then(m => m.EDITORIAL_ROUTES)
      },
      {
        path: 'licensing',
        loadChildren: () => import('./features/licensing/licensing.routes').then(m => m.LICENSING_ROUTES)
      },
      {
        path: 'subscription',
        loadChildren: () => import('./features/subscription/subscription.routes').then(m => m.SUBSCRIPTION_ROUTES)
      },
      {
        path: 'royalty',
        loadChildren: () => import('./features/royalty/royalty.routes').then(m => m.ROYALTY_ROUTES)
      },
      {
        path: 'notifications',
        loadChildren: () => import('./features/notifications/notifications.routes').then(m => m.NOTIFICATIONS_ROUTES)
      },
      {
        path: 'iam',
        loadChildren: () => import('./features/iam/iam.routes').then(m => m.IAM_ROUTES)
      },
      {
        path: 'audit',
        loadChildren: () => import('./features/iam/audit.routes').then(m => m.AUDIT_ROUTES)
      },
      {
        path: 'analytics',
        loadChildren: () => import('./features/analytics/analytics.routes').then(m => m.ANALYTICS_ROUTES)
      },
      { path: '**', component: NotFound }
    ]
  },
  { path: '**', component: NotFound }
];
