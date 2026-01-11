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

  constructor(public authService: AuthService) {}

  get isOwner(): boolean {
    const currentUser = this.authService.getCurrentUser();
    return currentUser?.id === this.post.userId;
  }

  onLike(): void {
    this.like.emit(this.post.id);
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
}
