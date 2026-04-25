import { Component, inject } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { ClienteService, ClienteDetalhe } from '../../services/cliente.service';
import { Installment } from '../../components/installments/installment-list.component';
import { InstallmentListComponent } from '../../components/installments/installment-list.component';
import { DecimalPipe } from '@angular/common';

@Component({
  selector: 'app-cliente-details',
  imports: [MatCardModule, MatButtonModule, InstallmentListComponent, DecimalPipe],
  templateUrl: './cliente-details.component.html',
  styleUrls: ['./cliente-details.component.css'],
})
export class ClienteDetailsPageComponent {
  private route = inject(ActivatedRoute);
  private clienteService = inject(ClienteService);

  public cliente: ClienteDetalhe | null = null;
  public openInstallments: Installment[] = [];

  ngOnInit() {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.clienteService
        .getClienteById(id)
        .subscribe({ next: (c) => (this.cliente = c), error: () => (this.cliente = null) });
      // Open installments endpoint will be available later; prepare empty array for now
      this.openInstallments = [];
    }
  }
}
