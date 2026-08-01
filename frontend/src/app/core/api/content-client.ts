import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, forkJoin, of } from 'rxjs';
import { map, switchMap } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import { MockStore, mockOf, mockError } from './mock-store';
import { ContentAsset, ContentTag, Creator } from '../models/content.models';

const CONTENT_SEED: ContentAsset[] = [
  { contentId: 10482, creatorId: 1, title: 'The Silent Frontier', type: 'Video', genre: 'Documentary', language: 'English', durationSeconds: 5400, synopsis: 'A documentary exploring remote frontier communities and the people who sustain them across four continents.', filePath: '/media/10482.mp4', thumbnailPath: '', status: 'Published', publishedDate: '2026-07-18' },
  { contentId: 10479, creatorId: 2, title: 'Modern Jazz Sessions Vol. 3', type: 'Audio', genre: 'Jazz', language: 'English', durationSeconds: 2700, synopsis: 'Live jazz session recordings.', filePath: '/media/10479.mp3', thumbnailPath: '', status: 'UnderReview' },
  { contentId: 10475, creatorId: 3, title: 'Urban Photography Guide', type: 'Article', genre: 'Photography', language: 'English', durationSeconds: 0, synopsis: 'A guide to urban photography techniques.', filePath: '/media/10475.pdf', thumbnailPath: '', status: 'Published', publishedDate: '2026-07-10' },
  { contentId: 10470, creatorId: 4, title: 'Deep Space — 4K Series', type: 'Video', genre: 'Science', language: 'English', durationSeconds: 9600, synopsis: 'A 4K documentary series on deep space exploration.', filePath: '/media/10470.mp4', thumbnailPath: '', status: 'Draft' },
  { contentId: 10466, creatorId: 5, title: 'Culinary Journeys: Asia', type: 'Video', genre: 'Food', language: 'English', durationSeconds: 4200, synopsis: 'Exploring the culinary traditions of Asia.', filePath: '/media/10466.mp4', thumbnailPath: '', status: 'Published', publishedDate: '2026-07-09' }
];

const CREATOR_SEED: Creator[] = [
  { creatorId: 1, userId: 101, displayName: 'Aria Films', genre: 'Documentary', country: 'US', royaltyTier: 'Platinum', bankAccountRef: 'US-IBAN-0021', status: 'Active' },
  { creatorId: 2, userId: 102, displayName: 'BlueNote Studio', genre: 'Audio', country: 'US', royaltyTier: 'Gold', bankAccountRef: 'US-IBAN-0034', status: 'Active' },
  { creatorId: 3, userId: 103, displayName: 'Lena Ortiz', genre: 'Photography', country: 'ES', royaltyTier: 'Silver', bankAccountRef: 'ES-IBAN-0087', status: 'Active' },
  { creatorId: 4, userId: 104, displayName: 'Cosmos Media', genre: 'Science', country: 'US', royaltyTier: 'Gold', bankAccountRef: 'US-IBAN-0099', status: 'PendingReview' },
  { creatorId: 5, userId: 105, displayName: 'Tastemakers', genre: 'Food', country: 'US', royaltyTier: 'Standard', bankAccountRef: 'US-IBAN-0142', status: 'Active' }
];

const TAG_SEED: ContentTag[] = [
  { tagId: 1, contentId: 10482, tagName: 'documentary', tagCategory: 'Genre' },
  { tagId: 2, contentId: 10482, tagName: 'nature', tagCategory: 'Theme' },
  { tagId: 3, contentId: 10479, tagName: 'jazz', tagCategory: 'Genre' },
  { tagId: 4, contentId: 10470, tagName: 'space', tagCategory: 'Theme' },
  { tagId: 5, contentId: 10466, tagName: 'travel', tagCategory: 'Mood' }
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
