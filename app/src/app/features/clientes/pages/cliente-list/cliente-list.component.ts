import { Component, DestroyRef, inject, signal, ViewChild } from '@angular/core';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatPaginator, MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTableModule } from '@angular/material/table';
import { CurrencyPipe } from '@angular/common';
import { debounceTime, distinctUntilChanged } from 'rxjs';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { Router } from '@angular/router';
import { ClienteService } from '../../services/cliente.service';
import { Cliente } from '../../../../shared/index';
import { ClienteCreateDialogComponent } from '../../components/cliente-create-dialog/cliente-create-dialog.component';

@Component({
  selector: 'app-cliente-list',
  imports: [
    ReactiveFormsModule,
    MatButtonModule,
    MatCardModule,
    MatDialogModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatPaginatorModule,
    MatProgressSpinnerModule,
    MatTableModule,
    CurrencyPipe,
  ],
  templateUrl: './cliente-list.component.html',
  styleUrls: ['./cliente-list.component.css'],
})
export class ClienteListComponent {
  private clienteService = inject(ClienteService);
  private router = inject(Router);
  private dialog = inject(MatDialog);
  private destroyRef = inject(DestroyRef);

  @ViewChild(MatPaginator) paginator!: MatPaginator;

  dataSource = signal<Cliente[]>([]);
  totalElements = signal(0);
  pageSize = signal(10);
  currentPage = signal(0);
  isLoading = signal(false);

  searchControl = new FormControl('', { nonNullable: true });

  displayedColumns = ['nome', 'cpf', 'telefone', 'limiteCredito', 'saldoDevedor', 'ativo', 'actions'];

  ngOnInit() {
    this.loadClientes();

    this.searchControl.valueChanges
      .pipe(debounceTime(400), distinctUntilChanged(), takeUntilDestroyed(this.destroyRef))
      .subscribe(() => {
        this.currentPage.set(0);
        if (this.paginator) this.paginator.pageIndex = 0;
        this.loadClientes();
      });
  }

  loadClientes() {
    this.isLoading.set(true);
    const nome = this.searchControl.value || undefined;

    this.clienteService.getClientesPage(nome, this.currentPage(), this.pageSize()).subscribe({
      next: (res) => {
        this.dataSource.set(res.content);
        this.totalElements.set(res.totalElements);
        this.isLoading.set(false);
      },
      error: () => {
        this.dataSource.set([]);
        this.totalElements.set(0);
        this.isLoading.set(false);
      },
    });
  }

  onPageChange(event: PageEvent) {
    this.pageSize.set(event.pageSize);
    this.currentPage.set(event.pageIndex);
    this.loadClientes();
  }

  clearSearch() {
    this.searchControl.setValue('');
  }

  viewDetails(cliente: Cliente) {
    this.router.navigate(['/clientes', cliente.id]);
  }

  openNewCustomerDialog() {
    const dialogRef = this.dialog.open(ClienteCreateDialogComponent, { width: '480px' });
    dialogRef.afterClosed().subscribe((result) => {
      if (result === 'created') this.loadClientes();
    });
  }
}
