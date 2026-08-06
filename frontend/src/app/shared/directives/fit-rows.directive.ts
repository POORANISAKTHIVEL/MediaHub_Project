import { AfterViewInit, Directive, ElementRef, EventEmitter, Input, NgZone, OnDestroy, Output } from '@angular/core';

/** Measures how many rows of `rowSelector` fit between this element's current top position and
 *  the bottom of the viewport (minus space reserved for whatever sits below it, e.g. a pagination
 *  bar), and emits that count. Lets a list page size itself to the screen with zero scrolling —
 *  pagination takes over for anything that doesn't fit, instead of an internal scrollbar.
 *  The host's own top position is used (not its height) so this works before the final row
 *  count is known — adding/removing rows below the host never moves its top. */
@Directive({ selector: '[appFitRows]' })
export class FitRowsDirective implements AfterViewInit, OnDestroy {
  @Input() rowSelector = 'tbody tr';
  @Input() minRows = 3;
  @Input() reservedBelowPx = 100; // pagination bar height + safety margin for row-height rounding
  // Set true for a wrapping CSS grid of cards (multiple items per visual row) instead of a
  // one-item-per-row list/table — capacity is then columnsPerRow × rowsThatFit, not just rows.
  @Input() gridMode = false;
  @Output() rowsThatFit = new EventEmitter<number>();

  private ro?: ResizeObserver;
  private lastEmitted = -1;

  constructor(private el: ElementRef<HTMLElement>, private zone: NgZone) {}

  ngAfterViewInit() {
    this.ro = new ResizeObserver(() => this.zone.run(() => this.measure()));
    this.ro.observe(document.documentElement);
    queueMicrotask(() => this.measure());
  }

  ngOnDestroy() {
    this.ro?.disconnect();
  }

  private measure() {
    if (!this.rowSelector) return;
    const host = this.el.nativeElement;
    const row = host.querySelector(this.rowSelector) as HTMLElement | null;
    if (!row) return;
    const rowRect = row.getBoundingClientRect();
    if (!rowRect.height) return;

    const hostTop = host.getBoundingClientRect().top;
    const availableHeight = window.innerHeight - hostTop - this.reservedBelowPx;
    const rowsThatFitVertically = Math.max(1, Math.floor(availableHeight / rowRect.height));

    let count: number;
    if (this.gridMode && rowRect.width) {
      const columns = Math.max(1, Math.floor(host.clientWidth / rowRect.width));
      count = rowsThatFitVertically * columns;
    } else {
      count = rowsThatFitVertically;
    }
    count = Math.max(this.minRows, count);

    if (count !== this.lastEmitted) {
      this.lastEmitted = count;
      this.rowsThatFit.emit(count);
    }
  }
}
