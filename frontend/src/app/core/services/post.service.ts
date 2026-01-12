import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import { Post, CreatePostRequest, UpdatePostRequest } from '../models/post.model';

@Injectable({
  providedIn: 'root'
})
export class PostService {
  constructor(private http: HttpClient) {}

  getFeedPosts(page: number = 0, limit: number = 20): Observable<{ posts: Post[], total: number }> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', limit.toString());
    return this.http.get<any>(`${environment.apiUrl}/feed`, { params }).pipe(
      map((pageResp) => ({ posts: pageResp?.content || pageResp, total: pageResp?.totalElements ?? 0 }))
    );
  }

  getUserPosts(userId: string, page: number = 0, limit: number = 20): Observable<{ posts: Post[], total: number }> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', limit.toString());
    return this.http.get<any>(`${environment.apiUrl}/users/${userId}/posts`, { params }).pipe(
      map((pageResp) => ({ posts: pageResp?.content || pageResp, total: pageResp?.totalElements ?? 0 }))
    );
  }

  getPost(postId: string): Observable<Post> {
    return this.http.get<Post>(`${environment.apiUrl}/posts/${postId}`);
  }

  createPost(data: CreatePostRequest): Observable<Post> {
    return this.http.post<Post>(`${environment.apiUrl}/posts`, data);
  }

  updatePost(postId: string, data: UpdatePostRequest): Observable<Post> {
    return this.http.put<Post>(`${environment.apiUrl}/posts/${postId}`, data);
  }

  deletePost(postId: string): Observable<void> {
    return this.http.delete<void>(`${environment.apiUrl}/posts/${postId}`);
  }

  toggleLike(postId: string): Observable<{ liked: boolean }> {
    return this.http.post<{ liked: boolean }>(`${environment.apiUrl}/posts/${postId}/like`, {});
  }

  likePost(postId: string): Observable<void> {
    return this.http.post<void>(`${environment.apiUrl}/posts/${postId}/like`, {});
  }

  unlikePost(postId: string): Observable<void> {
    return this.http.delete<void>(`${environment.apiUrl}/posts/${postId}/like`);
  }
}
