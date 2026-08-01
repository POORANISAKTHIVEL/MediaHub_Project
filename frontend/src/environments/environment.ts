export const environment = {
  production: false,
  apiBaseUrl: 'http://localhost:8094',
  // Per-module mock switches — flip to false once that module is verified against the
  // real backend. Kept granular (rather than one global useMock) so modules can be
  // integrated and tested one at a time without breaking the others.
  useMockAuth: false,
  useMockContent: false,
  useMockEditorial: false,
  useMockLicensing: false,
  useMockSubscription: false,
  useMockRoyalty: false,
  useMockNotification: false,
  useMockAnalytics: false
};
