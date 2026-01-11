import { Component, Inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatSnackBar } from '@angular/material/snack-bar';
import { CommentListComponent } from '../../../shared/components/comment-list/comment-list.component';
import { CommentService } from '../../../core/services/comment.service';
import { Comment } from '../../../core/models/comment.model';

@Component({
  selector: 'app-comment-dialog',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatProgressSpinnerModule,
    MatPaginatorModule,
    CommentListComponent
  ],
  templateUrl: './comment-dialog.component.html',
  styleUrls: ['./comment-dialog.component.css']
})
export class CommentDialogComponent implements OnInit {
  commentForm: FormGroup;
  comments: Comment[] = [];
  isLoading = false;
  isSubmitting = false;
  currentPage = 0;
  pageSize = 10;
  totalComments = 0;

  constructor(
    private fb: FormBuilder,
    private commentService: CommentService,
    private snackBar: MatSnackBar,
    @Inject(MAT_DIALOG_DATA) public data: { postId: string },
    private dialogRef: MatDialogRef<CommentDialogComponent>
  ) {
    this.commentForm = this.fb.group({
      text: ['', [Validators.required, Validators.maxLength(500)]]
    });
  }

  ngOnInit(): void {
    this.loadComments();
  }

  loadComments(page: number = 0): void {
    this.isLoading = true;
    this.commentService.getComments(this.data.postId, page, this.pageSize).subscribe({
      next: (response) => {
        this.comments = response.comments;
        this.totalComments = response.total;
        this.currentPage = page;
        this.isLoading = false;
      },
      error: () => {
        this.isLoading = false;
        this.snackBar.open('Failed to load comments', 'Close', { duration: 3000 });
      }
    });
  }

  onPageChange(event: PageEvent): void {
    this.loadComments(event.pageIndex);
  }

  onSubmit(): void {
    if (this.commentForm.valid && !this.isSubmitting) {
      this.isSubmitting = true;
      this.commentService.createComment(this.data.postId, this.commentForm.value).subscribe({
        next: (comment) => {
          // Reload comments to show the new one
          this.loadComments(0);
          this.commentForm.reset();
          this.isSubmitting = false;
          this.snackBar.open('Comment added!', 'Close', { duration: 2000 });
        },
        error: () => {
          this.isSubmitting = false;
          this.snackBar.open('Failed to add comment', 'Close', { duration: 3000 });
        }
      });
    }
  }

  onDeleteComment(commentId: string): void {
    if (confirm('Delete this comment?')) {
      this.commentService.deleteComment(this.data.postId, commentId).subscribe({
        next: () => {
          this.comments = this.comments.filter(c => c.id !== commentId);
          this.totalComments--;
          this.snackBar.open('Comment deleted', 'Close', { duration: 2000 });
        },
        error: () => {
          this.snackBar.open('Failed to delete comment', 'Close', { duration: 3000 });
        }
      });
    }
  }

  onLikeComment(commentId: string): void {
    this.commentService.likeComment(this.data.postId, commentId).subscribe({
      next: () => {
        this.snackBar.open('Comment liked!', 'Close', { duration: 2000 });
      },
      error: () => {
        this.snackBar.open('Failed to like comment', 'Close', { duration: 3000 });
      }
    });
  }

  onUnlikeComment(commentId: string): void {
    this.commentService.unlikeComment(this.data.postId, commentId).subscribe({
      next: () => {
        this.snackBar.open('Like removed', 'Close', { duration: 2000 });
      },
      error: () => {
        this.snackBar.open('Failed to unlike comment', 'Close', { duration: 3000 });
      }
    });
  }

  onClose(): void {
    this.dialogRef.close(this.comments.length > 0);
  }
}
