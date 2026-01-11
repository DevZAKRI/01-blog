import { Injectable } from '@angular/core';
import { Subject, Observable } from 'rxjs';

export type ToastType = 'info' | 'success' | 'warning' | 'error';

export interface ToastMessage {
  id?: string;
  message: string;
  type?: ToastType;
  duration?: number;
}

@Injectable({ providedIn: 'root' })
export class UIToastService {
  private subject = new Subject<ToastMessage>();

  get messages(): Observable<ToastMessage> { return this.subject.asObservable(); }

  show(message: string, type: ToastType = 'info', duration = 4000) {
    this.subject.next({ message, type, duration });
  }

  error(message: string, duration = 5000) { this.show(message, 'error', duration); }
  success(message: string, duration = 3000) { this.show(message, 'success', duration); }
  info(message: string, duration = 3000) { this.show(message, 'info', duration); }
  warn(message: string, duration = 4000) { this.show(message, 'warning', duration); }
}
