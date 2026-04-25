import { Component, inject, OnInit } from '@angular/core';
import { AuthService } from '../../services/auth.service';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { Router } from '@angular/router';

@Component({
  selector: 'app-dashboard',
  imports: [MatCardModule, MatButtonModule, MatIconModule],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardPageComponent implements OnInit {
  public authService = inject(AuthService);
  private router = inject(Router);

  ngOnInit() {
    if (!this.authService.currentUser()) {
      this.authService.loadProfile().subscribe();
    }
  }

  logout() {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
