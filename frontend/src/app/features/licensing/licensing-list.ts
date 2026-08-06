import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { LicensingClient } from '../../core/api/licensing-client';
import { LicenseAgreement, LicenseExpiringSoon, RightsType } from '../../core/models/licensing.models';
import { StatusBadge } from '../../shared/components/status-badge';
import { RowMenu, RowMenuItem } from '../../shared/components/row-menu';
import { LoadingSpinner } from '../../shared/components/loading-spinner';
import { Pagination } from '../../shared/components/pagination';
import { FitRowsDirective } from '../../shared/directives/fit-rows.directive';
import { ToastService } from '../../shared/services/toast.service';
import { CONTENT_ID_MAX_DIGITS, clampContentId, clampDigits, contentIdError as contentIdErrorFor, digitFieldError, digitLimitMessage, textLimitMessage } from '../../shared/utils/content-id';

const LICENSOR_ID_MAX_DIGITS = 10;
const LICENSE_FEE_MAX_DIGITS = 4;
const LICENSEE_REF_MAX_LENGTH = 50;

@Component({
  selector: 'app-licensing-list',
  imports: [FormsModule, RouterLink, StatusBadge, RowMenu, LoadingSpinner, Pagination, FitRowsDirective],
  templateUrl: './licensing-list.html'
})
export class LicensingList implements OnInit {
  private licensing = inject(LicensingClient);
  private toast = inject(ToastService);
  private router = inject(Router);

  loading = signal(true);
  licenses = signal<LicenseAgreement[]>([]);
  expiringSoon = signal<LicenseExpiringSoon[]>([]);

  page = signal(0);
  pageSize = signal(10);
  totalPages = computed(() => Math.max(1, Math.ceil(this.licenses().length / this.pageSize())));
  pagedLicenses = computed(() => this.licenses().slice(this.page() * this.pageSize(), (this.page() + 1) * this.pageSize()));

  onRowsThatFit(n: number) {
    if (n === this.pageSize()) return;
    this.pageSize.set(n);
    this.page.set(0);
  }

  creating = signal(false);
  form: { contentId: number; licensorId: number; licenseeRef: string; territory: string; rightsType: RightsType; startDate: string; endDate: string; licenseFee: number } =
    { contentId: 0, licensorId: 0, licenseeRef: '', territory: '', rightsType: 'Non-Exclusive', startDate: '', endDate: '', licenseFee: 0 };
  contentIdTouched = signal(false);
  contentIdLimitMsg = signal('');
  licensorIdTouched = signal(false);
  licensorIdLimitMsg = signal('');
  licenseFeeLimitMsg = signal('');
  licenseeRefLimitMsg = signal('');
  licenseeRefMax = LICENSEE_REF_MAX_LENGTH;

  get contentIdError(): string {
    return this.contentIdLimitMsg() || (this.contentIdTouched() ? contentIdErrorFor(this.form.contentId) : '');
  }

  onContentIdChange(value: number) {
    this.contentIdLimitMsg.set(digitLimitMessage(value, CONTENT_ID_MAX_DIGITS, 'Content ID'));
    this.form.contentId = clampContentId(value);
  }

  get licensorIdError(): string {
    return this.licensorIdLimitMsg() || (this.licensorIdTouched() ? digitFieldError(this.form.licensorId, LICENSOR_ID_MAX_DIGITS, 'Licensor ID') : '');
  }

  onLicensorIdChange(value: number) {
    this.licensorIdLimitMsg.set(digitLimitMessage(value, LICENSOR_ID_MAX_DIGITS, 'Licensor ID'));
    this.form.licensorId = clampDigits(value, LICENSOR_ID_MAX_DIGITS);
  }

  onLicenseFeeChange(value: number) {
    this.licenseFeeLimitMsg.set(digitLimitMessage(value, LICENSE_FEE_MAX_DIGITS, 'License Fee'));
    this.form.licenseFee = clampDigits(value, LICENSE_FEE_MAX_DIGITS);
  }

  onLicenseeRefChange(value: string) {
    const raw = value ?? '';
    this.licenseeRefLimitMsg.set(textLimitMessage(raw, LICENSEE_REF_MAX_LENGTH, 'Licensee Ref'));
    this.form.licenseeRef = raw.slice(0, LICENSEE_REF_MAX_LENGTH);
  }

  ngOnInit() {
    this.load();
  }

  load() {
    this.loading.set(true);
    this.licensing.getAllLicenses().subscribe(rows => {
      this.licenses.set(rows);
      this.loading.set(false);
    });
    this.licensing.getExpiringSoon().subscribe(rows => this.expiringSoon.set(rows));
  }

  menuFor(_l: LicenseAgreement): RowMenuItem[] {
    return [{ label: 'View', action: 'view' }, { label: 'Edit', action: 'edit' }];
  }

  onAction(action: string, l: LicenseAgreement) {
    if (action === 'view') this.router.navigate(['/licensing', l.licenseId]);
    if (action === 'edit') this.router.navigate(['/licensing', l.licenseId, 'edit']);
  }

  openCreate() {
    this.form = { contentId: 0, licensorId: 0, licenseeRef: '', territory: '', rightsType: 'Non-Exclusive', startDate: '', endDate: '', licenseFee: 0 };
    this.contentIdTouched.set(false);
    this.contentIdLimitMsg.set('');
    this.licensorIdTouched.set(false);
    this.licensorIdLimitMsg.set('');
    this.licenseFeeLimitMsg.set('');
    this.licenseeRefLimitMsg.set('');
    this.creating.set(true);
  }

  create() {
    this.contentIdTouched.set(true);
    this.licensorIdTouched.set(true);
    if (contentIdErrorFor(this.form.contentId) || digitFieldError(this.form.licensorId, LICENSOR_ID_MAX_DIGITS, 'Licensor ID') || !this.form.licenseeRef.trim() || !this.form.territory.trim() || !this.form.startDate || !this.form.endDate) return;
    this.licensing.createLicense(this.form).subscribe({
      next: () => { this.toast.ok('License created successfully'); this.creating.set(false); this.load(); },
      error: () => this.toast.warn('Content not found in Content Catalog')
    });
  }
}
