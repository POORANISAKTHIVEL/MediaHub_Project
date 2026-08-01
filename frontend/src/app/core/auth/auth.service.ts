import { Injectable, signal, computed } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of, throwError } from 'rxjs';
import { delay, map, tap } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import { LoginRequest, LoginResponse, RegisterRequest, SessionClaims } from '../models/iam.models';
import { SEED_USERS, claimsFor, SeedUser } from './seed-users';

const STORAGE_KEY = 'mediahub.session';

export interface StoredSession {
  accessToken: string;
  claims: SessionClaims;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private mockRegistered: SeedUser[] = [];
  private nextMockId = 1000;

  private sessionSignal = signal<StoredSession | null>(this.readStoredSession());

  readonly session = this.sessionSignal.asReadonly();
  readonly isLoggedIn = computed(() => !!this.sessionSignal());
  readonly currentUser = computed(() => this.sessionSignal()?.claims ?? null);
  readonly permissions = computed(() => this.sessionSignal()?.claims.permissions ?? []);
  readonly roleType = computed(() => this.sessionSignal()?.claims.roleType ?? null);

  constructor(private http: HttpClient) {}

  private readStoredSession(): StoredSession | null {
    try {
      const raw = localStorage.getItem(STORAGE_KEY);
      if (!raw) return null;
      const parsed: StoredSession = JSON.parse(raw);
      if (parsed.claims.exp * 1000 < Date.now()) {
        localStorage.removeItem(STORAGE_KEY);
        return null;
      }
      return parsed;
    } catch {
      return null;
    }
  }

  private persist(session: StoredSession) {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(session));
    this.sessionSignal.set(session);
  }

  hasPermission(perm: string): boolean {
    return this.permissions().includes(perm);
  }

  hasAnyPermission(perms: string[]): boolean {
    return perms.some(p => this.hasPermission(p));
  }

  hasRole(role: string): boolean {
    return this.roleType() === role;
  }

  login(req: LoginRequest): Observable<LoginResponse> {
    if (!environment.useMockAuth) {
      return this.http.post<LoginResponse>(`${environment.apiBaseUrl}/mediaHub/iam/auth/login/v1.0`, req)
        .pipe(tap(res => this.persist({ accessToken: res.accessToken, claims: this.decodeRealClaims(res) })));
    }
    const user = [...SEED_USERS, ...this.mockRegistered].find(u => u.email === req.email);
    if (!user || user.password !== req.password) {
      return throwError(() => ({ status: 401, error: { message: 'INVALID_CREDENTIALS' } })).pipe(delay(300));
    }
    if (user.status === 'suspended') {
      return throwError(() => ({ status: 403, error: { message: 'ACCOUNT_SUSPENDED' } })).pipe(delay(300));
    }
    if (user.status === 'inactive') {
      return throwError(() => ({ status: 403, error: { message: 'ACCOUNT_INACTIVE' } })).pipe(delay(300));
    }
    const claims = claimsFor(user);
    const accessToken = 'mock.' + btoa(JSON.stringify(claims)) + '.token';
    const response: LoginResponse = {
      accessToken,
      tokenType: 'Bearer',
      expiresIn: 1800,
      user: { userId: user.userId, name: user.name, email: user.email, roleId: user.roleId, roleType: user.roleType, status: user.status }
    };
    return of(response).pipe(
      delay(400),
      tap(() => this.persist({ accessToken, claims }))
    );
  }

  register(req: RegisterRequest): Observable<{ message: string }> {
    if (!environment.useMockAuth) {
      return this.http.post<{ message: string }>(`${environment.apiBaseUrl}/mediaHub/iam/auth/register/v1.0`, req);
    }
    if ([...SEED_USERS, ...this.mockRegistered].some(u => u.email === req.email)) {
      return throwError(() => ({ status: 409, error: { message: 'EMAIL_ALREADY_EXISTS' } })).pipe(delay(300));
    }
    const user: SeedUser = {
      userId: this.nextMockId++,
      name: req.name,
      email: req.email,
      password: req.password,
      phone: req.phone,
      country: req.country,
      roleId: 1,
      roleType: 'subscriber',
      status: 'active'
    };
    this.mockRegistered.push(user);
    return of({ message: 'Account created successfully' }).pipe(delay(400));
  }

  logout(): Observable<{ message: string }> {
    const userId = this.currentUser()?.userId;
    localStorage.removeItem(STORAGE_KEY);
    this.sessionSignal.set(null);
    if (!environment.useMockAuth && userId) {
      return this.http.post<{ message: string }>(`${environment.apiBaseUrl}/mediaHub/iam/auth/logout/v1.0?userId=${userId}`, {});
    }
    return of({ message: 'Logged out. Session terminated.' }).pipe(delay(150));
  }

  private decodeRealClaims(res: LoginResponse): SessionClaims {
    try {
      const payload = JSON.parse(atob(res.accessToken.split('.')[1]));
      return { ...payload, name: res.user.name } as SessionClaims;
    } catch {
      return {
        sub: String(res.user.userId), userId: res.user.userId, roleId: res.user.roleId,
        roleType: res.user.roleType, email: res.user.email, country: '', name: res.user.name,
        permissions: [], exp: Math.floor(Date.now() / 1000) + res.expiresIn
      };
    }
  }
}
