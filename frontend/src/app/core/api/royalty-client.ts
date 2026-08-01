import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { MockStore, mockOf, mockError } from './mock-store';
import { RoyaltyPayout, RoyaltyRule, RoyaltyStatement } from '../models/royalty.models';
import { AuthService } from '../auth/auth.service';
import { NotificationClient } from './notification-client';

const RULE_SEED: RoyaltyRule[] = [
  { ruleID: 1, creatorTier: 'Platinum', revenueSharePercent: 70, minimumPayoutThreshold: 50, payoutFrequency: 'Monthly', effectiveDate: '2026-01-01', status: 'Active' },
  { ruleID: 2, creatorTier: 'Gold', revenueSharePercent: 60, minimumPayoutThreshold: 50, payoutFrequency: 'Monthly', effectiveDate: '2026-01-01', status: 'Active' },
  { ruleID: 3, creatorTier: 'Standard', revenueSharePercent: 45, minimumPayoutThreshold: 25, payoutFrequency: 'Quarterly', effectiveDate: '2026-01-01', status: 'Active' }
];

const STATEMENT_SEED: RoyaltyStatement[] = [
  { statementID: 88, creatorID: 1, period: '2026-Q2', totalViews: 482000, totalRevenue: 24500, royaltyAmount: 17150, status: 'Finalised' },
  { statementID: 89, creatorID: 5, period: '2026-Q2', totalViews: 118000, totalRevenue: 6200, royaltyAmount: 2790, status: 'Draft' }
];

const PAYOUT_SEED: RoyaltyPayout[] = [
  { payoutID: 1, statementID: 88, creatorID: 1, amount: 17150, payoutDate: '2026-07-15', method: 'BankTransfer', status: 'Processed' }
];

@Injectable({ providedIn: 'root' })
export class RoyaltyClient {
  private http = inject(HttpClient);
  private base = `${environment.apiBaseUrl}/royalty/api`;
  private auth = inject(AuthService);
  private notification = inject(NotificationClient);

  private notify(message: string) {
    const userId = this.auth.currentUser()?.userId ?? 1;
    this.notification.create({ userId, message, category: 'ROYALTY' }).subscribe();
  }

  private rules = new MockStore<RoyaltyRule>(RULE_SEED, 'ruleID');
  private statements = new MockStore<RoyaltyStatement>(STATEMENT_SEED, 'statementID');
  private payouts = new MockStore<RoyaltyPayout>(PAYOUT_SEED, 'payoutID');

  // ---- Rules ----
  getAllRules(): Observable<RoyaltyRule[]> {
    if (!environment.useMockRoyalty) return this.http.get<RoyaltyRule[]>(`${this.base}/royalty-rules`);
    return mockOf(this.rules.all());
  }

  createRule(payload: Partial<RoyaltyRule>): Observable<RoyaltyRule> {
    if (!environment.useMockRoyalty) return this.http.post<RoyaltyRule>(`${this.base}/royalty-rules`, payload);
    const row = this.rules.insert({ ...payload, status: 'Active' } as any);
    return mockOf(row);
  }

  /** Frontend-only extension — the real RoyaltyRuleController has no PUT endpoint for editing an
   *  existing rule's fields (only /deactivate and DELETE exist). Needs a backend addition to go live. */
  updateRule(ruleID: number, payload: Partial<RoyaltyRule>): Observable<{ message: string }> {
    if (!environment.useMockRoyalty) return mockError(400, 'Editing an existing royalty rule is not supported by the backend yet.');
    const r = this.rules.update(ruleID, payload);
    return r ? mockOf({ message: 'Rule updated successfully' }) : mockError(404, 'Rule not found');
  }

  deactivateRule(ruleID: number): Observable<{ message: string }> {
    if (!environment.useMockRoyalty) return this.http.put<{ message: string }>(`${this.base}/royalty-rules/${ruleID}/deactivate`, {});
    this.rules.update(ruleID, { status: 'Inactive' });
    return mockOf({ message: 'Rule deactivated' });
  }

  deleteRule(ruleID: number): Observable<{ message: string }> {
    if (!environment.useMockRoyalty) return this.http.delete<{ message: string }>(`${this.base}/royalty-rules/${ruleID}`);
    const r = this.rules.find(x => x.ruleID === ruleID);
    if (!r) return mockError(404, 'Rule not found');
    if (r.status === 'Active') return mockError(400, 'Cannot delete Active royalty rule. Deactivate it first.');
    this.rules.remove(ruleID);
    return mockOf({ message: 'Rule deleted' });
  }

  // ---- Statements ----
  getAllStatements(): Observable<RoyaltyStatement[]> {
    if (!environment.useMockRoyalty) return this.http.get<RoyaltyStatement[]>(`${this.base}/royalty-statements`);
    return mockOf(this.statements.all());
  }

  getStatement(id: number): Observable<RoyaltyStatement | undefined> {
    if (!environment.useMockRoyalty) return this.http.get<RoyaltyStatement>(`${this.base}/royalty-statements/${id}`);
    return mockOf(this.statements.find(s => s.statementID === id));
  }

  /** The real RoyaltyStatementController persists whatever royaltyAmount it's given — it doesn't
   *  compute a share from the creator's tier/rule itself. Apply the same flat 60% used by the
   *  mock so a real statement doesn't come back with royaltyAmount stuck at 0. */
  generateStatement(payload: { creatorID: number; period: string; totalViews: number; totalRevenue: number }): Observable<RoyaltyStatement> {
    if (!environment.useMockRoyalty) {
      const royaltyAmount = Math.round(payload.totalRevenue * 0.6 * 100) / 100;
      return this.http.post<RoyaltyStatement>(`${this.base}/royalty-statements`, { ...payload, royaltyAmount });
    }
    const royaltyAmount = Math.round(payload.totalRevenue * 0.6 * 100) / 100;
    const row = this.statements.insert({ ...payload, royaltyAmount, status: 'Draft' } as any);
    return mockOf(row);
  }

  finaliseStatement(id: number): Observable<{ message: string }> {
    if (!environment.useMockRoyalty) return this.http.put<{ message: string }>(`${this.base}/royalty-statements/${id}/finalise`, {});
    const s = this.statements.find(x => x.statementID === id);
    if (!s) return mockError(404, 'Statement not found');
    if (s.status !== 'Draft') return mockError(400, 'Only Draft statements can be finalised.');
    this.statements.update(id, { status: 'Finalised' });
    this.notify(`Royalty statement STMT-${id} has been finalised and is ready for payout processing.`);
    return mockOf({ message: 'Statement finalised' });
  }

  markPaid(id: number): Observable<{ message: string }> {
    if (!environment.useMockRoyalty) return this.http.put<{ message: string }>(`${this.base}/royalty-statements/${id}/mark-paid`, {});
    const s = this.statements.find(x => x.statementID === id);
    if (!s) return mockError(404, 'Statement not found');
    if (s.status !== 'Finalised') return mockError(400, 'Only Finalised statements can be marked as Paid.');
    this.statements.update(id, { status: 'Paid' });
    this.notify(`Royalty statement STMT-${id} has been marked as Paid.`);
    return mockOf({ message: 'Statement marked as Paid' });
  }

  // ---- Payouts ----
  getAllPayouts(): Observable<RoyaltyPayout[]> {
    if (!environment.useMockRoyalty) return this.http.get<RoyaltyPayout[]>(`${this.base}/royalty-payouts`);
    return mockOf(this.payouts.all());
  }

  createPayout(payload: { statementID: number; creatorID: number; amount: number; method: 'BankTransfer' | 'WalletCredit' }): Observable<RoyaltyPayout> {
    if (!environment.useMockRoyalty) return this.http.post<RoyaltyPayout>(`${this.base}/royalty-payouts`, payload);
    const row = this.payouts.insert({ ...payload, payoutDate: new Date().toISOString().slice(0, 10), status: 'Pending' } as any);
    return mockOf(row);
  }

  markProcessed(payoutID: number): Observable<{ message: string }> {
    if (!environment.useMockRoyalty) return this.http.put<{ message: string }>(`${this.base}/royalty-payouts/${payoutID}/process`, {});
    this.payouts.update(payoutID, { status: 'Processed' });
    this.notify(`Payout PAY-${payoutID} has been processed.`);
    return mockOf({ message: 'Payout marked Processed' });
  }

  markFailed(payoutID: number, reason: string): Observable<{ message: string }> {
    if (!environment.useMockRoyalty) return this.http.put<{ message: string }>(`${this.base}/royalty-payouts/${payoutID}/fail?reason=${encodeURIComponent(reason)}`, {});
    this.payouts.update(payoutID, { status: 'Failed' });
    this.notify(`Payout PAY-${payoutID} has failed: ${reason}`);
    return mockOf({ message: 'Payout marked Failed' });
  }

  /** Frontend-only extension — the real RoyaltyPayoutController has no endpoint to reset a
   *  Failed payout back to Pending (only process/fail/delete). */
  retryPayout(payoutID: number): Observable<{ message: string }> {
    if (!environment.useMockRoyalty) return mockError(400, 'Retrying a payout is not supported by the backend yet.');
    this.payouts.update(payoutID, { status: 'Pending' });
    return mockOf({ message: 'Payout retry scheduled' });
  }

  hasPayoutForStatement(statementID: number): boolean {
    return this.payouts.filterBy(p => p.statementID === statementID).length > 0;
  }
}
