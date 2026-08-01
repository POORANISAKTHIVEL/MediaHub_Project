import { Component, inject } from '@angular/core';
import { ToastService } from '../services/toast.service';

@Component({
  selector: 'app-toast-container',
  template: `
    <div class="toast-wrap">
      @for (t of toast.toasts(); track t.id) {
        <div class="toast">
          <span [class]="t.kind === 'warn' ? 'warn' : 'ok'">{{ t.kind === 'warn' ? '⚠' : '✓' }}</span>
          <span>{{ t.message }}</span>
          <span class="x" (click)="toast.dismiss(t.id)">✕</span>
        </div>
      }
    </div>
  `
})
export class ToastContainer {
  toast = inject(ToastService);
}
