import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { forkJoin } from 'rxjs';
import { EditorialClient } from '../../core/api/editorial-client';
import { ContentClient } from '../../core/api/content-client';
import { LicensingClient } from '../../core/api/licensing-client';
import { PublicationSchedule as ScheduleModel } from '../../core/models/editorial.models';
import { AuthService } from '../../core/auth/auth.service';
import { StatusBadge } from '../../shared/components/status-badge';
import { LoadingSpinner } from '../../shared/components/loading-spinner';
import { RowMenu, RowMenuItem } from '../../shared/components/row-menu';
import { Pagination } from '../../shared/components/pagination';
import { FitRowsDirective } from '../../shared/directives/fit-rows.directive';
import { ToastService } from '../../shared/services/toast.service';
import { ConfirmService } from '../../shared/services/confirm.service';
import { formatDateTime } from '../../shared/utils/date-format';
import { clampContentId, contentIdError as contentIdErrorFor } from '../../shared/utils/content-id';

@Component({
  selector: 'app-publication-schedule',
  imports: [FormsModule, StatusBadge, LoadingSpinner, RowMenu, Pagination, FitRowsDirective],
  templateUrl: './publication-schedule.html'
})
export class PublicationSchedule implements OnInit {
  private editorial = inject(EditorialClient);
  private content = inject(ContentClient);
  private licensing = inject(LicensingClient);
  private toast = inject(ToastService);
  private confirm = inject(ConfirmService);
  auth = inject(AuthService);

  formatDateTime = formatDateTime;

  canPublish(): boolean {
    return this.auth.hasPermission('editorial:manage');
  }

  loading = signal(true);
  schedules = signal<ScheduleModel[]>([]);
  contentTitles = signal<Record<number, string>>({});

  page = signal(0);
  pageSize = signal(10);
  totalPages = computed(() => Math.max(1, Math.ceil(this.schedules().length / this.pageSize())));
  pagedSchedules = computed(() => this.schedules().slice(this.page() * this.pageSize(), (this.page() + 1) * this.pageSize()));

  onRowsThatFit(n: number) {
    if (n === this.pageSize()) return;
    this.pageSize.set(n);
    this.page.set(0);
  }

  creating = signal(false);
  form = { contentID: 0, publishDateTime: '', expiryDateTime: '', territory: '' };
  contentIdTouched = signal(false);
  territoryTouched = signal(false);

  get contentIdError(): string {
    return this.contentIdTouched() ? contentIdErrorFor(this.form.contentID) : '';
  }

  onContentIdChange(value: number) {
    this.form.contentID = clampContentId(value);
  }

  get territoryError(): string {
    if (!this.territoryTouched()) return '';
    const v = this.form.territory.trim();
    if (!v) return 'Territory is required';
    if (/\d/.test(v)) return 'Territory may not contain numbers';
    if (/[^a-zA-Z ,]/.test(v)) return 'Territory may only contain letters, commas and spaces';
    if (/,\s/.test(v)) return 'No spaces after a comma — use "US,CA" not "US, CA"';
    return '';
  }

  onTerritoryChange(value: string) {
    this.form.territory = value;
    this.territoryTouched.set(true);
  }

  cancelling = signal<ScheduleModel | null>(null);
  cancelReason = '';

  viewing = signal<ScheduleModel | null>(null);

  ngOnInit() {
    this.load();
    this.content.fetchContents().subscribe(list => {
      const map: Record<number, string> = {};
      list.forEach(c => map[c.contentId] = c.title);
      this.contentTitles.set(map);
    });
  }

  load() {
    this.loading.set(true);
    this.editorial.getAllSchedules().subscribe(rows => {
      this.schedules.set(rows);
      this.loading.set(false);
    });
  }

  titleFor(id: number): string {
    return this.contentTitles()[id] ?? ('CNT-' + id);
  }

  openCreate() {
    this.form = { contentID: 0, publishDateTime: '', expiryDateTime: '', territory: '' };
    this.contentIdTouched.set(false);
    this.territoryTouched.set(false);
    this.creating.set(true);
  }

  checkingPrereqs = signal(false);

  create() {
    this.contentIdTouched.set(true);
    this.territoryTouched.set(true);
    if (contentIdErrorFor(this.form.contentID) || !this.form.publishDateTime || !this.form.expiryDateTime || this.territoryError) return;

    // Business rule: a rights manager must have granted a license and a territory restriction
    // for this content before it can be scheduled for publication.
    this.checkingPrereqs.set(true);
    forkJoin({
      licenses: this.licensing.getAllLicenses('Active'),
      restrictions: this.licensing.getTerritoryRestrictions(this.form.contentID)
    }).subscribe({
      next: ({ licenses, restrictions }) => {
        this.checkingPrereqs.set(false);
        const hasLicense = licenses.some(l => l.contentId === this.form.contentID);
        const hasRestriction = restrictions.length > 0;

        if (!hasLicense || !hasRestriction) {
          const missing = [!hasLicense && 'an active license', !hasRestriction && 'a territory restriction'].filter(Boolean).join(' and ');
          this.toast.warn(`This content needs ${missing} before it can be scheduled. Set it up in Licensing first.`);
          return;
        }

        this.editorial.createSchedule(this.form).subscribe({
          next: () => {
            this.toast.ok('Schedule created successfully');
            this.creating.set(false);
            this.load();
          },
          error: (err) => this.toast.warn(err?.error?.message ?? 'Unable to create schedule')
        });
      },
      error: (err) => {
        this.checkingPrereqs.set(false);
        this.toast.warn(err?.error?.message ?? 'Unable to verify licensing prerequisites');
      }
    });
  }

  publish(s: ScheduleModel) {
    this.editorial.publishSchedule(s.scheduleID).subscribe(() => {
      this.toast.ok('Content published');
      this.load();
    });
  }

  openCancel(s: ScheduleModel) {
    this.cancelReason = '';
    this.cancelling.set(s);
  }

  confirmCancel() {
    const s = this.cancelling();
    if (!s || !this.cancelReason.trim()) return;
    this.editorial.cancelSchedule(s.scheduleID, this.cancelReason).subscribe(() => {
      this.toast.ok('Schedule cancelled');
      this.cancelling.set(null);
      this.load();
    });
  }

  async remove(s: ScheduleModel) {
    const ok = await this.confirm.ask(`Delete this schedule for "${this.titleFor(s.contentID)}"?`, 'Delete', true);
    if (!ok) return;
    this.editorial.deleteSchedule(s.scheduleID).subscribe({
      next: () => { this.toast.ok('Schedule deleted'); this.load(); },
      error: (err) => this.toast.warn(err?.error?.message ?? 'Unable to delete schedule')
    });
  }

  menuFor(s: ScheduleModel): RowMenuItem[] {
    const items: RowMenuItem[] = [{ label: 'View', action: 'view' }];
    if (s.status !== 'Scheduled') return items;
    if (this.canPublish()) items.push({ label: 'Publish', action: 'publish' });
    if (this.auth.hasPermission('editorial:manage')) items.push({ label: 'Cancel', action: 'cancel' });
    if (this.auth.hasPermission('editorial:manage')) items.push({ label: 'Delete', action: 'delete' });
    return items;
  }

  onAction(action: string, s: ScheduleModel) {
    if (action === 'view') this.viewing.set(s);
    if (action === 'publish') this.publish(s);
    if (action === 'cancel') this.openCancel(s);
    if (action === 'delete') this.remove(s);
  }
}
