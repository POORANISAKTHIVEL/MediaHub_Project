import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-stat-card',
  template: `
    <div class="stat">
      <div class="stat__top">
        <span>{{ label }}</span>
        <div class="stat__ico" [style.background]="bg" [style.color]="color">{{ icon }}</div>
      </div>
      <div class="stat__val">{{ value }}</div>
      @if (delta) {
        <div class="stat__delta" [class]="deltaUp ? 'up' : 'down'">{{ delta }}</div>
      }
    </div>
  `
})
export class StatCard {
  @Input() label = '';
  @Input() value: string | number = '';
  @Input() icon = '📊';
  @Input() color = 'var(--brand-600)';
  @Input() bg = 'var(--brand-50)';
  @Input() delta = '';
  @Input() deltaUp = true;
}
