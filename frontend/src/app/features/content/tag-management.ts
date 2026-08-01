import { Component, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ContentClient } from '../../core/api/content-client';
import { ContentTag } from '../../core/models/content.models';
import { AuthService } from '../../core/auth/auth.service';
import { ConfirmService } from '../../shared/services/confirm.service';
import { ToastService } from '../../shared/services/toast.service';
import { LoadingSpinner } from '../../shared/components/loading-spinner';

@Component({
  selector: 'app-tag-management',
  imports: [FormsModule, LoadingSpinner],
  templateUrl: './tag-management.html'
})
export class TagManagement implements OnInit {
  private content = inject(ContentClient);
  private confirm = inject(ConfirmService);
  private toast = inject(ToastService);
  auth = inject(AuthService);

  loading = signal(true);
  tags = signal<ContentTag[]>([]);
  creating = signal(false);
  newTagName = '';
  newTagCategory: ContentTag['tagCategory'] = 'Genre';
  newTagContentId = 0;

  ngOnInit() {
    this.load();
  }

  load() {
    this.loading.set(true);
    this.content.fetchTags().subscribe(t => {
      this.tags.set(t);
      this.loading.set(false);
    });
  }

  create() {
    if (!this.newTagName.trim() || !this.newTagContentId) return;
    this.content.addTag({ tagName: this.newTagName.trim(), tagCategory: this.newTagCategory, contentId: this.newTagContentId }).subscribe(() => {
      this.toast.ok('Tag added successfully');
      this.creating.set(false);
      this.newTagName = '';
      this.load();
    });
  }

  async remove(t: ContentTag) {
    const ok = await this.confirm.ask(`Remove tag "${t.tagName}"?`, 'Remove', true);
    if (!ok) return;
    this.content.removeTag(t.tagId).subscribe(() => {
      this.toast.ok('Tag removed successfully');
      this.load();
    });
  }
}
