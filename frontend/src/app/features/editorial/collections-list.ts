import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { EditorialClient } from '../../core/api/editorial-client';
import { ContentCollection, CollectionCategory } from '../../core/models/editorial.models';
import { AuthService } from '../../core/auth/auth.service';
import { StatusBadge } from '../../shared/components/status-badge';
import { FilterChip } from '../../shared/components/filter-chip';
import { LoadingSpinner } from '../../shared/components/loading-spinner';
import { ToastService } from '../../shared/services/toast.service';
import { ConfirmService } from '../../shared/services/confirm.service';

const CATEGORY_OPTIONS = ['All', 'Featured', 'Trending', 'Curated', 'New'];
const STATUS_OPTIONS = ['All', 'Scheduled', 'Active', 'Expired'];

@Component({
  selector: 'app-collections-list',
  imports: [FormsModule, StatusBadge, FilterChip, LoadingSpinner],
  templateUrl: './collections-list.html'
})
export class CollectionsList implements OnInit {
  private editorial = inject(EditorialClient);
  private toast = inject(ToastService);
  private confirm = inject(ConfirmService);
  private router = inject(Router);
  auth = inject(AuthService);

  loading = signal(true);
  all = signal<ContentCollection[]>([]);
  categoryFilter = signal('');
  statusFilter = signal('');
  categoryOptions = CATEGORY_OPTIONS;
  statusOptions = STATUS_OPTIONS;

  creating = signal(false);
  form: { name: string; category: CollectionCategory; publishDate: string; expiryDate: string; contentID: number } =
    { name: '', category: 'Featured', publishDate: '', expiryDate: '', contentID: 0 };

  rows = computed(() => this.all()
    .filter(c => !this.categoryFilter() || c.category === this.categoryFilter())
    .filter(c => !this.statusFilter() || c.status === this.statusFilter())
  );

  ngOnInit() {
    this.load();
  }

  load() {
    this.loading.set(true);
    this.editorial.getAllCollections().subscribe(rows => {
      this.all.set(rows);
      this.loading.set(false);
    });
  }

  openCreate() {
    this.form = { name: '', category: 'Featured', publishDate: '', expiryDate: '', contentID: 0 };
    this.creating.set(true);
  }

  create() {
    if (!this.form.name.trim() || !this.form.publishDate || !this.form.expiryDate || !this.form.contentID) return;
    const { contentID, ...rest } = this.form;
    this.editorial.createCollection({ ...rest, contentIDs: [contentID] }).subscribe({
      next: () => {
        this.toast.ok('Collection created successfully');
        this.creating.set(false);
        this.load();
      },
      error: (err) => this.toast.warn(err?.error?.error ?? 'Unable to create collection')
    });
  }

  manage(c: ContentCollection) {
    this.router.navigate(['/editorial/collections', c.collectionID]);
  }

  async expire(c: ContentCollection) {
    const ok = await this.confirm.ask(`Expire "${c.name}"? It will no longer be shown to viewers.`, 'Expire');
    if (!ok) return;
    this.editorial.expireCollection(c.collectionID).subscribe(() => { this.toast.ok('Collection expired'); this.load(); });
  }

  async remove(c: ContentCollection) {
    const ok = await this.confirm.ask(`Delete "${c.name}"?`, 'Delete', true);
    if (!ok) return;
    this.editorial.deleteCollection(c.collectionID).subscribe({
      next: () => { this.toast.ok('Collection deleted'); this.load(); },
      error: (err) => this.toast.warn(err?.error?.message ?? 'Unable to delete collection')
    });
  }
}
