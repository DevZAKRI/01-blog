import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import { User } from '../models/user.model';
import { Post } from '../models/post.model';
import { Report } from '../models/report.model';

export interface AdminStats {
  totalUsers: number;
  totalPosts: number;
  pendingReports: number;
  bannedUsers: number;
  hiddenPosts: number;
}

@Injectable({
  providedIn: 'root'
})
export class AdminService {
  private readonly apiUrl = `${environment.apiUrl}/admin`;

  constructor(private http: HttpClient) {}

  // ==================== STATS ====================

  getStats(): Observable<AdminStats> {
    return this.http.get<AdminStats>(`${this.apiUrl}/stats`);
  }

  // ==================== USERS ====================

  getAllUsers(page = 0, size = 20): Observable<User[]> {
    return this.http.get<any>(`${this.apiUrl}/users`, { params: { page: page.toString(), size: size.toString() } }).pipe(
      map((response) => response?.content || response)
    );
  }

  getUser(userId: string): Observable<User> {
    return this.http.get<User>(`${this.apiUrl}/users/${userId}`);
  }

  banUser(userId: string): Observable<any> {
    return this.http.put<any>(`${this.apiUrl}/users/${userId}/ban`, {});
  }

  unbanUser(userId: string): Observable<any> {
    return this.http.put<any>(`${this.apiUrl}/users/${userId}/unban`, {});
  }

  deleteUser(userId: string): Observable<any> {
    return this.http.delete<any>(`${this.apiUrl}/users/${userId}`);
  }

  updateUserRole(userId: string, role: 'USER' | 'ADMIN'): Observable<any> {
    return this.http.put<any>(`${this.apiUrl}/users/${userId}/role`, { role });
  }

  // ==================== POSTS ====================

  getAllPosts(page = 0, size = 20): Observable<Post[]> {
    return this.http.get<any>(`${this.apiUrl}/posts`, { params: { page: page.toString(), size: size.toString() } }).pipe(
      map((response) => response?.content || response)
    );
  }

  getPost(postId: string): Observable<Post> {
    return this.http.get<Post>(`${this.apiUrl}/posts/${postId}`);
  }

  hidePost(postId: string): Observable<any> {
    return this.http.put<any>(`${this.apiUrl}/posts/${postId}/hide`, {});
  }

  unhidePost(postId: string): Observable<any> {
    return this.http.put<any>(`${this.apiUrl}/posts/${postId}/unhide`, {});
  }

  deletePost(postId: string): Observable<any> {
    return this.http.delete<any>(`${this.apiUrl}/posts/${postId}`);
  }

  // ==================== REPORTS ====================

  getAllReports(page = 0, size = 20, status?: string): Observable<Report[]> {
    const params: any = { page: page.toString(), size: size.toString() };
    if (status) {
      params.status = status;
    }
    return this.http.get<any>(`${this.apiUrl}/reports`, { params }).pipe(
      map((response) => response?.content || response)
    );
  }

  getReport(reportId: string): Observable<Report> {
    return this.http.get<Report>(`${this.apiUrl}/reports/${reportId}`);
  }

  updateReportStatus(reportId: string, status: string): Observable<Report> {
    return this.http.patch<Report>(`${this.apiUrl}/reports/${reportId}`, { status });
  }

  deleteReport(reportId: string): Observable<any> {
    return this.http.delete<any>(`${this.apiUrl}/reports/${reportId}`);
  }

  banReportedUser(reportId: string): Observable<any> {
    return this.http.put<any>(`${this.apiUrl}/reports/${reportId}/ban-user`, {});
  }
}
