import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { LicensingClient } from '../../core/api/licensing-client';
import { LicenseAgreement, LicenseExpiringSoon, RightsType } from '../../core/models/licensing.models';
import { StatusBadge } from '../../shared/components/status-badge';
import { RowMenu, RowMenuItem } from '../../shared/components/row-menu';
import { LoadingSpinner } from '../../shared/components/loading-spinner';
import { Pagination } from '../../shared/components/pagination';
import { ToastService } from '../../shared/services/toast.service';

@Component({
  selector: 'app-licensing-list',
  imports: [FormsModule, RouterLink, StatusBadge, RowMenu, LoadingSpinner, Pagination],
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
  pageSize = 10;
  totalPages = computed(() => Math.max(1, Math.ceil(this.licenses().length / this.pageSize)));
  pagedLicenses = computed(() => this.licenses().slice(this.page() * this.pageSize, (this.page() + 1) * this.pageSize));

  creating = signal(false);
  form: { contentId: number; licensorId: number; licenseeRef: string; territory: string; rightsType: RightsType; startDate: string; endDate: string; licenseFee: number } =
    { contentId: 0, licensorId: 0, licenseeRef: '', territory: '', rightsType: 'Non-Exclusive', startDate: '', endDate: '', licenseFee: 0 };

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
    this.creating.set(true);
  }

  create() {
    if (!this.form.contentId || !this.form.licenseeRef.trim() || !this.form.territory.trim() || !this.form.startDate || !this.form.endDate) return;
    this.licensing.createLicense(this.form).subscribe({
      next: () => { this.toast.ok('License created successfully'); this.creating.set(false); this.load(); },
      error: () => this.toast.warn('Content not found in Content Catalog')
    });
  }
}
