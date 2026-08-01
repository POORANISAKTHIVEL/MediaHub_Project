import { Injectable, signal } from '@angular/core';

export interface ConfirmState {
  message: string;
  confirmLabel: string;
  danger: boolean;
  resolve: (result: boolean) => void;
}

@Injectable({ providedIn: 'root' })
export class ConfirmService {
  readonly state = signal<ConfirmState | null>(null);

  ask(message: string, confirmLabel = 'Confirm', danger = false): Promise<boolean> {
    return new Promise(resolve => {
      this.state.set({ message, confirmLabel, danger, resolve });
    });
  }

  resolve(result: boolean) {
    this.state()?.resolve(result);
    this.state.set(null);
  }
}
