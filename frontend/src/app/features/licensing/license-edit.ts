import { Component, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { LicensingClient } from '../../core/api/licensing-client';
import { LicenseAgreement, LicenseStatus, RightsType } from '../../core/models/licensing.models';
import { StatusBadge } from '../../shared/components/status-badge';
import { LoadingSpinner } from '../../shared/components/loading-spinner';
import { ToastService } from '../../shared/services/toast.service';

@Component({
  selector: 'app-license-edit',
  imports: [FormsModule, RouterLink, StatusBadge, LoadingSpinner],
  templateUrl: './license-edit.html'
})
export class LicenseEdit implements OnInit {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private licensing = inject(LicensingClient);
  private toast = inject(ToastService);

  loading = signal(true);
  license = signal<LicenseAgreement | null>(null);
  form: { territory: string; rightsType: RightsType; endDate: string; status: LicenseStatus } =
    { territory: '', rightsType: 'Non-Exclusive', endDate: '', status: 'Active' };

  get locked(): boolean {
    const l = this.license();
    return l?.status === 'Expired' || l?.status === 'Terminated';
  }

  ngOnInit() {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.licensing.getLicense(id).subscribe(l => {
      this.license.set(l ?? null);
      if (l) this.form = { territory: l.territory, rightsType: l.rightsType, endDate: l.endDate, status: l.status };
      this.loading.set(false);
    });
  }

  save() {
    const l = this.license();
    if (!l) return;
    this.licensing.updateLicense(l.licenseId, this.form).subscribe({
      next: () => {
        this.toast.ok('License updated successfully');
        this.router.navigate(['/licensing', l.licenseId]);
      },
      error: (err) => this.toast.warn(err?.error?.message ?? 'Unable to update license')
    });
  }

  terminate() {
    const l = this.license();
    if (!l) return;
    // Send the full form, not just { status }, since the backend overwrites every field
    // from the request body — a partial payload would null out territory/rightsType/endDate.
    this.licensing.updateLicense(l.licenseId, { ...this.form, status: 'Terminated' }).subscribe({
      next: () => {
        this.toast.ok('License terminated');
        this.router.navigate(['/licensing', l.licenseId]);
      },
      error: (err) => this.toast.warn(err?.error?.message ?? 'Unable to terminate license')
    });
  }
}
