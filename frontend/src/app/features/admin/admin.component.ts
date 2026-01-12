import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatTabsModule } from '@angular/material/tabs';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatCardModule } from '@angular/material/card';
import { MatChipsModule } from '@angular/material/chips';
import { MatMenuModule } from '@angular/material/menu';
import { MatDialogModule } from '@angular/material/dialog';
import { MatSelectModule } from '@angular/material/select';
import { MatDividerModule } from '@angular/material/divider';
import { Router } from '@angular/router';
import { AdminService, AdminStats } from '../../core/services/admin.service';
import { AuthService } from '../../core/services/auth.service';
import { User } from '../../core/models/user.model';
import { Post } from '../../core/models/post.model';
import { Report } from '../../core/models/report.model';

@Component({
  selector: 'app-admin',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatTabsModule,
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatSnackBarModule,
    MatTooltipModule,
    MatCardModule,
    MatChipsModule,
    MatMenuModule,
    MatDialogModule,
    MatSelectModule,
    MatDividerModule
  ],
  templateUrl: './admin.component.html',
  styleUrls: ['./admin.component.css']
})
export class AdminComponent implements OnInit {
  // Stats
  stats: AdminStats | null = null;
  isLoadingStats = true;

  // Data
  users: User[] = [];
  posts: Post[] = [];
  reports: Report[] = [];

  // Loading states
  isLoadingUsers = false;
  isLoadingPosts = false;
  isLoadingReports = false;

  // Report filter
  reportStatusFilter = '';

  // Table columns
  userColumns = ['avatar', 'username', 'email', 'role', 'status', 'createdAt', 'actions'];
  postColumns = ['content', 'author', 'stats', 'status', 'createdAt', 'actions'];
  reportColumns = ['reporter', 'reportedUser', 'reason', 'status', 'createdAt', 'actions'];

  // Current user (to prevent self-actions)
  currentUserId: string | null = null;

  constructor(
    private adminService: AdminService,
    private authService: AuthService,
    private router: Router,
    private snackBar: MatSnackBar
  ) {
    const currentUser = this.authService.getCurrentUser();
    this.currentUserId = currentUser?.id || null;
  }

  ngOnInit(): void {
    this.loadStats();
    this.loadUsers();
    this.loadPosts();
    this.loadReports();
  }

  // ==================== STATS ====================

  loadStats(): void {
    this.isLoadingStats = true;
    this.adminService.getStats().subscribe({
      next: (stats) => {
        this.stats = stats;
        this.isLoadingStats = false;
      },
      error: () => {
        this.isLoadingStats = false;
        this.showMessage('Failed to load stats', true);
      }
    });
  }

  // ==================== USERS ====================

  loadUsers(): void {
    this.isLoadingUsers = true;
    this.adminService.getAllUsers().subscribe({
      next: (users) => {
        this.users = users;
        this.isLoadingUsers = false;
      },
      error: () => {
        this.isLoadingUsers = false;
        this.showMessage('Failed to load users', true);
      }
    });
  }

  banUser(userId: string): void {
    if (!confirm('Are you sure you want to ban this user? They will not be able to log in.')) return;

    this.adminService.banUser(userId).subscribe({
      next: () => {
        const user = this.users.find(u => u.id === userId);
        if (user) user.banned = true;
        this.showMessage('User banned successfully');
        this.loadStats();
      },
      error: () => this.showMessage('Failed to ban user', true)
    });
  }

  unbanUser(userId: string): void {
    if (!confirm('Are you sure you want to unban this user?')) return;

    this.adminService.unbanUser(userId).subscribe({
      next: () => {
        const user = this.users.find(u => u.id === userId);
        if (user) user.banned = false;
        this.showMessage('User unbanned successfully');
        this.loadStats();
      },
      error: () => this.showMessage('Failed to unban user', true)
    });
  }

  deleteUser(userId: string): void {
    if (!confirm('⚠️ WARNING: This will permanently delete the user and ALL their data (posts, comments, likes, etc.). This action cannot be undone. Continue?')) return;

    this.adminService.deleteUser(userId).subscribe({
      next: () => {
        this.users = this.users.filter(u => u.id !== userId);
        this.showMessage('User deleted successfully');
        this.loadStats();
        this.loadPosts(); // Refresh posts as some might have been deleted
      },
      error: (err) => this.showMessage(err.error?.error || 'Failed to delete user', true)
    });
  }

  toggleUserRole(user: User): void {
    const newRole = user.role === 'ADMIN' ? 'USER' : 'ADMIN';
    if (!confirm(`Change ${user.username}'s role to ${newRole}?`)) return;

    this.adminService.updateUserRole(user.id, newRole).subscribe({
      next: () => {
        user.role = newRole as any;
        this.showMessage(`User role updated to ${newRole}`);
      },
      error: () => this.showMessage('Failed to update user role', true)
    });
  }

  // ==================== POSTS ====================

  loadPosts(): void {
    this.isLoadingPosts = true;
    this.adminService.getAllPosts().subscribe({
      next: (posts) => {
        this.posts = posts;
        this.isLoadingPosts = false;
      },
      error: () => {
        this.isLoadingPosts = false;
        this.showMessage('Failed to load posts', true);
      }
    });
  }

  hidePost(postId: string): void {
    this.adminService.hidePost(postId).subscribe({
      next: () => {
        const post = this.posts.find(p => p.id === postId);
        if (post) post.hidden = true;
        this.showMessage('Post hidden successfully');
        this.loadStats();
      },
      error: () => this.showMessage('Failed to hide post', true)
    });
  }

  unhidePost(postId: string): void {
    this.adminService.unhidePost(postId).subscribe({
      next: () => {
        const post = this.posts.find(p => p.id === postId);
        if (post) post.hidden = false;
        this.showMessage('Post unhidden successfully');
        this.loadStats();
      },
      error: () => this.showMessage('Failed to unhide post', true)
    });
  }

  deletePost(postId: string): void {
    if (!confirm('Are you sure you want to permanently delete this post?')) return;

    this.adminService.deletePost(postId).subscribe({
      next: () => {
        this.posts = this.posts.filter(p => p.id !== postId);
        this.showMessage('Post deleted successfully');
        this.loadStats();
      },
      error: () => this.showMessage('Failed to delete post', true)
    });
  }

  // ==================== REPORTS ====================

  loadReports(): void {
    this.isLoadingReports = true;
    this.adminService.getAllReports(0, 20, this.reportStatusFilter || undefined).subscribe({
      next: (reports) => {
        this.reports = reports;
        this.isLoadingReports = false;
      },
      error: () => {
        this.isLoadingReports = false;
        this.showMessage('Failed to load reports', true);
      }
    });
  }

  onReportFilterChange(): void {
    this.loadReports();
  }

  updateReportStatus(reportId: string, status: string): void {
    this.adminService.updateReportStatus(reportId, status).subscribe({
      next: (report) => {
        const r = this.reports.find(rep => rep.id === reportId);
        if (r) r.status = status as any;
        this.showMessage(`Report marked as ${status}`);
        this.loadStats();
      },
      error: () => this.showMessage('Failed to update report', true)
    });
  }

  banReportedUser(reportId: string): void {
    const report = this.reports.find(r => r.id === reportId);
    if (!report?.reportedUser) return;

    if (!confirm(`Ban user ${report.reportedUser.username} and resolve this report?`)) return;

    this.adminService.banReportedUser(reportId).subscribe({
      next: () => {
        report.status = 'RESOLVED' as any;
        if (report.reportedUser) report.reportedUser.banned = true;
        this.showMessage('User banned and report resolved');
        this.loadStats();
        this.loadUsers();
      },
      error: () => this.showMessage('Failed to ban user', true)
    });
  }

  deleteReport(reportId: string): void {
    if (!confirm('Delete this report?')) return;

    this.adminService.deleteReport(reportId).subscribe({
      next: () => {
        this.reports = this.reports.filter(r => r.id !== reportId);
        this.showMessage('Report deleted');
        this.loadStats();
      },
      error: () => this.showMessage('Failed to delete report', true)
    });
  }

  // ==================== HELPERS ====================

  private showMessage(message: string, isError = false): void {
    this.snackBar.open(message, 'Close', {
      duration: 3000,
      panelClass: isError ? 'snack-error' : 'snack-success'
    });
  }

  getAvatarUrl(user: User | undefined): string {
    if (!user) return 'https://robohash.org/unknown?set=set4';
    // Check for avatar field and make sure it's not the default
    const avatar = user.avatar;
    if (avatar && !avatar.includes('default-avatar') && avatar.trim() !== '') {
      // Ensure full URL
      if (avatar.startsWith('http')) {
        return avatar;
      }
      // Relative path - works with nginx proxy in production
      return avatar.startsWith('/') ? avatar : `/${avatar}`;
    }
    return `https://robohash.org/${user.username || user.id}?set=set4`;
  }

  getPostAuthorAvatar(post: Post): string {
    const avatar = (post as any).authorAvatar;
    if (avatar && !avatar.includes('default-avatar') && avatar.trim() !== '') {
      if (avatar.startsWith('http')) {
        return avatar;
      }
      return avatar.startsWith('/') ? avatar : `/${avatar}`;
    }
    return `https://robohash.org/${(post as any).authorUsername || 'user'}?set=set4`;
  }

  isCurrentUser(userId: string): boolean {
    return this.currentUserId === userId;
  }

  viewPost(postId: string): void {
    this.router.navigate(['/post', postId]);
  }

  truncate(text: string, length: number): string {
    if (!text) return '';
    return text.length > length ? text.substring(0, length) + '...' : text;
  }
}
