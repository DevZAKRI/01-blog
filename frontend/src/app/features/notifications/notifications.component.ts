import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatListModule } from '@angular/material/list';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { NotificationService } from '../../core/services/notification.service';
import { Notification } from '../../core/models/notification.model';

@Component({
  selector: 'app-notifications',
  standalone: true,
  imports: [
    CommonModule,
    MatListModule,
    MatIconModule,
    MatButtonModule,
    MatProgressSpinnerModule,
    MatSnackBarModule
  ],
  templateUrl: './notifications.component.html',
  styleUrls: ['./notifications.component.css']
})
export class NotificationsComponent implements OnInit {
  notifications: Notification[] = [];
  isLoading = true;

  constructor(
    private notificationService: NotificationService,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.loadNotifications();
  }

  loadNotifications(): void {
    this.isLoading = true;
    this.notificationService.getNotifications().subscribe({
      next: (notifications) => {
        this.notifications = notifications;
        this.isLoading = false;
      },
      error: () => {
        this.isLoading = false;
        this.snackBar.open('Failed to load notifications', 'Close', { duration: 3000 });
      }
    });
  }

  hasUnread(): boolean {
    return this.notifications.some(n => !n.isRead);
  }

  toggleReadStatus(notification: Notification, event: Event): void {
    event.stopPropagation();
    if (notification.isRead) {
      this.notificationService.markAsUnread(notification.id).subscribe({
        next: () => {
          notification.isRead = false;
        },
        error: () => {
          this.snackBar.open('Failed to mark as unread', 'Close', { duration: 2000 });
        }
      });
    } else {
      this.notificationService.markAsRead(notification.id).subscribe({
        next: () => {
          notification.isRead = true;
        },
        error: () => {
          this.snackBar.open('Failed to mark as read', 'Close', { duration: 2000 });
        }
      });
    }
  }

  markAsRead(notification: Notification): void {
    if (!notification.isRead) {
      this.notificationService.markAsRead(notification.id).subscribe({
        next: () => {
          notification.isRead = true;
        }
      });
    }
  }

  markAllAsRead(): void {
    this.notificationService.markAllAsRead().subscribe({
      next: () => {
        this.notifications.forEach(n => n.isRead = true);
        this.snackBar.open('All notifications marked as read', 'Close', { duration: 2000 });
      },
      error: () => {
        this.snackBar.open('Failed to mark all as read', 'Close', { duration: 3000 });
      }
    });
  }

  getIcon(type: string): string {
    switch (type) {
      case 'new_subscriber': return 'person_add';
      case 'new_post': return 'article';
      default: return 'notifications';
    }
  }

  getTimeSince(dateString: string): string {
    const date = new Date(dateString);
    const seconds = Math.floor((new Date().getTime() - date.getTime()) / 1000);

    let interval = seconds / 31536000;
    if (interval > 1) return Math.floor(interval) + 'y ago';

    interval = seconds / 2592000;
    if (interval > 1) return Math.floor(interval) + 'mo ago';

    interval = seconds / 86400;
    if (interval > 1) return Math.floor(interval) + 'd ago';

    interval = seconds / 3600;
    if (interval > 1) return Math.floor(interval) + 'h ago';

    interval = seconds / 60;
    if (interval > 1) return Math.floor(interval) + 'm ago';

    return 'just now';
  }
}
