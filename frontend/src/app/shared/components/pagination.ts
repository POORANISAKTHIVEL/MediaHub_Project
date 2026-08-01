import { Component, EventEmitter, Input, Output } from '@angular/core';

/** Server-side pagination control: emits the requested page index (0-based) and lets the
 *  caller re-fetch from its backend client — never paginates an already-loaded full list locally. */
@Component({
  selector: 'app-pagination',
  template: `
    <div class="pagination">
      <span>Showing <b>{{ rangeStart }}–{{ rangeEnd }}</b> of <b>{{ totalElements }}</b></span>
      <div class="pager">
        <button [disabled]="currentPage === 0" (click)="go(currentPage - 1)">‹</button>
        @for (p of pageNumbers(); track p) {
          <button [class.active]="p === currentPage" (click)="go(p)">{{ p + 1 }}</button>
        }
        <button [disabled]="currentPage >= totalPages - 1" (click)="go(currentPage + 1)">›</button>
      </div>
    </div>
  `
})
export class Pagination {
  @Input() currentPage = 0;
  @Input() totalPages = 1;
  @Input() totalElements = 0;
  @Input() pageSize = 10;
  @Output() pageChange = new EventEmitter<number>();

  get rangeStart(): number {
    return this.totalElements === 0 ? 0 : this.currentPage * this.pageSize + 1;
  }
  get rangeEnd(): number {
    return Math.min(this.totalElements, (this.currentPage + 1) * this.pageSize);
  }

  pageNumbers(): number[] {
    const total = Math.max(this.totalPages, 1);
    const windowSize = 5;
    let start = Math.max(0, this.currentPage - Math.floor(windowSize / 2));
    let end = Math.min(total, start + windowSize);
    start = Math.max(0, end - windowSize);
    return Array.from({ length: end - start }, (_, i) => start + i);
  }

  go(page: number) {
    if (page < 0 || page > this.totalPages - 1) return;
    this.pageChange.emit(page);
  }
}
