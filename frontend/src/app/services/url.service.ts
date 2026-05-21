import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ShortenRequest, ShortenResponse, UrlMapping } from './auth';
import { environment } from '../../environments/environment.prod';

@Injectable({ providedIn: 'root' })
export class UrlService {
private apiUrl = environment.apiUrl + '/api';
  private http = inject(HttpClient);

  shorten(body: ShortenRequest): Observable<ShortenResponse> {
    return this.http.post<ShortenResponse>(`${this.apiUrl}/url/add`, body);
  }

  getUserUrls(): Observable<UrlMapping[]> {
    return this.http.get<UrlMapping[]>(`${this.apiUrl}/url/my`);
  }

  deleteUrl(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/url/${id}`);
  }
}
