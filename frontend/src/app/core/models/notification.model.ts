export type NotificationType = 'new_subscriber' | 'new_post';

export interface Notification {
  id: string;
  receiverId: string;
  actorId?: string;
  type: NotificationType;
  content: string;
  isRead: boolean;
  createdAt: string;
}
