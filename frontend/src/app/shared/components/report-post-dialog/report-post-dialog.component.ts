import { Component, Inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ReportService } from '../../../core/services/report.service';

export interface ReportPostDialogData {
  postId: string;
  postTitle?: string;
}

@Component({
  selector: 'app-report-post-dialog',
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
  template: `
    <div class="report-dialog">
      <div class="dialog-header">
        <mat-icon class="report-icon">flag</mat-icon>
        <h2 mat-dialog-title>Report Post</h2>
      </div>
      <mat-dialog-content>
        <p class="info-text">Please provide a detailed reason for reporting this post. Our team will review your report.</p>
        <form [formGroup]="reportForm">
          <mat-form-field appearance="outline" class="full-width">
            <mat-label>Reason for Report</mat-label>
            <textarea matInput
                      formControlName="reason"
                      rows="4"
                      placeholder="Describe the issue (e.g., spam, harassment, inappropriate content)..."></textarea>
            <mat-hint>Minimum 10 characters</mat-hint>
            <mat-error *ngIf="reportForm.get('reason')?.hasError('required')">
              Reason is required
            </mat-error>
            <mat-error *ngIf="reportForm.get('reason')?.hasError('minlength')">
              Please provide at least 10 characters
            </mat-error>
          </mat-form-field>
        </form>
      </mat-dialog-content>
      <mat-dialog-actions align="end">
        <button mat-button (click)="onCancel()" [disabled]="isLoading">Cancel</button>
        <button mat-raised-button color="warn" (click)="onSubmit()"
                [disabled]="isLoading || reportForm.invalid">
          <mat-spinner diameter="20" *ngIf="isLoading"></mat-spinner>
          <span *ngIf="!isLoading">Submit Report</span>
        </button>
      </mat-dialog-actions>
    </div>
  `,
  styles: [`
    .report-dialog {
      min-width: 400px;
    }

    .dialog-header {
      display: flex;
      align-items: center;
      gap: 12px;
      margin-bottom: 8px;
    }

    .report-icon {
      color: #f59e0b;
      width: 28px;
      height: 28px;
      font-size: 28px;
    }

    h2 {
      margin: 0;
      font-size: 1.25rem;
      font-weight: 500;
    }

    .info-text {
      color: rgba(0, 0, 0, 0.6);
      margin-bottom: 16px;
      line-height: 1.5;
    }

    .full-width {
      width: 100%;
    }

    button mat-spinner {
      display: inline-block;
      margin-right: 8px;
    }

    mat-dialog-actions {
      padding-top: 8px;
    }
  `]
})
export class ReportPostDialogComponent {
  reportForm: FormGroup;
  isLoading = false;

  constructor(
    private fb: FormBuilder,
    private reportService: ReportService,
    private snackBar: MatSnackBar,
    @Inject(MAT_DIALOG_DATA) public data: ReportPostDialogData,
    private dialogRef: MatDialogRef<ReportPostDialogComponent>
  ) {
    this.reportForm = this.fb.group({
      reason: ['', [Validators.required, Validators.minLength(10)]]
    });
  }

  onSubmit(): void {
    if (this.reportForm.valid && !this.isLoading) {
      this.isLoading = true;
      const reportData = {
        postId: this.data.postId,
        reason: this.reportForm.value.reason
      };

      this.reportService.reportPost(reportData).subscribe({
        next: () => {
          this.snackBar.open('Post reported successfully. Our team will review it.', 'Close', { duration: 4000 });
          this.dialogRef.close(true);
        },
        error: () => {
          this.isLoading = false;
          this.snackBar.open('Failed to submit report', 'Close', { duration: 3000 });
        }
      });
    }
  }

  onCancel(): void {
    this.dialogRef.close();
  }
}
