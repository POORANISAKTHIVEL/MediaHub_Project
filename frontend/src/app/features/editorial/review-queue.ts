import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { Router } from '@angular/router';
import { EditorialClient } from '../../core/api/editorial-client';
import { ContentClient } from '../../core/api/content-client';
import { EditorialReview } from '../../core/models/editorial.models';
import { AuthService } from '../../core/auth/auth.service';
import { StatusBadge } from '../../shared/components/status-badge';
import { LoadingSpinner } from '../../shared/components/loading-spinner';
import { RowMenu, RowMenuItem } from '../../shared/components/row-menu';

type Tab = 'Pending' | 'Approved' | 'Rejected';

@Component({
  selector: 'app-review-queue',
  imports: [StatusBadge, LoadingSpinner, RowMenu],
  templateUrl: './review-queue.html'
})
export class ReviewQueue implements OnInit {
  private editorial = inject(EditorialClient);
  private content = inject(ContentClient);
  private router = inject(Router);
  auth = inject(AuthService);

  loading = signal(true);
  all = signal<EditorialReview[]>([]);
  contentTitles = signal<Record<number, string>>({});
  tab = signal<Tab>('Pending');

  rows = computed(() => {
    const t = this.tab();
    return this.all().filter(r =>
      t === 'Pending' ? r.status === 'Pending' :
      t === 'Approved' ? r.decision === 'Approved' :
      r.decision === 'Rejected'
    );
  });

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
      this.all.set(r);
      this.loading.set(false);
    });
  }

  titleFor(contentID: number): string {
    return this.contentTitles()[contentID] ?? ('CNT-' + contentID);
  }

  menuFor(_r: EditorialReview): RowMenuItem[] {
    return [{ label: 'Edit', action: 'edit' }];
  }

  onRowAction(action: string, r: EditorialReview) {
    if (action === 'edit') this.router.navigate(['/editorial/reviews', r.reviewID]);
  }
}
