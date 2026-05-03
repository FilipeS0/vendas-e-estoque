import { Component, DestroyRef, TemplateRef, ViewChild, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormBuilder, FormControl, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatTableModule } from '@angular/material/table';
import { MatPaginatorModule, MatPaginator, PageEvent } from '@angular/material/paginator';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatDividerModule } from '@angular/material/divider';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { debounceTime, distinctUntilChanged } from 'rxjs';
import { EstoqueService, EstoqueAtual, MovimentacaoRequest } from '../services/estoque.service';
import { ReportsService } from '../../relatorios/services/reports.service';

@Component({
  selector: 'app-estoque-list',
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
    MatSnackBarModule,
    MatDialogModule,
    MatDividerModule,
    MatProgressSpinnerModule,
  ],
  templateUrl: './estoque-list.component.html',
  styleUrls: ['./estoque-list.component.css'],
})
export class EstoqueListComponent {
  private estoqueService = inject(EstoqueService);
  private snackBar = inject(MatSnackBar);
  private dialog = inject(MatDialog);
  private destroyRef = inject(DestroyRef);
  private fb = inject(FormBuilder);
  private reportsService = inject(ReportsService);

  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild('movimentacaoDialog') movimentacaoDialog!: TemplateRef<any>;

  dataSource = signal<EstoqueAtual[]>([]);
  totalElements = signal(0);
  pageSize = signal(10);
  currentPage = signal(0);
  isLoading = signal(false);
  dialogProduct = signal<EstoqueAtual | null>(null);

  searchControl = new FormControl('');

  movimentacaoForm = this.fb.group({
    tipo: ['ENTRADA_COMPRA', Validators.required],
    quantidade: [null, [Validators.required, Validators.min(1)]],
    motivo: ['', Validators.required],
  });

  movimentacaoTypes = [
    { value: 'ENTRADA_COMPRA', label: 'Entrada compra' },
    { value: 'ENTRADA_DEVOLUCAO', label: 'Entrada devolução' },
    { value: 'SAIDA_PERDA', label: 'Saída perda' },
    { value: 'AJUSTE_INVENTARIO', label: 'Ajuste inventário' },
  ];

  displayedColumns = [
    'codigoInterno',
    'produtoNome',
    'quantidadeAtual',
    'quantidadeMinima',
    'status',
    'actions',
  ];

  ngOnInit() {
    this.loadEstoque();

    this.searchControl.valueChanges
      .pipe(debounceTime(400), distinctUntilChanged(), takeUntilDestroyed(this.destroyRef))
      .subscribe(() => {
        this.currentPage.set(0);
        if (this.paginator) {
          this.paginator.pageIndex = 0;
        }
        this.loadEstoque();
      });
  }

  ngAfterViewInit() {
    if (this.paginator) {
      this.paginator.page
        .pipe(takeUntilDestroyed(this.destroyRef))
        .subscribe((event: PageEvent) => {
          this.currentPage.set(event.pageIndex);
          this.pageSize.set(event.pageSize);
          this.loadEstoque();
        });
    }
  }

  loadEstoque() {
    this.isLoading.set(true);
    const nome = this.searchControl.value || undefined;

    this.estoqueService.getEstoque(nome, this.currentPage(), this.pageSize()).subscribe({
      next: (res) => {
        this.dataSource.set(res.content);
        this.totalElements.set(res.totalElements);
        this.isLoading.set(false);
      },
      error: () => {
        this.snackBar.open('Erro ao carregar estoque.', 'OK', { duration: 3000 });
        this.isLoading.set(false);
      },
    });
  }

  openMovimentacaoDialog(produto: EstoqueAtual) {
    this.dialogProduct.set(produto);
    this.movimentacaoForm.reset({ tipo: 'ENTRADA_COMPRA', quantidade: null, motivo: '' });
    this.dialog.open(this.movimentacaoDialog);
  }

  submitMovimentacao(dialogRef: any) {
    if (this.movimentacaoForm.invalid) {
      this.movimentacaoForm.markAllAsTouched();
      return;
    }

    const produto = this.dialogProduct();
    if (!produto) {
      dialogRef.close();
      return;
    }

    const request = this.movimentacaoForm.getRawValue() as unknown as MovimentacaoRequest;
    this.isLoading.set(true);

    this.estoqueService.registrarMovimentacao(produto.produtoId, request).subscribe({
      next: () => {
        this.snackBar.open('Movimentação registrada com sucesso!', 'OK', { duration: 3000 });
        dialogRef.close();
        this.loadEstoque();
      },
      error: () => {
        this.snackBar.open('Erro ao registrar movimentação.', 'OK', {
          duration: 3000,
          panelClass: ['error-snackbar'],
        });
        this.isLoading.set(false);
      },
    });
  }

  closeMovimentacaoDialog(dialogRef: any) {
    dialogRef.close();
  }

  isLowStock(item: EstoqueAtual) {
    return item.quantidadeAtual <= item.quantidadeMinima;
  }

  exportPdf() {
    this.reportsService.exportarPdf('estoque/posicao', {}).subscribe((blob) => {
      const url = URL.createObjectURL(blob);
      window.open(url, '_blank');
    });
  }
}
