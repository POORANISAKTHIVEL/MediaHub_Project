import { Component, inject, OnInit, signal } from '@angular/core';
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
import { ToastService } from '../../shared/services/toast.service';
import { ConfirmService } from '../../shared/services/confirm.service';

@Component({
  selector: 'app-publication-schedule',
  imports: [FormsModule, StatusBadge, LoadingSpinner, RowMenu],
  templateUrl: './publication-schedule.html'
})
export class PublicationSchedule implements OnInit {
  private editorial = inject(EditorialClient);
  private content = inject(ContentClient);
  private licensing = inject(LicensingClient);
  private toast = inject(ToastService);
  private confirm = inject(ConfirmService);
  auth = inject(AuthService);

  canPublish(): boolean {
    return this.auth.hasRole('admin') || this.auth.hasPermission('content:publish');
  }

  loading = signal(true);
  schedules = signal<ScheduleModel[]>([]);
  contentTitles = signal<Record<number, string>>({});

  creating = signal(false);
  form = { contentID: 0, publishDateTime: '', expiryDateTime: '', territory: '' };

  cancelling = signal<ScheduleModel | null>(null);
  cancelReason = '';

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
    this.creating.set(true);
  }

  checkingPrereqs = signal(false);

  create() {
    if (!this.form.contentID || !this.form.publishDateTime || !this.form.expiryDateTime || !this.form.territory.trim()) return;

    // Business rule: a rights manager must have granted a license and a territory restriction
    // for this content before it can be scheduled for publication.
    this.checkingPrereqs.set(true);
    forkJoin({
      licenses: this.licensing.getAllLicenses('Active'),
      restrictions: this.licensing.getTerritoryRestrictions(this.form.contentID)
    }).subscribe(({ licenses, restrictions }) => {
      this.checkingPrereqs.set(false);
      const hasLicense = licenses.some(l => l.contentId === this.form.contentID);
      const hasRestriction = restrictions.length > 0;

      if (!hasLicense || !hasRestriction) {
        const missing = [!hasLicense && 'an active license', !hasRestriction && 'a territory restriction'].filter(Boolean).join(' and ');
        this.toast.warn(`This content needs ${missing} before it can be scheduled. Set it up in Licensing first.`);
        return;
      }

      this.editorial.createSchedule(this.form).subscribe(() => {
        this.toast.ok('Schedule created successfully');
        this.creating.set(false);
        this.load();
      });
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
    if (s.status !== 'Scheduled') return [];
    const items: RowMenuItem[] = [];
    if (this.canPublish()) items.push({ label: 'Publish', action: 'publish' });
    if (this.auth.hasPermission('content:write')) items.push({ label: 'Cancel', action: 'cancel' });
    if (this.auth.hasPermission('content:delete')) items.push({ label: 'Delete', action: 'delete' });
    return items;
  }

  onAction(action: string, s: ScheduleModel) {
    if (action === 'publish') this.publish(s);
    if (action === 'cancel') this.openCancel(s);
    if (action === 'delete') this.remove(s);
  }
}
