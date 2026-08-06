import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, forkJoin, of } from 'rxjs';
import { map, switchMap } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import { MockStore, mockOf, mockError } from './mock-store';
import { ContentAsset, ContentTag, Creator } from '../models/content.models';

const CONTENT_SEED: ContentAsset[] = [
  { contentId: 20001, creatorId: 1, title: 'Space Documentary', type: 'Video', genre: 'Documentary', language: 'English', durationSeconds: 720, synopsis: "A sweeping look at humanity's push into orbit and beyond.", filePath: 'Videos/space-documentary.mp4', thumbnailPath: '', status: 'Published', publishedDate: '2026-07-18' },
  { contentId: 20002, creatorId: 1, title: 'Future Cities', type: 'Video', genre: 'Documentary', language: 'English', durationSeconds: 540, synopsis: "How tomorrow's urban centers are being designed today.", filePath: 'Videos/future-cities.mp4', thumbnailPath: '', status: 'Published', publishedDate: '2026-07-17' },
  { contentId: 20003, creatorId: 1, title: 'AI Landscape', type: 'Image', genre: 'Digital Art', language: 'English', durationSeconds: 0, synopsis: 'A generative-art landscape exploring AI-assisted imagery.', filePath: 'Images/ai-landscape.jpg', thumbnailPath: '', status: 'Published', publishedDate: '2026-07-16' },
  { contentId: 20004, creatorId: 1, title: 'Nature Gallery', type: 'Image', genre: 'Photography', language: 'English', durationSeconds: 0, synopsis: 'A curated gallery of natural landscapes.', filePath: 'Images/nature-gallery.jpg', thumbnailPath: '', status: 'Published', publishedDate: '2026-07-15' },
  { contentId: 20005, creatorId: 1, title: 'Future of Streaming Platforms', type: 'Article', genre: 'Technology', language: 'English', durationSeconds: 0, synopsis: 'An analysis of where streaming platforms are headed next.', filePath: 'Articles/future-of-streaming-platforms.html', thumbnailPath: '', status: 'Published', publishedDate: '2026-07-14' },
  { contentId: 20006, creatorId: 1, title: 'Introduction to Digital Rights Management', type: 'Article', genre: 'Technology', language: 'English', durationSeconds: 0, synopsis: 'A primer on how DRM protects licensed digital content.', filePath: 'Articles/introduction-to-digital-rights-management.html', thumbnailPath: '', status: 'Published', publishedDate: '2026-07-13' }
];

const CREATOR_SEED: Creator[] = [
  { creatorId: 1, userId: 101, displayName: 'Aria Films', genre: 'Documentary', country: 'US', royaltyTier: 'Platinum', bankAccountRef: 'US-IBAN-0021', status: 'Active' },
  { creatorId: 2, userId: 102, displayName: 'BlueNote Studio', genre: 'Audio', country: 'US', royaltyTier: 'Gold', bankAccountRef: 'US-IBAN-0034', status: 'Active' },
  { creatorId: 3, userId: 103, displayName: 'Lena Ortiz', genre: 'Photography', country: 'ES', royaltyTier: 'Silver', bankAccountRef: 'ES-IBAN-0087', status: 'Active' },
  { creatorId: 4, userId: 104, displayName: 'Cosmos Media', genre: 'Science', country: 'US', royaltyTier: 'Gold', bankAccountRef: 'US-IBAN-0099', status: 'PendingReview' },
  { creatorId: 5, userId: 105, displayName: 'Tastemakers', genre: 'Food', country: 'US', royaltyTier: 'Standard', bankAccountRef: 'US-IBAN-0142', status: 'Active' }
];

const TAG_SEED: ContentTag[] = [
  { tagId: 1, contentId: 20001, tagName: 'space', tagCategory: 'Theme' },
  { tagId: 2, contentId: 20001, tagName: 'documentary', tagCategory: 'Genre' },
  { tagId: 3, contentId: 20002, tagName: 'urban', tagCategory: 'Theme' },
  { tagId: 4, contentId: 20003, tagName: 'ai-art', tagCategory: 'Theme' },
  { tagId: 5, contentId: 20004, tagName: 'nature', tagCategory: 'Theme' }
];

@Injectable({ providedIn: 'root' })
export class ContentClient {
  private http = inject(HttpClient);
  private base = `${environment.apiBaseUrl}/content/mediahub/contentCatalog`;

  private content = new MockStore<ContentAsset>(CONTENT_SEED, 'contentId');
  private creators = new MockStore<Creator>(CREATOR_SEED, 'creatorId');
  private tags = new MockStore<ContentTag>(TAG_SEED, 'tagId');

  /** This module's mutation endpoints (createContent, updateContent, deleteContent, addTag,
   *  removeTag, etc.) return a bare text message ("Content created successfully"), not
   *  {message: "..."} JSON — normalize both shapes into {message} for the callers. */
  private asMessage<T>(obs: Observable<T>): Observable<{ message: string }> {
    return obs.pipe(map((res: any) => ({ message: typeof res === 'string' ? res : res?.message ?? 'Success' })));
  }

  // ---- Content assets ----
  fetchContents(): Observable<ContentAsset[]> {
    if (!environment.useMockContent) return this.http.get<ContentAsset[]>(`${this.base}/contentAsset/fetchContents`);
    return mockOf(this.content.all());
  }

  fetchContentById(id: number): Observable<ContentAsset | undefined> {
    if (!environment.useMockContent) return this.http.get<ContentAsset>(`${this.base}/contentAsset/fetchContentById/${id}`);
    return mockOf(this.content.find(c => c.contentId === id));
  }

  createContent(payload: Partial<ContentAsset>): Observable<{ message: string }> {
    if (!environment.useMockContent) {
      return this.asMessage(this.http.post(`${this.base}/contentAsset/createContent`, payload, { responseType: 'text' }));
    }
    this.content.insert({ ...payload, status: payload.status || 'Draft' } as any);
    return mockOf({ message: 'Content created successfully' });
  }

  updateContent(id: number, payload: Partial<ContentAsset>): Observable<{ message: string }> {
    if (!environment.useMockContent) {
      return this.asMessage(this.http.put(`${this.base}/contentAsset/updateContent/${id}`, payload, { responseType: 'text' }));
    }
    const c = this.content.update(id, payload);
    return c ? mockOf({ message: 'Content updated successfully' }) : mockError(404, 'Content not found');
  }

  updateContentStatus(id: number, status: string): Observable<{ message: string }> {
    if (!environment.useMockContent) {
      return this.asMessage(this.http.put(`${this.base}/contentAsset/updateContentStatus/${id}`, { status }, { responseType: 'text' }));
    }
    const patch: Partial<ContentAsset> = { status: status as any };
    if (status === 'Published') patch.publishedDate = new Date().toISOString().slice(0, 10);
    const c = this.content.update(id, patch);
    return c ? mockOf({ message: 'Status updated successfully' }) : mockError(404, 'Content not found');
  }

  deleteContent(id: number): Observable<{ message: string }> {
    if (!environment.useMockContent) {
      return this.asMessage(this.http.delete(`${this.base}/contentAsset/deleteContent/${id}`, { responseType: 'text' }));
    }
    const c = this.content.find(x => x.contentId === id);
    if (!c) return mockError(404, 'Content not found');
    if (c.status !== 'Draft') return mockError(400, 'Content can only be deleted when status is Draft');
    this.content.remove(id);
    return mockOf({ message: 'Content deleted successfully' });
  }

  // ---- Creators ----
  fetchCreators(): Observable<Creator[]> {
    if (!environment.useMockContent) return this.http.get<Creator[]>(`${this.base}/creator/fetchCreators`);
    return mockOf(this.creators.all());
  }

  fetchCreatorById(id: number): Observable<Creator | undefined> {
    if (!environment.useMockContent) return this.http.get<Creator>(`${this.base}/creator/fetchCreatorById/${id}`);
    return mockOf(this.creators.find(c => c.creatorId === id));
  }

  createCreator(payload: Partial<Creator>): Observable<{ message: string }> {
    if (!environment.useMockContent) {
      return this.asMessage(this.http.post(`${this.base}/creator/createCreator`, payload, { responseType: 'text' }));
    }
    if (!payload.userId || !payload.displayName) return mockError(400, 'userId and displayName are required');
    this.creators.insert({ ...payload, status: payload.status || 'PendingReview' } as any);
    return mockOf({ message: 'Creator created successfully' });
  }

  updateCreator(id: number, payload: Partial<Creator>): Observable<{ message: string }> {
    if (!environment.useMockContent) {
      return this.asMessage(this.http.put(`${this.base}/creator/updateCreator/${id}`, payload, { responseType: 'text' }));
    }
    const c = this.creators.update(id, payload);
    return c ? mockOf({ message: 'Creator updated successfully' }) : mockError(404, 'Creator not found');
  }

  updateCreatorStatus(id: number, status: string): Observable<{ message: string }> {
    if (!environment.useMockContent) {
      return this.asMessage(this.http.put(`${this.base}/creator/updateCreatorStatus/${id}`, { status }, { responseType: 'text' }));
    }
    const c = this.creators.update(id, { status } as any);
    return c ? mockOf({ message: 'Status updated successfully' }) : mockError(404, 'Creator not found');
  }

  contentByCreator(creatorId: number): ContentAsset[] {
    return this.content.filterBy(c => c.creatorId === creatorId);
  }

  // ---- Tags ----
  /** Real backend has no "get all tags" endpoint — only fetchTagsByContent/{contentId}. This
   *  aggregates tags across every content asset to give the Tag Management screen a full list. */
  fetchTags(): Observable<ContentTag[]> {
    if (!environment.useMockContent) {
      return this.fetchContents().pipe(
        switchMap(contents => contents.length
          ? forkJoin(contents.map(c => this.tagsByContent(c.contentId)))
          : of([] as ContentTag[][])),
        map(lists => lists.flat())
      );
    }
    return mockOf(this.tags.all());
  }

  tagsByContent(contentId: number): Observable<ContentTag[]> {
    if (!environment.useMockContent) return this.http.get<ContentTag[]>(`${this.base}/contentTag/fetchTagsByContent/${contentId}`);
    return mockOf(this.tags.filterBy(t => t.contentId === contentId));
  }

  addTag(payload: Partial<ContentTag>): Observable<{ message: string }> {
    if (!environment.useMockContent) {
      return this.asMessage(this.http.post(`${this.base}/contentTag/addTag`, payload, { responseType: 'text' }));
    }
    this.tags.insert(payload as any);
    return mockOf({ message: 'Tag added successfully' });
  }

  removeTag(tagId: number): Observable<{ message: string }> {
    if (!environment.useMockContent) {
      return this.asMessage(this.http.delete(`${this.base}/contentTag/removeTag/${tagId}`, { responseType: 'text' }));
    }
    this.tags.remove(tagId);
    return mockOf({ message: 'Tag removed successfully' });
  }
}
