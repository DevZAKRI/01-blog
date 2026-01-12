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
import { UploadService } from '../../../core/services/upload.service';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { ReportPostDialogComponent } from '../report-post-dialog/report-post-dialog.component';

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
    RouterModule,
    MatDialogModule
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
  @Output() report = new EventEmitter<string>();

  isLikingInProgress = false;

  // Lightbox state
  lightboxOpen = false;
  lightboxUrl = '';
  lightboxType: 'image' | 'video' = 'image';
  lightboxIndex = 0;

  constructor(
    public authService: AuthService,
    private postService: PostService,
    private uploadService: UploadService,
    private snackBar: MatSnackBar,
    private dialog: MatDialog
  ) {}

  get isOwner(): boolean {
    const currentUser = this.authService.getCurrentUser();
    return currentUser?.id === this.post.authorId;
  }

  isAuthorAdmin(): boolean {
    return this.post.author?.role === 'ADMIN';
  }

  onLike(): void {
    if (this.isLikingInProgress) return;

    // Don't allow liking hidden posts
    if (this.post.hidden) {
      this.snackBar.open('Cannot interact with hidden posts', 'Close', { duration: 2000 });
      return;
    }

    this.isLikingInProgress = true;
    const wasLiked = this.post.isLiked;

    this.postService.toggleLike(this.post.id).subscribe({
      next: (res) => {
        this.post.isLiked = res.liked;
        this.post.likesCount += res.liked ? 1 : -1;
        this.isLikingInProgress = false;
      },
      error: () => {
        this.snackBar.open('Failed to update like', 'Close', { duration: 2000 });
        this.isLikingInProgress = false;
      }
    });
  }

  onComment(): void {
    // Don't allow commenting on hidden posts
    if (this.post.hidden) {
      this.snackBar.open('Cannot interact with hidden posts', 'Close', { duration: 2000 });
      return;
    }
    this.comment.emit(this.post.id);
  }

  onEdit(): void {
    this.edit.emit(this.post);
  }

  onDelete(): void {
    this.delete.emit(this.post.id);
  }

  onReport(): void {
    const dialogRef = this.dialog.open(ReportPostDialogComponent, {
      width: '450px',
      data: { postId: this.post.id, postTitle: this.post.title }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.report.emit(this.post.id);
      }
    });
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
    return this.uploadService.getMediaTypeFromUrl(url);
  }

  /**
   * Get the full URL for a media item
   */
  getMediaUrl(url: string): string {
    return this.uploadService.getFullUrl(url);
  }

  onImageError(event: Event): void {
    const img = event.target as HTMLImageElement;
    img.src = 'data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHdpZHRoPSIyMDAiIGhlaWdodD0iMjAwIiB2aWV3Qm94PSIwIDAgMjAwIDIwMCI+PHJlY3QgZmlsbD0iI2VlZSIgd2lkdGg9IjIwMCIgaGVpZ2h0PSIyMDAiLz48dGV4dCBmaWxsPSIjOTk5IiBmb250LWZhbWlseT0iQXJpYWwiIGZvbnQtc2l6ZT0iMTQiIHRleHQtYW5jaG9yPSJtaWRkbGUiIHg9IjEwMCIgeT0iMTAwIj5JbWFnZSBub3QgYXZhaWxhYmxlPC90ZXh0Pjwvc3ZnPg==';
    img.alt = 'Image not available';
  }

  onVideoError(event: Event): void {
    const video = event.target as HTMLVideoElement;
    video.style.display = 'none';
    // Show a placeholder instead
    const parent = video.parentElement;
    if (parent && !parent.querySelector('.video-error')) {
      const placeholder = document.createElement('div');
      placeholder.className = 'video-error';
      placeholder.innerHTML = '<span>Video not available</span>';
      parent.appendChild(placeholder);
    }
  }

  isValidUrl(url: string): boolean {
    return !!url && url.trim().length > 0 && (url.startsWith('http') || url.startsWith('/'));
  }

  // Lightbox methods
  openLightbox(index: number): void {
    if (!this.post.mediaUrls || index >= this.post.mediaUrls.length) return;
    const url = this.post.mediaUrls[index];
    this.lightboxUrl = this.getMediaUrl(url);
    this.lightboxType = this.getMediaType(url);
    this.lightboxIndex = index;
    this.lightboxOpen = true;
    document.body.style.overflow = 'hidden';
  }

  closeLightbox(): void {
    this.lightboxOpen = false;
    this.lightboxUrl = '';
    document.body.style.overflow = '';
  }

  prevMedia(event: Event): void {
    event.stopPropagation();
    if (!this.post.mediaUrls) return;
    const newIndex = this.lightboxIndex - 1;
    if (newIndex >= 0) {
      this.openLightbox(newIndex);
    }
  }

  nextMedia(event: Event): void {
    event.stopPropagation();
    if (!this.post.mediaUrls) return;
    const newIndex = this.lightboxIndex + 1;
    if (newIndex < this.post.mediaUrls.length) {
      this.openLightbox(newIndex);
    }
  }

  onLightboxBackdropClick(event: MouseEvent): void {
    if ((event.target as HTMLElement).classList.contains('lightbox-overlay')) {
      this.closeLightbox();
    }
  }
}
