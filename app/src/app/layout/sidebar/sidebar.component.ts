import { Component, inject } from '@angular/core';
import { RouterModule } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { MatListModule } from '@angular/material/list';
import { MatButtonModule } from '@angular/material/button';
import { UiService } from '../../core/services/ui.service';

@Component({
  selector: 'app-sidebar',
  imports: [RouterModule, MatIconModule, MatListModule, MatButtonModule],
  templateUrl: './sidebar.component.html',
  styleUrls: ['./sidebar.component.css'],
})
export class SidebarComponent {
  private uiService = inject(UiService);
  readonly sidebarOpen = this.uiService.sidebarOpen;
  toggleSidebar() { this.uiService.toggleSidebar(); }
}
