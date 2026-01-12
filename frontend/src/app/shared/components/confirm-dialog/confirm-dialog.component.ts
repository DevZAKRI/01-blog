import { Component, Inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MAT_DIALOG_DATA, MatDialogRef, MatDialogModule } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';

export interface ConfirmDialogData {
  title: string;
  message: string;
  confirmText?: string;
  cancelText?: string;
  type?: 'warning' | 'danger' | 'info';
}

@Component({
  selector: 'app-confirm-dialog',
  standalone: true,
  imports: [
    CommonModule,
    MatDialogModule,
    MatButtonModule,
    MatIconModule
  ],
  template: `
    <div class="confirm-dialog" [class]="'type-' + (data.type || 'info')">
      <div class="dialog-header">
        <mat-icon class="dialog-icon">
          {{ getIcon() }}
        </mat-icon>
        <h2 mat-dialog-title>{{ data.title }}</h2>
      </div>
      <mat-dialog-content>
        <p>{{ data.message }}</p>
      </mat-dialog-content>
      <mat-dialog-actions align="end">
        <button mat-button (click)="onCancel()">
          {{ data.cancelText || 'Cancel' }}
        </button>
        <button mat-raised-button [color]="getButtonColor()" (click)="onConfirm()">
          {{ data.confirmText || 'Confirm' }}
        </button>
      </mat-dialog-actions>
    </div>
  `,
  styles: [`
    .confirm-dialog {
      min-width: 320px;
      max-width: 450px;
    }

    .dialog-header {
      display: flex;
      align-items: center;
      gap: 12px;
      margin-bottom: 8px;
    }

    .dialog-icon {
      width: 32px;
      height: 32px;
      font-size: 32px;
    }

    .type-warning .dialog-icon {
      color: #f59e0b;
    }

    .type-danger .dialog-icon {
      color: #ef4444;
    }

    .type-info .dialog-icon {
      color: #3b82f6;
    }

    h2 {
      margin: 0;
      font-size: 1.25rem;
      font-weight: 500;
    }

    mat-dialog-content p {
      color: rgba(0, 0, 0, 0.7);
      line-height: 1.5;
      margin: 0;
    }

    mat-dialog-actions {
      padding-top: 16px;
    }
  `]
})
export class ConfirmDialogComponent {
  constructor(
    @Inject(MAT_DIALOG_DATA) public data: ConfirmDialogData,
    private dialogRef: MatDialogRef<ConfirmDialogComponent>
  ) {}

  getIcon(): string {
    switch (this.data.type) {
      case 'danger': return 'error';
      case 'warning': return 'warning';
      default: return 'help_outline';
    }
  }

  getButtonColor(): string {
    return this.data.type === 'danger' ? 'warn' : 'primary';
  }

  onConfirm(): void {
    this.dialogRef.close(true);
  }

  onCancel(): void {
    this.dialogRef.close(false);
  }
}
