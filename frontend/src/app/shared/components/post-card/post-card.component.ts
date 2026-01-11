import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { AvatarPipe } from '../../../core/pipes/avatar.pipe';
import { MatMenuModule } from '@angular/material/menu';
import { RouterModule } from '@angular/router';
import { Post } from '../../../core/models/post.model';
import { AuthService } from '../../../core/services/auth.service';
import { PostService } from '../../../core/services/post.service';
import { MatSnackBar } from '@angular/material/snack-bar';

@Component({
  selector: 'app-post-card',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    AvatarPipe,
    MatMenuModule,
    RouterModule
  ],
  templateUrl: './post-card.component.html',
  styleUrls: ['./post-card.component.css']
})
export class PostCardComponent {
  @Input() post!: Post;
  @Input() showActions = true;
  @Output() like = new EventEmitter<string>();
  @Output() comment = new EventEmitter<string>();
  @Output() edit = new EventEmitter<Post>();
  @Output() delete = new EventEmitter<string>();

  isLikingInProgress = false;

  constructor(
    public authService: AuthService,
    private postService: PostService,
    private snackBar: MatSnackBar
  ) {}

  get isOwner(): boolean {
    const currentUser = this.authService.getCurrentUser();
    return currentUser?.id === this.post.authorId;
  }

  onLike(): void {
    if (this.isLikingInProgress) return;

    this.isLikingInProgress = true;
    if (this.post.isLiked) {
      this.postService.unlikePost(this.post.id).subscribe({
        next: () => {
          this.post.isLiked = false;
          this.post.likesCount--;
          this.isLikingInProgress = false;
        },
        error: () => {
          this.snackBar.open('Failed to unlike post', 'Close', { duration: 2000 });
          this.isLikingInProgress = false;
        }
      });
    } else {
      this.postService.likePost(this.post.id).subscribe({
        next: () => {
          this.post.isLiked = true;
          this.post.likesCount++;
          this.isLikingInProgress = false;
        },
        error: () => {
          this.snackBar.open('Failed to like post', 'Close', { duration: 2000 });
          this.isLikingInProgress = false;
        }
      });
    }
  }

  onComment(): void {
    this.comment.emit(this.post.id);
  }

  onEdit(): void {
    this.edit.emit(this.post);
  }

  onDelete(): void {
    this.delete.emit(this.post.id);
  }

  getTimeSince(dateString: string): string {
    const date = new Date(dateString);
    const seconds = Math.floor((new Date().getTime() - date.getTime()) / 1000);

    let interval = seconds / 31536000;
    if (interval > 1) return Math.floor(interval) + 'y';

    interval = seconds / 2592000;
    if (interval > 1) return Math.floor(interval) + 'mo';

    interval = seconds / 86400;
    if (interval > 1) return Math.floor(interval) + 'd';

    interval = seconds / 3600;
    if (interval > 1) return Math.floor(interval) + 'h';

    interval = seconds / 60;
    if (interval > 1) return Math.floor(interval) + 'm';

    return Math.floor(seconds) + 's';
  }

  getMediaType(url: string): 'image' | 'video' {
    const ext = url.split('.').pop()?.toLowerCase();
    return (ext === 'mp4' || ext === 'webm' || ext === 'ogg' || ext === 'mov') ? 'video' : 'image';
  }
}
