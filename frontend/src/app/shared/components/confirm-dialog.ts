import { Component, inject } from '@angular/core';
import { ConfirmService } from '../services/confirm.service';

@Component({
  selector: 'app-confirm-dialog',
  template: `
    @if (confirm.state(); as s) {
      <div class="overlay" (click)="confirm.resolve(false)">
        <div class="modal" style="width:420px" (click)="$event.stopPropagation()">
          <div class="modal__head"><h3>Confirm</h3></div>
          <div class="modal__body"><p style="color:var(--ink-700);font-size:14px">{{ s.message }}</p></div>
          <div class="modal__foot">
            <button class="btn btn--ghost" (click)="confirm.resolve(false)">Cancel</button>
            <button [class]="s.danger ? 'btn btn--red' : 'btn btn--primary'" (click)="confirm.resolve(true)">{{ s.confirmLabel }}</button>
          </div>
        </div>
      </div>
    }
  `
})
export class ConfirmDialog {
  confirm = inject(ConfirmService);
}
