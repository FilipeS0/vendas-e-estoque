import { Component, inject, signal } from '@angular/core';
import { MatCardModule } from '@angular/material/card';
import { MatTableModule } from '@angular/material/table';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatChipsModule } from '@angular/material/chips';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { FornecedorService, FornecedorItem } from '../services/fornecedor.service';
import { FornecedorCreateDialogComponent } from '../components/fornecedor-create-dialog/fornecedor-create-dialog.component';

@Component({
  selector: 'app-fornecedor-list',
  imports: [
    MatCardModule,
    MatTableModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatChipsModule,
    MatButtonModule,
    MatDialogModule,
    MatSnackBarModule,
  ],
  templateUrl: './fornecedor-list.component.html',
  styleUrls: ['./fornecedor-list.component.css'],
})
export class FornecedorListComponent {
  private fornecedorService = inject(FornecedorService);
  private dialog = inject(MatDialog);
  private snackBar = inject(MatSnackBar);

  dataSource = signal<FornecedorItem[]>([]);
  isLoading = signal(true);
  displayedColumns = ['nome', 'cnpj', 'telefone', 'email', 'ativo'];

  ngOnInit() {
    this.fornecedorService.getAll().subscribe({
      next: (data) => {
        this.dataSource.set(data.content);
        this.isLoading.set(false);
      },
      error: () => {
        this.isLoading.set(false);
      },
    });
  }

  openNewFornecedorDialog() {
    const dialogRef = this.dialog.open(FornecedorCreateDialogComponent, { width: '550px' });
    dialogRef.afterClosed().subscribe((result) => {
      if (result) {
        this.isLoading.set(true);
        this.fornecedorService.create(result).subscribe({
          next: () => {
            this.snackBar.open('Fornecedor criado com sucesso!', 'OK', { duration: 3000 });
            this.loadFornecedores();
          },
          error: () => {
            this.isLoading.set(false);
            this.snackBar.open('Erro ao criar fornecedor.', 'OK', { duration: 5000 });
          },
        });
      }
    });
  }

  private loadFornecedores() {
    this.isLoading.set(true);
    this.fornecedorService.getAll().subscribe({
      next: (data) => {
        this.dataSource.set(data.content);
        this.isLoading.set(false);
      },
      error: () => {
        this.isLoading.set(false);
      },
    });
  }
}
