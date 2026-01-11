import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import { AuthResponse, LoginRequest, RegisterRequest, User } from '../models/user.model';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private currentUserSubject = new BehaviorSubject<User | null>(null);
  public currentUser$ = this.currentUserSubject.asObservable();

  constructor(private http: HttpClient) {
    this.loadUserFromStorage();
  }

  private loadUserFromStorage(): void {
    const token = localStorage.getItem('accessToken');
    const userStr = localStorage.getItem('currentUser');

    if (token && userStr) {
      try {
        const user = JSON.parse(userStr);
        this.currentUserSubject.next(user);
      } catch (e) {
        this.logout();
      }
    }
  }

  register(data: RegisterRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${environment.apiUrl}/auth/register`, data)
      .pipe(tap(response => this.handleAuth(response)));
  }

  login(data: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${environment.apiUrl}/auth/login`, data)
      .pipe(tap(response => this.handleAuth(response)));
  }

  logout(): void {
    localStorage.removeItem('accessToken');
    localStorage.removeItem('currentUser');
    this.currentUserSubject.next(null);
  }

  getToken(): string | null {
    return localStorage.getItem('accessToken');
  }

  getCurrentUser(): User | null {
    return this.currentUserSubject.value;
  }

  isAuthenticated(): boolean {
    return !!this.getToken();
  }

  isAdmin(): boolean {
    const user = this.getCurrentUser();
    return user?.role === 'ADMIN';
  }

  private handleAuth(response: Partial<AuthResponse> & any): void {
    // backend previously returned { token }, newer version returns { accessToken, user }
    const token = response.accessToken ?? response.token;
    const user = response.user ?? null;

    if (token) {
      localStorage.setItem('accessToken', token);
    }

    if (user) {
      // normalize avatar field coming from backend: support both avatarUrl and avatar
      if (!user.avatar && user.avatarUrl) user.avatar = user.avatarUrl;
      // if it's a server-relative path like /uploads/..., prefix with apiUrl
      if (user.avatar && user.avatar.startsWith('/uploads')) user.avatar = environment.apiUrl + user.avatar;

      localStorage.setItem('currentUser', JSON.stringify(user));
      this.currentUserSubject.next(user);
    } else {
      // clear user if not provided
      localStorage.removeItem('currentUser');
      this.currentUserSubject.next(null);
    }
  }
}
