import { Component, inject, signal } from '@angular/core';
import { MatCardModule } from '@angular/material/card';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';

import { UsuarioService } from '../../services/usuario.service';
import { Usuario } from '../../../../shared/index';
import { UsuarioFormDialogComponent } from '../../components/usuario-form-dialog/usuario-form-dialog.component';

@Component({
  selector: 'app-usuario-list',
  imports: [
    MatCardModule, MatTableModule, MatButtonModule, MatIconModule,
    MatSnackBarModule, MatDialogModule
  ],
  templateUrl: './usuario-list.component.html',
  styleUrls: ['./usuario-list.component.css']
})
export class UsuarioListComponent {
  private usuarioService = inject(UsuarioService);
  private dialog = inject(MatDialog);
  private snackBar = inject(MatSnackBar);

  usuarios = signal<Usuario[]>([]);
  isLoading = signal(false);
  displayedColumns = ['nome', 'email', 'perfil', 'status', 'actions'];

  ngOnInit() {
    this.carregar();
  }

  carregar() {
    this.isLoading.set(true);
    this.usuarioService.getUsuarios(0, 50).subscribe({
      next: (res) => {
        this.usuarios.set(res.content);
        this.isLoading.set(false);
      },
      error: () => {
        this.snackBar.open('Erro ao carregar usuários.', 'OK', { duration: 3000 });
        this.isLoading.set(false);
      }
    });
  }

  abrirDialog(usuario?: Usuario) {
    const dialogRef = this.dialog.open(UsuarioFormDialogComponent, {
      width: '500px',
      data: usuario
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) this.carregar();
    });
  }

  inativar(usuario: Usuario) {
    if (confirm(`Deseja realmente inativar o usuário ${usuario.nome}?`)) {
      this.usuarioService.inativarUsuario(usuario.id).subscribe({
        next: () => {
          this.snackBar.open('Usuário inativado com sucesso!', 'OK', { duration: 3000 });
          this.carregar();
        },
        error: () => {
          this.snackBar.open('Erro ao inativar usuário.', 'OK', { duration: 3000 });
        }
      });
    }
  }
}
