import { Component, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { EditorialClient } from '../../core/api/editorial-client';
import { ContentClient } from '../../core/api/content-client';
import { EditorialReview } from '../../core/models/editorial.models';
import { ContentAsset, ContentTag } from '../../core/models/content.models';
import { AuthService } from '../../core/auth/auth.service';
import { StatusBadge } from '../../shared/components/status-badge';
import { LoadingSpinner } from '../../shared/components/loading-spinner';
import { ContentPreview } from '../../shared/components/content-preview';
import { ToastService } from '../../shared/services/toast.service';

@Component({
  selector: 'app-review-detail',
  imports: [FormsModule, RouterLink, StatusBadge, LoadingSpinner, ContentPreview],
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
  contentAsset = signal<ContentAsset | null>(null);
  creatorName = signal('');
  tags = signal<ContentTag[]>([]);
  remarks = '';
  remarksTouched = signal(false);
  readonly remarksMax = 300;

  get remarksError(): string {
    if (!this.remarksTouched()) return '';
    if (!this.remarks.trim()) return 'Review comment is required';
    if (this.remarks.length > this.remarksMax) return `Comment must be ${this.remarksMax} characters or fewer`;
    return '';
  }

  submitting = signal(false);
  viewOnly = signal(false);

  ngOnInit() {
    this.viewOnly.set(this.route.snapshot.queryParamMap.get('mode') === 'view');
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.editorial.getReview(id).subscribe(r => {
      this.review.set(r ?? null);
      this.remarks = r?.remarks ?? '';
      this.loading.set(false);
      if (r) {
        this.content.fetchContentById(r.contentID).subscribe(c => {
          this.contentTitle.set(c?.title ?? ('CNT-' + r.contentID));
          this.contentType.set(c?.type ?? '');
          this.contentAsset.set(c ?? null);
          if (c) this.content.fetchCreatorById(c.creatorId).subscribe(cr => this.creatorName.set(cr?.displayName ?? ('Creator #' + c.creatorId)));
        });
        this.content.tagsByContent(r.contentID).subscribe(t => this.tags.set(t));
      }
    });
  }

  // The editorial decision (EditorialReview.decision) and the content's own status
  // (ContentAsset.status) are separate records in separate services — approving/rejecting a
  // review never touched the content's status on its own, so Content Catalog kept showing
  // "UnderReview" forever. Sync the content status here right after the decision is recorded.
  private act(obs: ReturnType<EditorialClient['approve']>, okMessage: string, contentID: number, newContentStatus: 'Published' | 'Draft') {
    this.submitting.set(true);
    obs.subscribe({
      next: () => {
        this.content.updateContentStatus(contentID, newContentStatus).subscribe({
          next: () => {
            this.toast.ok(okMessage);
            this.router.navigate(['/editorial/reviews']);
          },
          error: () => {
            this.toast.ok(okMessage);
            this.router.navigate(['/editorial/reviews']);
          }
        });
      },
      error: (err) => { this.submitting.set(false); this.toast.warn(err?.error?.message ?? 'Action failed'); }
    });
  }

  approve() {
    this.remarksTouched.set(true);
    if (this.remarksError) return;
    const r = this.review();
    if (!r) return;
    this.act(this.editorial.approve(r.reviewID, this.remarks), 'Review approved', r.contentID, 'Published');
  }

  reject() {
    this.remarksTouched.set(true);
    if (this.remarksError) return;
    const r = this.review();
    if (!r) return;
    this.act(this.editorial.reject(r.reviewID, this.remarks), 'Review rejected', r.contentID, 'Draft');
  }

  requestRevision() {
    this.remarksTouched.set(true);
    if (this.remarksError) return;
    const r = this.review();
    if (!r) return;
    const contentID = r.contentID;
    this.submitting.set(true);
    this.editorial.requestRevision(r.reviewID, this.remarks).subscribe({
      next: () => {
        this.content.updateContentStatus(contentID, 'Draft').subscribe();
        this.toast.ok('Revision requested');
        this.router.navigate(['/editorial/reviews']);
      },
      error: (err) => { this.submitting.set(false); this.toast.warn(err?.error?.message ?? 'Action failed'); }
    });
  }
}
