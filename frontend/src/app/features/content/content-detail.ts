import { Component, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { ContentClient } from '../../core/api/content-client';
import { EditorialClient } from '../../core/api/editorial-client';
import { ContentAsset, ContentTag } from '../../core/models/content.models';
import { StatusBadge } from '../../shared/components/status-badge';
import { LoadingSpinner } from '../../shared/components/loading-spinner';
import { AuthService } from '../../core/auth/auth.service';
import { ToastService } from '../../shared/services/toast.service';

@Component({
  selector: 'app-content-detail',
  imports: [RouterLink, StatusBadge, LoadingSpinner],
  templateUrl: './content-detail.html'
})
export class ContentDetail implements OnInit {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private content = inject(ContentClient);
  private editorial = inject(EditorialClient);
  private toast = inject(ToastService);
  auth = inject(AuthService);

  loading = signal(true);
  item = signal<ContentAsset | null>(null);
  tags = signal<ContentTag[]>([]);

  ngOnInit() {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.content.fetchContentById(id).subscribe(c => {
      this.item.set(c ?? null);
      this.loading.set(false);
    });
    this.content.tagsByContent(id).subscribe(t => this.tags.set(t));
  }

  archive() {
    const c = this.item();
    if (!c) return;
    this.content.updateContentStatus(c.contentId, 'Archived').subscribe(() => {
      this.item.set({ ...c, status: 'Archived' });
    });
  }

  submitForReview() {
    const c = this.item();
    if (!c) return;
    this.editorial.submitForReview(c.contentId).subscribe({
      next: () => {
        this.content.updateContentStatus(c.contentId, 'UnderReview').subscribe(() => {
          this.toast.ok('Content submitted for review');
          this.item.set({ ...c, status: 'UnderReview' });
        });
      },
      error: (err) => this.toast.warn(err?.error?.message ?? 'Unable to submit for review')
    });
  }
}
