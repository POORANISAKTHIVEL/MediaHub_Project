import { Observable, of, throwError } from 'rxjs';
import { delay } from 'rxjs/operators';

/** Generic in-memory CRUD store used by every mock api client — mirrors demo.html's
 *  data-array + goKey() re-render pattern, but as a reusable typed helper. */
export class MockStore<T> {
  private rows: T[];
  private nextId: number;

  constructor(seed: T[], private idKey: keyof T) {
    this.rows = [...seed];
    const maxId = seed.reduce((m, r) => Math.max(m, Number(r[idKey]) || 0), 0);
    this.nextId = maxId + 1;
  }

  /** Always returns a fresh array reference — callers typically feed this straight into an
   *  Angular signal.set(); reusing the same mutated reference would make signal equality
   *  checks think nothing changed, and any computed() depending on it would never recompute. */
  all(): T[] {
    return [...this.rows];
  }

  find(predicate: (row: T) => boolean): T | undefined {
    return this.rows.find(predicate);
  }

  filterBy(predicate: (row: T) => boolean): T[] {
    return this.rows.filter(predicate);
  }

  insert(partial: Omit<T, never>): T {
    const row = { ...(partial as any), [this.idKey]: this.nextId++ } as T;
    this.rows.push(row);
    return row;
  }

  update(id: any, patch: Partial<T>): T | undefined {
    const row = this.rows.find(r => r[this.idKey] === id);
    if (!row) return undefined;
    Object.assign(row as any, patch);
    return row;
  }

  remove(id: any): boolean {
    const idx = this.rows.findIndex(r => r[this.idKey] === id);
    if (idx === -1) return false;
    this.rows.splice(idx, 1);
    return true;
  }
}

export function mockOf<T>(value: T, ms = 300): Observable<T> {
  return of(value).pipe(delay(ms));
}

export function mockError(status: number, message: string, ms = 300): Observable<never> {
  return throwError(() => ({ status, error: { message } })).pipe(delay(ms));
}
