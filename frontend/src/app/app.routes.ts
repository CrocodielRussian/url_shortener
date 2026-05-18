import { Routes } from '@angular/router';
import { AuthGuard } from './guards/auth.guard';
import { LoginComponent } from './login/login';
import { MainComponent } from './main/main';

export const routes: Routes = [
  { 
    path: 'login', 
    loadComponent: () => import('./login/login').then(m => m.LoginComponent) 
  },
  
  { 
    path: 'main', 
    component: MainComponent,
    canActivate: [AuthGuard],
  },
  
  { 
    path: '',
    redirectTo: 'login', 
    pathMatch: 'full' 
  }
];