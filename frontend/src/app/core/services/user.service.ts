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
    return this.http.get<User>(`${environment.apiUrl}/users/${userId}`);
  }

  updateProfile(data: Partial<User>): Observable<User> {
    return this.http.put<User>(`${environment.apiUrl}/users/me`, data);
  }

  subscribe(userId: string): Observable<void> {
    return this.http.post<void>(`${environment.apiUrl}/users/${userId}/subscribe`, {});
  }

  unsubscribe(userId: string): Observable<void> {
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
