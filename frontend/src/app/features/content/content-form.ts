import { Component, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { ContentClient } from '../../core/api/content-client';
import { ContentAsset, Creator } from '../../core/models/content.models';
import { ToastService } from '../../shared/services/toast.service';
import { LoadingSpinner } from '../../shared/components/loading-spinner';

@Component({
  selector: 'app-content-form',
  imports: [FormsModule, RouterLink, LoadingSpinner],
  templateUrl: './content-form.html'
})
export class ContentForm implements OnInit {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private content = inject(ContentClient);
  private toast = inject(ToastService);

  loading = signal(true);
  saving = signal(false);
  creators = signal<Creator[]>([]);
  editingId = signal<number | null>(null);

  form: { title: string; type: ContentAsset['type']; creatorId: number; genre: string; language: string; synopsis: string; filePath: string } =
    { title: '', type: 'Video', creatorId: 0, genre: '', language: 'English', synopsis: '', filePath: '' };

  get isEdit(): boolean {
    return this.editingId() !== null;
  }

  ngOnInit() {
    this.content.fetchCreators().subscribe(rows => {
      this.creators.set(rows);
      if (!this.form.creatorId && rows.length) this.form.creatorId = rows[0].creatorId;
    });

    const idParam = this.route.snapshot.paramMap.get('id');
    if (idParam) {
      const id = Number(idParam);
      this.editingId.set(id);
      this.content.fetchContentById(id).subscribe(c => {
        if (c) {
          this.form = { title: c.title, type: c.type, creatorId: c.creatorId, genre: c.genre ?? '', language: c.language ?? '', synopsis: c.synopsis ?? '', filePath: c.filePath ?? '' };
        }
        this.loading.set(false);
      });
    } else {
      this.loading.set(false);
    }
  }

  save() {
    if (!this.form.title.trim() || !this.form.creatorId) {
      this.toast.warn('Title and Creator are required');
      return;
    }
    this.saving.set(true);
    const id = this.editingId();
    if (id !== null) {
      this.content.updateContent(id, this.form).subscribe(() => {
        this.toast.ok('Content updated successfully');
        this.router.navigate(['/content', id]);
      });
    } else {
      this.content.createContent(this.form).subscribe(() => {
        this.toast.ok('Content created successfully');
        this.router.navigate(['/content']);
      });
    }
  }
}
