export const environment = {
  production: false,
  apiBaseUrl: 'http://localhost:8094',
  // Content-catalog service's own port, hit directly (bypassing the gateway) for local demo
  // asset previews — that endpoint is public (no JWT), and native <video>/<img> tags never
  // carry the Authorization header the gateway-proxied API calls rely on.
  assetBaseUrl: 'http://localhost:8093',
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
