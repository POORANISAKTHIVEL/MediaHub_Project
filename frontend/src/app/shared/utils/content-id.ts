export const CONTENT_ID_MAX_DIGITS = 10;
const CONTENT_ID_MAX_VALUE = Number('9'.repeat(CONTENT_ID_MAX_DIGITS));

export function contentIdError(value: number | null | undefined): string {
  if (!value) return 'Content ID is required';
  if (!Number.isInteger(value) || value < 1) return 'Content ID must be a positive whole number';
  if (value > CONTENT_ID_MAX_VALUE) return `Content ID must be ${CONTENT_ID_MAX_DIGITS} digits or fewer`;
  return '';
}

// Bound to (ngModelChange) on the Content ID number input so typing an 11th digit is dropped
// immediately, instead of only being caught by validation after the fact.
export function clampContentId(value: number | null): number {
  return clampDigits(value, CONTENT_ID_MAX_DIGITS);
}

// Generic versions of the above, for other numeric ID/amount fields with their own digit cap
// (e.g. Licensor ID, License Fee) that need the same "can't type past N digits" behavior.
export function clampDigits(value: number | null, maxDigits: number): number {
  if (!value || value < 0) return 0;
  const digits = String(Math.trunc(value)).slice(0, maxDigits);
  return digits ? Number(digits) : 0;
}

export function digitFieldError(value: number | null | undefined, maxDigits: number, label: string): string {
  if (!value) return `${label} is required`;
  if (!Number.isInteger(value) || value < 1) return `${label} must be a positive whole number`;
  if (String(value).length > maxDigits) return `${label} must be ${maxDigits} digits or fewer`;
  return '';
}

// Since the input is clamped on every keystroke, the stored value never actually exceeds
// maxDigits — so a plain "value too long" check would never fire. This instead looks at the
// raw value BEFORE clamping (called from the (ngModelChange) handler) to catch the moment the
// user actually typed/pasted past the limit, so a message can show right when it happens.
export function digitLimitMessage(rawValue: number | null, maxDigits: number, label: string): string {
  if (rawValue == null) return '';
  const digits = String(Math.trunc(Math.abs(rawValue)));
  return digits.length > maxDigits ? `${label} can only be up to ${maxDigits} digits` : '';
}

export function textLimitMessage(rawValue: string, maxLength: number, label: string): string {
  return rawValue.length > maxLength ? `Limit exceeded — only ${maxLength} characters allowed for ${label}` : '';
}
