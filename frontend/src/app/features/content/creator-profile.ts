import { Component, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { ContentClient } from '../../core/api/content-client';
import { Creator, ContentAsset } from '../../core/models/content.models';
import { StatusBadge } from '../../shared/components/status-badge';
import { LoadingSpinner } from '../../shared/components/loading-spinner';

@Component({
  selector: 'app-creator-profile',
  imports: [StatusBadge, LoadingSpinner],
  templateUrl: './creator-profile.html'
})
export class CreatorProfile implements OnInit {
  private route = inject(ActivatedRoute);
  private content = inject(ContentClient);

  loading = signal(true);
  creator = signal<Creator | null>(null);
  content_ = signal<ContentAsset[]>([]);

  ngOnInit() {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.content.fetchCreatorById(id).subscribe(c => {
      this.creator.set(c ?? null);
      this.content_.set(c ? this.content.contentByCreator(c.creatorId) : []);
      this.loading.set(false);
    });
  }
}
