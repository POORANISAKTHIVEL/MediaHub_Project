import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../core/auth/auth.service';
import { ToastService } from '../../shared/services/toast.service';

@Component({
  selector: 'app-profile',
  imports: [FormsModule],
  templateUrl: './profile.html'
})
export class Profile {
  auth = inject(AuthService);
  private toast = inject(ToastService);

  name = signal(this.auth.currentUser()?.name ?? '');
  phone = signal('');
  country = signal(this.auth.currentUser()?.country ?? '');

  save() {
    // Backend: PUT /mediaHub/iam/users/updateUser/v1/{userId} — mocked here since account
    // mutation isn't part of this milestone's seeded flows.
    this.toast.ok('Profile updated successfully');
  }
}
