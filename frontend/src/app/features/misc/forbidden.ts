import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-forbidden',
  imports: [RouterLink],
  template: `
    <div class="card"><div class="card__head"><h3>Access denied (403)</h3></div>
      <div class="empty">
        <div class="empty__ico" style="background:var(--red-50);color:var(--red-600)">⚠</div>
        <h3>Access denied</h3>
        <p style="max-width:280px">You don't have the permission required to view this page.</p>
        <a class="btn btn--ghost" routerLink="/dashboard" style="margin-top:16px">← Back to dashboard</a>
      </div>
    </div>
  `
})
export class Forbidden {}
