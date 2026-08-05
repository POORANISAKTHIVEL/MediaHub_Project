import { Component, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { RoyaltyClient } from '../../core/api/royalty-client';
import { RoyaltyStatement } from '../../core/models/royalty.models';
import { StatusBadge } from '../../shared/components/status-badge';
import { LoadingSpinner } from '../../shared/components/loading-spinner';
import { ToastService } from '../../shared/services/toast.service';
import { AuthService } from '../../core/auth/auth.service';

@Component({
  selector: 'app-statement-detail',
  imports: [RouterLink, StatusBadge, LoadingSpinner],
  templateUrl: './statement-detail.html'
})
export class StatementDetail implements OnInit {
  auth = inject(AuthService);
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private royalty = inject(RoyaltyClient);
  private toast = inject(ToastService);

  loading = signal(true);
  statement = signal<RoyaltyStatement | null>(null);

  ngOnInit() {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.royalty.getStatement(id).subscribe(s => {
      this.statement.set(s ?? null);
      this.loading.set(false);
    });
  }

  finalise() {
    const s = this.statement();
    if (!s) return;
    this.royalty.finaliseStatement(s.statementID).subscribe(() => {
      this.toast.ok('Statement finalised');
      this.statement.set({ ...s, status: 'Finalised' });
    });
  }

  processPayout() {
    const s = this.statement();
    if (!s) return;
    this.royalty.createPayout({ statementID: s.statementID, creatorID: s.creatorID, amount: s.royaltyAmount, method: 'BankTransfer' }).subscribe(() => {
      this.toast.ok('Payout created');
      this.router.navigate(['/royalty/payouts']);
    });
  }
}
