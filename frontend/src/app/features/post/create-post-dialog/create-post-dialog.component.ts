import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { MatDialogRef, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar } from '@angular/material/snack-bar';
import { PostService } from '../../../core/services/post.service';

@Component({
  selector: 'app-create-post-dialog',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './create-post-dialog.component.html',
  styleUrls: ['./create-post-dialog.component.css']
})
export class CreatePostDialogComponent {
  postForm: FormGroup;
  isLoading = false;
  mediaPreview: string | null = null;

  constructor(
    private fb: FormBuilder,
    private postService: PostService,
    private dialogRef: MatDialogRef<CreatePostDialogComponent>,
    private snackBar: MatSnackBar
  ) {
    this.postForm = this.fb.group({
      content: ['', [Validators.required, Validators.maxLength(1000)]],
      mediaUrl: [''],
      mediaType: ['image']
    });
  }

  onMediaUrlChange(): void {
    const url = this.postForm.get('mediaUrl')?.value;
    if (url) {
      this.mediaPreview = url;
    } else {
      this.mediaPreview = null;
    }
  }

  onSubmit(): void {
    if (this.postForm.valid && !this.isLoading) {
      this.isLoading = true;
      const data = this.postForm.value;

      if (!data.mediaUrl) {
        delete data.mediaUrl;
        delete data.mediaType;
      }

      this.postService.createPost(data).subscribe({
        next: (post) => {
          this.snackBar.open('Post created successfully!', 'Close', { duration: 3000 });
          this.dialogRef.close(post);
        },
        error: () => {
          this.isLoading = false;
          this.snackBar.open('Failed to create post', 'Close', { duration: 3000 });
        }
      });
    }
  }

  onCancel(): void {
    this.dialogRef.close();
  }
}
