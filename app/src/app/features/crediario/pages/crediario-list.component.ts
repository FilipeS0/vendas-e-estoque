import { Component, DestroyRef, inject, signal, ViewChild } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { CurrencyPipe, DatePipe } from '@angular/common';
import { MatTableModule } from '@angular/material/table';
import { MatPaginator, MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { MatChipsModule } from '@angular/material/chips';
import { MatDialog, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { FormControl } from '@angular/forms';
import { debounceTime, distinctUntilChanged } from 'rxjs';
import {
  CrediarioService,
  ParcelaResponse,
  StatusParcela,
  LiquidarParcelaRequest,
} from '../services/crediario.service';
import { ClienteService } from '../../clientes/services/cliente.service';
import { CaixaService } from '../../caixa/services/caixa.service';
import { ReportsService } from '../../relatorios/services/reports.service';
import { Caixa } from '../../../shared/index';

@Component({
  selector: 'app-crediario-list',
  imports: [
    ReactiveFormsModule,
    MatTableModule,
    MatPaginatorModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatIconModule,
    MatCardModule,
    MatChipsModule,
    MatDialogModule,
    MatProgressSpinnerModule,
    CurrencyPipe,
    DatePipe,
  ],
  templateUrl: './crediario-list.component.html',
  styleUrls: ['./crediario-list.component.css'],
})
export class CrediarioListComponent {
  private crediarioService = inject(CrediarioService);
  private clienteService = inject(ClienteService);
  private caixaService = inject(CaixaService);
  private snackBar = inject(MatSnackBar);
  private reportsService = inject(ReportsService);
  private dialog = inject(MatDialog);
  private fb = inject(FormBuilder);
  private destroyRef = inject(DestroyRef);

  @ViewChild(MatPaginator) paginator!: MatPaginator;

  dataSource = signal<ParcelaResponse[]>([]);
  totalElements = signal(0);
  pageSize = signal(10);
  currentPage = signal(0);
  isLoading = signal(false);
  caixas = signal<Caixa[]>([]);
  parcelaSelecionada = signal<ParcelaResponse | null>(null);

  clienteSearch = new FormControl('');
  statusFilter = new FormControl<StatusParcela | ''>('');

  liquidarForm = this.fb.group({
    valorPago: [null as number | null, [Validators.required, Validators.min(0.01)]],
    caixaId: ['', Validators.required],
  });

  statusOptions: { value: StatusParcela | ''; label: string }[] = [
    { value: '', label: 'Todos' },
    { value: 'PENDENTE', label: 'Pendente' },
    { value: 'ATRASADO', label: 'Atrasado' },
    { value: 'PAGO_PARCIAL', label: 'Pago Parcial' },
    { value: 'PAGO', label: 'Pago' },
    { value: 'CANCELADO', label: 'Cancelado' },
  ];

  displayedColumns = [
    'cliente',
    'numeroParcela',
    'valor',
    'dataVencimento',
    'valorPago',
    'status',
    'actions',
  ];

  ngOnInit() {
    this.loadParcelas();
    this.loadCaixas();

    this.clienteSearch.valueChanges
      .pipe(debounceTime(400), distinctUntilChanged(), takeUntilDestroyed(this.destroyRef))
      .subscribe(() => {
        this.currentPage.set(0);
        this.loadParcelas();
      });

    this.statusFilter.valueChanges.pipe(takeUntilDestroyed(this.destroyRef)).subscribe(() => {
      this.currentPage.set(0);
      this.loadParcelas();
    });
  }

  loadParcelas() {
    this.isLoading.set(true);
    const status = this.statusFilter.value || undefined;

    this.crediarioService
      .getParcelas(
        undefined,
        status as StatusParcela | undefined,
        this.currentPage(),
        this.pageSize(),
      )
      .subscribe({
        next: (res) => {
          this.dataSource.set(res.content);
          this.totalElements.set(res.totalElements);
          this.isLoading.set(false);
        },
        error: () => {
          this.snackBar.open('Erro ao carregar parcelas.', 'OK', { duration: 3000 });
          this.isLoading.set(false);
        },
      });
  }

  private async loadCaixas() {
    try {
      const all = await this.caixaService.getCaixas();
      this.caixas.set(
        all.filter(
          (c) => c.status?.toLowerCase() === 'aberto' || c.status?.toLowerCase() === 'open',
        ),
      );
    } catch {
      this.caixas.set([]);
    }
  }

  onPageChange(event: PageEvent) {
    this.pageSize.set(event.pageSize);
    this.currentPage.set(event.pageIndex);
    this.loadParcelas();
  }

  openLiquidarDialog(parcela: ParcelaResponse, templateRef: any) {
    this.parcelaSelecionada.set(parcela);
    this.liquidarForm.reset({ valorPago: parcela.valor, caixaId: '' });
    this.dialog.open(templateRef, { width: '420px' });
  }

  submitLiquidar(dialogRef: MatDialogRef<any>) {
    if (this.liquidarForm.invalid) {
      this.liquidarForm.markAllAsTouched();
      return;
    }
    const parcela = this.parcelaSelecionada();
    if (!parcela) return;

    const req: LiquidarParcelaRequest = {
      valorPago: this.liquidarForm.value.valorPago!,
      caixaId: this.liquidarForm.value.caixaId!,
    };

    this.isLoading.set(true);
    this.crediarioService.liquidarParcela(parcela.id, req).subscribe({
      next: () => {
        this.snackBar.open('Parcela liquidada com sucesso!', 'OK', { duration: 3000 });
        dialogRef.close();
        this.loadParcelas();
      },
      error: (e) => {
        this.snackBar.open(e?.error?.message || 'Erro ao liquidar parcela.', 'OK', {
          duration: 3000,
        });
        this.isLoading.set(false);
      },
    });
  }

  statusColor(status: StatusParcela): string {
    const map: Record<StatusParcela, string> = {
      PENDENTE: 'accent',
      ATRASADO: 'warn',
      PAGO_PARCIAL: 'accent',
      PAGO: 'primary',
      CANCELADO: '',
    };
    return map[status] ?? '';
  }

  isPendente(status: StatusParcela) {
    return status === 'PENDENTE' || status === 'ATRASADO' || status === 'PAGO_PARCIAL';
  }

  exportPdf() {
    this.reportsService.exportarPdf('contas-a-receber/resumo', {}).subscribe((blob) => {
      const url = URL.createObjectURL(blob);
      window.open(url, '_blank');
    });
  }
}
