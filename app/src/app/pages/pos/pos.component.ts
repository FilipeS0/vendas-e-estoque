import { Component, computed, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormBuilder, FormControl, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatTableModule } from '@angular/material/table';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatDividerModule } from '@angular/material/divider';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { CurrencyPipe } from '@angular/common';
import { debounceTime, distinctUntilChanged } from 'rxjs';
import { ProdutoService, ProdutoResponse } from '../../services/produto.service';
import { ClienteService } from '../../services/cliente.service';
import { CaixaService } from '../../services/caixa.service';
import { VendaService, PagamentoRequest, VendaRequest } from '../../services/venda.service';
import { ClienteSummary, Caixa } from '../../models';

interface CarrinhoItem {
  produtoId: string;
  nome: string;
  quantidade: number;
  precoUnitario: number;
  valorTotal: number;
}

@Component({
  selector: 'app-pos',
  imports: [
    ReactiveFormsModule,
    MatTableModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatIconModule,
    MatCardModule,
    MatSnackBarModule,
    MatDividerModule,
    MatProgressSpinnerModule,
    CurrencyPipe,
  ],
  templateUrl: './pos.component.html',
  styleUrls: ['./pos.component.css'],
})
export class PosPageComponent {
  private produtoService = inject(ProdutoService);
  private clienteService = inject(ClienteService);
  private caixaService = inject(CaixaService);
  private vendaService = inject(VendaService);
  private snackBar = inject(MatSnackBar);
  private fb = inject(FormBuilder);
  private destroyRef = inject(DestroyRef);

  produtosBuscados = signal<ProdutoResponse[]>([]);
  carrinho = signal<CarrinhoItem[]>([]);
  pagamentos = signal<PagamentoRequest[]>([]);
  clienteSelecionado = signal<ClienteSummary | null>(null);
  caixaAtiva = signal<Caixa | null>(null);
  isLoading = signal(false);
  clientes = signal<ClienteSummary[]>([]);
  desconto = signal<number>(0);

  searchControl = new FormControl('');

  paymentForm = this.fb.group({
    formaPagamento: ['DINHEIRO', Validators.required],
    valor: [null, [Validators.required, Validators.min(0.01)]],
  });

  paymentTypes = [
    { value: 'DINHEIRO', label: 'Dinheiro' },
    { value: 'CARTAO_DEBITO', label: 'Cartão Débito' },
    { value: 'CARTAO_CREDITO', label: 'Cartão Crédito' },
    { value: 'PIX', label: 'PIX' },
    { value: 'CREDIARIO', label: 'Crediário' },
  ];

  displayedColumns = ['nome', 'quantidade', 'precoUnitario', 'valorTotal', 'actions'];

  valorBruto = computed(() => this.carrinho().reduce((sum, item) => sum + item.valorTotal, 0));
  total = computed(() => Math.max(0, this.valorBruto() - this.desconto()));
  totalPago = computed(() => this.pagamentos().reduce((sum, payment) => sum + payment.valor, 0));
  troco = computed(() => Math.max(0, this.totalPago() - this.total()));

  ngOnInit() {
    this.loadClientes();
    this.loadCaixa();
    this.loadProdutos('');

    this.searchControl.valueChanges
      .pipe(debounceTime(400), distinctUntilChanged(), takeUntilDestroyed(this.destroyRef))
      .subscribe((value) => {
        this.loadProdutos(value || '');
      });
  }

  private loadClientes() {
    this.clienteService
      .getClientes()
      .then((clientes) =>
        this.clientes.set(
          clientes.map((cliente) => ({
            id: cliente.id,
            nome: cliente.nome,
          })),
        ),
      )
      .catch(() => this.clientes.set([]));
  }

  private async loadCaixa() {
    this.isLoading.set(true);
    try {
      const caixas = await this.caixaService.getCaixas();
      const caixaAberta = caixas.find(
        (caixa) =>
          caixa.status?.toLowerCase() === 'aberto' || caixa.status?.toLowerCase() === 'open',
      );
      this.caixaAtiva.set(caixaAberta ?? null);
    } catch {
      this.caixaAtiva.set(null);
    } finally {
      this.isLoading.set(false);
    }
  }

  private loadProdutos(nome: string) {
    this.produtoService.getProdutos(nome || undefined, 0, 20).subscribe({
      next: (res) => this.produtosBuscados.set(res.content),
      error: () => this.produtosBuscados.set([]),
    });
  }

  addProduto(produto: ProdutoResponse) {
    this.carrinho.update((items) => {
      const existing = items.find((item) => item.produtoId === produto.id);
      if (existing) {
        return items.map((item) =>
          item.produtoId === produto.id
            ? {
                ...item,
                quantidade: item.quantidade + 1,
                valorTotal: (item.quantidade + 1) * item.precoUnitario,
              }
            : item,
        );
      }
      return [
        ...items,
        {
          produtoId: produto.id,
          nome: produto.nome,
          quantidade: 1,
          precoUnitario: produto.precoVenda,
          valorTotal: produto.precoVenda,
        },
      ];
    });
  }

  updateQuantidade(item: CarrinhoItem, quantidade: number) {
    if (quantidade < 1) {
      this.removeItem(item.produtoId);
      return;
    }
    this.carrinho.update((items) =>
      items.map((current) =>
        current.produtoId === item.produtoId
          ? {
              ...current,
              quantidade,
              valorTotal: quantidade * current.precoUnitario,
            }
          : current,
      ),
    );
  }

  removeItem(produtoId: string) {
    this.carrinho.update((items) => items.filter((item) => item.produtoId !== produtoId));
  }

  addPagamento() {
    if (this.paymentForm.invalid) {
      this.paymentForm.markAllAsTouched();
      return;
    }

    const rawValue = this.paymentForm.getRawValue();
    const pagamento: PagamentoRequest = {
      formaPagamento: (rawValue.formaPagamento ?? 'DINHEIRO') as PagamentoRequest['formaPagamento'],
      valor: Number(rawValue.valor) || 0,
    };

    this.pagamentos.update((items) => [...items, pagamento]);
    this.paymentForm.reset({ formaPagamento: 'DINHEIRO', valor: null });
  }

  removePagamento(paymentToRemove: PagamentoRequest) {
    this.pagamentos.update((items) => items.filter((payment) => payment !== paymentToRemove));
  }

  confirmVenda() {
    if (this.carrinho().length === 0) {
      this.snackBar.open('Adicione ao menos um item ao carrinho.', 'OK', { duration: 3000 });
      return;
    }

    if (this.totalPago() < this.total()) {
      this.snackBar.open('O pagamento deve cobrir o total da venda.', 'OK', { duration: 3000 });
      return;
    }

    if (!this.caixaAtiva()) {
      this.snackBar.open('Não há caixa ativo no momento.', 'OK', { duration: 3000 });
      return;
    }

    const request: VendaRequest = {
      caixaId: this.caixaAtiva()!.id,
      clienteId: this.clienteSelecionado()?.id ?? undefined,
      itens: this.carrinho().map((item) => ({
        produtoId: item.produtoId,
        quantidade: item.quantidade,
      })),
      pagamentos: this.pagamentos(),
      valorDesconto: this.desconto() > 0 ? this.desconto() : undefined,
    };

    this.isLoading.set(true);
    this.vendaService.criarVenda(request).subscribe({
      next: (res) => {
        this.snackBar.open(`Venda #${res.numero} registrada com sucesso!`, 'OK', {
          duration: 4000,
        });
        this.carrinho.set([]);
        this.pagamentos.set([]);
        this.clienteSelecionado.set(null);
        this.desconto.set(0);
        this.searchControl.setValue('');
        this.produtosBuscados.set([]);
        this.paymentForm.reset({ formaPagamento: 'DINHEIRO', valor: null });
        this.loadCaixa();
        this.isLoading.set(false);
      },
      error: (error) => {
        const message = error?.error?.message || 'Erro ao registrar venda.';
        this.snackBar.open(message, 'OK', { duration: 4000, panelClass: ['error-snackbar'] });
        this.isLoading.set(false);
      },
    });
  }
}
