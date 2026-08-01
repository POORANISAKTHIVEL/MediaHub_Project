import { Component, inject, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { LicensingClient } from '../../core/api/licensing-client';
import { LicenseExpiringSoon } from '../../core/models/licensing.models';
import { LoadingSpinner } from '../../shared/components/loading-spinner';

@Component({
  selector: 'app-licensing-expiring',
  imports: [RouterLink, LoadingSpinner],
  templateUrl: './licensing-expiring.html'
})
export class LicensingExpiring implements OnInit {
  private licensing = inject(LicensingClient);

  loading = signal(true);
  rows = signal<LicenseExpiringSoon[]>([]);

  ngOnInit() {
    this.licensing.getExpiringSoon().subscribe(rows => {
      this.rows.set(rows);
      this.loading.set(false);
    });
  }
}
