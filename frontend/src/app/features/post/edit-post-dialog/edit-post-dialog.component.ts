import { Component, Inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatDialogModule, MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Post } from '../../../core/models/post.model';
import { PostService } from '../../../core/services/post.service';
import { UploadService } from '../../../core/services/upload.service';

@Component({
  selector: 'app-edit-post-dialog',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './edit-post-dialog.component.html',
  styleUrls: ['./edit-post-dialog.component.css']
})
export class EditPostDialogComponent implements OnInit {
  postForm!: FormGroup;
  isLoading = false;
  isUploading = false;
  selectedFiles: File[] = [];
  mediaPreviews: string[] = [];
  uploadedMediaUrls: string[] = [];
  mediaTypes: string[] = []; // Track 'image' or 'video' for selected files
  uploadedMediaTypes: string[] = []; // Track 'image' or 'video' for uploaded files

  constructor(
    private fb: FormBuilder,
    private postService: PostService,
    private uploadService: UploadService,
    private snackBar: MatSnackBar,
    public dialogRef: MatDialogRef<EditPostDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: { post: Post }
  ) {}

  ngOnInit(): void {
    this.initializeForm();
  }

  private initializeForm(): void {
    this.postForm = this.fb.group({
      title: [this.data.post.title || '', [Validators.required, Validators.maxLength(200)]],
      description: [this.data.post.description || '', [Validators.required, Validators.minLength(1)]]
    });

    // Load existing media URLs
    if (this.data.post.mediaUrls && this.data.post.mediaUrls.length > 0) {
      this.uploadedMediaUrls = [...this.data.post.mediaUrls];
      // Determine media types from URLs
      this.uploadedMediaTypes = this.data.post.mediaUrls.map(url => {
        const ext = url.split('.').pop()?.toLowerCase();
        return (ext === 'mp4' || ext === 'webm' || ext === 'ogg' || ext === 'mov') ? 'video' : 'image';
      });
    }
  }

  onMediaFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (!input.files) return;

    const files = Array.from(input.files);
    const totalCount = this.getTotalMediaCount() + files.length;

    if (totalCount > 4) {
      this.snackBar.open(`Maximum 4 images allowed. You have ${this.getTotalMediaCount()} already.`, 'Close', { duration: 3000 });
      return;
    }

    for (const file of files) {
      const mediaType = this.uploadService.getMediaType(file);
      if (mediaType !== 'image' && mediaType !== 'video') {
        this.snackBar.open('Only image and video files are allowed', 'Close', { duration: 2000 });
        continue;
      }

      if (file.size > 50 * 1024 * 1024) {
        this.snackBar.open(`File ${file.name} is too large (max 50MB)`, 'Close', { duration: 2000 });
        continue;
      }

      this.selectedFiles.push(file);
      this.mediaTypes.push(mediaType);

      // Generate preview
      const reader = new FileReader();
      reader.onload = () => {
        this.mediaPreviews.push(reader.result as string);
      };
      reader.readAsDataURL(file);
    }

    // Reset input
    input.value = '';
  }

  onUploadMediaFiles(): void {
    if (this.selectedFiles.length === 0) return;

    this.isUploading = true;
    let uploadedCount = 0;
    const newMediaUrls: string[] = [];

      const uploadNextFile = () => {
      if (uploadedCount >= this.selectedFiles.length) {
        // All files uploaded
        this.uploadedMediaUrls.push(...newMediaUrls);
        // Track media types for uploaded files
        this.mediaTypes.forEach(type => this.uploadedMediaTypes.push(type));
        this.selectedFiles = [];
        this.mediaPreviews = [];
        this.mediaTypes = [];
        this.isUploading = false;
        this.snackBar.open(`${newMediaUrls.length} media file(s) uploaded successfully`, 'Close', { duration: 2000 });
        return;
      }

      const file = this.selectedFiles[uploadedCount];

      this.uploadService.uploadFile(file).subscribe({
        next: (response) => {
          newMediaUrls.push(response.path);
          uploadedCount++;
          uploadNextFile();
        },
        error: () => {
          this.snackBar.open(`Failed to upload ${file.name}`, 'Close', { duration: 2000 });
          uploadedCount++;
          uploadNextFile();
        }
      });
    };

    uploadNextFile();
  }

  removeMedia(index: number, isUploaded: boolean): void {
    if (isUploaded) {
      this.uploadedMediaUrls.splice(index, 1);
      this.uploadedMediaTypes.splice(index, 1);
    } else {
      this.mediaPreviews.splice(index, 1);
      this.selectedFiles.splice(index, 1);
      this.mediaTypes.splice(index, 1);
    }
  }

  clearAllMedia(): void {
    this.selectedFiles = [];
    this.mediaPreviews = [];
    this.uploadedMediaUrls = [];
    this.mediaTypes = [];
    this.uploadedMediaTypes = [];
  }

  getTotalMediaCount(): number {
    return this.selectedFiles.length + this.uploadedMediaUrls.length;
  }

  canAddMore(): boolean {
    return this.getTotalMediaCount() < 4;
  }

  onCancel(): void {
    this.dialogRef.close();
  }

  onSubmit(): void {
    if (this.postForm.invalid || this.isLoading || this.isUploading) {
      return;
    }

    // Upload any new files first
    if (this.selectedFiles.length > 0) {
      this.onUploadMediaFiles();
      // Wait for upload to complete, then submit
      const checkUpload = setInterval(() => {
        if (!this.isUploading && this.selectedFiles.length === 0) {
          clearInterval(checkUpload);
          this.submitPostUpdate();
        }
      }, 100);
    } else {
      this.submitPostUpdate();
    }
  }

  private submitPostUpdate(): void {
    this.isLoading = true;

    const updateRequest = {
      title: this.postForm.get('title')?.value,
      description: this.postForm.get('description')?.value,
      mediaUrls: this.uploadedMediaUrls.length > 0 ? this.uploadedMediaUrls : undefined
    };

    this.postService.updatePost(this.data.post.id, updateRequest).subscribe({
      next: () => {
        this.snackBar.open('Post updated successfully', 'Close', { duration: 2000 });
        this.isLoading = false;
        this.dialogRef.close(true);
      },
      error: (error) => {
        console.error('Error updating post:', error);
        this.snackBar.open('Failed to update post', 'Close', { duration: 2000 });
        this.isLoading = false;
      }
    });
  }
}
