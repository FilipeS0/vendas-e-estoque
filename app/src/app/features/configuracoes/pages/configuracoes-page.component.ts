import { Component, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { ConfiguracoesService } from '../services/configuracoes.service';

@Component({
  selector: 'app-configuracoes-page',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatIconModule,
    MatSnackBarModule,
  ],
  templateUrl: './configuracoes-page.component.html',
  styleUrls: ['./configuracoes-page.component.css']
})
export class ConfiguracoesPageComponent implements OnInit {
  private fb = inject(FormBuilder);
  private configService = inject(ConfiguracoesService);
  private snackBar = inject(MatSnackBar);

  isLoading = signal(false);

  form = this.fb.group({
    razaoSocial: ['', Validators.required],
    cnpj: ['', Validators.required],
    inscricaoEstadual: [''],
    endereco: [''],
    regimeTributario: ['SIMPLES_NACIONAL', Validators.required],
    ambienteSefaz: ['HOMOLOGACAO', Validators.required],
    serieNfce: [1, [Validators.required, Validators.min(1)]],
    numeroSequencialNfce: [1, [Validators.required, Validators.min(1)]],
    impressoraTermicaIp: [''],
    impressoraTermicaPorta: [9100],
    alertaEstoqueMinimoGlobal: [5, [Validators.required, Validators.min(0)]]
  });

  ngOnInit() {
    this.carregar();
  }

  async carregar() {
    this.isLoading.set(true);
    const config = await this.configService.getConfiguracao();
    if (config) {
      this.form.patchValue(config);
    }
    this.isLoading.set(false);
  }

  salvar() {
    if (this.form.invalid) return;
    this.isLoading.set(true);
    this.configService.salvarConfiguracao(this.form.getRawValue() as any).subscribe({
      next: () => {
        this.snackBar.open('Configurações salvas com sucesso!', 'OK', { duration: 3000 });
        this.isLoading.set(false);
      },
      error: () => {
        this.snackBar.open('Erro ao salvar configurações.', 'OK', { duration: 3000 });
        this.isLoading.set(false);
      }
    });
  }
}
