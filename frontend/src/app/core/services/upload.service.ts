import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface UploadResponse {
  path: string;
}

@Injectable({ providedIn: 'root' })
export class UploadService {
  constructor(private http: HttpClient) {}

  upload(file: File): Observable<UploadResponse> {
    const fd = new FormData();
    fd.append('file', file);
    return this.http.post<UploadResponse>(`${environment.apiUrl}/uploads`, fd);
  }

  uploadFile(file: File): Observable<UploadResponse> {
    return this.upload(file);
  }

  // Helper to detect media type from file
  getMediaType(file: File): 'image' | 'video' | null {
    if (file.type.startsWith('image/')) {
      return 'image';
    } else if (file.type.startsWith('video/')) {
      return 'video';
    }
    return null;
  }

  // Validate file size (max 50MB)
  isValidFileSize(file: File, maxSizeMB: number = 50): boolean {
    const maxBytes = maxSizeMB * 1024 * 1024;
    return file.size <= maxBytes;
  }

  // Get file extension
  getFileExtension(filename: string): string {
    return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
  }
}
