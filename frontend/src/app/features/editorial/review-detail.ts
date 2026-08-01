import { Component, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { EditorialClient } from '../../core/api/editorial-client';
import { ContentClient } from '../../core/api/content-client';
import { EditorialReview } from '../../core/models/editorial.models';
import { ContentTag } from '../../core/models/content.models';
import { AuthService } from '../../core/auth/auth.service';
import { StatusBadge } from '../../shared/components/status-badge';
import { LoadingSpinner } from '../../shared/components/loading-spinner';
import { ToastService } from '../../shared/services/toast.service';

@Component({
  selector: 'app-review-detail',
  imports: [FormsModule, RouterLink, StatusBadge, LoadingSpinner],
  templateUrl: './review-detail.html'
})
export class ReviewDetail implements OnInit {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private editorial = inject(EditorialClient);
  private content = inject(ContentClient);
  private toast = inject(ToastService);
  auth = inject(AuthService);

  loading = signal(true);
  review = signal<EditorialReview | null>(null);
  contentTitle = signal('');
  contentType = signal('');
  creatorName = signal('');
  tags = signal<ContentTag[]>([]);
  remarks = '';
  submitting = signal(false);

  ngOnInit() {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.editorial.getReview(id).subscribe(r => {
      this.review.set(r ?? null);
      this.remarks = r?.remarks ?? '';
      this.loading.set(false);
      if (r) {
        this.content.fetchContentById(r.contentID).subscribe(c => {
          this.contentTitle.set(c?.title ?? ('CNT-' + r.contentID));
          this.contentType.set(c?.type ?? '');
          if (c) this.content.fetchCreatorById(c.creatorId).subscribe(cr => this.creatorName.set(cr?.displayName ?? ('Creator #' + c.creatorId)));
        });
        this.content.tagsByContent(r.contentID).subscribe(t => this.tags.set(t));
      }
    });
  }

  private act(obs: ReturnType<EditorialClient['approve']>, okMessage: string) {
    this.submitting.set(true);
    obs.subscribe({
      next: () => {
        this.toast.ok(okMessage);
        this.router.navigate(['/editorial/reviews']);
      },
      error: (err) => { this.submitting.set(false); this.toast.warn(err?.error?.message ?? 'Action failed'); }
    });
  }

  approve() {
    const r = this.review();
    if (!r) return;
    this.act(this.editorial.approve(r.reviewID, this.remarks), 'Review approved');
  }

  reject() {
    const r = this.review();
    if (!r) return;
    this.act(this.editorial.reject(r.reviewID, this.remarks), 'Review rejected');
  }

  requestRevision() {
    const r = this.review();
    if (!r) return;
    this.act(this.editorial.requestRevision(r.reviewID, this.remarks), 'Revision requested');
  }
}
