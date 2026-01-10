import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { PostCardComponent } from '../../shared/components/post-card/post-card.component';
import { PostService } from '../../core/services/post.service';
import { Post } from '../../core/models/post.model';
import { CommentDialogComponent } from '../post/comment-dialog/comment-dialog.component';

@Component({
  selector: 'app-feed',
  standalone: true,
  imports: [
    CommonModule,
    PostCardComponent,
    MatProgressSpinnerModule,
    MatButtonModule,
    MatIconModule,
    MatDialogModule,
    MatSnackBarModule
  ],
  templateUrl: './feed.component.html',
  styleUrls: ['./feed.component.css']
})
export class FeedComponent implements OnInit {
  posts: Post[] = [];
  isLoading = false;
  page = 1;
  hasMore = true;

  constructor(
    private postService: PostService,
    private dialog: MatDialog,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.loadPosts();
  }

  loadPosts(): void {
    if (this.isLoading || !this.hasMore) return;

    this.isLoading = true;
    this.postService.getFeedPosts(this.page, 10).subscribe({
      next: (response) => {
        this.posts = [...this.posts, ...response.posts];
        this.hasMore = this.posts.length < response.total;
        this.page++;
        this.isLoading = false;
      },
      error: (error) => {
        this.isLoading = false;
        this.snackBar.open('Failed to load posts', 'Close', { duration: 3000 });
      }
    });
  }

  onLike(postId: string): void {
    const post = this.posts.find(p => p.id === postId);
    if (!post) return;

    const action = post.isLiked
      ? this.postService.unlikePost(postId)
      : this.postService.likePost(postId);

    action.subscribe({
      next: () => {
        post.isLiked = !post.isLiked;
        post.likesCount += post.isLiked ? 1 : -1;
      },
      error: () => {
        this.snackBar.open('Action failed', 'Close', { duration: 3000 });
      }
    });
  }

  onComment(postId: string): void {
    const dialogRef = this.dialog.open(CommentDialogComponent, {
      width: '600px',
      data: { postId }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        const post = this.posts.find(p => p.id === postId);
        if (post) {
          post.commentsCount++;
        }
      }
    });
  }

  onDelete(postId: string): void {
    if (confirm('Are you sure you want to delete this post?')) {
      this.postService.deletePost(postId).subscribe({
        next: () => {
          this.posts = this.posts.filter(p => p.id !== postId);
          this.snackBar.open('Post deleted successfully', 'Close', { duration: 3000 });
        },
        error: () => {
          this.snackBar.open('Failed to delete post', 'Close', { duration: 3000 });
        }
      });
    }
  }

  onEdit(post: Post): void {
    this.snackBar.open('Edit functionality coming soon!', 'Close', { duration: 3000 });
  }
}
