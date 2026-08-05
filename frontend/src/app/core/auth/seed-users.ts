import { RoleType, SessionClaims } from '../models/iam.models';

/** Mirrors DataLoader.java exactly (roles, permissions, seed users) so switching to the
 *  real backend later requires zero changes to credentials or permission checks. */
export const ROLE_PERMISSIONS: Record<RoleType, string[]> = {
  admin: [
    'content:read', 'content:write', 'content:publish', 'content:delete',
    'royalty:view', 'royalty:approve',
    'plan:configure', 'plan:view',
    'subscription:view', 'subscription:manage',
    'role:manage', 'permission:manage',
    'user:suspend', 'user:manage',
    'report:view', 'license:manage', 'audit:read',
    'notification:view', 'notification:send', 'notification:update', 'notification:analytics',
    'editorial:manage'
  ],
  subscriber: ['content:read', 'plan:view', 'subscription:view'],
  creator: ['content:read', 'content:write', 'royalty:view', 'plan:view', 'subscription:view'],
  editorial: ['content:read', 'content:publish', 'content:delete', 'editorial:manage', 'plan:view', 'subscription:view'],
  rightsManager: ['content:read', 'license:manage', 'plan:view', 'subscription:view'],
  revenueAnalyst: ['royalty:view', 'royalty:approve', 'report:view', 'plan:view', 'subscription:view']
};

export interface SeedUser {
  userId: number;
  name: string;
  email: string;
  password: string;
  phone: string;
  country: string;
  roleId: number;
  roleType: RoleType;
  status: 'active' | 'suspended' | 'inactive';
}

export const SEED_USERS: SeedUser[] = [
  { userId: 1, name: 'System Admin', email: 'admin@mediahub.com', password: '$2b$12$adminHash001', phone: '+91-9000000001', country: 'IN', roleId: 6, roleType: 'admin', status: 'active' },
  { userId: 2, name: 'Arjun Sharma', email: 'arjun@email.com', password: '$2b$12$subscriberHash002', phone: '+91-9000000002', country: 'IN', roleId: 1, roleType: 'subscriber', status: 'active' },
  { userId: 3, name: 'Priya Menon', email: 'priya.menon@email.com', password: '$2b$12$creatorHash003', phone: '+91-9000000003', country: 'IN', roleId: 2, roleType: 'creator', status: 'active' },
  { userId: 4, name: 'Ravi Kumar', email: 'ravi.kumar@email.com', password: '$2b$12$editorialHash004', phone: '+91-9000000004', country: 'IN', roleId: 3, roleType: 'editorial', status: 'active' },
  { userId: 5, name: 'Sneha Pillai', email: 'sneha.pillai@email.com', password: '$2b$12$rightsHash005', phone: '+91-9000000005', country: 'IN', roleId: 4, roleType: 'rightsManager', status: 'active' },
  { userId: 6, name: 'Karthik Nair', email: 'karthik.nair@email.com', password: '$2b$12$analystHash006', phone: '+91-9000000006', country: 'IN', roleId: 5, roleType: 'revenueAnalyst', status: 'active' }
];

export function claimsFor(user: SeedUser): SessionClaims {
  return {
    sub: String(user.userId),
    userId: user.userId,
    roleId: user.roleId,
    roleType: user.roleType,
    email: user.email,
    country: user.country,
    name: user.name,
    permissions: ROLE_PERMISSIONS[user.roleType],
    exp: Math.floor(Date.now() / 1000) + 1800
  };
}
