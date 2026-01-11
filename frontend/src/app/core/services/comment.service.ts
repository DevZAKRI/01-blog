import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import { Comment, CreateCommentRequest } from '../models/comment.model';

@Injectable({
  providedIn: 'root'
})
export class CommentService {
  constructor(private http: HttpClient) {}

  getComments(postId: string, page: number = 0, size: number = 20): Observable<{ comments: Comment[], total: number }> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    return this.http.get<any>(`${environment.apiUrl}/posts/${postId}/comments`, { params }).pipe(
      map((response) => ({
        comments: response?.content || response,
        total: response?.totalElements ?? 0
      }))
    );
  }

  createComment(postId: string, data: CreateCommentRequest): Observable<Comment> {
    return this.http.post<Comment>(`${environment.apiUrl}/posts/${postId}/comments`, data);
  }

  deleteComment(postId: string, commentId: string): Observable<void> {
    return this.http.delete<void>(`${environment.apiUrl}/posts/${postId}/comments/${commentId}`);
  }

  likeComment(postId: string, commentId: string): Observable<void> {
    return this.http.post<void>(`${environment.apiUrl}/posts/${postId}/comments/${commentId}/like`, {});
  }

  unlikeComment(postId: string, commentId: string): Observable<void> {
    return this.http.delete<void>(`${environment.apiUrl}/posts/${postId}/comments/${commentId}/like`);
  }

  getCommentLikesCount(postId: string, commentId: string): Observable<number> {
    return this.http.get<number>(`${environment.apiUrl}/posts/${postId}/comments/${commentId}/likes/count`);
  }
}
