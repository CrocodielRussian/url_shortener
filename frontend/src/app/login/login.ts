import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { AuthService } from '../services/auth.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [FormsModule, CommonModule],
  templateUrl: './login.html',
  styleUrl: './login.css'
})
export class LoginComponent {
  username: string = '';
  password: string = '';

  constructor(
    private authService: AuthService, 
    private router: Router
  ) {}

  onLogin() {
	  this.authService.login(this.username, this.password).subscribe({
      next: () => {
        console.log('Login success!');
        this.router.navigate(['/main']);
      },
      error: () => alert('Ошибка входа')
	  });
	}

  onRegister() {
    this.authService.register(this.username, this.password).subscribe({
      next: () => alert('Регистрация успешна, теперь войдите'),
      error: () => alert('Ошибка регистрации')
    });
  }
}