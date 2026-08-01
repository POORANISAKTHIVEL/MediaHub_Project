import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-empty-state',
  template: `
    <div class="empty">
      <div class="empty__ico">{{ icon }}</div>
      <h3>{{ title }}</h3>
      <p style="max-width:280px">{{ message }}</p>
    </div>
  `
})
export class EmptyState {
  @Input() icon = '🗂';
  @Input() title = 'Nothing here yet';
  @Input() message = 'No records match your current filters.';
}
