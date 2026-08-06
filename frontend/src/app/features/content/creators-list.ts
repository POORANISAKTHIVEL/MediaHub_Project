import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { ContentClient } from '../../core/api/content-client';
import { Creator } from '../../core/models/content.models';
import { AuthService } from '../../core/auth/auth.service';
import { StatusBadge } from '../../shared/components/status-badge';
import { RowMenu, RowMenuItem } from '../../shared/components/row-menu';
import { LoadingSpinner } from '../../shared/components/loading-spinner';
import { Pagination } from '../../shared/components/pagination';
import { FitRowsDirective } from '../../shared/directives/fit-rows.directive';
import { ToastService } from '../../shared/services/toast.service';
import { ConfirmService } from '../../shared/services/confirm.service';

const NAME_PATTERN = /^[A-Za-z\s]+$/;
// Real-world bank account references (IBAN, etc.) top out at 34 characters — the international
// IBAN maximum — and are alphanumeric plus hyphens (this app's own seed data uses e.g. "US-IBAN-0021").
const BANK_REF_MAX = 34;
const BANK_REF_PATTERN = /^[A-Za-z0-9-]*$/;

@Component({
  selector: 'app-creators-list',
  imports: [FormsModule, StatusBadge, RowMenu, LoadingSpinner, Pagination, FitRowsDirective],
  templateUrl: './creators-list.html'
})
export class CreatorsList implements OnInit {
  private content = inject(ContentClient);
  private toast = inject(ToastService);
  private confirm = inject(ConfirmService);
  private router = inject(Router);
  auth = inject(AuthService);

  loading = signal(true);
  all = signal<Creator[]>([]);
  searchTerm = signal('');

  creators = computed(() => {
    const term = this.searchTerm().trim().toLowerCase();
    if (!term) return this.all();
    return this.all().filter(c => c.displayName.toLowerCase().includes(term) || ('crt-' + c.creatorId).toLowerCase().includes(term) || String(c.creatorId).includes(term));
  });

  page = signal(0);
  pageSize = signal(10);
  totalPages = computed(() => Math.max(1, Math.ceil(this.creators().length / this.pageSize())));
  pagedCreators = computed(() => this.creators().slice(this.page() * this.pageSize(), (this.page() + 1) * this.pageSize()));

  onSearchChange(term: string) {
    this.searchTerm.set(term);
    this.page.set(0);
  }

  // Called by [appFitRows] with however many rows actually fit the screen without scrolling —
  // pagination absorbs the rest instead of an internal scrollbar.
  onRowsThatFit(n: number) {
    if (n === this.pageSize()) return;
    this.pageSize.set(n);
    this.page.set(0);
  }

  creating = signal(false);
  editing = signal<Creator | null>(null);
  form = { displayName: '', genre: '', country: '', royaltyTier: 'Standard', userId: 0, bankAccountRef: '' };
  nameTouched = signal(false);
  bankRefTouched = signal(false);
  bankRefMax = BANK_REF_MAX;

  get nameError(): string {
    if (!this.nameTouched()) return '';
    const v = this.form.displayName.trim();
    if (!v) return 'Display name is required';
    if (!NAME_PATTERN.test(v)) return 'Only letters and spaces are allowed';
    return '';
  }

  get bankRefError(): string {
    if (!this.bankRefTouched()) return '';
    const v = this.form.bankAccountRef.trim();
    if (!v) return '';
    if (v.length > BANK_REF_MAX) return `Must be ${BANK_REF_MAX} characters or fewer`;
    if (!BANK_REF_PATTERN.test(v)) return 'Only letters, numbers and hyphens are allowed';
    return '';
  }

  ngOnInit() {
    this.load();
  }

  load() {
    this.loading.set(true);
    this.content.fetchCreators().subscribe(rows => {
      this.all.set(rows);
      this.loading.set(false);
    });
  }

  menuFor(c: Creator): RowMenuItem[] {
    const items: RowMenuItem[] = [{ label: 'View', action: 'view' }];
    if (this.auth.hasPermission('content:write')) items.push({ label: 'Edit', action: 'edit' });
    // Lifecycle transitions (PendingReview -> Active -> Suspended) are an admin-only decision.
    if (this.auth.hasRole('admin')) {
      if (c.status === 'PendingReview') items.push({ label: 'Activate', action: 'activate' });
      if (c.status === 'Active') items.push({ label: 'Suspend', action: 'suspend' });
      if (c.status === 'Suspended') items.push({ label: 'Reactivate', action: 'activate' });
    }
    return items;
  }

  onAction(action: string, c: Creator) {
    if (action === 'view') this.router.navigate(['/creators', c.creatorId]);
    if (action === 'edit') this.openEdit(c);
    if (action === 'activate') this.setStatus(c, 'Active');
    if (action === 'suspend') this.suspend(c);
  }

  private setStatus(c: Creator, status: Creator['status']) {
    this.content.updateCreatorStatus(c.creatorId, status).subscribe({
      next: () => { this.toast.ok(`Creator ${status === 'Active' ? 'activated' : status.toLowerCase()} successfully`); this.load(); },
      error: (err) => this.toast.warn(err?.error?.message ?? 'Unable to update creator status')
    });
  }

  async suspend(c: Creator) {
    const ok = await this.confirm.ask(`Suspend "${c.displayName}"? They will lose access until reactivated.`, 'Suspend', true);
    if (!ok) return;
    this.setStatus(c, 'Suspended');
  }

  openCreate() {
    this.form = { displayName: '', genre: '', country: '', royaltyTier: 'Standard', userId: 0, bankAccountRef: '' };
    this.nameTouched.set(false);
    this.bankRefTouched.set(false);
    this.creating.set(true);
  }

  openEdit(c: Creator) {
    this.form = { displayName: c.displayName, genre: c.genre ?? '', country: c.country ?? '', royaltyTier: c.royaltyTier ?? 'Standard', userId: c.userId, bankAccountRef: c.bankAccountRef ?? '' };
    this.nameTouched.set(false);
    this.bankRefTouched.set(false);
    this.editing.set(c);
  }

  save() {
    this.nameTouched.set(true);
    this.bankRefTouched.set(true);
    if (this.nameError || this.bankRefError) {
      this.toast.warn(this.nameError || this.bankRefError);
      return;
    }
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
        // Backend returns a plain-text body (not JSON) for this endpoint, so the message is
        // err.error itself, not err.error.message — check both so real errors surface at all.
        error: (err) => this.toast.warn(err?.error?.message ?? err?.error ?? 'Unable to create creator')
      });
    }
  }
}
