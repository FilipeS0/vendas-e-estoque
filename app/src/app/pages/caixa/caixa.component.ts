import { Component, inject, signal, TemplateRef, ViewChild } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatTableModule } from '@angular/material/table';
import { MatDividerModule } from '@angular/material/divider';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { CurrencyPipe, DatePipe } from '@angular/common';
import { CaixaService, Lancamento } from '../../services/caixa.service';
import { Caixa } from '../../models';

@Component({
  selector: 'app-caixa',
  imports: [
    ReactiveFormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatSnackBarModule,
    MatDialogModule,
    MatTableModule,
    MatDividerModule,
    MatProgressSpinnerModule,
    CurrencyPipe,
    DatePipe,
  ],
  templateUrl: './caixa.component.html',
  styleUrls: ['./caixa.component.css'],
})
export class CaixaComponent {
  private fb = inject(FormBuilder);
  private caixaService = inject(CaixaService);
  private snackBar = inject(MatSnackBar);
  private dialog = inject(MatDialog);

  @ViewChild('lancamentoDialog') dialogTemplate!: TemplateRef<{
    tipo: 'SANGRIA' | 'SUPRIMENTO';
  }>;

  caixaAberta = signal<Caixa | null>(null);
  lancamentos = signal<Lancamento[]>([]);
  isLoading = signal(false);
  closeMessage = signal<string | null>(null);

  lancamentoForm = this.fb.group({
    valor: [null, [Validators.required, Validators.min(0)]],
    descricao: [''],
  });

  openForm = this.fb.group({
    valorAbertura: [null, [Validators.required, Validators.min(0)]],
  });

  closeForm = this.fb.group({
    valorFechamentoFis: [null, [Validators.required, Validators.min(0)]],
  });

  displayedColumns = ['tipo', 'formaPagamento', 'valor', 'descricao'];

  ngOnInit() {
    this.loadCaixa();
  }

  async loadCaixa() {
    this.isLoading.set(true);
    this.closeMessage.set(null);

    try {
      const caixas = await this.caixaService.getCaixas();
      const aberto = caixas.find(
        (caixa) =>
          caixa.status?.toLowerCase() === 'aberto' || caixa.status?.toLowerCase() === 'open',
      );

      this.caixaAberta.set(aberto ?? null);

      if (aberto) {
        await this.loadLancamentos(aberto.id);
      } else {
        this.lancamentos.set([]);
      }
    } catch (error: any) {
      this.snackBar.open('Erro ao carregar caixa.', 'OK', { duration: 3000 });
    } finally {
      this.isLoading.set(false);
    }
  }

  private async loadLancamentos(caixaId: string) {
    try {
      const items = await this.caixaService.getLancamentos(caixaId);
      this.lancamentos.set(items);
    } catch (error: any) {
      this.lancamentos.set([]);
    }
  }

  async openCaixa() {
    if (this.openForm.invalid) {
      this.openForm.markAllAsTouched();
      return;
    }

    const valorAbertura = this.openForm.get('valorAbertura')?.value ?? 0;
    this.isLoading.set(true);

    try {
      const caixa = await this.caixaService.abrirCaixa(valorAbertura);
      this.caixaAberta.set(caixa);
      this.openForm.reset();
      this.snackBar.open('Caixa aberto com sucesso!', 'OK', { duration: 3000 });
      await this.loadLancamentos(caixa.id);
    } catch (error: any) {
      this.snackBar.open('Erro ao abrir caixa.', 'OK', {
        duration: 3000,
        panelClass: ['error-snackbar'],
      });
    } finally {
      this.isLoading.set(false);
    }
  }

  async fecharCaixa() {
    const caixa = this.caixaAberta();
    if (!caixa) {
      return;
    }

    if (this.closeForm.invalid) {
      this.closeForm.markAllAsTouched();
      return;
    }

    const valorFechamentoFis = this.closeForm.get('valorFechamentoFis')?.value ?? 0;
    this.isLoading.set(true);

    try {
      const resultado = await this.caixaService.fecharCaixa(caixa.id, valorFechamentoFis);
      this.closeMessage.set(`Diferença do caixa: ${resultado.diferenca ?? 0}`);
      this.caixaAberta.set(null);
      this.lancamentos.set([]);
      this.closeForm.reset();
      this.snackBar.open('Caixa fechado com sucesso!', 'OK', { duration: 3000 });
    } catch (error: any) {
      this.snackBar.open('Erro ao fechar caixa.', 'OK', {
        duration: 3000,
        panelClass: ['error-snackbar'],
      });
    } finally {
      this.isLoading.set(false);
    }
  }

  openLancamentoDialog(tipo: 'SANGRIA' | 'SUPRIMENTO') {
    const caixa = this.caixaAberta();
    if (!caixa) {
      return;
    }

    this.lancamentoForm.reset();
    const dialogRef = this.dialog.open(this.dialogTemplate, {
      data: { tipo },
    });

    dialogRef.afterClosed().subscribe(async (result) => {
      if (!result) {
        return;
      }

      this.isLoading.set(true);
      try {
        if (tipo === 'SANGRIA') {
          await this.caixaService.registrarSangria(caixa.id, result.valor, result.descricao);
        } else {
          await this.caixaService.registrarSuprimento(caixa.id, result.valor, result.descricao);
        }

        this.snackBar.open(
          `${tipo === 'SANGRIA' ? 'Sangria' : 'Suprimento'} registrada com sucesso!`,
          'OK',
          { duration: 3000 },
        );
        await this.loadLancamentos(caixa.id);
      } catch (error: any) {
        this.snackBar.open(`Erro ao registrar ${tipo.toLowerCase()}.`, 'OK', {
          duration: 3000,
          panelClass: ['error-snackbar'],
        });
      } finally {
        this.isLoading.set(false);
      }
    });
  }

  submitLancamento(dialogRef: any) {
    if (this.lancamentoForm.invalid) {
      this.lancamentoForm.markAllAsTouched();
      return;
    }
    dialogRef.close(this.lancamentoForm.getRawValue());
  }

  closeLancamentoDialog(dialogRef: any) {
    dialogRef.close();
  }
}
