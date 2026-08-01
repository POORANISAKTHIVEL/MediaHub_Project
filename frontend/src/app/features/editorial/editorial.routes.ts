import { Routes } from '@angular/router';
import { permissionGuard } from '../../core/auth/auth.guard';
import { ReviewQueue } from './review-queue';
import { ReviewDetail } from './review-detail';
import { CollectionsList } from './collections-list';
import { CollectionDetail } from './collection-detail';
import { PublicationSchedule } from './publication-schedule';

export const EDITORIAL_ROUTES: Routes = [
  {
    path: '',
    canActivate: [permissionGuard],
    data: { permissions: ['content:read'] },
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'reviews' },
      { path: 'reviews', component: ReviewQueue },
      { path: 'reviews/:id', component: ReviewDetail },
      { path: 'collections', component: CollectionsList },
      { path: 'collections/:id', component: CollectionDetail },
      { path: 'schedule', component: PublicationSchedule }
    ]
  }
];
