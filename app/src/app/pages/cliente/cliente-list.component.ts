import { Component, DestroyRef, inject, signal } from '@angular/core';
import { FormBuilder, FormControl, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatDialog, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTableModule } from '@angular/material/table';
import { CurrencyPipe } from '@angular/common';
import { debounceTime, distinctUntilChanged } from 'rxjs';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { Router } from '@angular/router';
import { ClienteService } from '../../services/cliente.service';
import { Cliente, ClienteRequest } from '../../models';

@Component({
  selector: 'app-cliente-list',
  standalone: true,
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
  private fb = inject(FormBuilder);
  private destroyRef = inject(DestroyRef);

  allClientes = signal<Cliente[]>([]);
  dataSource = signal<Cliente[]>([]);
  totalElements = signal(0);
  pageSize = signal(10);
  currentPage = signal(0);
  isLoading = signal(false);

  searchControl = new FormControl('', { nonNullable: true });
  displayedColumns = [
    'nome',
    'cpf',
    'telefone',
    'limiteCredito',
    'saldoDevedor',
    'ativo',
    'actions',
  ];

  ngOnInit() {
    this.loadClientes();

    this.searchControl.valueChanges
      .pipe(debounceTime(400), distinctUntilChanged(), takeUntilDestroyed(this.destroyRef))
      .subscribe(() => {
        this.currentPage.set(0);
        this.applyFilters();
      });
  }

  private async loadClientes() {
    this.isLoading.set(true);
    try {
      const clientes = await this.clienteService.getClientes();
      this.allClientes.set(clientes);
      this.applyFilters();
    } catch {
      this.allClientes.set([]);
      this.dataSource.set([]);
      this.totalElements.set(0);
    } finally {
      this.isLoading.set(false);
    }
  }

  private applyFilters() {
    const term = this.searchControl.value.trim().toLowerCase();
    const filtered = this.allClientes().filter((cliente) =>
      cliente.nome.toLowerCase().includes(term),
    );

    this.totalElements.set(filtered.length);
    const start = this.currentPage() * this.pageSize();
    this.dataSource.set(filtered.slice(start, start + this.pageSize()));
  }

  onPageChange(event: PageEvent) {
    this.pageSize.set(event.pageSize);
    this.currentPage.set(event.pageIndex);
    this.applyFilters();
  }

  clearSearch() {
    this.searchControl.setValue('');
  }

  viewDetails(cliente: Cliente) {
    this.router.navigate(['/clientes', cliente.id]);
  }

  openNewCustomerDialog() {
    const dialogRef = this.dialog.open(ClienteCreateDialogComponent, {
      width: '480px',
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (result === 'created') {
        this.loadClientes();
      }
    });
  }
}

@Component({
  selector: 'app-cliente-create-dialog',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatButtonModule,
    MatDialogModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
  ],
  template: `
    <h2 mat-dialog-title>Novo cliente</h2>
    <mat-dialog-content class="dialog-content">
      <form [formGroup]="form" class="dialog-form">
        <mat-form-field appearance="outline" class="full-width">
          <mat-label>Nome</mat-label>
          <input matInput formControlName="nome" autocomplete="name" />
          <mat-error *ngIf="form.controls.nome.invalid && form.controls.nome.touched">
            O nome é obrigatório.
          </mat-error>
        </mat-form-field>

        <mat-form-field appearance="outline" class="full-width">
          <mat-label>CPF</mat-label>
          <input
            matInput
            formControlName="cpf"
            maxlength="14"
            autocomplete="off"
            (input)="formatCpf($event)"
            placeholder="000.000.000-00"
          />
        </mat-form-field>

        <mat-form-field appearance="outline" class="full-width">
          <mat-label>Telefone</mat-label>
          <input matInput formControlName="telefone" autocomplete="tel" />
        </mat-form-field>

        <mat-form-field appearance="outline" class="full-width">
          <mat-label>Limite de crédito</mat-label>
          <input matInput type="number" formControlName="limiteCredito" min="0" />
          <mat-error
            *ngIf="form.controls.limiteCredito.invalid && form.controls.limiteCredito.touched"
          >
            O limite deve ser maior ou igual a 0.
          </mat-error>
        </mat-form-field>
      </form>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-stroked-button (click)="cancel()">Cancelar</button>
      <button
        mat-flat-button
        color="primary"
        (click)="submit()"
        [disabled]="form.invalid || isSaving()"
      >
        Criar cliente
      </button>
    </mat-dialog-actions>
  `,
  styles: [
    `
      .dialog-content {
        display: grid;
        gap: 16px;
        min-width: 360px;
      }

      .full-width {
        width: 100%;
      }
    `,
  ],
})
export class ClienteCreateDialogComponent {
  private clienteService = inject(ClienteService);
  private dialogRef = inject(MatDialogRef<ClienteCreateDialogComponent>);
  private fb = inject(FormBuilder);

  isSaving = signal(false);

  form = this.fb.group({
    nome: ['', Validators.required],
    cpf: [''],
    telefone: [''],
    limiteCredito: [0, [Validators.required, Validators.min(0)]],
  });

  formatCpf(event: Event) {
    const input = event.target as HTMLInputElement;
    const digits = input.value.replace(/\D/g, '').slice(0, 11);
    let formatted = digits;

    if (digits.length > 3) {
      formatted = `${digits.slice(0, 3)}.${digits.slice(3)}`;
    }
    if (digits.length > 6) {
      formatted = `${digits.slice(0, 3)}.${digits.slice(3, 6)}.${digits.slice(6)}`;
    }
    if (digits.length > 9) {
      formatted = `${digits.slice(0, 3)}.${digits.slice(3, 6)}.${digits.slice(6, 9)}-${digits.slice(9)}`;
    }

    this.form.controls.cpf.setValue(formatted, { emitEvent: false });
  }

  cancel() {
    this.dialogRef.close();
  }

  submit() {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const rawValue = this.form.getRawValue();
    const request: ClienteRequest = {
      nome: rawValue.nome || '',
      cpf: rawValue.cpf?.trim() || undefined,
      telefone: rawValue.telefone?.trim() || undefined,
      limiteCredito: Number(rawValue.limiteCredito) || 0,
    };

    this.isSaving.set(true);
    this.clienteService.createCliente(request).subscribe({
      next: () => {
        this.isSaving.set(false);
        this.dialogRef.close('created');
      },
      error: () => {
        this.isSaving.set(false);
        this.dialogRef.close();
      },
    });
  }
}
