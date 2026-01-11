import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Subscription } from 'rxjs';
import { UIToastService, ToastMessage } from '../../../core/services/ui-toast.service';

@Component({
  selector: 'app-toast',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="toast-container" aria-live="polite">
      <div *ngFor="let t of toasts" class="toast" [ngClass]="t.type">
        <div class="toast-content">{{ t.message }}</div>
        <button class="toast-close" (click)="dismiss(t.id)">×</button>
      </div>
    </div>
  `,
  styles: [
    `:host { position: relative; z-index: 1500; }
     .toast-container { position: fixed; top: 64px; right: 16px; display: flex; flex-direction: column; gap: 8px; }
     .toast { min-width: 220px; max-width: 380px; padding: 12px 14px; border-radius: 8px; box-shadow: 0 4px 12px rgba(0,0,0,0.12); color: white; display: flex; align-items: center; justify-content: space-between; }
     .toast .toast-content { flex: 1; padding-right: 8px; }
     .toast .toast-close { background: transparent; border: none; color: rgba(255,255,255,0.9); font-size: 18px; cursor: pointer; }
     .toast.error { background: #d32f2f; }
     .toast.success { background: #388e3c; }
     .toast.info { background: #1976d2; }
     .toast.warning { background: #f57c00; }
  `]
})
export class ToastComponent {
  toasts: ToastMessage[] = [];
  sub: Subscription;

  constructor(private toast: UIToastService) {
    this.sub = this.toast.messages.subscribe((m) => {
      const id = m.id || Math.random().toString(36).slice(2, 9);
      const tm: ToastMessage = { ...m, id };
      this.toasts.push(tm);
      setTimeout(() => this.dismiss(id), m.duration ?? 4000);
    });
  }

  dismiss(id?: string) {
    if (!id) return;
    this.toasts = this.toasts.filter(t => t.id !== id);
  }

  ngOnDestroy() { this.sub.unsubscribe(); }
}
