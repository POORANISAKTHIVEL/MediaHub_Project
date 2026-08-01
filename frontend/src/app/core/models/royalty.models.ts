export interface RoyaltyRule {
  ruleID: number;
  creatorTier: string;
  revenueSharePercent: number;
  minimumPayoutThreshold: number;
  payoutFrequency: 'Monthly' | 'Quarterly';
  effectiveDate: string;
  status: 'Active' | 'Inactive';
}

export interface RoyaltyStatement {
  statementID: number;
  creatorID: number;
  period: string;
  totalViews: number;
  totalRevenue: number;
  royaltyAmount: number;
  status: 'Draft' | 'Finalised' | 'Paid';
}

export interface RoyaltyPayout {
  payoutID: number;
  statementID: number;
  creatorID: number;
  amount: number;
  payoutDate: string;
  method: 'BankTransfer' | 'WalletCredit';
  status: 'Pending' | 'Processed' | 'Failed';
}
