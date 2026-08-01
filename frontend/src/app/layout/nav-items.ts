export interface NavEntry {
  group?: string;
  route?: string;
  icon?: string;
  label?: string;
  badge?: string;
  permissions?: string[];
}

/** Mirrors demo.html's NAV_ITEMS exactly (icons/labels/order), with each item's required
 *  permission derived from the backend's @PreAuthorize authority strings so the sidebar only
 *  ever shows what the user is actually allowed to open. */
export const NAV_ITEMS: NavEntry[] = [
  { group: 'Account' },
  { route: '/profile', icon: '◔', label: 'Profile' },
  { group: 'Overview' },
  { route: '/dashboard', icon: '▚', label: 'Dashboard' },
  { group: 'Content' },
  { route: '/content', icon: '🎞', label: 'Content Catalog', permissions: ['content:read'] },
  { route: '/creators', icon: '👤', label: 'Creators', permissions: ['content:read'] },
  { route: '/content/tags', icon: '🏷', label: 'Tag Management', permissions: ['content:read'] },
  { route: '/editorial/reviews', icon: '✎', label: 'Editorial', permissions: ['content:read'] },
  { route: '/editorial/collections', icon: '▦', label: 'Collections', permissions: ['content:read'] },
  { route: '/editorial/schedule', icon: '🗓', label: 'Publication Calendar', permissions: ['content:read'] },
  { route: '/licensing', icon: '⚖', label: 'Licensing', permissions: ['license:manage'] },
  { group: 'Commerce' },
  { route: '/subscription/plans', icon: '◈', label: 'Subscriptions', permissions: ['plan:configure', 'plan:view'] },
  { route: '/royalty', icon: '$', label: 'Royalty', permissions: ['royalty:view'] },
  { group: 'Subscriber Portal' },
  { route: '/subscription/catalog', icon: '◈', label: 'Plan Catalog', permissions: ['plan:view'] },
  { route: '/subscription/my', icon: '⊞', label: 'My Subscription', permissions: ['subscription:view'] },
  { group: 'Platform' },
  { route: '/analytics', icon: '◔', label: 'Analytics & Reports', permissions: ['report:view'] },
  { route: '/notifications', icon: '◇', label: 'Notifications' },
  { route: '/iam/users', icon: '⛨', label: 'IAM & Users', permissions: ['user:manage', 'role:manage', 'permission:manage'] },
  { route: '/audit', icon: '▤', label: 'Audit & Compliance', permissions: ['audit:read'] }
];
