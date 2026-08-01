export type ReviewStatus = 'Pending' | 'Completed';
export type ReviewDecision = 'Approved' | 'Rejected' | 'RevisionRequired' | null;
export type CollectionCategory = 'Featured' | 'Trending' | 'Curated' | 'New';
export type CollectionStatus = 'Scheduled' | 'Active' | 'Expired';
export type ScheduleStatus = 'Scheduled' | 'Published' | 'Cancelled';

export interface EditorialReview {
  reviewID: number;
  contentID: number;
  reviewerID: number;
  submissionDate: string;
  reviewDate?: string;
  decision: ReviewDecision;
  remarks?: string;
  status: ReviewStatus;
}

export interface ContentCollection {
  collectionID: number;
  name: string;
  category: CollectionCategory;
  contentIDs: number[];
  publishDate: string;
  expiryDate: string;
  status: CollectionStatus;
}

export interface PublicationSchedule {
  scheduleID: number;
  contentID: number;
  publishDateTime: string;
  expiryDateTime: string;
  territory: string;
  status: ScheduleStatus;
}
