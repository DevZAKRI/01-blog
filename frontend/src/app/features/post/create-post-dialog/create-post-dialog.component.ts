import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { MatDialogRef, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatIconModule } from '@angular/material/icon';
import { PostService } from '../../../core/services/post.service';
import { UploadService } from '../../../core/services/upload.service';

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
    MatProgressSpinnerModule,
    MatIconModule
  ],
  templateUrl: './create-post-dialog.component.html',
  styleUrls: ['./create-post-dialog.component.css']
})
export class CreatePostDialogComponent {
  postForm: FormGroup;
  isLoading = false;
  isUploading = false;
  mediaPreviews: string[] = [];
  mediaTypes: string[] = []; // Track if each preview is 'image' or 'video'
  selectedFiles: File[] = [];
  uploadedMediaUrls: string[] = [];
  uploadedMediaTypes: string[] = []; // Track types of uploaded media

  constructor(
    private fb: FormBuilder,
    private postService: PostService,
    private uploadService: UploadService,
    private dialogRef: MatDialogRef<CreatePostDialogComponent>,
    private snackBar: MatSnackBar
  ) {
    this.postForm = this.fb.group({
      title: ['', [Validators.required, Validators.maxLength(200)]],
      description: ['', [Validators.required, Validators.maxLength(5000)]],
      mediaUrls: [[]]
    });
  }

  onMediaFileSelected(event: any): void {
    const files = event.target.files;
    if (files && files.length > 0) {
      for (let i = 0; i < files.length; i++) {
        const file = files[i];

        // Check total files limit (max 4)
        if (this.selectedFiles.length + this.uploadedMediaUrls.length >= 4) {
          this.snackBar.open('Maximum 4 images allowed per post', 'Close', { duration: 3000 });
          break;
        }

        // Validate file size
        if (!this.uploadService.isValidFileSize(file)) {
          this.snackBar.open(`${file.name}: File size must be less than 50MB`, 'Close', { duration: 3000 });
          continue;
        }

        // Get media type
        const mediaType = this.uploadService.getMediaType(file);
        if (mediaType !== 'image' && mediaType !== 'video') {
          this.snackBar.open(`${file.name}: Only image and video files are supported`, 'Close', { duration: 3000 });
          continue;
        }

        this.selectedFiles.push(file);
        this.mediaTypes.push(mediaType);

        // Create preview
        const reader = new FileReader();
        reader.onload = (e: any) => {
          this.mediaPreviews.push(e.target.result);
        };
        reader.readAsDataURL(file);
      }
    }
  }

  onUploadMediaFiles(): void {
    if (this.selectedFiles.length === 0) return;

    this.isUploading = true;
    let uploadedCount = 0;

    this.selectedFiles.forEach((file, index) => {
      const fileMediaType = this.uploadService.getMediaType(file);
      this.uploadService.upload(file).subscribe({
        next: (response) => {
          this.uploadedMediaUrls.push(response.path);
          if (fileMediaType) {
            this.uploadedMediaTypes.push(fileMediaType);
          }
          uploadedCount++;

          if (uploadedCount === this.selectedFiles.length) {
            this.postForm.patchValue({ mediaUrls: this.uploadedMediaUrls });
            this.isUploading = false;
            this.snackBar.open('All media uploaded successfully!', 'Close', { duration: 2000 });
            // Clear selected files after upload
            this.selectedFiles = [];
          }
        },
        error: () => {
          this.isUploading = false;
          this.snackBar.open(`Failed to upload ${file.name}`, 'Close', { duration: 3000 });
        }
      });
    });
  }

  removeMedia(index: number, isUploaded: boolean = false): void {
    if (isUploaded) {
      this.uploadedMediaUrls.splice(index, 1);
      this.uploadedMediaTypes.splice(index, 1);
      this.postForm.patchValue({ mediaUrls: this.uploadedMediaUrls });
    } else {
      this.selectedFiles.splice(index, 1);
      this.mediaPreviews.splice(index, 1);
      this.mediaTypes.splice(index, 1);
    }
  }

  clearAllMedia(): void {
    this.selectedFiles = [];
    this.mediaPreviews = [];
    this.mediaTypes = [];
    this.uploadedMediaUrls = [];
    this.uploadedMediaTypes = [];
    this.postForm.patchValue({ mediaUrls: [] });
  }

  onSubmit(): void {
    if (this.postForm.valid && !this.isLoading) {
      // If media is selected but not uploaded yet, upload it first
      if (this.selectedFiles.length > 0 && this.uploadedMediaUrls.length < this.selectedFiles.length) {
        this.onUploadMediaFiles();
        return;
      }

      this.isLoading = true;
      const data = this.postForm.value;

      // Remove empty media array
      if (!data.mediaUrls || data.mediaUrls.length === 0) {
        delete data.mediaUrls;
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

  getTotalMediaCount(): number {
    return this.selectedFiles.length + this.uploadedMediaUrls.length;
  }

  canAddMore(): boolean {
    return this.getTotalMediaCount() < 4;
  }
}
