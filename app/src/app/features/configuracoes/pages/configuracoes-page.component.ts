import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { ConfiguracoesService } from '../services/configuracoes.service';
import { DatePipe } from '@angular/common';

@Component({
  selector: 'app-configuracoes-page',
  imports: [
    ReactiveFormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatIconModule,
    MatSnackBarModule,
    DatePipe,
  ],
  templateUrl: './configuracoes-page.component.html',
  styleUrls: ['./configuracoes-page.component.css']
})
export class ConfiguracoesPageComponent {
  private fb = inject(FormBuilder);
  private configService = inject(ConfiguracoesService);
  private snackBar = inject(MatSnackBar);

  isLoading = signal(false);
  statusCertificado = signal<any>(null);
  selectedFile: File | null = null;

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

  certificadoForm = this.fb.group({
    senha: ['', Validators.required]
  });

  ngOnInit() {
    this.carregar();
    this.carregarStatusCertificado();
  }

  async carregar() {
    this.isLoading.set(true);
    const config = await this.configService.getConfiguracao();
    if (config) {
      this.form.patchValue(config);
    }
    this.isLoading.set(false);
  }

  carregarStatusCertificado() {
    this.configService.getStatusCertificado().subscribe({
      next: (status) => this.statusCertificado.set(status),
      error: () => this.statusCertificado.set(null)
    });
  }

  onFileSelected(event: any) {
    this.selectedFile = event.target.files[0];
  }

  uploadCertificado() {
    if (!this.selectedFile || this.certificadoForm.invalid) return;

    this.isLoading.set(true);
    const senha = this.certificadoForm.value.senha!;

    this.configService.uploadCertificado(this.selectedFile, senha).subscribe({
      next: () => {
        this.snackBar.open('Certificado enviado com sucesso!', 'OK', { duration: 3000 });
        this.isLoading.set(false);
        this.selectedFile = null;
        this.certificadoForm.reset();
        this.carregarStatusCertificado();
      },
      error: (err) => {
        this.snackBar.open(err.error?.message || 'Erro ao enviar certificado.', 'OK', { duration: 3000 });
        this.isLoading.set(false);
      }
    });
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
