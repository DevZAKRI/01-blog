import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { UserService } from '../../core/services/user.service';
import { AuthService } from '../../core/services/auth.service';
import { AvatarPipe } from '../../core/pipes/avatar.pipe';

interface User {
  id: number;
  username: string;
  email?: string;
  bio?: string;
  avatarUrl?: string;
  subscribersCount?: number;
  subscriptionsCount?: number;
  isSubscribed?: boolean;
}

@Component({
  selector: 'app-explore',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatSnackBarModule,
    AvatarPipe
  ],
  templateUrl: './explore.component.html',
  styleUrls: ['./explore.component.css']
})
export class ExploreComponent implements OnInit {
  users: User[] = [];
  isLoading = true;
  page = 0;
  size = 20;
  hasMore = true;
  subscribingUsers = new Set<number>();
  currentUserId: number | null = null;

  constructor(
    private userService: UserService,
    private authService: AuthService,
    private router: Router,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    // Get current user ID
    const currentUser = this.authService.getCurrentUser();
    this.currentUserId = currentUser?.id ? Number(currentUser.id) : null;
    console.log('[Explore] Current user ID:', this.currentUserId);

    this.loadUsers();
  }

  loadUsers(): void {
    if (!this.hasMore && this.page > 0) return; // Only check hasMore after first load
    if (this.isLoading && this.page > 0) return; // Only prevent duplicate loads after first load

    this.isLoading = true;
    console.log('[Explore] Loading users - page:', this.page, 'size:', this.size);
    this.userService.getUsers(this.page, this.size).subscribe({
      next: (response) => {
        console.log('[Explore] Received users:', response);
        this.users.push(...response.content);
        this.hasMore = !response.last;
        this.page++;
        this.isLoading = false;
      },
      error: (error) => {
        console.error('[Explore] Failed to load users:', error);
        this.snackBar.open('Failed to load users', 'Close', { duration: 3000 });
        this.isLoading = false;
      }
    });
  }

  navigateToProfile(userId: number): void {
    this.router.navigate(['/profile', userId]);
  }

  subscribe(userId: number, event: Event): void {
    event.stopPropagation();

    const user = this.users.find(u => u.id === userId);
    if (!user) return;

    // Toggle subscription status
    if (user.isSubscribed) {
      this.unsubscribe(userId, event);
      return;
    }

    this.subscribingUsers.add(userId);

    this.userService.subscribe(userId).subscribe({
      next: () => {
        this.snackBar.open('Subscribed successfully!', 'Close', { duration: 2000 });
        this.subscribingUsers.delete(userId);
        // Update user status
        if (user) {
          user.isSubscribed = true;
          if (user.subscribersCount !== undefined) {
            user.subscribersCount++;
          }
        }
      },
      error: (error) => {
        console.error('Failed to subscribe', error);
        this.snackBar.open('Failed to subscribe', 'Close', { duration: 3000 });
        this.subscribingUsers.delete(userId);
      }
    });
  }

  unsubscribe(userId: number, event: Event): void {
    event.stopPropagation();
    this.subscribingUsers.add(userId);

    this.userService.unsubscribe(userId).subscribe({
      next: () => {
        this.snackBar.open('Unsubscribed successfully!', 'Close', { duration: 2000 });
        this.subscribingUsers.delete(userId);
        // Update user status
        const user = this.users.find(u => u.id === userId);
        if (user) {
          user.isSubscribed = false;
          if (user.subscribersCount !== undefined) {
            user.subscribersCount--;
          }
        }
      },
      error: (error) => {
        console.error('Failed to unsubscribe', error);
        this.snackBar.open('Failed to unsubscribe', 'Close', { duration: 3000 });
        this.subscribingUsers.delete(userId);
      }
    });
  }

  isOwnProfile(userId: number): boolean {
    return this.currentUserId === userId;
  }

  isSubscribing(userId: number): boolean {
    return this.subscribingUsers.has(userId);
  }

  onScroll(): void {
    if (!this.isLoading && this.hasMore) {
      this.loadUsers();
    }
  }
}
