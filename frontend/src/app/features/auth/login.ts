import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../core/auth/auth.service';

@Component({
  selector: 'app-login',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './login.html'
})
export class Login {
  private fb = inject(FormBuilder);
  private auth = inject(AuthService);
  private router = inject(Router);

  form = this.fb.nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required]]
  });

  submitting = signal(false);
  serverError = signal('');

  get emailError(): string {
    const c = this.form.controls.email;
    if (!c.touched && !this.submittedOnce) return '';
    if (c.hasError('required')) return '⚠ Email is required';
    if (c.hasError('email')) return '⚠ Enter a valid email address';
    return '';
  }
  get passwordError(): string {
    const c = this.form.controls.password;
    if (!c.touched && !this.submittedOnce) return '';
    if (c.hasError('required')) return '⚠ Password is required';
    return '';
  }

  private submittedOnce = false;

  submit() {
    this.submittedOnce = true;
    this.serverError.set('');
    this.form.markAllAsTouched();
    if (this.form.invalid) return;

    this.submitting.set(true);
    this.auth.login(this.form.getRawValue()).subscribe({
      next: () => {
        this.submitting.set(false);
        this.router.navigate(['/dashboard']);
      },
      error: (err) => {
        this.submitting.set(false);
        const msg = err?.error?.message ?? 'LOGIN_FAILED';
        this.serverError.set(
          msg === 'INVALID_CREDENTIALS' ? '⚠ Invalid email or password' :
          msg === 'ACCOUNT_SUSPENDED' ? '⚠ This account has been suspended' :
          msg === 'ACCOUNT_INACTIVE' ? '⚠ This account is inactive' :
          '⚠ Something went wrong. Please try again.'
        );
      }
    });
  }
}
