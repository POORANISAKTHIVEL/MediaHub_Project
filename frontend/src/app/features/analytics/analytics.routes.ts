import { Routes } from '@angular/router';
import { permissionGuard } from '../../core/auth/auth.guard';
import { AnalyticsDashboard } from './analytics-dashboard';
import { ReportsList } from './reports-list';
import { GenerateReport } from './generate-report';
import { ReportDetail } from './report-detail';

export const ANALYTICS_ROUTES: Routes = [
  {
    path: '',
    canActivate: [permissionGuard],
    data: { permissions: ['report:view'] },
    children: [
      { path: '', component: AnalyticsDashboard },
      { path: 'reports', component: ReportsList },
      { path: 'generate', component: GenerateReport },
      { path: 'reports/:id', component: ReportDetail }
    ]
  }
];
