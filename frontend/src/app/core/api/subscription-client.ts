import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import { MockStore, mockOf, mockError } from './mock-store';
import { SubscriptionHistory, SubscriptionPlan, UserSubscription } from '../models/subscription.models';
import { AuthService } from '../auth/auth.service';
import { NotificationClient } from './notification-client';

const PLAN_SEED: SubscriptionPlan[] = [
  { planId: 1, name: 'Free', price: 0, billingCycle: 'Monthly', contentAccessLevel: 'Basic', maxDevices: 1, downloadAllowed: 0, status: 'Active' },
  { planId: 2, name: 'Basic', price: 4.99, billingCycle: 'Monthly', contentAccessLevel: 'Standard', maxDevices: 2, downloadAllowed: 1, status: 'Active' },
  { planId: 3, name: 'Standard', price: 9.99, billingCycle: 'Monthly', contentAccessLevel: 'Full', maxDevices: 4, downloadAllowed: 1, status: 'Active' },
  { planId: 4, name: 'Premium', price: 14.99, billingCycle: 'Monthly', contentAccessLevel: 'Full + Exclusive', maxDevices: 6, downloadAllowed: 1, status: 'Active' }
];

const SUB_SEED: UserSubscription[] = [
  { subscriptionId: 1, userId: 2, planId: 3, startDate: '2026-06-01', endDate: '2026-08-01', renewalType: 'AutoRenew', status: 'Active' },
  { subscriptionId: 2, userId: 3, planId: 2, startDate: '2026-05-01', endDate: '2026-07-01', renewalType: 'Manual', status: 'Expired' }
];

const HISTORY_SEED: SubscriptionHistory[] = [
  { historyId: 1, subscriptionId: 1, userId: 2, fromPlanId: 2, toPlanId: 3, changeType: 'Upgrade', changeDate: '2026-06-01T10:00:00', remarks: 'Upgraded to Standard' }
];

@Injectable({ providedIn: 'root' })
export class SubscriptionClient {
  private http = inject(HttpClient);
  private base = `${environment.apiBaseUrl}/subscription/mediaHub/subscriptionPlan`;
  private auth = inject(AuthService);
  private notification = inject(NotificationClient);

  private notify(message: string) {
    const userId = this.auth.currentUser()?.userId ?? 1;
    this.notification.create({ userId, message, category: 'SUBSCRIPTION' }).subscribe();
  }

  private plans = new MockStore<SubscriptionPlan>(PLAN_SEED, 'planId');
  private subs = new MockStore<UserSubscription>(SUB_SEED, 'subscriptionId');
  private history = new MockStore<SubscriptionHistory>(HISTORY_SEED, 'historyId');

 
  private realPlanCache = new Map<number, SubscriptionPlan>();

 
  fetchPlans(): Observable<SubscriptionPlan[]> {
    if (!environment.useMockSubscription) {
      return this.http.get<SubscriptionPlan[]>(`${this.base}/plans/fetchPlans`).pipe(
        map(rows => { rows.forEach(p => this.realPlanCache.set(p.planId, p)); return rows; })
      );
    }
    return mockOf(this.plans.all());
  }

  createPlan(payload: Partial<SubscriptionPlan>): Observable<{ message: string }> {
    if (!environment.useMockSubscription) return this.http.post<{ message: string }>(`${this.base}/plans/createPlan`, payload);
    if (this.plans.find(p => p.name === payload.name)) return mockError(409, 'Plan name already exists');
    this.plans.insert({ ...payload, status: 'Active' } as any);
    return mockOf({ message: 'Plan created successfully' });
  }

 
  updatePlan(planId: number, payload: Partial<SubscriptionPlan>): Observable<{ message: string }> {
    if (!environment.useMockSubscription) {
      const keys = Object.keys(payload);
      if (keys.length === 1 && keys[0] === 'status') {
        return mockError(400, 'Changing a plan\'s status is not supported by the backend yet.');
      }
      return this.http.put<{ message: string }>(`${this.base}/plans/updatePlan/${planId}`, payload);
    }
    const p = this.plans.update(planId, payload);
    return p ? mockOf({ message: 'Plan updated successfully' }) : mockError(404, 'Plan not found');
  }

  
  fetchSubscriptions(): Observable<UserSubscription[]> {
    if (!environment.useMockSubscription) return this.http.get<UserSubscription[]>(`${this.base}/usersubscriptions/fetchSubscriptions`);
    return mockOf(this.subs.all());
  }

 
  fetchSubscriptionForUser(userId: number): Observable<UserSubscription | undefined> {
    if (!environment.useMockSubscription) {
      return this.fetchSubscriptions().pipe(map(rows => rows.find(s => s.userId === userId && s.status === 'Active')));
    }
    return mockOf(this.subs.find(s => s.userId === userId && s.status === 'Active'));
  }

  getSubscriptionById(subscriptionId: number): Observable<UserSubscription | undefined> {
    if (!environment.useMockSubscription) return this.http.get<UserSubscription>(`${this.base}/usersubscriptions/fetchSubscription/${subscriptionId}`);
    return mockOf(this.subs.find(s => s.subscriptionId === subscriptionId));
  }

  subscriptionCountForUser(userId: number): Observable<number> {
    if (!environment.useMockSubscription) {
      return this.fetchSubscriptions().pipe(map(rows => rows.filter(s => s.userId === userId).length));
    }
    return mockOf(this.subs.filterBy(s => s.userId === userId).length);
  }

 
  suspendSubscription(subscriptionId: number): Observable<{ message: string }> {
    if (!environment.useMockSubscription) return mockError(400, 'Suspending a subscription is not supported by the backend yet.');
    const s = this.subs.find(x => x.subscriptionId === subscriptionId);
    if (!s) return mockError(404, 'Subscription not found');
    this.subs.update(subscriptionId, { status: 'Suspended' });
    return mockOf({ message: 'Subscription suspended' });
  }


  reactivateSubscription(subscriptionId: number): Observable<{ message: string }> {
    if (!environment.useMockSubscription) return mockError(400, 'Reactivating a subscription is not supported by the backend yet.');
    const s = this.subs.find(x => x.subscriptionId === subscriptionId);
    if (!s) return mockError(404, 'Subscription not found');
    if (s.status !== 'Suspended' && s.status !== 'Expired') return mockError(400, 'Only Suspended or Expired subscriptions can be reactivated');
    this.subs.update(subscriptionId, { status: 'Active' });
    return mockOf({ message: 'Subscription reactivated' });
  }

  createSubscription(payload: { userId: number; planId: number; startDate: string; endDate: string; renewalType: 'AutoRenew' | 'Manual' }): Observable<{ message: string }> {
    if (!environment.useMockSubscription) return this.http.post<{ message: string }>(`${this.base}/usersubscriptions/createSubscription`, payload);
    if (this.subs.find(s => s.userId === payload.userId && s.status === 'Active')) return mockError(409, 'Active subscription already exists');
    const row = this.subs.insert({ ...payload, status: 'Active' } as any);
    this.notify(`New subscription created for User #${row.userId} on plan "${this.planName(row.planId)}".`);
    return mockOf({ message: 'Subscription created successfully' });
  }

  updateSubscription(subscriptionId: number, planId: number): Observable<{ message: string }> {
    if (!environment.useMockSubscription) return this.http.put<{ message: string }>(`${this.base}/usersubscriptions/updateSubscription/${subscriptionId}`, { planId });
    const s = this.subs.find(x => x.subscriptionId === subscriptionId);
    if (!s) return mockError(404, 'Subscription not found');
    const fromPlanId = s.planId;
    this.subs.update(subscriptionId, { planId });
    if (fromPlanId !== planId) {
      this.history.insert({ subscriptionId, userId: s.userId, fromPlanId, toPlanId: planId, changeType: 'PlanChange', changeDate: new Date().toISOString(), remarks: 'Plan changed by admin' } as any);
    }
    return mockOf({ message: 'Subscription updated successfully' });
  }

  renewSubscription(subscriptionId: number, endDate: string): Observable<{ message: string }> {
    if (!environment.useMockSubscription) return this.http.put<{ message: string }>(`${this.base}/usersubscriptions/renewSubscription/${subscriptionId}`, { endDate });
    const s = this.subs.find(x => x.subscriptionId === subscriptionId);
    if (!s) return mockError(404, 'Subscription not found');
    if (s.status !== 'Active') return mockError(409, 'Only Active subscriptions can be renewed');
    this.subs.update(subscriptionId, { endDate });
    this.notify(`Subscription #${subscriptionId} for User #${s.userId} was renewed until ${endDate}.`);
    return mockOf({ message: 'Subscription renewed successfully' });
  }

  cancelSubscription(subscriptionId: number): Observable<{ message: string }> {
    if (!environment.useMockSubscription) return this.http.put<{ message: string }>(`${this.base}/usersubscriptions/cancelSubscription/${subscriptionId}`, {});
    const s = this.subs.find(x => x.subscriptionId === subscriptionId);
    if (!s) return mockError(404, 'Subscription not found');
    if (s.status === 'Cancelled') return mockError(409, 'Subscription already cancelled');
    this.subs.update(subscriptionId, { status: 'Cancelled' });
    this.notify(`Subscription #${subscriptionId} for User #${s.userId} was cancelled.`);
    return mockOf({ message: 'Subscription cancelled successfully' });
  }

  analytics(): Observable<{ totalSubscriptions: number; activeSubscriptions: number; cancelledSubscriptions: number; expiredSubscriptions: number }> {
    const rows = this.subs.all();
    return mockOf({
      totalSubscriptions: rows.length,
      activeSubscriptions: rows.filter(s => s.status === 'Active').length,
      cancelledSubscriptions: rows.filter(s => s.status === 'Cancelled').length,
      expiredSubscriptions: rows.filter(s => s.status === 'Expired').length
    });
  }

 
  fetchHistories(): Observable<SubscriptionHistory[]> {
    if (!environment.useMockSubscription) return this.http.get<SubscriptionHistory[]>(`${this.base}/subscriptionhistory/fetchHistories`);
    return mockOf(this.history.all());
  }

  planName(planId: number): string {
    if (!environment.useMockSubscription) {
      return this.realPlanCache.get(planId)?.name ?? ('Plan #' + planId);
    }
    return this.plans.find(p => p.planId === planId)?.name ?? ('Plan #' + planId);
  }

  planPrice(planId: number): number {
    if (!environment.useMockSubscription) {
      return this.realPlanCache.get(planId)?.price ?? 0;
    }
    return this.plans.find(p => p.planId === planId)?.price ?? 0;
  }
}
