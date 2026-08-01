import { Injectable, signal } from '@angular/core';

export interface Toast {
  id: number;
  message: string;
  kind: 'ok' | 'warn' | 'info';
}

@Injectable({ providedIn: 'root' })
export class ToastService {
  private nextId = 1;
  readonly toasts = signal<Toast[]>([]);

  show(message: string, kind: Toast['kind'] = 'info') {
    const id = this.nextId++;
    this.toasts.update(list => [...list, { id, message, kind }]);
    setTimeout(() => this.dismiss(id), 3200);
  }

  ok(message: string) { this.show(message, 'ok'); }
  warn(message: string) { this.show(message, 'warn'); }

  dismiss(id: number) {
    this.toasts.update(list => list.filter(t => t.id !== id));
  }
}
