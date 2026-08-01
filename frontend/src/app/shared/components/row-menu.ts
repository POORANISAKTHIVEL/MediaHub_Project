import { Component, EventEmitter, HostListener, Input, Output } from '@angular/core';

export interface RowMenuItem {
  label: string;
  action: string;
}

/** Reusable "⋯" row-action dropdown — mirrors demo.html's ROW_MENUS pattern (text-only items, no icons). */
@Component({
  selector: 'app-row-menu',
  template: `
    <span class="row-actions">
      <span class="icon-btn tip" data-tip="More actions" (click)="toggle($event)" style="position:relative">
        ⋯
        @if (open) {
          <div class="menu" style="position:absolute;top:100%;right:0" (click)="$event.stopPropagation()">
            @for (i of items; track i.action) {
              <button (click)="select(i)">{{ i.label }}</button>
            }
          </div>
        }
      </span>
    </span>
  `
})
export class RowMenu {
  @Input() items: RowMenuItem[] = [];
  @Output() actionSelected = new EventEmitter<string>();
  open = false;

  toggle(ev: Event) {
    ev.stopPropagation();
    this.open = !this.open;
  }

  select(i: RowMenuItem) {
    this.actionSelected.emit(i.action);
    this.open = false;
  }

  @HostListener('document:click')
  closeOnOutsideClick() {
    this.open = false;
  }
}
