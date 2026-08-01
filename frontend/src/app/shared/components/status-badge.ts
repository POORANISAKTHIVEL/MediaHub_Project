import { Component, Input } from '@angular/core';

const DEFAULT_MAP: Record<string, string> = {
  active: 'b-green', published: 'b-green', approved: 'b-green', processed: 'b-green', paid: 'b-green', finalised: 'b-green', success: 'b-green', completed: 'b-green', read: 'b-green',
  pending: 'b-amber', draft: 'b-amber', scheduled: 'b-amber', underreview: 'b-amber', pendingreview: 'b-amber', unread: 'b-amber',
  suspended: 'b-red', inactive: 'b-red', expired: 'b-red', terminated: 'b-red', failed: 'b-red', rejected: 'b-red', cancelled: 'b-red', removed: 'b-red', blocked: 'b-red', critical: 'b-red', high: 'b-red',
  archived: 'b-gray', dismissed: 'b-gray', discontinued: 'b-gray', revisionrequired: 'b-blue', medium: 'b-amber', low: 'b-gray'
};

@Component({
  selector: 'app-status-badge',
  template: `<span class="badge" [class]="cls">{{ status }}</span>`
})
export class StatusBadge {
  @Input() status = '';
  get cls(): string {
    const key = this.status.toLowerCase().replace(/[\s_-]/g, '');
    return DEFAULT_MAP[key] ?? 'b-gray';
  }
}
