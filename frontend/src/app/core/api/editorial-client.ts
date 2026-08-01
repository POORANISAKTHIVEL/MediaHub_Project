import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { environment } from '../../../environments/environment';
import { MockStore, mockOf, mockError } from './mock-store';
import { CollectionCategory, ContentCollection, EditorialReview, PublicationSchedule } from '../models/editorial.models';
import { AuthService } from '../auth/auth.service';
import { NotificationClient } from './notification-client';

const REVIEW_SEED: EditorialReview[] = [
  { reviewID: 2231, contentID: 10479, reviewerID: 1, submissionDate: '2026-07-24', decision: null, remarks: '', status: 'Pending' },
  { reviewID: 2229, contentID: 10475, reviewerID: 1, submissionDate: '2026-07-23', decision: null, remarks: '', status: 'Pending' },
  { reviewID: 2224, contentID: 10470, reviewerID: 1, submissionDate: '2026-07-21', decision: null, remarks: '', status: 'Pending' },
  { reviewID: 2205, contentID: 10482, reviewerID: 1, submissionDate: '2026-07-16', reviewDate: '2026-07-18', decision: 'Approved', remarks: 'Looks great', status: 'Completed' },
  { reviewID: 2210, contentID: 10466, reviewerID: 1, submissionDate: '2026-07-06', reviewDate: '2026-07-09', decision: 'Approved', remarks: 'Approved for release', status: 'Completed' }
];

const COLLECTION_SEED: ContentCollection[] = [
  { collectionID: 1, name: 'Summer Favorites', category: 'Featured', contentIDs: [10482, 10466], publishDate: '2026-07-01', expiryDate: '2026-08-31', status: 'Active' },
  { collectionID: 2, name: 'Trending This Week', category: 'Trending', contentIDs: [10475], publishDate: '2026-07-20', expiryDate: '2026-07-27', status: 'Active' },
  { collectionID: 3, name: 'Editor Picks', category: 'Curated', contentIDs: [], publishDate: '2026-08-01', expiryDate: '2026-09-01', status: 'Scheduled' }
];

const SCHEDULE_SEED: PublicationSchedule[] = [
  { scheduleID: 1, contentID: 10470, publishDateTime: '2026-08-02T09:00:00', expiryDateTime: '2027-08-02T09:00:00', territory: 'US, CA', status: 'Scheduled' },
  { scheduleID: 2, contentID: 10479, publishDateTime: '2026-07-31T09:00:00', expiryDateTime: '2027-07-31T09:00:00', territory: 'Global', status: 'Scheduled' }
];

@Injectable({ providedIn: 'root' })
export class EditorialClient {
  private http = inject(HttpClient);
  private base = `${environment.apiBaseUrl}/editorial/MediaHub/editorial`;
  private auth = inject(AuthService);
  private notification = inject(NotificationClient);

  private notify(message: string, category: 'CONTENT' | 'EDITORIAL') {
    const userId = this.auth.currentUser()?.userId ?? 1;
    this.notification.create({ userId, message, category }).subscribe();
  }

  private reviews = new MockStore<EditorialReview>(REVIEW_SEED, 'reviewID');
  private collections = new MockStore<ContentCollection>(COLLECTION_SEED, 'collectionID');
  private schedules = new MockStore<PublicationSchedule>(SCHEDULE_SEED, 'scheduleID');

  // ---- Reviews ----
  getAllReviews(): Observable<EditorialReview[]> {
    if (!environment.useMockEditorial) return this.http.get<EditorialReview[]>(`${this.base}/reviews`);
    return mockOf(this.reviews.all());
  }

  getReview(reviewID: number): Observable<EditorialReview | undefined> {
    if (!environment.useMockEditorial) {
      return this.http.get<{ review: EditorialReview }>(`${this.base}/reviews/${reviewID}`).pipe(map(r => r.review));
    }
    return mockOf(this.reviews.find(r => r.reviewID === reviewID));
  }

  /** Submits a content asset into the review queue — mirrors POST /reviews (contentID + reviewerID),
   *  called from Content Catalog once a creator/admin is done editing a Draft asset. */
  submitForReview(contentID: number, reviewerID = 1): Observable<{ message: string }> {
    if (!environment.useMockEditorial) return this.http.post<{ message: string }>(`${this.base}/reviews`, { contentID, reviewerID });
    if (this.reviews.find(r => r.contentID === contentID && r.status === 'Pending')) {
      return mockError(409, 'This content is already in the review queue');
    }
    this.reviews.insert({
      contentID, reviewerID, submissionDate: new Date().toISOString().slice(0, 10),
      decision: null, remarks: '', status: 'Pending'
    } as any);
    this.notify(`Content CNT-${contentID} was submitted for editorial review.`, 'EDITORIAL');
    return mockOf({ message: 'Content submitted for review' });
  }

  approve(reviewID: number, remarks: string): Observable<{ message: string }> {
    if (!environment.useMockEditorial) return this.http.post<{ message: string }>(`${this.base}/reviews/${reviewID}/approve`, { remarks });
    const r = this.reviews.update(reviewID, { decision: 'Approved', status: 'Completed', remarks, reviewDate: new Date().toISOString().slice(0, 10) });
    if (!r) return mockError(404, 'Review not found');
    this.notify(`Review REV-${reviewID} was approved.`, 'EDITORIAL');
    return mockOf({ message: 'Review approved' });
  }

  reject(reviewID: number, remarks: string): Observable<{ message: string }> {
    if (!environment.useMockEditorial) return this.http.post<{ message: string }>(`${this.base}/reviews/${reviewID}/reject`, { remarks });
    const r = this.reviews.update(reviewID, { decision: 'Rejected', status: 'Completed', remarks, reviewDate: new Date().toISOString().slice(0, 10) });
    if (!r) return mockError(404, 'Review not found');
    this.notify(`Review REV-${reviewID} was rejected.`, 'EDITORIAL');
    return mockOf({ message: 'Review rejected' });
  }

  requestRevision(reviewID: number, remarks: string): Observable<{ message: string }> {
    if (!environment.useMockEditorial) return this.http.post<{ message: string }>(`${this.base}/reviews/${reviewID}/revise`, { remarks });
    const r = this.reviews.update(reviewID, { decision: 'RevisionRequired', status: 'Pending', remarks });
    return r ? mockOf({ message: 'Revision requested' }) : mockError(404, 'Review not found');
  }

  // ---- Collections ----
  getAllCollections(): Observable<ContentCollection[]> {
    if (!environment.useMockEditorial) return this.http.get<ContentCollection[]>(`${this.base}/collections`);
    return mockOf(this.collections.all());
  }

  createCollection(payload: { name: string; category: CollectionCategory; publishDate: string; expiryDate: string; contentIDs: number[] }): Observable<{ message: string }> {
    if (!environment.useMockEditorial) return this.http.post<{ message: string }>(`${this.base}/collections`, payload);
    this.collections.insert({ ...payload, status: 'Scheduled' } as any);
    return mockOf({ message: 'Collection created successfully' });
  }

  /** The real PUT /collections/{id} endpoint overwrites the whole entity from the request body
   *  (name, category, dates included) — sending only {contentIDs} would null out the rest, so
   *  callers must pass the full, already-updated collection object. */
  updateCollectionItems(collection: ContentCollection): Observable<{ message: string }> {
    if (!environment.useMockEditorial) return this.http.put<{ message: string }>(`${this.base}/collections/${collection.collectionID}`, collection);
    const c = this.collections.update(collection.collectionID, { contentIDs: collection.contentIDs });
    return c ? mockOf({ message: 'Collection updated' }) : mockError(404, 'Collection not found');
  }

  expireCollection(collectionID: number): Observable<{ message: string }> {
    if (!environment.useMockEditorial) return this.http.post<{ message: string }>(`${this.base}/collections/${collectionID}/expire`, {});
    this.collections.update(collectionID, { status: 'Expired' });
    return mockOf({ message: 'Collection expired' });
  }

  deleteCollection(collectionID: number): Observable<{ message: string }> {
    if (!environment.useMockEditorial) return this.http.delete<{ message: string }>(`${this.base}/collections/${collectionID}`);
    const c = this.collections.find(x => x.collectionID === collectionID);
    if (!c) return mockError(404, 'Collection not found');
    if (c.status === 'Active') return mockError(400, 'Cannot delete Active collection. Expire it first.');
    this.collections.remove(collectionID);
    return mockOf({ message: 'Collection deleted' });
  }

  // ---- Publication schedules ----
  getAllSchedules(): Observable<PublicationSchedule[]> {
    if (!environment.useMockEditorial) return this.http.get<PublicationSchedule[]>(`${this.base}/schedules`);
    return mockOf(this.schedules.all());
  }

  createSchedule(payload: { contentID: number; publishDateTime: string; expiryDateTime: string; territory: string }): Observable<{ message: string }> {
    if (!environment.useMockEditorial) return this.http.post<{ message: string }>(`${this.base}/schedules`, payload);
    this.schedules.insert({ ...payload, status: 'Scheduled' } as any);
    return mockOf({ message: 'Schedule created successfully' });
  }

  publishSchedule(scheduleID: number): Observable<{ message: string }> {
    if (!environment.useMockEditorial) return this.http.post<{ message: string }>(`${this.base}/schedules/${scheduleID}/publish`, {});
    const s = this.schedules.update(scheduleID, { status: 'Published' });
    if (s) this.notify(`Content CNT-${s.contentID} has been published (Schedule SCH-${scheduleID}).`, 'CONTENT');
    return mockOf({ message: 'Content published' });
  }

  cancelSchedule(scheduleID: number, reason: string): Observable<{ message: string }> {
    if (!environment.useMockEditorial) return this.http.post<{ message: string }>(`${this.base}/schedules/${scheduleID}/cancel`, { reason });
    this.schedules.update(scheduleID, { status: 'Cancelled' });
    return mockOf({ message: 'Schedule cancelled' });
  }

  deleteSchedule(scheduleID: number): Observable<{ message: string }> {
    if (!environment.useMockEditorial) return this.http.delete<{ message: string }>(`${this.base}/schedules/${scheduleID}`);
    const s = this.schedules.find(x => x.scheduleID === scheduleID);
    if (!s) return mockError(404, 'Schedule not found');
    if (s.status === 'Published') return mockError(400, 'Cannot delete Published schedule');
    this.schedules.remove(scheduleID);
    return mockOf({ message: 'Schedule deleted' });
  }
}
