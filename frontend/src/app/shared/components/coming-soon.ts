import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-coming-soon',
  template: `
    <div class="page-head"><div><h1>{{ title }}</h1><p>This module is being built next.</p></div></div>
    <div class="card"><div class="empty"><div class="empty__ico">🚧</div><h3>{{ title }} — coming soon</h3></div></div>
  `
})
export class ComingSoon {
  @Input() title = 'Module';
}
