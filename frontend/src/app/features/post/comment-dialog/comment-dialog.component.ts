import { Component, Inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
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

  constructor(
    private fb: FormBuilder,
    private commentService: CommentService,
    private snackBar: MatSnackBar,
    @Inject(MAT_DIALOG_DATA) public data: { postId: string },
    private dialogRef: MatDialogRef<CommentDialogComponent>
  ) {
    this.commentForm = this.fb.group({
      content: ['', [Validators.required, Validators.maxLength(500)]]
    });
  }

  ngOnInit(): void {
    this.loadComments();
  }

  loadComments(): void {
    this.isLoading = true;
    this.commentService.getComments(this.data.postId).subscribe({
      next: (comments) => {
        this.comments = comments;
        this.isLoading = false;
      },
      error: () => {
        this.isLoading = false;
        this.snackBar.open('Failed to load comments', 'Close', { duration: 3000 });
      }
    });
  }

  onSubmit(): void {
    if (this.commentForm.valid && !this.isSubmitting) {
      this.isSubmitting = true;
      this.commentService.createComment(this.data.postId, this.commentForm.value).subscribe({
        next: (comment) => {
          this.comments.push(comment);
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
          this.snackBar.open('Comment deleted', 'Close', { duration: 2000 });
        },
        error: () => {
          this.snackBar.open('Failed to delete comment', 'Close', { duration: 3000 });
        }
      });
    }
  }

  onClose(): void {
    this.dialogRef.close(this.comments.length > 0);
  }
}
