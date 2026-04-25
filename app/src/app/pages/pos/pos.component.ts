import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
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
    CommonModule,
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
  public clientes: ClienteSummary[] = [];
  public selectedCustomerId: string | null = null;

  ngOnInit() {
    this.clienteService
      .getClientes()
      .subscribe({ next: (c) => (this.clientes = c), error: () => (this.clientes = []) });
  }

  openCrediario() {
    if (!this.selectedCustomerId) return;
    // Placeholder action: navigate to customer credit/payment flow later
    alert(`Abrindo Crediário para cliente ${this.selectedCustomerId}`);
  }
}
