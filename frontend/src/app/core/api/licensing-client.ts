import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, forkJoin, of } from 'rxjs';
import { map, switchMap } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import { MockStore, mockOf, mockError } from './mock-store';
import { LicenseAgreement, LicenseExpiringSoon, TerritoryRestriction } from '../models/licensing.models';
import { AuthService } from '../auth/auth.service';
import { NotificationClient } from './notification-client';
import { ContentClient } from './content-client';

const LICENSE_SEED: LicenseAgreement[] = [
  { licenseId: 8841, contentId: 10482, licensorId: 1, licenseeRef: 'StreamCo Global', territory: 'US, CA', rightsType: 'Exclusive', startDate: '2026-01-01', endDate: '2027-01-01', licenseFee: 12000, status: 'Active' },
  { licenseId: 8842, contentId: 10479, licensorId: 2, licenseeRef: 'AudioWave Inc', territory: 'Global', rightsType: 'Non-Exclusive', startDate: '2026-02-01', endDate: '2026-08-05', licenseFee: 4200, status: 'Active' },
  { licenseId: 8843, contentId: 10475, licensorId: 3, licenseeRef: 'PhotoHub Media', territory: 'EU', rightsType: 'Limited', startDate: '2025-06-01', endDate: '2026-06-01', licenseFee: 1800, status: 'Expired' },
  { licenseId: 8844, contentId: 10466, licensorId: 5, licenseeRef: 'FoodNet Studios', territory: 'US', rightsType: 'Exclusive', startDate: '2026-03-01', endDate: '2028-03-01', licenseFee: 9000, status: 'Active' }
];

const TERRITORY_SEED: TerritoryRestriction[] = [
  { restrictionId: 2201, contentId: 10482, restrictedCountries: 'CN, RU', allowedCountries: 'US, CA, EU', effectiveDate: '2026-01-01', status: 'Active' },
  { restrictionId: 2202, contentId: 10475, restrictedCountries: 'None', allowedCountries: 'EU only', effectiveDate: '2025-06-01', status: 'Active' }
];

@Injectable({ providedIn: 'root' })
export class LicensingClient {
  private http = inject(HttpClient);
  private base = `${environment.apiBaseUrl}/licensing/mediaHub/contentLicensing`;
  private auth = inject(AuthService);
  private notification = inject(NotificationClient);
  private content = inject(ContentClient);

  private notify(message: string) {
    const userId = this.auth.currentUser()?.userId ?? 1;
    this.notification.create({ userId, message, category: 'LICENSE' }).subscribe();
  }

  private licenses = new MockStore<LicenseAgreement>(LICENSE_SEED, 'licenseId');
  private restrictions = new MockStore<TerritoryRestriction>(TERRITORY_SEED, 'restrictionId');

  getAllLicenses(status?: string): Observable<LicenseAgreement[]> {
    if (!environment.useMockLicensing) return this.http.get<LicenseAgreement[]>(`${this.base}/getAllLicenses/v1.0${status ? '?status=' + status : ''}`);
    const rows = status ? this.licenses.filterBy(l => l.status === status) : this.licenses.all();
    return mockOf(rows);
  }

  getExpiringSoon(): Observable<LicenseExpiringSoon[]> {
    if (!environment.useMockLicensing) return this.http.get<LicenseExpiringSoon[]>(`${this.base}/getExpiringSoonLicenses/v1.0`);
    const today = new Date();
    const rows = this.licenses.filterBy(l => l.status === 'Active').map(l => {
      const end = new Date(l.endDate);
      const days = Math.ceil((end.getTime() - today.getTime()) / 86400000);
      return { licenseId: l.licenseId, territory: l.territory, endDate: l.endDate, daysRemaining: days };
    }).filter(x => x.daysRemaining >= 0 && x.daysRemaining <= 7);
    return mockOf(rows);
  }

  getLicense(id: number): Observable<LicenseAgreement | undefined> {
    if (!environment.useMockLicensing) return this.http.get<LicenseAgreement>(`${this.base}/getLicense/v1.0/${id}`);
    return mockOf(this.licenses.find(l => l.licenseId === id));
  }

  createLicense(payload: Partial<LicenseAgreement>): Observable<{ message: string }> {
    if (!environment.useMockLicensing) return this.http.post<{ message: string }>(`${this.base}/createLicense/v1.0`, payload);
    const row = this.licenses.insert({ ...payload, status: 'Active' } as any);
    this.notify(`New license LIC-${row.licenseId} created for content CNT-${row.contentId} (${row.territory}).`);
    return mockOf({ message: 'License created successfully' });
  }

  updateLicense(id: number, payload: Partial<LicenseAgreement>): Observable<{ message: string }> {
    if (!environment.useMockLicensing) return this.http.put<{ message: string }>(`${this.base}/updateLicense/v1.0/${id}`, payload);
    const l = this.licenses.find(x => x.licenseId === id);
    if (!l) return mockError(404, 'License not found');
    if (l.status === 'Expired' || l.status === 'Terminated') return mockError(400, `License is ${l.status} and read-only`);
    this.licenses.update(id, payload);
    return mockOf({ message: 'License updated successfully' });
  }

  analytics(): Observable<{ totalLicenses: number; activeLicenses: number; expiredLicenses: number; terminatedLicenses: number }> {
    const rows = this.licenses.all();
    return mockOf({
      totalLicenses: rows.length,
      activeLicenses: rows.filter(l => l.status === 'Active').length,
      expiredLicenses: rows.filter(l => l.status === 'Expired').length,
      terminatedLicenses: rows.filter(l => l.status === 'Terminated').length
    });
  }

  // ---- Territory restrictions ----
  /** Real backend has no "get all" endpoint — only getTerritoryRestriction/v1.0/{contentId}.
   *  When no contentId is given, aggregate across every content asset instead. */
  getTerritoryRestrictions(contentId?: number): Observable<TerritoryRestriction[]> {
    if (!environment.useMockLicensing) {
      if (contentId) return this.http.get<TerritoryRestriction[]>(`${this.base}/getTerritoryRestriction/v1.0/${contentId}`);
      return this.content.fetchContents().pipe(
        switchMap(contents => contents.length
          ? forkJoin(contents.map(c => this.http.get<TerritoryRestriction[]>(`${this.base}/getTerritoryRestriction/v1.0/${c.contentId}`)))
          : of([] as TerritoryRestriction[][])),
        map(lists => lists.flat())
      );
    }
    const rows = contentId ? this.restrictions.filterBy(t => t.contentId === contentId && t.status === 'Active') : this.restrictions.all();
    return mockOf(rows);
  }

  createTerritoryRestriction(payload: Partial<TerritoryRestriction>): Observable<{ message: string }> {
    if (!environment.useMockLicensing) return this.http.post<{ message: string }>(`${this.base}/createTerritoryRestriction/v1.0`, payload);
    this.restrictions.insert({ ...payload, status: 'Active' } as any);
    return mockOf({ message: 'Territory rule created successfully' });
  }

  updateTerritoryRestriction(id: number, payload: Partial<TerritoryRestriction>): Observable<{ message: string }> {
    if (!environment.useMockLicensing) return this.http.put<{ message: string }>(`${this.base}/updateTerritoryRestriction/v1.0/${id}`, payload);
    const t = this.restrictions.update(id, payload);
    return t ? mockOf({ message: 'Territory rule updated successfully' }) : mockError(404, 'Territory restriction not found');
  }

  /** Frontend-only extension — TerritoryRestrictionRequestDTO has no status field and the real
   *  backend has no endpoint to toggle a restriction's Active/Inactive state at all. */
  toggleTerritoryStatus(id: number): Observable<{ message: string }> {
    if (!environment.useMockLicensing) {
      return mockError(400, 'Toggling territory restriction status is not supported by the backend yet.');
    }
    const t = this.restrictions.find(x => x.restrictionId === id);
    if (!t) return mockError(404, 'Territory restriction not found');
    this.restrictions.update(id, { status: t.status === 'Active' ? 'Inactive' : 'Active' });
    return mockOf({ message: 'Territory restriction updated' });
  }
}
