export type PlanStatus = 'Active' | 'Discontinued';

export type SubStatus = 'Active' | 'Cancelled' | 'Expired' | 'Suspended';

export interface SubscriptionPlan {
  planId: number;
  name: string;
  price: number;
  billingCycle: 'Monthly' | 'Quarterly' | 'Annual';
  contentAccessLevel?: string;
  maxDevices?: number;
  downloadAllowed?: number;
  status: PlanStatus;
}

export interface SubUser {
  userId: number;
  name: string;
  roles: string;
  email: string;
  phone?: string;
  country?: string;
  status: 'Active' | 'Suspended';
}

export interface UserSubscription {
  subscriptionId: number;
  userId: number;
  planId: number;
  startDate: string;
  endDate: string;
  renewalType: 'AutoRenew' | 'Manual';
  status: SubStatus;
}

export interface SubscriptionHistory {
  historyId: number;
  subscriptionId: number;
  userId: number;
  fromPlanId?: number;
  toPlanId: number;
  changeType: string;
  changeDate: string;
  remarks?: string;
}
