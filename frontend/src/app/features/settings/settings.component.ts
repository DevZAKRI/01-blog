import { Component, OnInit, ViewChild, ElementRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatIconModule } from '@angular/material/icon';
import { AuthService } from '../../core/services/auth.service';
import { UserService } from '../../core/services/user.service';
import { UploadService } from '../../core/services/upload.service';
import { environment } from '../../../environments/environment';
import { AvatarPipe } from '../../core/pipes/avatar.pipe';

@Component({
  selector: 'app-settings',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatProgressSpinnerModule,
    MatSnackBarModule,
    MatIconModule,
    AvatarPipe
  ],
  templateUrl: './settings.component.html',
  styleUrls: ['./settings.component.css']
})
export class SettingsComponent implements OnInit {
  profileForm: FormGroup;
  isLoading = false;
  @ViewChild('avatarFileInput') avatarFileInput!: ElementRef<HTMLInputElement>;
  selectedFile?: File;
  previewUrl?: string;
  uploading = false;

  constructor(
    private fb: FormBuilder,
    public authService: AuthService,
    private userService: UserService,
    private uploadService: UploadService,
    private snackBar: MatSnackBar
  ) {
    this.profileForm = this.fb.group({
      username: ['', [Validators.required, Validators.minLength(3), Validators.pattern(/^[a-zA-Z0-9_]+$/)]],
      fullName: [''],
      bio: ['', Validators.maxLength(200)],
      avatar: ['']
    });
  }

  ngOnInit(): void {
    const currentUser = this.authService.getCurrentUser();
    if (currentUser) {
      // normalize preview URL if needed
      this.previewUrl = this.resolveAvatarUrl(currentUser.avatar);
      this.profileForm.patchValue({
        username: currentUser.username,
        fullName: currentUser.fullName || '',
        bio: currentUser.bio || '',
        avatar: currentUser.avatar || ''
      });
    }
  }

  onSubmit(): void {
    if (this.profileForm.valid && !this.isLoading) {
      this.isLoading = true;
      this.userService.updateProfile(this.profileForm.value).subscribe({
        next: (updatedUser) => {
          const currentUser = this.authService.getCurrentUser();
          if (currentUser) {
            Object.assign(currentUser, updatedUser);
            // ensure avatar is normalized for UI
            currentUser.avatar = updatedUser.avatar || currentUser.avatar;
            if (currentUser.avatar && currentUser.avatar.startsWith('/uploads')) {
              const apiRoot = environment.apiUrl.replace(/\/api\/v1\/?$/, '');
              currentUser.avatar = apiRoot + currentUser.avatar;
            }
            this.authService.updateCurrentUser(currentUser);
            this.previewUrl = this.resolveAvatarUrl(currentUser.avatar);
          }
          this.isLoading = false;
          this.snackBar.open('Profile updated successfully!', 'Close', { duration: 3000 });
        },
        error: () => {
          this.isLoading = false;
          this.snackBar.open('Failed to update profile', 'Close', { duration: 3000 });
        }
      });
    }
  }

  onFileSelected(ev: Event): void {
    const input = ev.target as HTMLInputElement;
    if (!input.files || input.files.length === 0) return;
    const file = input.files[0];
    this.selectedFile = file;
    const reader = new FileReader();
    reader.onload = () => this.previewUrl = reader.result as string;
    reader.readAsDataURL(file);
    // auto-upload
    this.uploadAvatar();
  }

  uploadAvatar(): void {
    if (!this.selectedFile) return;
    this.uploading = true;
    this.uploadService.upload(this.selectedFile).subscribe({
      next: (res) => {
        // server returns { path: '/uploads/...' }
        this.profileForm.patchValue({ avatar: res.path });
        this.uploading = false;
        // update preview to full url for client (uploads are served at /uploads/** not under /api/v1)
        const apiRoot = environment.apiUrl.replace(/\/api\/v1\/?$/, '');
        this.previewUrl = res.path.startsWith('/uploads') ? apiRoot + res.path : res.path;
        this.snackBar.open('Avatar uploaded. Remember to Save Changes.', 'Close', { duration: 3000 });
      },
      error: () => {
        this.uploading = false;
        this.snackBar.open('Avatar upload failed', 'Close', { duration: 3000 });
      }
    });
  }

  triggerFileSelect(): void {
    // Programmatically open the hidden file input
    this.avatarFileInput.nativeElement.click();
  }

  resolveAvatarUrl(val?: string): string {
    if (!val) return '';
    const apiRoot = environment.apiUrl.replace(/\/api\/v1\/?$/, '');
    return val.startsWith('/uploads') ? apiRoot + val : val;
  }
}
