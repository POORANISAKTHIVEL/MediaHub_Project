import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-not-found',
  imports: [RouterLink],
  template: `
    <div class="empty" style="min-height:100vh">
      <div class="empty__ico">🗂</div>
      <h3>Page not found</h3>
      <p style="max-width:280px">The page you're looking for doesn't exist.</p>
      <a class="btn btn--primary" routerLink="/dashboard" style="margin-top:16px">← Back to dashboard</a>
    </div>
  `
})
export class NotFound {}
