import { ApplicationConfig, provideBrowserGlobalErrorListeners } from '@angular/core';
import { provideRouter } from '@angular/router';
import { AuthService } from './services/auth.service';
import { UrlService } from './services/url.service';
import { AuthGuard } from './guards/auth.guard';
import { routes } from './app.routes';

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(), 
    provideRouter(routes),
    AuthService,
    UrlService,
    AuthGuard,
  ],
};
