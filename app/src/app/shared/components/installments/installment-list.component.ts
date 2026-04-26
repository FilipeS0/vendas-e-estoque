import { Component, input } from '@angular/core';
import { MatTableModule } from '@angular/material/table';
import { MatIconModule } from '@angular/material/icon';
import { DecimalPipe } from '@angular/common';

export interface Installment {
  id: string;
  parcela: number;
  vencimento: string;
  valor: number;
  status?: string;
}

@Component({
  selector: 'app-installment-list',
  imports: [MatTableModule, MatIconModule, DecimalPipe],
  templateUrl: './installment-list.component.html',
  styleUrls: ['./installment-list.component.css'],
})
export class InstallmentListComponent {
  installments = input<Installment[]>([]);
  displayedColumns: string[] = ['parcela', 'vencimento', 'valor', 'status'];
}
