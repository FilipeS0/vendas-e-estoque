/**
 * Shared Models for ERP Frontend
 * Names match backend API (Portuguese field names)
 */

export interface Cliente {
  id: string;
  nome: string;
  cpf?: string;
  email?: string;
  telefone?: string;
  endereco?: string;
  limiteCredito: number;
  saldoDevedor: number;
  ativo?: boolean;
  criadoEm?: string;
}

export interface ClienteRequest {
  nome: string;
  cpf?: string;
  telefone?: string;
  limiteCredito: number;
}

export interface ClienteSummary {
  id: string;
  nome: string;
}

export interface ClienteDetalhe extends Cliente {
  creditLimit?: number;
  currentDebt?: number;
}

export interface Produto {
  id: string;
  codigoInterno?: string;
  codigoBarras: string;
  nome: string;
  descricao?: string;
  categoriaId?: string;
  fornecedorId?: string;
  precoCusto: number;
  precoVenda: number;
  ncm: string;
  cest?: string;
  cfop: string;
  situacaoTributaria?: string;
  aliquotaIcms?: number;
  aliquotaPis?: number;
  aliquotaCofins?: number;
  ativo?: boolean;
  criadoEm?: string;
}

export interface Categoria {
  id: string;
  nome: string;
  descricao?: string;
  ativo?: boolean;
}

export interface Fornecedor {
  id: string;
  nome: string;
  cnpj?: string;
  telefone?: string;
  email?: string;
  ativo?: boolean;
}

export interface Installment {
  id: string;
  parcela: number;
  vencimento: string;
  valor: number;
  status?: string;
  dataPagamento?: string;
  valorPago?: number;
}

export interface Crediario {
  id: string;
  clienteId: string;
  vendaId: string;
  valorTotal: number;
  valorPago: number;
  status: string;
  criadoEm: string;
  parcelas?: Installment[];
}

// ─── Dashboard ────────────────────────────────────────────────────────────────

export interface VendasPorDia {
  data: string;
  total: number;
}

export interface TopProduto {
  nome: string;
  quantidade: number;
  total: number;
}

export interface DashboardStats {
  faturamentoTotal: number;
  totalVendas: number;
  ticketMedio: number;
  produtosAbaixoMinimo: number;
  vendasRecentemente: VendasPorDia[];
  topProdutos: TopProduto[];
  faturamentoPorFormaPagamento: Record<string, number>;
}

// ─── Reports ──────────────────────────────────────────────────────────────────

export interface SeriesPoint {
  date: string;
  value: number;
}

export interface VendaPeriodoItem {
  data: string;
  quantidade: number;
  total: number;
}

export interface VendaFormaPagItem {
  formaPagamento: string;
  vendas: number;
  total: number;
}

export interface ProdutoRankingItem {
  posicao: number;
  produto: string;
  quantidade: number;
  total: number;
}

// ─── Vendas / Caixa ───────────────────────────────────────────────────────────

export interface Venda {
  id: string;
  numero: number;
  operadorId: string;
  caixaId: string;
  clienteId?: string;
  dataHora: string;
  valorBruto: number;
  valorDesconto: number;
  valorTotal: number;
  status: string;
}

export interface Caixa {
  id: string;
  operadorId: string;
  dataAbertura: string;
  dataFechamento?: string;
  valorAbertura: number;
  valorFechamentoSis?: number;
  valorFechamentoFis?: number;
  status: string;
  diferenca?: number;
}

export interface ClienteExtrato {
  clienteId: string;
  nome: string;
  limiteCredito: number;
  saldoDevedor: number;
  saldoDisponivel: number;
  ultimasCompras: Venda[];
  parcelas: Installment[];
}

// ─── Pagination ───────────────────────────────────────────────────────────────

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}
// ─── Usuários ─────────────────────────────────────────────────────────────────

export interface Perfil {
  id: string;
  nome: string;
}

export interface Usuario {
  id: string;
  nome: string;
  email: string;
  perfilNome: string;
  perfilId: string;
  ativo: boolean;
}

export interface UsuarioRequest {
  nome: string;
  email: string;
  senha?: string;
  perfilId: string;
  ativo: boolean;
}
