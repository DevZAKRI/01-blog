import { Injectable } from '@angular/core';
import { HttpClient, HttpEventType, HttpEvent } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { map, catchError } from 'rxjs/operators';
import { environment } from '../../../environments/environment';

export interface UploadResponse {
  path: string;
  mediaType?: 'image' | 'video';
  filename?: string;
  size?: number;
}

export interface UploadProgress {
  progress: number;
  response?: UploadResponse;
}

@Injectable({ providedIn: 'root' })
export class UploadService {
  // Backend base URL (without /api/v1)
  private readonly backendBaseUrl: string;

  constructor(private http: HttpClient) {
    // Extract base URL from apiUrl (remove /api/v1)
    this.backendBaseUrl = environment.apiUrl.replace('/api/v1', '');
  }

  /**
   * Upload a file to the server
   */
  upload(file: File): Observable<UploadResponse> {
    const fd = new FormData();
    fd.append('file', file);

    return this.http.post<UploadResponse>(`${environment.apiUrl}/uploads`, fd).pipe(
      map(response => ({
        ...response,
        // Convert relative path to full URL for display
        path: this.getFullUrl(response.path)
      })),
      catchError(error => {
        console.error('Upload failed:', error);
        const message = error.error?.error || error.message || 'Upload failed';
        return throwError(() => new Error(message));
      })
    );
  }

  /**
   * Upload a file with progress tracking
   */
  uploadWithProgress(file: File): Observable<UploadProgress> {
    const fd = new FormData();
    fd.append('file', file);

    return this.http.post<UploadResponse>(`${environment.apiUrl}/uploads`, fd, {
      reportProgress: true,
      observe: 'events'
    }).pipe(
      map((event: HttpEvent<UploadResponse>) => {
        switch (event.type) {
          case HttpEventType.UploadProgress:
            const progress = event.total
              ? Math.round(100 * event.loaded / event.total)
              : 0;
            return { progress };
          case HttpEventType.Response:
            const response = event.body;
            if (response) {
              return {
                progress: 100,
                response: {
                  ...response,
                  path: this.getFullUrl(response.path)
                }
              };
            }
            return { progress: 100 };
          default:
            return { progress: 0 };
        }
      }),
      catchError(error => {
        console.error('Upload failed:', error);
        const message = error.error?.error || error.message || 'Upload failed';
        return throwError(() => new Error(message));
      })
    );
  }

  /**
   * Alias for upload method
   */
  uploadFile(file: File): Observable<UploadResponse> {
    return this.upload(file);
  }

  /**
   * Convert a relative path to a full URL
   */
  getFullUrl(path: string): string {
    if (!path) return '';
    // If already a full URL, return as-is
    if (path.startsWith('http://') || path.startsWith('https://')) {
      return path;
    }
    // Prepend backend base URL
    return `${this.backendBaseUrl}${path}`;
  }

  /**
   * Detect media type from file MIME type
   */
  getMediaType(file: File): 'image' | 'video' | null {
    if (file.type.startsWith('image/')) {
      return 'image';
    } else if (file.type.startsWith('video/')) {
      return 'video';
    }
    return null;
  }

  /**
   * Detect media type from URL (by extension)
   */
  getMediaTypeFromUrl(url: string): 'image' | 'video' {
    if (!url) return 'image';
    const ext = url.split('.').pop()?.toLowerCase();
    const videoExtensions = ['mp4', 'webm', 'ogg', 'ogv', 'mov', 'avi', 'mpeg'];
    return videoExtensions.includes(ext || '') ? 'video' : 'image';
  }

  /**
   * Validate file size (max 50MB by default)
   */
  isValidFileSize(file: File, maxSizeMB: number = 50): boolean {
    const maxBytes = maxSizeMB * 1024 * 1024;
    return file.size <= maxBytes;
  }

  /**
   * Validate file type
   */
  isValidFileType(file: File): boolean {
    const allowedImageTypes = ['image/png', 'image/jpeg', 'image/jpg', 'image/gif', 'image/webp'];
    const allowedVideoTypes = ['video/mp4', 'video/quicktime', 'video/webm', 'video/ogg', 'video/mpeg'];
    return allowedImageTypes.includes(file.type) || allowedVideoTypes.includes(file.type);
  }

  /**
   * Get file extension
   */
  getFileExtension(filename: string): string {
    return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
  }

  /**
   * Format file size for display
   */
  formatFileSize(bytes: number): string {
    if (bytes === 0) return '0 Bytes';
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
  }
}
