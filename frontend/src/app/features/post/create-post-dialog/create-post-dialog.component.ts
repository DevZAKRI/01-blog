import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { MatDialogRef, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatIconModule } from '@angular/material/icon';
import { PostService } from '../../../core/services/post.service';
import { UploadService } from '../../../core/services/upload.service';
import { forkJoin } from 'rxjs';

interface MediaItem {
  url: string;
  type: 'image' | 'video';
  isUploaded: boolean;
  file?: File;
  preview?: string;
  uploadProgress?: number;
}

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
    MatProgressBarModule,
    MatIconModule
  ],
  templateUrl: './create-post-dialog.component.html',
  styleUrls: ['./create-post-dialog.component.css']
})
export class CreatePostDialogComponent {
  postForm: FormGroup;
  isLoading = false;
  isUploading = false;

  // Unified media items array
  mediaItems: MediaItem[] = [];

  // Track overall upload progress
  overallProgress = 0;

  // Lightbox preview state
  previewOpen = false;
  previewItem: MediaItem | null = null;
  previewIndex = 0;

  constructor(
    private fb: FormBuilder,
    private postService: PostService,
    private uploadService: UploadService,
    private dialogRef: MatDialogRef<CreatePostDialogComponent>,
    private snackBar: MatSnackBar
  ) {
    this.postForm = this.fb.group({
      title: ['', [Validators.required, Validators.maxLength(200)]],
      description: ['', [Validators.required, Validators.maxLength(5000)]]
    });
  }

  onMediaFileSelected(event: any): void {
    const files = event.target.files;
    if (!files || files.length === 0) return;

    for (let i = 0; i < files.length; i++) {
      const file = files[i];

      // Check total files limit (max 4)
      if (this.mediaItems.length >= 4) {
        this.snackBar.open('Maximum 4 media files allowed per post', 'Close', { duration: 3000 });
        break;
      }

      // Validate file type
      if (!this.uploadService.isValidFileType(file)) {
        this.snackBar.open(`${file.name}: Unsupported file type`, 'Close', { duration: 3000 });
        continue;
      }

      // Validate file size
      if (!this.uploadService.isValidFileSize(file)) {
        this.snackBar.open(`${file.name}: File size must be less than 50MB`, 'Close', { duration: 3000 });
        continue;
      }

      // Get media type
      const mediaType = this.uploadService.getMediaType(file);
      if (!mediaType) {
        this.snackBar.open(`${file.name}: Only image and video files are supported`, 'Close', { duration: 3000 });
        continue;
      }

      // Create media item with preview
      const mediaItem: MediaItem = {
        url: '',
        type: mediaType,
        isUploaded: false,
        file: file,
        uploadProgress: 0
      };

      // Create preview
      const reader = new FileReader();
      reader.onload = (e: any) => {
        mediaItem.preview = e.target.result;
      };
      reader.readAsDataURL(file);

      this.mediaItems.push(mediaItem);
    }

    // Reset file input
    event.target.value = '';
  }

  async uploadAllMedia(): Promise<boolean> {
    const pendingItems = this.mediaItems.filter(item => !item.isUploaded && item.file);

    if (pendingItems.length === 0) {
      return true;
    }

    this.isUploading = true;
    this.overallProgress = 0;

    let successCount = 0;
    let failCount = 0;

    // Upload files sequentially to show progress properly
    for (const item of pendingItems) {
      if (!item.file) continue;

      try {
        const response = await this.uploadService.upload(item.file).toPromise();
        if (response) {
          item.url = response.path;
          item.isUploaded = true;
          item.uploadProgress = 100;
          successCount++;
        }
      } catch (error: any) {
        console.error('Upload failed:', error);
        item.uploadProgress = 0;
        failCount++;
        this.snackBar.open(`Failed to upload ${item.file.name}: ${error.message || 'Unknown error'}`, 'Close', { duration: 3000 });
      }

      // Update overall progress
      this.overallProgress = Math.round(((successCount + failCount) / pendingItems.length) * 100);
    }

    this.isUploading = false;

    if (failCount > 0) {
      this.snackBar.open(`${failCount} file(s) failed to upload`, 'Close', { duration: 3000 });
      return false;
    }

    this.snackBar.open('All media uploaded successfully!', 'Close', { duration: 2000 });
    return true;
  }

  removeMedia(index: number): void {
    this.mediaItems.splice(index, 1);
  }

  clearAllMedia(): void {
    this.mediaItems = [];
    this.overallProgress = 0;
  }

  async onSubmit(): Promise<void> {
    if (!this.postForm.valid || this.isLoading) {
      return;
    }

    // Upload any pending media first
    const pendingMedia = this.mediaItems.filter(item => !item.isUploaded && item.file);
    if (pendingMedia.length > 0) {
      const uploadSuccess = await this.uploadAllMedia();
      if (!uploadSuccess) {
        // Some uploads failed, don't proceed
        return;
      }
    }

    this.isLoading = true;

    // Build post data
    const data: any = {
      title: this.postForm.value.title,
      description: this.postForm.value.description
    };

    // Add uploaded media URLs
    const uploadedUrls = this.mediaItems
      .filter(item => item.isUploaded && item.url)
      .map(item => item.url);

    if (uploadedUrls.length > 0) {
      data.mediaUrls = uploadedUrls;
    }

    this.postService.createPost(data).subscribe({
      next: (post) => {
        this.snackBar.open('Post created successfully!', 'Close', { duration: 3000 });
        this.dialogRef.close(post);
      },
      error: (error) => {
        this.isLoading = false;
        console.error('Create post failed:', error);
        this.snackBar.open('Failed to create post', 'Close', { duration: 3000 });
      }
    });
  }

  onCancel(): void {
    this.dialogRef.close();
  }

  getTotalMediaCount(): number {
    return this.mediaItems.length;
  }

  canAddMore(): boolean {
    return this.mediaItems.length < 4;
  }

  getPendingCount(): number {
    return this.mediaItems.filter(item => !item.isUploaded && item.file).length;
  }

  getUploadedCount(): number {
    return this.mediaItems.filter(item => item.isUploaded).length;
  }

  // Lightbox methods
  openPreview(index: number): void {
    this.previewIndex = index;
    this.previewItem = this.mediaItems[index];
    this.previewOpen = true;
  }

  closePreview(): void {
    this.previewOpen = false;
    this.previewItem = null;
  }

  prevPreview(): void {
    if (this.previewIndex > 0) {
      this.previewIndex--;
      this.previewItem = this.mediaItems[this.previewIndex];
    }
  }

  nextPreview(): void {
    if (this.previewIndex < this.mediaItems.length - 1) {
      this.previewIndex++;
      this.previewItem = this.mediaItems[this.previewIndex];
    }
  }

  onPreviewBackdropClick(event: MouseEvent): void {
    // Close only if clicking the backdrop, not the content
    if ((event.target as HTMLElement).classList.contains('lightbox-backdrop')) {
      this.closePreview();
    }
  }
}
