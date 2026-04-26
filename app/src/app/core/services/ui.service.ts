import { Injectable, signal } from '@angular/core';

export type NotificationSeverity = 'success' | 'warning' | 'error' | 'info';

export interface UiNotification {
  message: string;
  severity: NotificationSeverity;
  timestamp: number;
}

@Injectable({
  providedIn: 'root',
})
export class UiService {
  sidebarOpen = signal(true);
  loading = signal(false);
  notification = signal<UiNotification | null>(null);

  toggleSidebar() {
    this.sidebarOpen.update((current) => !current);
  }

  openSidebar() {
    this.sidebarOpen.set(true);
  }

  closeSidebar() {
    this.sidebarOpen.set(false);
  }

  showLoading() {
    this.loading.set(true);
  }

  hideLoading() {
    this.loading.set(false);
  }

  notify(message: string, severity: NotificationSeverity = 'info') {
    this.notification.set({
      message,
      severity,
      timestamp: Date.now(),
    });
  }

  clearNotification() {
    this.notification.set(null);
  }
}
