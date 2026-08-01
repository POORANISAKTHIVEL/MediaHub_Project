import { Component, EventEmitter, HostListener, Input, Output } from '@angular/core';

@Component({
  selector: 'app-filter-chip',
  template: `
    <span class="chip" (click)="toggle($event)" style="position:relative">
      {{ label }}: {{ value || 'All' }} ▾
      @if (openMenu) {
        <div class="menu" style="position:absolute;top:calc(100% + 6px);left:0" (click)="$event.stopPropagation()">
          @for (o of options; track o) {
            <button (click)="select(o)">{{ o }}</button>
          }
        </div>
      }
    </span>
  `
})
export class FilterChip {
  @Input() label = 'Filter';
  @Input() value = '';
  @Input() options: string[] = ['All'];
  @Output() valueChange = new EventEmitter<string>();
  openMenu = false;

  toggle(ev: Event) {
    ev.stopPropagation();
    this.openMenu = !this.openMenu;
  }

  select(o: string) {
    this.valueChange.emit(o === 'All' ? '' : o);
    this.openMenu = false;
  }

  @HostListener('document:click')
  closeOnOutsideClick() {
    this.openMenu = false;
  }
}
