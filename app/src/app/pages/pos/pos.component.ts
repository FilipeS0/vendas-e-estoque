import { Component, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { ClienteService, ClienteSummary } from '../../services/cliente.service';

@Component({
  selector: 'app-pos',
  imports: [
    MatCardModule,
    MatFormFieldModule,
    MatSelectModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
  ],
  templateUrl: './pos.component.html',
  styleUrls: ['./pos.component.css'],
})
export class PosPageComponent {
  private clienteService = inject(ClienteService);
  clientes = signal<ClienteSummary[]>([]);
  selectedCustomerId = signal<string | null>(null);

  ngOnInit() {
    this.clienteService
      .getClientes()
      .pipe(takeUntilDestroyed(this))
      .subscribe({ next: (c) => this.clientes.set(c), error: () => this.clientes.set([]) });
  }

  openCrediario() {
    if (!this.selectedCustomerId()) return;
    // Placeholder action: navigate to customer credit/payment flow later
    alert(`Abrindo Crediário para cliente ${this.selectedCustomerId()}`);
  }
}
