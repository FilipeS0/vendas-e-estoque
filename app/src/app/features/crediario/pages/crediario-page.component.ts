import { Component, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { DatePipe, CurrencyPipe, LowerCasePipe } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { CrediarioService, ParcelaResponse } from '../services/crediario.service';

@Component({
  selector: 'app-crediario-page',
  standalone: true,
  imports: [
    ReactiveFormsModule, MatCardModule, MatTableModule, MatButtonModule, 
    MatIconModule, MatFormFieldModule, MatSelectModule, MatSnackBarModule,
    DatePipe, CurrencyPipe, LowerCasePipe
  ],
  templateUrl: './crediario-page.component.html',
  styleUrls: ['./crediario-page.component.css']
})
export class CrediarioPageComponent implements OnInit {
  private crediarioService = inject(CrediarioService);
  private fb = inject(FormBuilder);
  private snackBar = inject(MatSnackBar);

  parcelas = signal<ParcelaResponse[]>([]);
  isLoading = signal(false);

  filterForm = this.fb.group({
    status: ['']
  });

  displayedColumns = ['numeroParcela', 'valor', 'dataVencimento', 'status', 'valorPago', 'actions'];

  ngOnInit() {
    this.carregar();
    this.filterForm.get('status')?.valueChanges.subscribe(() => {
      this.carregar();
    });
  }

  carregar() {
    this.isLoading.set(true);
    const status = this.filterForm.value.status || undefined;
    this.crediarioService.getParcelas(undefined, status, 0, 50).subscribe({
      next: (res) => {
        this.parcelas.set(res.content);
        this.isLoading.set(false);
      },
      error: () => {
        this.snackBar.open('Erro ao carregar parcelas.', 'OK', { duration: 3000 });
        this.isLoading.set(false);
      }
    });
  }

  liquidar(parcela: ParcelaResponse) {
    const caixaId = prompt('Digite o UUID do Caixa ativo para liquidar a parcela:', '');
    if (!caixaId) return;

    this.isLoading.set(true);
    this.crediarioService.liquidarParcela(parcela.id, { valorPago: parcela.valor, caixaId }).subscribe({
      next: () => {
        this.snackBar.open('Parcela liquidada com sucesso!', 'OK', { duration: 3000 });
        this.carregar();
      },
      error: () => {
        this.snackBar.open('Erro ao liquidar parcela.', 'OK', { duration: 3000 });
        this.isLoading.set(false);
      }
    });
  }
}
