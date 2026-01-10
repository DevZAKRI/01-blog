export enum NotificationType {
  LIKE = 'LIKE',
  COMMENT = 'COMMENT',
  SUBSCRIPTION = 'SUBSCRIPTION'
}

export interface Notification {
  id: string;
  userId: string;
  type: NotificationType;
  message: string;
  relatedId?: string;
  isRead: boolean;
  createdAt: string;
}
