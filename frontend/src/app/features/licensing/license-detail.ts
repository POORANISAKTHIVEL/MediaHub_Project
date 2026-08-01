import { Component, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { LicensingClient } from '../../core/api/licensing-client';
import { LicenseAgreement } from '../../core/models/licensing.models';
import { StatusBadge } from '../../shared/components/status-badge';
import { LoadingSpinner } from '../../shared/components/loading-spinner';

@Component({
  selector: 'app-license-detail',
  imports: [RouterLink, StatusBadge, LoadingSpinner],
  templateUrl: './license-detail.html'
})
export class LicenseDetail implements OnInit {
  private route = inject(ActivatedRoute);
  private licensing = inject(LicensingClient);

  loading = signal(true);
  license = signal<LicenseAgreement | null>(null);

  ngOnInit() {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.licensing.getLicense(id).subscribe(l => {
      this.license.set(l ?? null);
      this.loading.set(false);
    });
  }
}
