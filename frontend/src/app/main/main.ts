import { Component, inject, signal, OnInit } from '@angular/core';
import { FormBuilder, Validators, ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { UrlService } from '../services/url.service';
import { AuthService } from '../services/auth.service';
import { UrlMapping } from '../services/auth';

@Component({
  selector: 'app-main',
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './main.html',
  styleUrl: './main.css'
})
export class Main implements OnInit {
  private fb = inject(FormBuilder);
  private urlSvc = inject(UrlService);
  private auth = inject(AuthService);

  form = this.fb.group({
    originalUrl: ['', [Validators.required, Validators.pattern(/^https?:\/\/.+/)]]
  });

  urls = signal<UrlMapping[]>([]);
  shortening = signal(false);
  loadingUrls = signal(false);
  shortenError = signal<string | null>(null);
  newShortUrl = signal<string | null>(null);
  copiedId = signal<number | 'new' | null>(null);
  deletingId = signal<number | null>(null);

  get originalUrl() { return this.form.get('originalUrl')!; }

  ngOnInit() { this.loadUrls(); }

  loadUrls() {
    this.loadingUrls.set(true);
    this.urlSvc.getUserUrls().subscribe({
      next: data => { this.urls.set(data); this.loadingUrls.set(false); },
      error: () => this.loadingUrls.set(false)
    });
  }

  submit() {
    if (this.form.invalid || this.shortening()) return;
    this.shortenError.set(null);
    this.newShortUrl.set(null);
    this.shortening.set(true);

    this.urlSvc.shorten({ originalUrl: this.originalUrl.value! }).subscribe({
      next: res => {
        this.newShortUrl.set(res.shortUrl);
        this.shortening.set(false);
        this.form.reset();
        this.loadUrls();
      },
      error: (err: HttpErrorResponse) => {
        this.shortening.set(false);
        this.shortenError.set(
          err.status === 400
            ? 'Некорректный URL. Проверьте формат.'
            : (err.error?.message ?? 'Не удалось сократить ссылку.')
        );
      }
    });
  }

  copy(text: string, id: number | 'new') {
    navigator.clipboard.writeText(text).then(() => {
      this.copiedId.set(id);
      setTimeout(() => this.copiedId.set(null), 2000);
    });
  }

  delete(id: number) {
    this.deletingId.set(id);
    this.urlSvc.deleteUrl(id).subscribe({
      next: () => { this.urls.update(l => l.filter(u => u.id !== id)); this.deletingId.set(null); },
      error: () => this.deletingId.set(null)
    });
  }

  open(url: string) { window.open(url, '_blank'); }

  logout() { this.auth.logout(); }

  truncate(url: string, max = 55) {
    return url.length > max ? url.slice(0, max) + '…' : url;
  }

  fmtDate(s: string) {
    return new Date(s).toLocaleDateString('ru-RU', { day: '2-digit', month: 'short', year: 'numeric' });
  }
}
