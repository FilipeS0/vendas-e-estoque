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
import { ProdutoService, ProdutoResponse } from '../../produtos/services/produto.service';
import { ClienteService } from '../../clientes/services/cliente.service';
import { CaixaService } from '../../caixa/services/caixa.service';
import { VendaService, PagamentoRequest, VendaRequest } from '../services/venda.service';
import { ClienteSummary, Caixa } from '../../../shared/index';

interface CarrinhoItem {
  produtoId: string; nome: string; quantidade: number; precoUnitario: number; valorTotal: number;
}

@Component({
  selector: 'app-pos',
  imports: [ReactiveFormsModule, MatTableModule, MatFormFieldModule, MatInputModule, MatSelectModule,
    MatButtonModule, MatIconModule, MatCardModule, MatSnackBarModule, MatDividerModule,
    MatProgressSpinnerModule, CurrencyPipe],
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
    numeroParcelas: [1, [Validators.min(1)]],
  });

  paymentTypes = [
    { value: 'DINHEIRO', label: 'Dinheiro' }, { value: 'CARTAO_DEBITO', label: 'Cartão Débito' },
    { value: 'CARTAO_CREDITO', label: 'Cartão Crédito' }, { value: 'PIX', label: 'PIX' },
    { value: 'CREDIARIO', label: 'Crediário' },
  ];

  displayedColumns = ['nome', 'quantidade', 'precoUnitario', 'valorTotal', 'actions'];
  valorBruto = computed(() => this.carrinho().reduce((sum, item) => sum + item.valorTotal, 0));
  total = computed(() => Math.max(0, this.valorBruto() - this.desconto()));
  totalPago = computed(() => this.pagamentos().reduce((sum, p) => sum + p.valor, 0));
  troco = computed(() => Math.max(0, this.totalPago() - this.total()));

  ngOnInit() {
    this.loadClientes(); this.loadCaixa(); this.loadProdutos('');
    this.searchControl.valueChanges.pipe(debounceTime(400), distinctUntilChanged(), takeUntilDestroyed(this.destroyRef))
      .subscribe((v) => this.loadProdutos(v || ''));
  }

  private loadClientes() {
    this.clienteService.getClientes().subscribe({
      next: (res) => this.clientes.set(res.content.map((c) => ({ id: c.id, nome: c.nome }))),
      error: () => this.clientes.set([])
    });
  }

  private async loadCaixa() {
    this.isLoading.set(true);
    try {
      const caixas = await this.caixaService.getCaixas();
      this.caixaAtiva.set(caixas.find((c) => c.status?.toLowerCase() === 'aberto' || c.status?.toLowerCase() === 'open') ?? null);
    } catch { this.caixaAtiva.set(null); } finally { this.isLoading.set(false); }
  }

  private loadProdutos(nome: string) {
    this.produtoService.getProdutos(nome || undefined, 0, 20).subscribe({
      next: (res) => this.produtosBuscados.set(res.content), error: () => this.produtosBuscados.set([]),
    });
  }

  addProduto(produto: ProdutoResponse) {
    this.carrinho.update((items) => {
      const existing = items.find((i) => i.produtoId === produto.id);
      if (existing) return items.map((i) => i.produtoId === produto.id
        ? { ...i, quantidade: i.quantidade + 1, valorTotal: (i.quantidade + 1) * i.precoUnitario } : i);
      return [...items, { produtoId: produto.id, nome: produto.nome, quantidade: 1, precoUnitario: produto.precoVenda, valorTotal: produto.precoVenda }];
    });
  }

  updateQuantidade(item: CarrinhoItem, quantidade: number) {
    if (quantidade < 1) { this.removeItem(item.produtoId); return; }
    this.carrinho.update((items) => items.map((i) => i.produtoId === item.produtoId ? { ...i, quantidade, valorTotal: quantidade * i.precoUnitario } : i));
  }

  removeItem(produtoId: string) { this.carrinho.update((items) => items.filter((i) => i.produtoId !== produtoId)); }

  addPagamento() {
    if (this.paymentForm.invalid) { this.paymentForm.markAllAsTouched(); return; }
    const raw = this.paymentForm.getRawValue();
    this.pagamentos.update((items) => [...items, { 
      formaPagamento: (raw.formaPagamento ?? 'DINHEIRO') as PagamentoRequest['formaPagamento'], 
      valor: Number(raw.valor) || 0,
      numeroParcelas: raw.formaPagamento === 'CREDIARIO' ? Number(raw.numeroParcelas) : undefined
    }]);
    this.paymentForm.reset({ formaPagamento: 'DINHEIRO', valor: null, numeroParcelas: 1 });
  }

  removePagamento(p: PagamentoRequest) { this.pagamentos.update((items) => items.filter((i) => i !== p)); }

  confirmVenda() {
    if (!this.carrinho().length) { this.snackBar.open('Adicione ao menos um item.', 'OK', { duration: 3000 }); return; }
    if (this.totalPago() < this.total()) { this.snackBar.open('Pagamento insuficiente.', 'OK', { duration: 3000 }); return; }
    if (!this.caixaAtiva()) { this.snackBar.open('Nenhum caixa ativo.', 'OK', { duration: 3000 }); return; }
    const request: VendaRequest = { caixaId: this.caixaAtiva()!.id, clienteId: this.clienteSelecionado()?.id,
      itens: this.carrinho().map((i) => ({ produtoId: i.produtoId, quantidade: i.quantidade })),
      pagamentos: this.pagamentos(), valorDesconto: this.desconto() > 0 ? this.desconto() : undefined };
    this.isLoading.set(true);
    this.vendaService.criarVenda(request).subscribe({
      next: (res) => {
        this.snackBar.open(`Venda #${res.numero} registrada!`, 'OK', { duration: 4000 });
        this.carrinho.set([]); this.pagamentos.set([]); this.clienteSelecionado.set(null);
        this.desconto.set(0); this.searchControl.setValue(''); this.produtosBuscados.set([]);
        this.paymentForm.reset({ formaPagamento: 'DINHEIRO', valor: null }); this.loadCaixa(); this.isLoading.set(false);
      },
      error: (e) => { this.snackBar.open(e?.error?.message || 'Erro ao registrar venda.', 'OK', { duration: 4000 }); this.isLoading.set(false); },
    });
  }
}
