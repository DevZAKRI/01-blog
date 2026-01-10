import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatTabsModule } from '@angular/material/tabs';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatTooltipModule } from '@angular/material/tooltip';
import { AdminService } from '../../core/services/admin.service';
import { ReportService } from '../../core/services/report.service';
import { User } from '../../core/models/user.model';
import { Post } from '../../core/models/post.model';
import { Report } from '../../core/models/report.model';

@Component({
  selector: 'app-admin',
  standalone: true,
  imports: [
    CommonModule,
    MatTabsModule,
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatSnackBarModule,
    MatTooltipModule
  ],
  templateUrl: './admin.component.html',
  styleUrls: ['./admin.component.css']
})
export class AdminComponent implements OnInit {
  users: User[] = [];
  posts: Post[] = [];
  reports: Report[] = [];
  isLoadingUsers = true;
  isLoadingPosts = true;
  isLoadingReports = true;

  userColumns = ['username', 'email', 'role', 'status', 'createdAt', 'actions'];
  postColumns = ['content', 'user', 'likesCount', 'commentsCount', 'status', 'createdAt', 'actions'];
  reportColumns = ['reporter', 'reportedUser', 'reason', 'status', 'createdAt', 'actions'];

  constructor(
    private adminService: AdminService,
    private reportService: ReportService,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.loadUsers();
    this.loadPosts();
    this.loadReports();
  }

  loadUsers(): void {
    this.isLoadingUsers = true;
    this.adminService.getAllUsers().subscribe({
      next: (users) => {
        this.users = users;
        this.isLoadingUsers = false;
      },
      error: () => {
        this.isLoadingUsers = false;
        this.snackBar.open('Failed to load users', 'Close', { duration: 3000 });
      }
    });
  }

  loadPosts(): void {
    this.isLoadingPosts = true;
    this.adminService.getAllPosts().subscribe({
      next: (posts) => {
        this.posts = posts;
        this.isLoadingPosts = false;
      },
      error: () => {
        this.isLoadingPosts = false;
        this.snackBar.open('Failed to load posts', 'Close', { duration: 3000 });
      }
    });
  }

  loadReports(): void {
    this.isLoadingReports = true;
    this.reportService.getReports().subscribe({
      next: (reports) => {
        this.reports = reports;
        this.isLoadingReports = false;
      },
      error: () => {
        this.isLoadingReports = false;
        this.snackBar.open('Failed to load reports', 'Close', { duration: 3000 });
      }
    });
  }

  banUser(userId: string): void {
    if (confirm('Are you sure you want to ban this user?')) {
      this.adminService.banUser(userId).subscribe({
        next: () => {
          const user = this.users.find(u => u.id === userId);
          if (user) user.banned = true;
          this.snackBar.open('User banned successfully', 'Close', { duration: 3000 });
        },
        error: () => {
          this.snackBar.open('Failed to ban user', 'Close', { duration: 3000 });
        }
      });
    }
  }

  unbanUser(userId: string): void {
    if (confirm('Are you sure you want to unban this user?')) {
      this.adminService.unbanUser(userId).subscribe({
        next: () => {
          const user = this.users.find(u => u.id === userId);
          if (user) user.banned = false;
          this.snackBar.open('User unbanned successfully', 'Close', { duration: 3000 });
        },
        error: () => {
          this.snackBar.open('Failed to unban user', 'Close', { duration: 3000 });
        }
      });
    }
  }

  deletePost(postId: string): void {
    if (confirm('Are you sure you want to delete this post?')) {
      this.adminService.deletePost(postId).subscribe({
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

  // User hide/unhide removed: use ban/unban instead

  hidePost(postId: string): void {
    this.adminService.hidePost(postId).subscribe({
      next: () => {
        const post = this.posts.find(p => p.id === postId);
        if (post) {
          post.hidden = true;
        }
        this.snackBar.open('Post hidden successfully', 'Close', { duration: 3000 });
      },
      error: () => {
        this.snackBar.open('Failed to hide post', 'Close', { duration: 3000 });
      }
    });
  }

  unhidePost(postId: string): void {
    this.adminService.unhidePost(postId).subscribe({
      next: () => {
        const post = this.posts.find(p => p.id === postId);
        if (post) {
          post.hidden = false;
        }
        this.snackBar.open('Post unhidden successfully', 'Close', { duration: 3000 });
      },
      error: () => {
        this.snackBar.open('Failed to unhide post', 'Close', { duration: 3000 });
      }
    });
  }

  updateReportStatus(reportId: string, status: string): void {
    this.reportService.updateReportStatus(reportId, status).subscribe({
      next: () => {
        const report = this.reports.find(r => r.id === reportId);
        if (report) {
          report.status = status as any;
        }
        this.snackBar.open('Report status updated', 'Close', { duration: 3000 });
      },
      error: () => {
        this.snackBar.open('Failed to update report', 'Close', { duration: 3000 });
      }
    });
  }
}
