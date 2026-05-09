import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { BrasilApiService } from '../../../../core/services/brasil-api.service';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-fornecedor-create-dialog',
  imports: [
    ReactiveFormsModule,
    MatButtonModule,
    MatDialogModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatSnackBarModule,
    MatProgressSpinnerModule,
    CommonModule,
  ],
  templateUrl: './fornecedor-create-dialog.component.html',
  styleUrls: ['./fornecedor-create-dialog.component.css'],
})
export class FornecedorCreateDialogComponent {
  private fb = inject(FormBuilder);
  private dialogRef = inject(MatDialogRef<FornecedorCreateDialogComponent>);
  private brasilApi = inject(BrasilApiService);
  private snackBar = inject(MatSnackBar);

  form = this.fb.group({
    nome: ['', Validators.required],
    cnpj: ['', [Validators.minLength(14), Validators.maxLength(18)]],
    email: ['', [Validators.email]],
    telefone: [''],
    cep: ['', [Validators.minLength(8), Validators.maxLength(9)]],
    logradouro: [''],
    numero: [''],
    bairro: [''],
    cidade: [''],
    uf: ['', [Validators.minLength(2), Validators.maxLength(2)]],
    complemento: [''],
  });

  isSaving = signal(false);
  isLoadingCnpj = signal(false);
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

  consultarCnpj() {
    const cnpj = this.form.get('cnpj')?.value?.replace(/\D/g, '');
    if (!cnpj || cnpj.length !== 14) return;

    this.isLoadingCnpj.set(true);
    this.brasilApi.consultarCnpj(cnpj).subscribe({
      next: (res) => {
        this.form.patchValue({
          nome: res.nome_fantasia || res.razao_social,
          email: res.email,
          telefone: res.telefone,
          cep: res.cep,
          logradouro: res.logradouro,
          numero: res.numero,
          bairro: res.bairro,
          cidade: res.municipio,
          uf: res.uf,
        });
        this.isLoadingCnpj.set(false);
        this.snackBar.open('Dados do CNPJ carregados!', 'OK', { duration: 2000 });
      },
      error: () => {
        this.isLoadingCnpj.set(false);
        this.snackBar.open('CNPJ não encontrado ou erro na busca.', 'OK', { duration: 3000 });
      },
    });
  }

  consultarCep() {
    const cep = this.form.get('cep')?.value?.replace(/\D/g, '');
    if (!cep || cep.length !== 8) return;

    this.isLoadingCep.set(true);
    this.brasilApi.consultarCep(cep).subscribe({
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

  formatCnpj(event: Event) {
    const input = event.target as HTMLInputElement;
    const value = input.value.replace(/\D/g, '').slice(0, 14);
    const formatted = value
      .replace(/^(\d{2})(\d)/, '$1.$2')
      .replace(/^(\d{2})\.(\d{3})(\d)/, '$1.$2.$3')
      .replace(/\.(\d{3})(\d)/, '.$1/$2')
      .replace(/(\d{4})(\d)/, '$1-$2');
    input.value = formatted;
    this.form.controls.cnpj.setValue(formatted, { emitEvent: false });
  }
}
