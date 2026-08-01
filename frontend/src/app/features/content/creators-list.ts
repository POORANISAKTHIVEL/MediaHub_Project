import { Component, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { ContentClient } from '../../core/api/content-client';
import { Creator } from '../../core/models/content.models';
import { AuthService } from '../../core/auth/auth.service';
import { StatusBadge } from '../../shared/components/status-badge';
import { RowMenu, RowMenuItem } from '../../shared/components/row-menu';
import { LoadingSpinner } from '../../shared/components/loading-spinner';
import { ToastService } from '../../shared/services/toast.service';

@Component({
  selector: 'app-creators-list',
  imports: [FormsModule, StatusBadge, RowMenu, LoadingSpinner],
  templateUrl: './creators-list.html'
})
export class CreatorsList implements OnInit {
  private content = inject(ContentClient);
  private toast = inject(ToastService);
  private router = inject(Router);
  auth = inject(AuthService);

  loading = signal(true);
  creators = signal<Creator[]>([]);

  creating = signal(false);
  editing = signal<Creator | null>(null);
  form = { displayName: '', genre: '', country: '', royaltyTier: 'Standard', userId: 0, bankAccountRef: '' };

  ngOnInit() {
    this.load();
  }

  load() {
    this.loading.set(true);
    this.content.fetchCreators().subscribe(rows => {
      this.creators.set(rows);
      this.loading.set(false);
    });
  }

  menuFor(_c: Creator): RowMenuItem[] {
    const items: RowMenuItem[] = [{ label: 'View', action: 'view' }];
    if (this.auth.hasPermission('content:write')) items.push({ label: 'Edit', action: 'edit' });
    return items;
  }

  onAction(action: string, c: Creator) {
    if (action === 'view') this.router.navigate(['/creators', c.creatorId]);
    if (action === 'edit') this.openEdit(c);
  }

  openCreate() {
    this.form = { displayName: '', genre: '', country: '', royaltyTier: 'Standard', userId: 0, bankAccountRef: '' };
    this.creating.set(true);
  }

  openEdit(c: Creator) {
    this.form = { displayName: c.displayName, genre: c.genre ?? '', country: c.country ?? '', royaltyTier: c.royaltyTier ?? 'Standard', userId: c.userId, bankAccountRef: c.bankAccountRef ?? '' };
    this.editing.set(c);
  }

  save() {
    if (!this.form.displayName.trim()) return;
    const editing = this.editing();
    if (editing) {
      this.content.updateCreator(editing.creatorId, this.form).subscribe(() => {
        this.toast.ok('Creator updated successfully');
        this.editing.set(null);
        this.load();
      });
    } else {
      if (!this.form.userId) { this.toast.warn('Linked user ID is required'); return; }
      this.content.createCreator(this.form).subscribe({
        next: () => { this.toast.ok('Creator created successfully'); this.creating.set(false); this.load(); },
        error: (err) => this.toast.warn(err?.error?.message ?? 'Unable to create creator')
      });
    }
  }
}
