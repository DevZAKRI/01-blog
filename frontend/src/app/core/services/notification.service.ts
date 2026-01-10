import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject, interval } from 'rxjs';
import { switchMap, tap, map } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import { Notification } from '../models/notification.model';

@Injectable({
  providedIn: 'root'
})
export class NotificationService {
  private unreadCountSubject = new BehaviorSubject<number>(0);
  public unreadCount$ = this.unreadCountSubject.asObservable();

  constructor(private http: HttpClient) {}

  getNotifications(): Observable<Notification[]> {
    return this.http.get<any>(`${environment.apiUrl}/notifications`).pipe(
      map((page) => page?.content || page),
      tap((notifications: Notification[]) => {
        const unreadCount = notifications.filter(n => !n.isRead).length;
        this.unreadCountSubject.next(unreadCount);
      })
    );
  }

  markAsRead(notificationId: string): Observable<void> {
    return this.http.post<void>(`${environment.apiUrl}/notifications/${notificationId}/read`, {})
      .pipe(tap(() => this.updateUnreadCount()));
  }

  markAllAsRead(): Observable<void> {
    return this.http.post<void>(`${environment.apiUrl}/notifications/mark-all-read`, {})
      .pipe(tap(() => this.unreadCountSubject.next(0)));
  }

  startPolling(intervalMs: number = 30000): void {
    interval(intervalMs)
      .pipe(switchMap(() => this.getNotifications()))
      .subscribe();
  }

  private updateUnreadCount(): void {
    this.http.get<{ unread: number }>(`${environment.apiUrl}/notifications/unread-count`).subscribe({
      next: (res) => this.unreadCountSubject.next(res.unread || 0),
      error: () => {}
    });
  }
}
