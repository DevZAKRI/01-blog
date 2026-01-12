import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatMenuModule } from '@angular/material/menu';
import { PostService } from '../../../core/services/post.service';
import { Post } from '../../../core/models/post.model';
import { AvatarPipe } from '../../../core/pipes/avatar.pipe';
import { CommentDialogComponent } from '../comment-dialog/comment-dialog.component';
import { EditPostDialogComponent } from '../edit-post-dialog/edit-post-dialog.component';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-post-detail',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatDialogModule,
    MatSnackBarModule,
    MatMenuModule,
    AvatarPipe
  ],
  templateUrl: './post-detail.component.html',
  styleUrls: ['./post-detail.component.css']
})
export class PostDetailComponent implements OnInit {
  post: Post | null = null;
  isLoading = true;
  isOwner = false;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private postService: PostService,
    private authService: AuthService,
    private dialog: MatDialog,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      const postId = params['id'];
      this.loadPost(postId);
    });
  }

  loadPost(postId: string): void {
    this.isLoading = true;
    this.postService.getPost(postId).subscribe({
      next: (post) => {
        this.post = post;
        this.isOwner = this.authService.getCurrentUser()?.id?.toString() === post.authorId;
        this.isLoading = false;
      },
      error: () => {
        this.isLoading = false;
        this.snackBar.open('Failed to load post', 'Close', { duration: 3000 });
        this.router.navigate(['/']);
      }
    });
  }

  onLike(): void {
    if (!this.post) return;

    const action = this.post.isLiked
      ? this.postService.unlikePost(this.post.id)
      : this.postService.likePost(this.post.id);

    action.subscribe({
      next: () => {
        if (this.post) {
          this.post.isLiked = !this.post.isLiked;
          this.post.likesCount += this.post.isLiked ? 1 : -1;
        }
      },
      error: () => {
        this.snackBar.open('Action failed', 'Close', { duration: 3000 });
      }
    });
  }

  onComment(): void {
    if (!this.post) return;
    const dialogRef = this.dialog.open(CommentDialogComponent, {
      width: '600px',
      data: { postId: this.post.id }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result && this.post) {
        this.post.commentsCount++;
      }
    });
  }

  onEdit(): void {
    if (!this.post) return;
    const dialogRef = this.dialog.open(EditPostDialogComponent, {
      width: '600px',
      data: { post: this.post }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.loadPost(this.post!.id);
      }
    });
  }

  onDelete(): void {
    if (!this.post) return;
    if (confirm('Are you sure you want to delete this post?')) {
      this.postService.deletePost(this.post.id).subscribe({
        next: () => {
          this.snackBar.open('Post deleted successfully', 'Close', { duration: 3000 });
          this.router.navigate(['/']);
        },
        error: () => {
          this.snackBar.open('Failed to delete post', 'Close', { duration: 3000 });
        }
      });
    }
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
    if (!url) return 'image';
    const ext = url.split('.').pop()?.toLowerCase();
    return (ext === 'mp4' || ext === 'webm' || ext === 'ogg' || ext === 'mov') ? 'video' : 'image';
  }

  onImageError(event: Event): void {
    const img = event.target as HTMLImageElement;
    img.src = 'data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHdpZHRoPSIyMDAiIGhlaWdodD0iMjAwIiB2aWV3Qm94PSIwIDAgMjAwIDIwMCI+PHJlY3QgZmlsbD0iI2VlZSIgd2lkdGg9IjIwMCIgaGVpZ2h0PSIyMDAiLz48dGV4dCBmaWxsPSIjOTk5IiBmb250LWZhbWlseT0iQXJpYWwiIGZvbnQtc2l6ZT0iMTQiIHRleHQtYW5jaG9yPSJtaWRkbGUiIHg9IjEwMCIgeT0iMTAwIj5JbWFnZSBub3QgYXZhaWxhYmxlPC90ZXh0Pjwvc3ZnPg==';
    img.alt = 'Image not available';
  }

  onVideoError(event: Event): void {
    const video = event.target as HTMLVideoElement;
    video.style.display = 'none';
    const parent = video.parentElement;
    if (parent && !parent.querySelector('.video-error')) {
      const placeholder = document.createElement('div');
      placeholder.className = 'video-error';
      placeholder.innerHTML = '<span>Video not available</span>';
      parent.appendChild(placeholder);
    }
  }

  goBack(): void {
    this.router.navigate(['/']);
  }
}
