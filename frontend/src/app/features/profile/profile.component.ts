import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { PostCardComponent } from '../../shared/components/post-card/post-card.component';
import { UserService } from '../../core/services/user.service';
import { PostService } from '../../core/services/post.service';
import { ReportService } from '../../core/services/report.service';
import { AuthService } from '../../core/services/auth.service';
import { User } from '../../core/models/user.model';
import { Post } from '../../core/models/post.model';
import { CreatePostDialogComponent } from '../post/create-post-dialog/create-post-dialog.component';
import { CommentDialogComponent } from '../post/comment-dialog/comment-dialog.component';
import { ReportDialogComponent } from '../report/report-dialog/report-dialog.component';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatDialogModule,
    MatSnackBarModule,
    PostCardComponent
  ],
  templateUrl: './profile.component.html',
  styleUrls: ['./profile.component.css']
})
export class ProfileComponent implements OnInit {
  user: User | null = null;
  posts: Post[] = [];
  isLoading = true;
  isSubscribed = false;
  isOwner = false;

  constructor(
    private route: ActivatedRoute,
    private userService: UserService,
    private postService: PostService,
    private reportService: ReportService,
    private authService: AuthService,
    private dialog: MatDialog,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      const userId = params['id'];
      this.loadProfile(userId);
    });
  }

  loadProfile(userId: string): void {
    this.isLoading = true;
    const currentUser = this.authService.getCurrentUser();
    this.isOwner = currentUser?.id === userId;

    this.userService.getUser(userId).subscribe({
      next: (user) => {
        this.user = user;
        this.loadPosts(userId);
        if (!this.isOwner) {
          this.checkSubscription(userId);
        }
      },
      error: () => {
        this.isLoading = false;
        this.snackBar.open('Failed to load profile', 'Close', { duration: 3000 });
      }
    });
  }

  loadPosts(userId: string): void {
    this.postService.getUserPosts(userId).subscribe({
      next: (response) => {
        this.posts = response.posts;
        this.isLoading = false;
      },
      error: () => {
        this.isLoading = false;
      }
    });
  }

  checkSubscription(userId: string): void {
    this.userService.isSubscribed(userId).subscribe({
      next: (result) => {
        this.isSubscribed = result.subscribed;
      }
    });
  }

  onSubscribe(): void {
    if (!this.user) return;

    const action = this.isSubscribed
      ? this.userService.unsubscribe(this.user.id)
      : this.userService.subscribe(this.user.id);

    action.subscribe({
      next: () => {
        this.isSubscribed = !this.isSubscribed;
        this.snackBar.open(
          this.isSubscribed ? 'Subscribed successfully' : 'Unsubscribed successfully',
          'Close',
          { duration: 3000 }
        );
      },
      error: () => {
        this.snackBar.open('Action failed', 'Close', { duration: 3000 });
      }
    });
  }

  onCreatePost(): void {
    const dialogRef = this.dialog.open(CreatePostDialogComponent, {
      width: '600px'
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.posts.unshift(result);
      }
    });
  }

  onReport(): void {
    if (!this.user) return;

    const dialogRef = this.dialog.open(ReportDialogComponent, {
      width: '500px',
      data: { userId: this.user.id, username: this.user.username }
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
