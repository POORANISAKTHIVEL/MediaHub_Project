import { Component, HostListener, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from '../core/auth/auth.service';
import { NAV_ITEMS, NavEntry } from './nav-items';
import { ToastContainer } from '../shared/components/toast-container';
import { ConfirmDialog } from '../shared/components/confirm-dialog';
import { ContentClient } from '../core/api/content-client';
import { IamClient } from '../core/api/iam-client';
import { LicensingClient } from '../core/api/licensing-client';

export interface SearchResult {
  icon: string;
  label: string;
  sub: string;
  route: string[];
}

// A group heading with nothing left under it after filtering (e.g. subscriber has none of the
// Commerce-group items) would otherwise still render as an empty section label.
function dropEmptyGroups(items: NavEntry[]): NavEntry[] {
  return items.filter((item, i) => {
    if (!item.group) return true;
    const next = items[i + 1];
    return !!next && !next.group;
  });
}

@Component({
  selector: 'app-shell',
  imports: [RouterOutlet, RouterLink, RouterLinkActive, ToastContainer, ConfirmDialog, FormsModule],
  templateUrl: './app-shell.html'
})
export class AppShell {
  auth = inject(AuthService);
  private router = inject(Router);
  private content = inject(ContentClient);
  private iam = inject(IamClient);
  private licensing = inject(LicensingClient);
  acctOpen = false;

  searchTerm = signal('');
  searchOpen = signal(false);
  searching = signal(false);
  searchResults = signal<SearchResult[]>([]);
  private searchDebounce: ReturnType<typeof setTimeout> | null = null;

  onSearchInput(term: string) {
    this.searchTerm.set(term);
    if (this.searchDebounce) clearTimeout(this.searchDebounce);
    if (term.trim().length < 2) {
      this.searchResults.set([]);
      this.searchOpen.set(false);
      return;
    }
    this.searchDebounce = setTimeout(() => this.runSearch(term.trim()), 300);
  }

  private runSearch(term: string) {
    this.searching.set(true);
    this.searchOpen.set(true);
    const needle = term.toLowerCase();

    forkJoin({
      content: this.content.fetchContents().pipe(catchError(() => of([]))),
      users: this.iam.getAllUsers().pipe(catchError(() => of([]))),
      licenses: this.licensing.getAllLicenses().pipe(catchError(() => of([])))
    }).subscribe(({ content, users, licenses }) => {
      const results: SearchResult[] = [
        ...content.filter(c => c.title.toLowerCase().includes(needle) || String(c.contentId).includes(needle))
          .slice(0, 5)
          .map(c => ({ icon: '🎞', label: c.title, sub: 'CNT-' + c.contentId, route: ['/content', String(c.contentId)] })),
        // No user-detail route exists yet — link back to the Users list rather than a 404.
        ...users.filter(u => u.name.toLowerCase().includes(needle) || u.email.toLowerCase().includes(needle))
          .slice(0, 5)
          .map(u => ({ icon: '◔', label: u.name, sub: u.email, route: ['/iam/users'] })),
        ...licenses.filter(l => l.licenseeRef.toLowerCase().includes(needle) || String(l.licenseId).includes(needle))
          .slice(0, 5)
          .map(l => ({ icon: '⚖', label: l.licenseeRef, sub: 'LIC-' + l.licenseId, route: ['/licensing', String(l.licenseId)] }))
      ];
      this.searchResults.set(results);
      this.searching.set(false);
    });
  }

  goToResult(r: SearchResult) {
    this.searchOpen.set(false);
    this.searchTerm.set('');
    this.searchResults.set([]);
    this.router.navigate(r.route);
  }

  navItems = dropEmptyGroups(NAV_ITEMS.filter(item => {
    if (item.hideForRoles?.includes(this.auth.currentUser()?.roleType ?? '')) return false;
    return item.group || !item.permissions || item.permissions.length === 0 || this.auth.hasAnyPermission(item.permissions);
  }));

  get initials(): string {
    const name = this.auth.currentUser()?.name ?? '';
    return name.split(' ').map(p => p[0]).join('').slice(0, 2).toUpperCase() || 'U';
  }

  toggleAccount(ev: Event) {
    ev.stopPropagation();
    this.acctOpen = !this.acctOpen;
  }

  @HostListener('document:click')
  closeAccount() {
    this.acctOpen = false;
    this.searchOpen.set(false);
  }

  logout() {
    this.auth.logout().subscribe(() => this.router.navigate(['/login']));
  }
}
