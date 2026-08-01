import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { MockStore, mockOf, mockError } from './mock-store';
import { AppNotification, NotificationCategory } from '../models/notification.models';

const SEED: AppNotification[] = [
  { notificationId: 1, userId: 1, message: 'New content released: The Silent Frontier', category: 'CONTENT', status: 'UNREAD', createdDate: '2026-07-28T09:00:00', contentId: 10482 },
  { notificationId: 2, userId: 1, message: 'Your subscription renews in 3 days', category: 'SUBSCRIPTION', status: 'UNREAD', createdDate: '2026-07-27T08:00:00' },
  { notificationId: 3, userId: 1, message: 'Royalty statement STMT-88 has been finalised and is ready for payout processing.', category: 'ROYALTY', status: 'READ', createdDate: '2026-07-26T14:00:00' },
  { notificationId: 4, userId: 1, message: 'License LIC-8842 expires in 5 days', category: 'LICENSE', status: 'UNREAD', createdDate: '2026-07-25T11:00:00', licenseId: 8842, expiryDate: '2026-08-05' },
  { notificationId: 5, userId: 1, message: 'Your submitted review REV-2229 requires revision', category: 'EDITORIAL', status: 'DISMISSED', createdDate: '2026-07-20T10:00:00' }
];

@Injectable({ providedIn: 'root' })
export class NotificationClient {
  private http = inject(HttpClient);
  private base = `${environment.apiBaseUrl}/notification/mediaHub/notifications`;

  private notifications = new MockStore<AppNotification>(SEED, 'notificationId');

  getAllForUser(userId: number): Observable<AppNotification[]> {
    if (!environment.useMockNotification) return this.http.get<AppNotification[]>(`${this.base}/getAllNotifications/v1.0/${userId}`);
    return mockOf(this.notifications.filterBy(n => n.userId === userId));
  }

  getUnreadForUser(userId: number): Observable<AppNotification[]> {
    if (!environment.useMockNotification) return this.http.get<AppNotification[]>(`${this.base}/getUnreadNotifications/v1.0/${userId}`);
    return mockOf(this.notifications.filterBy(n => n.userId === userId && n.status === 'UNREAD'));
  }

  create(payload: { userId: number; message: string; category: NotificationCategory }): Observable<{ message: string }> {
    if (!environment.useMockNotification) return this.http.post<{ message: string }>(`${this.base}/createNotification/v1.0`, payload);
    this.notifications.insert({ ...payload, status: 'UNREAD', createdDate: new Date().toISOString() } as any);
    return mockOf({ message: 'Notification created successfully' });
  }

  updateStatus(id: number, status: 'READ' | 'DISMISSED' | 'UNREAD'): Observable<{ message: string }> {
    if (!environment.useMockNotification) return this.http.put<{ message: string }>(`${this.base}/updateNotification/v1.0/${id}?status=${status}`, {});
    const n = this.notifications.find(x => x.notificationId === id);
    if (!n) return mockError(404, 'Notification not found');
    if (n.status === 'DISMISSED') return mockError(400, 'Dismissed notification cannot be updated');
    this.notifications.update(id, { status });
    return mockOf({ message: 'Notification updated successfully' });
  }

  analytics(userId: number): Observable<{ total: number; unread: number; read: number; dismissed: number }> {
    const rows = this.notifications.filterBy(n => n.userId === userId);
    return mockOf({
      total: rows.length,
      unread: rows.filter(n => n.status === 'UNREAD').length,
      read: rows.filter(n => n.status === 'READ').length,
      dismissed: rows.filter(n => n.status === 'DISMISSED').length
    });
  }

  get(id: number): Observable<AppNotification | undefined> {
    return mockOf(this.notifications.find(n => n.notificationId === id));
  }
}
