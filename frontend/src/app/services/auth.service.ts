import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { BehaviorSubject, Observable, throwError, of } from 'rxjs';
import { catchError,  map, tap } from 'rxjs/operators';
import { Router } from '@angular/router';
import { Auth } from './auth';
import { environment } from '../../environments/environment.prod';

export interface User {
  id: string;
  username: string;
  password: string;
}

export interface AuthResponse {
  token: string;
  user: User;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private apiUrl = environment.apiUrl + '/api/auth';
  private currentUserSubject = new BehaviorSubject<User | null>(null);
  public currentUser$ = this.currentUserSubject.asObservable();


  constructor(private http: HttpClient) {
     this.loadUserFromToken();
  }

  register(username: string, password: string): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/register`, {
      username,
      password,
    })
    .pipe(
      tap(response => this.handleAuthResponse(response))
    );
  }

  login(username: string, password: string): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/login`, { username, password })
    .pipe(
      tap(response => this.handleAuthResponse(response))
    );
  }


  logout(): void {
    localStorage.removeItem('token');
    this.currentUserSubject.next(null);
  }

  getToken(): string | null {
    return localStorage.getItem('token');
  }
  private handleAuthResponse(response: AuthResponse): void {
    localStorage.setItem('token', response.token);
    this.currentUserSubject.next(response.user);
  }

  private loadUserFromToken(): void {
    const token = this.getToken();
    if (token) {
      const payload = this.parseJwt(token);
      const user: User = {
        id: payload.sub,
        username: payload.username,
        password: payload.password
      };
      this.currentUserSubject.next(user);
    }
  }

  private parseJwt(token: string): any {
    const base64Url = token.split('.')[1];
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
    const jsonPayload = decodeURIComponent(
      atob(base64).split('').map(c => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2)).join('')
    );
    return JSON.parse(jsonPayload);
  }

  isAuthenticated(): boolean {
    return !!this.getToken();
  }
}