import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { Router } from '@angular/router';
import { EditorialClient } from '../../core/api/editorial-client';
import { ContentClient } from '../../core/api/content-client';
import { EditorialReview } from '../../core/models/editorial.models';
import { AuthService } from '../../core/auth/auth.service';
import { StatusBadge } from '../../shared/components/status-badge';
import { LoadingSpinner } from '../../shared/components/loading-spinner';
import { RowMenu, RowMenuItem } from '../../shared/components/row-menu';
import { Pagination } from '../../shared/components/pagination';
import { FitRowsDirective } from '../../shared/directives/fit-rows.directive';

type Tab = 'Pending' | 'Approved' | 'Rejected';

@Component({
  selector: 'app-review-queue',
  imports: [StatusBadge, LoadingSpinner, RowMenu, Pagination, FitRowsDirective],
  templateUrl: './review-queue.html'
})
export class ReviewQueue implements OnInit {
  private editorial = inject(EditorialClient);
  private content = inject(ContentClient);
  private router = inject(Router);
  auth = inject(AuthService);

  loading = signal(true);
  orderedAll = signal<EditorialReview[]>([]);
  contentTitles = signal<Record<number, string>>({});
  tab = signal<Tab>('Pending');

  rows = computed(() => {
    const t = this.tab();
    return this.orderedAll().filter(r =>
      t === 'Pending' ? r.status === 'Pending' :
      t === 'Approved' ? r.decision === 'Approved' :
      r.decision === 'Rejected'
    );
  });

  page = signal(0);
  pageSize = signal(10);
  totalPages = computed(() => Math.max(1, Math.ceil(this.rows().length / this.pageSize())));
  pagedRows = computed(() => this.rows().slice(this.page() * this.pageSize(), (this.page() + 1) * this.pageSize()));

  onRowsThatFit(n: number) {
    if (n === this.pageSize()) return;
    this.pageSize.set(n);
    this.page.set(0);
  }

  selectTab(t: Tab) {
    this.tab.set(t);
    this.page.set(0);
  }

  ngOnInit() {
    this.load();
    this.content.fetchContents().subscribe(list => {
      const map: Record<number, string> = {};
      list.forEach(c => map[c.contentId] = c.title);
      this.contentTitles.set(map);
    });
  }

  load() {
    this.loading.set(true);
    this.editorial.getAllReviews().subscribe(r => {
      this.orderedAll.set(r);
      this.loading.set(false);
    });
  }

  moveUp(r: EditorialReview) {
    const tabRows = this.rows();
    const idx = tabRows.findIndex(x => x.reviewID === r.reviewID);
    if (idx <= 0) return;
    const all = [...this.orderedAll()];
    const aIdx = all.findIndex(x => x.reviewID === r.reviewID);
    const bIdx = all.findIndex(x => x.reviewID === tabRows[idx - 1].reviewID);
    [all[aIdx], all[bIdx]] = [all[bIdx], all[aIdx]];
    this.orderedAll.set(all);
  }

  moveDown(r: EditorialReview) {
    const tabRows = this.rows();
    const idx = tabRows.findIndex(x => x.reviewID === r.reviewID);
    if (idx >= tabRows.length - 1) return;
    const all = [...this.orderedAll()];
    const aIdx = all.findIndex(x => x.reviewID === r.reviewID);
    const bIdx = all.findIndex(x => x.reviewID === tabRows[idx + 1].reviewID);
    [all[aIdx], all[bIdx]] = [all[bIdx], all[aIdx]];
    this.orderedAll.set(all);
  }

  titleFor(contentID: number): string {
    return this.contentTitles()[contentID] ?? ('CNT-' + contentID);
  }

  menuFor(_r: EditorialReview): RowMenuItem[] {
    return [
      { label: 'View', action: 'view' },
      { label: 'Edit', action: 'edit' }
    ];
  }

  onRowAction(action: string, r: EditorialReview) {
    if (action === 'view') this.router.navigate(['/editorial/reviews', r.reviewID], { queryParams: { mode: 'view' } });
    if (action === 'edit') this.router.navigate(['/editorial/reviews', r.reviewID]);
  }
}
