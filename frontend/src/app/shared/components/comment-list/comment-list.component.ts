import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatListModule } from '@angular/material/list';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { RouterModule } from '@angular/router';
import { Comment } from '../../../core/models/comment.model';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-comment-list',
  standalone: true,
  imports: [
    CommonModule,
    MatListModule,
    MatButtonModule,
    MatIconModule,
    RouterModule
  ],
  templateUrl: './comment-list.component.html',
  styleUrls: ['./comment-list.component.css']
})
export class CommentListComponent {
  @Input() comments: Comment[] = [];
  @Output() delete = new EventEmitter<string>();

  constructor(public authService: AuthService) {}

  isOwner(comment: Comment): boolean {
    const currentUser = this.authService.getCurrentUser();
    return currentUser?.id === comment.userId;
  }

  onDelete(commentId: string): void {
    this.delete.emit(commentId);
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
