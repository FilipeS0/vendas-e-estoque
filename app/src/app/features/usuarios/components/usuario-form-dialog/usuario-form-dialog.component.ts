import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';

import { UsuarioService } from '../../services/usuario.service';
import { Usuario, Perfil, UsuarioRequest } from '../../../../shared/index';

@Component({
  selector: 'app-usuario-form-dialog',
  imports: [
    ReactiveFormsModule, MatDialogModule, MatFormFieldModule,
    MatInputModule, MatSelectModule, MatButtonModule, MatSnackBarModule
  ],
  templateUrl: './usuario-form-dialog.component.html',
  styleUrls: ['./usuario-form-dialog.component.css']
})
export class UsuarioFormDialogComponent {
  private fb = inject(FormBuilder);
  private usuarioService = inject(UsuarioService);
  private snackBar = inject(MatSnackBar);
  private dialogRef = inject(MatDialogRef<UsuarioFormDialogComponent>);
  public data = inject<Usuario | undefined>(MAT_DIALOG_DATA);

  perfis = signal<Perfil[]>([]);
  isEdit = !!this.data;

  form = this.fb.group({
    nome: [this.data?.nome || '', Validators.required],
    email: [this.data?.email || '', [Validators.required, Validators.email]],
    senha: ['', this.isEdit ? [] : [Validators.required, Validators.minLength(6)]],
    perfilId: [this.data?.perfilId || '', Validators.required],
    ativo: [this.data?.ativo ?? true]
  });

  ngOnInit() {
    this.carregarPerfis();
  }

  carregarPerfis() {
    this.usuarioService.getPerfis().subscribe({
      next: (res) => this.perfis.set(res),
      error: () => this.snackBar.open('Erro ao carregar perfis.', 'OK', { duration: 3000 })
    });
  }

  salvar() {
    if (this.form.invalid) return;

    const req = this.form.value as UsuarioRequest;
    
    if (this.isEdit && this.data) {
      this.usuarioService.updateUsuario(this.data.id, req).subscribe({
        next: () => {
          this.snackBar.open('Usuário atualizado!', 'OK', { duration: 3000 });
          this.dialogRef.close(true);
        },
        error: (err) => this.snackBar.open(err.error?.message || 'Erro ao atualizar.', 'OK', { duration: 3000 })
      });
    } else {
      this.usuarioService.createUsuario(req).subscribe({
        next: () => {
          this.snackBar.open('Usuário criado!', 'OK', { duration: 3000 });
          this.dialogRef.close(true);
        },
        error: (err) => this.snackBar.open(err.error?.message || 'Erro ao criar.', 'OK', { duration: 3000 })
      });
    }
  }

  cancelar() {
    this.dialogRef.close();
  }
}
