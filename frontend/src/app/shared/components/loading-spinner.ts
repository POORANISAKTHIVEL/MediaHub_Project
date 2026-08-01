import { Component } from '@angular/core';

@Component({
  selector: 'app-loading-spinner',
  template: `
    <div style="display:flex;flex-direction:column;gap:10px;padding:24px">
      @for (w of [100,80,90,60]; track w) {
        <div [style.width.%]="w" style="height:14px;border-radius:6px;background:linear-gradient(90deg,#eef2f6,#e2e8f0,#eef2f6);background-size:200% 100%;animation:shimmer 1.2s infinite linear"></div>
      }
    </div>
    <style>@keyframes shimmer{0%{background-position:200% 0}100%{background-position:-200% 0}}</style>
  `
})
export class LoadingSpinner {}
