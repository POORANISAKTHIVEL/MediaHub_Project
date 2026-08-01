export type NotificationCategory = 'CONTENT' | 'SUBSCRIPTION' | 'ROYALTY' | 'LICENSE' | 'EDITORIAL';
export type NotificationStatus = 'UNREAD' | 'READ' | 'DISMISSED';

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
