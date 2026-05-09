import { Component, DestroyRef, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { BrasilApiService } from '../../../../core/services/brasil-api.service';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

@Component({
  selector: 'app-cliente-create-dialog',
  imports: [
    ReactiveFormsModule,
    MatButtonModule,
    MatDialogModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatSnackBarModule,
    MatProgressSpinnerModule,
  ],
  templateUrl: './cliente-create-dialog.component.html',
  styleUrls: ['./cliente-create-dialog.component.css'],
})
export class ClienteCreateDialogComponent {
  private fb = inject(FormBuilder);
  private dialogRef = inject(MatDialogRef<ClienteCreateDialogComponent>);
  private brasilApi = inject(BrasilApiService);
  private snackBar = inject(MatSnackBar);
  private readonly destroyRef = inject(DestroyRef);

  form = this.fb.group({
    nome: ['', Validators.required],
    cpf: [''],
    telefone: [''],
    cep: ['', [Validators.minLength(8), Validators.maxLength(9)]],
    logradouro: [''],
    numero: [''],
    bairro: [''],
    cidade: [''],
    uf: ['', [Validators.minLength(2), Validators.maxLength(2)]],
    complemento: [''],
    limiteCredito: [0, [Validators.required, Validators.min(0)]],
  });

  isSaving = signal(false);
  isLoadingCep = signal(false);

  submit() {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.isSaving.set(true);
    this.dialogRef.close(this.form.getRawValue());
  }

  cancel() {
    this.dialogRef.close();
  }

  formatCpf(event: Event) {
    const input = event.target as HTMLInputElement;
    const value = input.value.replace(/\D/g, '').slice(0, 11);
    const formatted = value
      .replace(/(\d{3})(\d)/, '$1.$2')
      .replace(/(\d{3})(\d)/, '$1.$2')
      .replace(/(\d{3})(\d{1,2})$/, '$1-$2');
    input.value = formatted;
    this.form.controls.cpf.setValue(formatted, { emitEvent: false });
  }

  consultarCep() {
    const cep = this.form.get('cep')?.value?.replace(/\D/g, '');
    if (!cep || cep.length !== 8) return;

    this.isLoadingCep.set(true);
    this.brasilApi.consultarCep(cep)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
      next: (res) => {
        this.form.patchValue({
          logradouro: res.street,
          bairro: res.neighborhood,
          cidade: res.city,
          uf: res.state,
        });
        this.isLoadingCep.set(false);
        this.snackBar.open('CEP encontrado!', 'OK', { duration: 2000 });
      },
      error: () => {
        this.isLoadingCep.set(false);
        this.snackBar.open('CEP não encontrado.', 'OK', { duration: 3000 });
      },
    });
  }
}
