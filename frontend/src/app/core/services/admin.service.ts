import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import { User } from '../models/user.model';
import { Post } from '../models/post.model';

@Injectable({
  providedIn: 'root'
})
export class AdminService {
  constructor(private http: HttpClient) {}

  getAllUsers(): Observable<User[]> {
    return this.http.get<any>(`${environment.apiUrl}/admin/users`).pipe(
      // backend returns a Page object: { content: [...] }
      map((page) => page?.content || page)
    );
  }

  getAllPosts(): Observable<Post[]> {
    return this.http.get<any>(`${environment.apiUrl}/admin/posts`).pipe(
      map((page) => page?.content || page)
    );
  }

  banUser(userId: string): Observable<void> {
    return this.http.put<void>(`${environment.apiUrl}/admin/users/${userId}/ban`, {});
  }

  unbanUser(userId: string): Observable<void> {
    return this.http.put<void>(`${environment.apiUrl}/admin/users/${userId}/unban`, {});
  }

  deletePost(postId: string): Observable<void> {
    return this.http.delete<void>(`${environment.apiUrl}/admin/posts/${postId}`);
  }

  hidePost(postId: string): Observable<void> {
    return this.http.put<void>(`${environment.apiUrl}/admin/posts/${postId}/hide`, {});
  }

  unhidePost(postId: string): Observable<void> {
    return this.http.put<void>(`${environment.apiUrl}/admin/posts/${postId}/unhide`, {});
  }
}
