import { Component, inject, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { RoyaltyClient } from '../../core/api/royalty-client';
import { StatCard } from '../../shared/components/stat-card';

@Component({
  selector: 'app-royalty-dashboard',
  imports: [RouterLink, StatCard],
  templateUrl: './royalty-dashboard.html'
})
export class RoyaltyDashboard implements OnInit {
  private royalty = inject(RoyaltyClient);

  totalRevenue = signal(0);
  totalRoyalty = signal(0);
  draftCount = signal(0);
  pendingPayouts = signal(0);

  ngOnInit() {
    this.royalty.getAllStatements().subscribe(rows => {
      this.totalRevenue.set(rows.reduce((s, r) => s + r.totalRevenue, 0));
      this.totalRoyalty.set(rows.reduce((s, r) => s + r.royaltyAmount, 0));
      this.draftCount.set(rows.filter(r => r.status === 'Draft').length);
    });
    this.royalty.getAllPayouts().subscribe(rows => {
      this.pendingPayouts.set(rows.filter(p => p.status === 'Pending').length);
    });
  }
}
