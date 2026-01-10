import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Comment, CreateCommentRequest } from '../models/comment.model';

@Injectable({
  providedIn: 'root'
})
export class CommentService {
  constructor(private http: HttpClient) {}

  getComments(postId: string): Observable<Comment[]> {
    return this.http.get<Comment[]>(`${environment.apiUrl}/posts/${postId}/comments`);
  }

  createComment(postId: string, data: CreateCommentRequest): Observable<Comment> {
    return this.http.post<Comment>(`${environment.apiUrl}/posts/${postId}/comments`, data);
  }

  deleteComment(postId: string, commentId: string): Observable<void> {
    return this.http.delete<void>(`${environment.apiUrl}/posts/${postId}/comments/${commentId}`);
  }
}
