import { Component, HostListener, inject } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from '../core/auth/auth.service';
import { NAV_ITEMS } from './nav-items';
import { ToastContainer } from '../shared/components/toast-container';
import { ConfirmDialog } from '../shared/components/confirm-dialog';

@Component({
  selector: 'app-shell',
  imports: [RouterOutlet, RouterLink, RouterLinkActive, ToastContainer, ConfirmDialog],
  templateUrl: './app-shell.html'
})
export class AppShell {
  auth = inject(AuthService);
  private router = inject(Router);
  acctOpen = false;

  navItems = NAV_ITEMS.filter(item =>
    item.group || !item.permissions || item.permissions.length === 0 || this.auth.hasAnyPermission(item.permissions)
  );

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
  }

  logout() {
    this.auth.logout().subscribe(() => this.router.navigate(['/login']));
  }
}
