import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { EditorialClient } from '../../core/api/editorial-client';
import { ContentClient } from '../../core/api/content-client';
import { ContentCollection } from '../../core/models/editorial.models';
import { ContentAsset } from '../../core/models/content.models';
import { AuthService } from '../../core/auth/auth.service';
import { LoadingSpinner } from '../../shared/components/loading-spinner';
import { ToastService } from '../../shared/services/toast.service';

@Component({
  selector: 'app-collection-detail',
  imports: [LoadingSpinner],
  templateUrl: './collection-detail.html'
})
export class CollectionDetail implements OnInit {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private editorial = inject(EditorialClient);
  private content = inject(ContentClient);
  private toast = inject(ToastService);
  auth = inject(AuthService);

  loading = signal(true);
  collection = signal<ContentCollection | null>(null);
  allContent = signal<ContentAsset[]>([]);

  available = computed(() => {
    const c = this.collection();
    if (!c) return [];
    return this.allContent().filter(a => !c.contentIDs.includes(a.contentId));
  });
  inCollection = computed(() => {
    const c = this.collection();
    if (!c) return [];
    return this.allContent().filter(a => c.contentIDs.includes(a.contentId));
  });

  ngOnInit() {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.editorial.getAllCollections().subscribe(all => {
      this.collection.set(all.find(c => c.collectionID === id) ?? null);
      this.loading.set(false);
    });
    this.content.fetchContents().subscribe(c => this.allContent.set(c));
  }

  add(item: ContentAsset) {
    const c = this.collection();
    if (!c) return;
    const next = [...c.contentIDs, item.contentId];
    this.editorial.updateCollectionItems({ ...c, contentIDs: next }).subscribe(() => {
      this.collection.set({ ...c, contentIDs: next });
      this.toast.ok('Added to collection');
    });
  }

  remove(item: ContentAsset) {
    const c = this.collection();
    if (!c) return;
    const next = c.contentIDs.filter(id => id !== item.contentId);
    this.editorial.updateCollectionItems({ ...c, contentIDs: next }).subscribe(() => {
      this.collection.set({ ...c, contentIDs: next });
      this.toast.ok('Removed from collection');
    });
  }

  back() {
    this.router.navigate(['/editorial/collections']);
  }
}
