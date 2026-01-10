import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { AuthService } from '../../core/services/auth.service';
import { UserService } from '../../core/services/user.service';

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
    MatSnackBarModule
  ],
  templateUrl: './settings.component.html',
  styleUrls: ['./settings.component.css']
})
export class SettingsComponent implements OnInit {
  profileForm: FormGroup;
  isLoading = false;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private userService: UserService,
    private snackBar: MatSnackBar
  ) {
    this.profileForm = this.fb.group({
      username: ['', [Validators.required, Validators.minLength(3)]],
      fullName: [''],
      bio: ['', Validators.maxLength(200)],
      avatar: ['']
    });
  }

  ngOnInit(): void {
    const currentUser = this.authService.getCurrentUser();
    if (currentUser) {
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
            localStorage.setItem('currentUser', JSON.stringify(currentUser));
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
}
