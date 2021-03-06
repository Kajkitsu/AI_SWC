import { Injectable } from '@angular/core';
import { CanActivate, Router } from '@angular/router';
import { Observable } from 'rxjs';
import { AuthService } from '../_services/auth.service';
import { AlertifyService } from '../_services/alertify.service';

@Injectable({
  providedIn: 'root',
})
export class AdminGuard implements CanActivate {
  constructor(
    private authService: AuthService,
    private router: Router,
    private alertifyService: AlertifyService
  ) {}

  canActivate(): boolean {
    if (this.authService.loggedIn() && this.authService.isAdminLogged()) {
      return true;
    }
    this.router.navigate(['/login']);
    return false;
  }
}