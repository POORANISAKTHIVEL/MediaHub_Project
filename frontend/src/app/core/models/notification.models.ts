export type NotificationCategory = 'CONTENT' | 'SUBSCRIPTION' | 'ROYALTY' | 'LICENSE' | 'EDITORIAL';
export type NotificationStatus = 'UNREAD' | 'READ' | 'DISMISSED';

/** Roles whose notifications feed is narrowed to categories relevant to their own work — e.g. a
 *  creator cares about what happens to their content (editorial decisions, publication/territory,
 *  royalty), not platform-wide subscription billing events that happen to share their account.
 *  Shared by the Notifications page, the Dashboard widget and the sidebar unread badge so they
 *  never disagree on what a given role should see. Roles absent from this map see everything. */
export const ROLE_NOTIFICATION_CATEGORIES: Record<string, NotificationCategory[]> = {
  creator: ['CONTENT', 'EDITORIAL', 'LICENSE', 'ROYALTY'],
  subscriber: ['SUBSCRIPTION', 'CONTENT']
};

export interface AppNotification {
  notificationId: number;
  userId: number;
  message: string;
  category: NotificationCategory;
  status: NotificationStatus;
  createdDate: string;
  licenseId?: number;
  contentId?: number;
  expiryDate?: string;
}
