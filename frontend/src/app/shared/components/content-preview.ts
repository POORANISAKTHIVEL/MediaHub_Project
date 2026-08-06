import { Component, Input, OnChanges, SimpleChanges, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { ContentAsset } from '../../core/models/content.models';
import { environment } from '../../../environments/environment';
import { LoadingSpinner } from './loading-spinner';

// Renders a real preview for a content asset's local demo file — video plays inline, images
// render inline, articles fetch their text and render it. Falls back to a friendly placeholder
// when there's no filePath, or the file can't be found under the configured assets folder.
@Component({
  selector: 'app-content-preview',
  imports: [LoadingSpinner],
  template: `
    @if (!asset?.filePath) {
      <div class="empty" style="padding:30px 10px">
        <div class="empty__ico">🗂</div>
        <h3>No preview available</h3>
        <p style="max-width:300px">This asset doesn't have a file path set yet.</p>
      </div>
    } @else if (failed()) {
      <div class="empty" style="padding:30px 10px">
        <div class="empty__ico">⚠</div>
        <h3>Preview not available</h3>
        <p style="max-width:320px">Couldn't find a file at <code class="mono">{{ asset?.filePath }}</code>. Make sure it exists under <code class="mono">backend/contentcatalog_git_individual/demo-assets/</code>.</p>
      </div>
    } @else if (asset?.type === 'Video') {
      <video controls style="width:100%;max-height:360px;border-radius:10px;background:#000;display:block" [src]="url()" (error)="failed.set(true)"></video>
    } @else if (asset?.type === 'Image') {
      <img style="width:100%;max-height:360px;object-fit:contain;border-radius:10px;background:var(--surface-2);display:block" [src]="url()" (error)="failed.set(true)">
    } @else {
      @if (articleLoading()) {
        <app-loading-spinner></app-loading-spinner>
      } @else if (articleText(); as text) {
        <div style="max-height:360px;overflow:auto;border:1px solid var(--line);border-radius:10px;padding:16px" [innerHTML]="text"></div>
        <div style="margin-top:10px"><a class="btn--link" [href]="url()" target="_blank" rel="noopener">Open raw file ↗</a></div>
      }
    }
  `
})
export class ContentPreview implements OnChanges {
  @Input() asset: ContentAsset | null = null;

  private http = inject(HttpClient);

  failed = signal(false);
  articleLoading = signal(false);
  articleText = signal<string | null>(null);

  url(): string {
    return this.asset?.filePath ? `${environment.assetBaseUrl}/media/${this.asset.filePath}` : '';
  }

  ngOnChanges(changes: SimpleChanges) {
    if (!changes['asset']) return;
    this.failed.set(false);
    this.articleText.set(null);
    if (this.asset?.type === 'Article' && this.asset.filePath) {
      this.loadArticle();
    }
  }

  private loadArticle() {
    this.articleLoading.set(true);
    this.http.get(this.url(), { responseType: 'text' }).subscribe({
      next: text => { this.articleText.set(text); this.articleLoading.set(false); },
      error: () => { this.failed.set(true); this.articleLoading.set(false); }
    });
  }
}
