export type LicenseStatus = 'Active' | 'Expired' | 'Terminated';
export type RightsType = 'Exclusive' | 'Non-Exclusive' | 'Limited';

export interface LicenseAgreement {
  licenseId: number;
  contentId: number;
  licensorId: number;
  licenseeRef: string;
  territory: string;
  rightsType: RightsType;
  startDate: string;
  endDate: string;
  licenseFee: number;
  status: LicenseStatus;
}

export interface LicenseExpiringSoon {
  licenseId: number;
  territory: string;
  endDate: string;
  daysRemaining: number;
}

export interface TerritoryRestriction {
  restrictionId: number;
  contentId: number;
  restrictedCountries: string;
  allowedCountries: string;
  effectiveDate: string;
  status: 'Active' | 'Inactive';
}
