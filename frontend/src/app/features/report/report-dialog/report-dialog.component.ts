import { Component, Inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ReportService } from '../../../core/services/report.service';

@Component({
  selector: 'app-report-dialog',
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
  templateUrl: './report-dialog.component.html',
  styleUrls: ['./report-dialog.component.css']
})
export class ReportDialogComponent {
  reportForm: FormGroup;
  isLoading = false;

  constructor(
    private fb: FormBuilder,
    private reportService: ReportService,
    private snackBar: MatSnackBar,
    @Inject(MAT_DIALOG_DATA) public data: { userId: string, username: string },
    private dialogRef: MatDialogRef<ReportDialogComponent>
  ) {
    this.reportForm = this.fb.group({
      reason: ['', [Validators.required, Validators.minLength(10)]]
    });
  }

  onSubmit(): void {
    if (this.reportForm.valid && !this.isLoading) {
      this.isLoading = true;
      const reportData = {
        reportedUserId: this.data.userId,
        reason: this.reportForm.value.reason
      };

      this.reportService.createReport(reportData).subscribe({
        next: () => {
          this.snackBar.open('Report submitted successfully', 'Close', { duration: 3000 });
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
