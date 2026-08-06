import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { EditorialClient } from '../../core/api/editorial-client';
import { ContentCollection, CollectionCategory } from '../../core/models/editorial.models';
import { AuthService } from '../../core/auth/auth.service';
import { StatusBadge } from '../../shared/components/status-badge';
import { FilterChip } from '../../shared/components/filter-chip';
import { LoadingSpinner } from '../../shared/components/loading-spinner';
import { Pagination } from '../../shared/components/pagination';
import { FitRowsDirective } from '../../shared/directives/fit-rows.directive';
import { ToastService } from '../../shared/services/toast.service';
import { ConfirmService } from '../../shared/services/confirm.service';
import { clampContentId, contentIdError as contentIdErrorFor } from '../../shared/utils/content-id';

const CATEGORY_OPTIONS = ['All', 'Featured', 'Trending', 'Curated', 'New'];
const STATUS_OPTIONS = ['All', 'Scheduled', 'Active', 'Expired'];

@Component({
  selector: 'app-collections-list',
  imports: [FormsModule, StatusBadge, FilterChip, LoadingSpinner, Pagination, FitRowsDirective],
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
  nameTouched = signal(false);
  contentIdTouched = signal(false);

  readonly nameMax = 50;
  private namePattern = /^[a-zA-Z ]*$/;

  get nameError(): string {
    if (!this.nameTouched()) return '';
    const v = this.form.name;
    if (!v.trim()) return 'Name is required';
    if (v.length > this.nameMax) return `Name must be ${this.nameMax} characters or fewer`;
    if (!this.namePattern.test(v)) return 'Name may only contain letters and spaces';
    return '';
  }

  onNameChange(value: string) {
    const cleaned = value.replace(/[^a-zA-Z ]/g, '').slice(0, this.nameMax);
    this.form.name = cleaned;
    this.nameTouched.set(true);
  }

  get contentIdError(): string {
    return this.contentIdTouched() ? contentIdErrorFor(this.form.contentID) : '';
  }

  onContentIdChange(value: number) {
    this.form.contentID = clampContentId(value);
  }

  rows = computed(() => this.all()
    .filter(c => !this.categoryFilter() || c.category === this.categoryFilter())
    .filter(c => !this.statusFilter() || c.status === this.statusFilter())
  );

  page = signal(0);
  pageSize = signal(10);
  totalPages = computed(() => Math.max(1, Math.ceil(this.rows().length / this.pageSize())));
  pagedRows = computed(() => this.rows().slice(this.page() * this.pageSize(), (this.page() + 1) * this.pageSize()));

  onRowsThatFit(n: number) {
    if (n === this.pageSize()) return;
    this.pageSize.set(n);
    this.page.set(0);
  }

  onCategoryFilterChange(value: string) {
    this.categoryFilter.set(value);
    this.page.set(0);
  }

  onStatusFilterChange(value: string) {
    this.statusFilter.set(value);
    this.page.set(0);
  }

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
    this.nameTouched.set(false);
    this.contentIdTouched.set(false);
    this.creating.set(true);
  }

  create() {
    this.nameTouched.set(true);
    this.contentIdTouched.set(true);
    if (this.nameError || !this.form.publishDate || !this.form.expiryDate || contentIdErrorFor(this.form.contentID)) return;
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
