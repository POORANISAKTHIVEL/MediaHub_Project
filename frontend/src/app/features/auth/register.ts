import { Component, inject, signal } from '@angular/core';
import { AbstractControl, FormBuilder, ReactiveFormsModule, ValidationErrors, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../core/auth/auth.service';

function passwordStrength(control: AbstractControl): ValidationErrors | null {
  const v: string = control.value ?? '';
  if (v.length === 0) return null;
  const ok = v.length >= 8 && /[0-9]/.test(v) && /[A-Z]/.test(v);
  return ok ? null : { weak: true };
}

@Component({
  selector: 'app-register',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './register.html'
})
export class Register {
  private fb = inject(FormBuilder);
  private auth = inject(AuthService);
  private router = inject(Router);

  form = this.fb.nonNullable.group({
    name: ['', [Validators.required, Validators.minLength(2)]],
    email: ['', [Validators.required, Validators.email]],
    phone: ['', [Validators.required, Validators.pattern(/^[0-9+\-\s]{7,15}$/)]],
    country: ['', [Validators.required]],
    password: ['', [Validators.required, passwordStrength]],
    confirmPassword: ['', [Validators.required]],
    agree: [false]
  });

  submitting = signal(false);
  serverError = signal('');
  private submittedOnce = false;

  private touchedOrSubmitted(name: keyof typeof this.form.controls): boolean {
    return this.form.controls[name].touched || this.submittedOnce;
  }

  get nameError(): string {
    if (!this.touchedOrSubmitted('name')) return '';
    if (this.form.controls.name.hasError('required')) return '⚠ Full name is required';
    if (this.form.controls.name.hasError('minlength')) return '⚠ Name is too short';
    return '';
  }
  get emailError(): string {
    if (!this.touchedOrSubmitted('email')) return '';
    if (this.form.controls.email.hasError('required')) return '⚠ Email is required';
    if (this.form.controls.email.hasError('email')) return '⚠ Enter a valid email address';
    return '';
  }
  get phoneError(): string {
    if (!this.touchedOrSubmitted('phone')) return '';
    if (this.form.controls.phone.hasError('required')) return '⚠ Phone number is required';
    if (this.form.controls.phone.hasError('pattern')) return '⚠ Enter a valid phone number';
    return '';
  }
  get countryError(): string {
    if (!this.touchedOrSubmitted('country')) return '';
    if (this.form.controls.country.hasError('required')) return '⚠ Country is required';
    return '';
  }
  get passwordError(): string {
    if (!this.touchedOrSubmitted('password')) return '';
    if (this.form.controls.password.hasError('required')) return '⚠ Password is required';
    if (this.form.controls.password.hasError('weak')) return '⚠ Min 8 chars, 1 number, 1 uppercase';
    return '';
  }
  get confirmError(): string {
    if (!this.touchedOrSubmitted('confirmPassword')) return '';
    const { password, confirmPassword } = this.form.getRawValue();
    if (!confirmPassword) return '⚠ Please confirm your password';
    if (password !== confirmPassword) return '⚠ Passwords do not match';
    return '';
  }
  get agreeError(): string {
    if (!this.submittedOnce) return '';
    return this.form.getRawValue().agree ? '' : '⚠ You must agree to the Terms & Privacy Policy';
  }

  toggleAgree() {
    this.form.controls.agree.setValue(!this.form.getRawValue().agree);
  }

  submit() {
    this.submittedOnce = true;
    this.serverError.set('');
    this.form.markAllAsTouched();

    const invalid = this.form.invalid || !!this.confirmError || !!this.agreeError;
    if (invalid) return;

    this.submitting.set(true);
    const { name, email, phone, country, password } = this.form.getRawValue();
    this.auth.register({ name, email, phone, country, password }).subscribe({
      next: () => {
        this.submitting.set(false);
        this.router.navigate(['/login']);
      },
      error: (err) => {
        this.submitting.set(false);
        const msg = err?.error?.message ?? 'REGISTER_FAILED';
        this.serverError.set(msg === 'EMAIL_ALREADY_EXISTS' ? '⚠ An account with this email already exists' : '⚠ Something went wrong. Please try again.');
      }
    });
  }
}
