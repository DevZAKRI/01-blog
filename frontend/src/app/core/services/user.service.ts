import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import { User } from '../models/user.model';
import { AuthService } from './auth.service';

@Injectable({
  providedIn: 'root'
})
export class UserService {
  constructor(private http: HttpClient, private authService: AuthService) {}

  getUser(userId: string): Observable<User> {
    return this.http.get<any>(`${environment.apiUrl}/users/${userId}`).pipe(
      map((user) => {
        if (user) {
          if (!user.avatar && user.avatarUrl) user.avatar = user.avatarUrl;
          if (user.avatar && user.avatar.startsWith('/uploads')) {
            const apiRoot = environment.apiUrl.replace(/\/api\/v1\/?$/, '');
            user.avatar = apiRoot + user.avatar;
          }
        }
        return user as User;
      })
    );
  }

  getUsers(page: number = 0, size: number = 20): Observable<any> {
    return this.http.get<any>(`${environment.apiUrl}/users?page=${page}&size=${size}`).pipe(
      map((response) => {
        if (response && response.content) {
          response.content = response.content.map((user: any) => {
            if (user) {
              if (!user.avatar && user.avatarUrl) user.avatar = user.avatarUrl;
              if (user.avatar && user.avatar.startsWith('/uploads')) {
                const apiRoot = environment.apiUrl.replace(/\/api\/v1\/?$/, '');
                user.avatar = apiRoot + user.avatar;
              }
            }
            return user;
          });
        }
        return response;
      })
    );
  }

  updateProfile(data: Partial<User>): Observable<User> {
    return this.http.put<User>(`${environment.apiUrl}/users/me`, data);
  }

  subscribe(userId: string | number): Observable<void> {
    return this.http.post<void>(`${environment.apiUrl}/users/${userId}/subscribe`, {});
  }

  unsubscribe(userId: string | number): Observable<void> {
    return this.http.post<void>(`${environment.apiUrl}/users/${userId}/unsubscribe`, {});
  }

  isSubscribed(userId: string): Observable<{ subscribed: boolean }> {
    return this.http.get<User>(`${environment.apiUrl}/users/${userId}`).pipe(
      map((user) => {
        const currentUser = this.authService.getCurrentUser();
        const subs = (user as any).subscriberIds || [];
        const subscribed = !!(currentUser && subs.map(String).includes(String(currentUser.id)));
        return { subscribed };
      })
    );
  }
}
