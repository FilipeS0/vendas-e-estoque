import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { MatSnackBar } from '@angular/material/snack-bar';
import { AuthService } from '../../../core/auth/auth.service';
import { MatDivider } from '@angular/material/divider';

@Component({
  selector: 'app-perfil',
  imports: [
    ReactiveFormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatChipsModule,
    MatDivider,
  ],
  templateUrl: './perfil.component.html',
  styleUrls: ['./perfil.component.css'],
})
export class PerfilComponent {
  private authService = inject(AuthService);
  private snackBar = inject(MatSnackBar);
  private fb = inject(FormBuilder);

  user = this.authService.currentUser;
  isSaving = signal(false);

  form = this.fb.group({
    senhaAtual: ['', Validators.required],
    novaSenha: ['', [Validators.required, Validators.minLength(6)]],
    confirmacao: ['', Validators.required],
  });

  get roles(): string[] {
    return this.user()?.authorities?.map((a) => a.authority) ?? [];
  }

  roleLabel(role: string): string {
    const map: Record<string, string> = {
      ROLE_ADMIN: 'Administrador',
      ROLE_GERENTE: 'Gerente',
      ROLE_OPERADOR: 'Operador',
    };
    return map[role] ?? role;
  }

  submitSenha() {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const { novaSenha, confirmacao } = this.form.value;
    if (novaSenha !== confirmacao) {
      this.snackBar.open('As senhas não coincidem.', 'OK', { duration: 3000 });
      return;
    }
    // TODO: chamar endpoint PUT /api/v1/auth/senha quando disponível no backend
    this.snackBar.open('Funcionalidade de troca de senha em breve.', 'OK', { duration: 3000 });
  }
}
