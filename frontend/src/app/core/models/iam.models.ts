export type UserStatus = 'active' | 'suspended' | 'inactive';
export type RoleType = 'admin' | 'subscriber' | 'creator' | 'rightsManager' | 'editorial' | 'revenueAnalyst';

export interface Role {
  roleId: number;
  roleType: string;
}

export interface Permission {
  permissionId: number;
  permissionType: string;
}

export interface IamUser {
  userId: number;
  name: string;
  email: string;
  phone?: string;
  country?: string;
  status: UserStatus;
  role: Role;
  roleType?: string;
  createdAt?: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  name: string;
  email: string;
  password: string;
  phone: string;
  country: string;
}

export interface LoginResponse {
  accessToken: string;
  tokenType: string;
  expiresIn: number;
  user: {
    userId: number;
    name: string;
    email: string;
    roleId: number;
    roleType: string;
    status: UserStatus;
  };
}

export interface SuspendRequest {
  reason: string;
}

export interface ActivateRequest {
  activatedBy: number;
}

export interface AuditLog {
  auditId: number;
  user: { userId: number; name: string };
  action: string;
  entityType: string;
  entityId: string;
  oldValue?: string;
  newValue?: string;
  ipAddress?: string;
  createdAt: string;
}

export type ModuleSource = 'IAM' | 'CONTENT' | 'SUBSCRIPTION' | 'EDITORIAL' | 'LICENSING' | 'ROYALTY' | 'ANALYTICS' | 'NOTIFICATION' | 'SYSTEM';
export type Severity = 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';

export interface AuditEvent {
  eventId: number;
  eventType: string;
  moduleSource: ModuleSource;
  performedBy: number;
  performedByRole: string;
  targetEntityType: string;
  targetEntityId: string;
  oldValue?: string;
  newValue?: string;
  ipAddress?: string;
  severity: Severity;
  status: 'SUCCESS' | 'FAILED' | 'BLOCKED';
  description?: string;
  createdAt: string;
}

export interface PageResponse<T> {
  data: T[];
  currentPage: number;
  totalPages: number;
  totalElements: number;
  pageSize: number;
  isFirst: boolean;
  isLast: boolean;
}

/** Decoded shape of the claims we keep in the session (mirrors the real JWT payload). */
export interface SessionClaims {
  sub: string;
  userId: number;
  roleId: number;
  roleType: string;
  email: string;
  country: string;
  permissions: string[];
  name: string;
  exp: number;
}
