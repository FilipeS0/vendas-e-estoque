# Especificação Técnica — Sistema de PDV para Varejo (Loja Física)

> **Versão:** 1.1.0  
> **Data:** Abril de 2026 · **Atualizado:** Maio de 2026  
> **Status:** Em desenvolvimento (Fase 2 concluída)  
> **Escopo:** Sistema interno de caixa, estoque, cadastros e relatórios com emissão de NFC-e

---

## Sumário

1. [Visão Geral](#1-visão-geral)
2. [Stack Tecnológica](#2-stack-tecnológica)
3. [Arquitetura do Sistema](#3-arquitetura-do-sistema)
4. [Módulos e Funcionalidades](#4-módulos-e-funcionalidades)
5. [Modelo de Domínio (DDD)](#5-modelo-de-domínio-ddd)
6. [Modelagem do Banco de Dados](#6-modelagem-do-banco-de-dados)
7. [Backend — Estrutura de Camadas](#7-backend--estrutura-de-camadas)
8. [Frontend — Estrutura Angular](#8-frontend--estrutura-angular)
9. [Integração com APIs Governamentais](#9-integração-com-apis-governamentais)
10. [Formas de Pagamento](#10-formas-de-pagamento)
11. [Relatórios](#11-relatórios)
12. [Segurança e Autenticação](#12-segurança-e-autenticação)
13. [Roadmap e Fases de Entrega](#13-roadmap-e-fases-de-entrega)
14. [Glossário](#14-glossário)

---

## 1. Visão Geral

### 1.1 Objetivo

Desenvolver um sistema de PDV (Ponto de Venda) para uso interno em loja física de varejo, contemplando:

- Operação de caixa (balcão e self-service)
- Cadastro e controle de produtos e estoque
- Suporte a múltiplas formas de pagamento (dinheiro, cartão, PIX, crediário/fiado)
- Balanço de caixa e relatórios de vendas
- Emissão de NFC-e (Nota Fiscal de Consumidor Eletrônica)
- Controle básico de clientes (principalmente para crediário)

### 1.2 Fora do Escopo (v1.0)

- E-commerce / loja virtual
- Multi-caixa (previsto para v2.0, arquitetura já preparada)
- Módulo fiscal avançado (NF-e de saída para pessoa jurídica)
- Integração com ERP externo
- Aplicativo mobile para clientes

### 1.3 Usuários do Sistema

| Perfil                | Descrição                                                  |
| --------------------- | ---------------------------------------------------------- |
| **Administrador**     | Acesso total: cadastros, relatórios, configurações fiscais |
| **Operador de Caixa** | Acesso ao PDV, consulta de estoque, fechamento de caixa    |
| **Gerente**           | Acesso a relatórios, estoque e aprovações de crediário     |

---

## 2. Stack Tecnológica

### 2.1 Backend

| Componente       | Tecnologia                     | Versão    |
| ---------------- | ------------------------------ | --------- |
| Linguagem        | Java                           | 21+ (LTS) |
| Framework        | Spring Boot                    | 4.0.6     |
| Persistência     | Spring Data JPA + Hibernate    | 3.x       |
| Banco de Dados   | PostgreSQL                     | 15+       |
| Mapeamento       | MapStruct                      | 1.6+      |
| Validação        | Jakarta Bean Validation        | 3.x       |
| Testes Unitários | JUnit 5 + Mockito              | 5.x / 5.x |
| Build            | Maven                          | 4.x       |
| Containerização  | Docker + Docker Compose        | —         |
| Documentação API | SpringDoc OpenAPI (Swagger UI) | 2.x       |
| Segurança        | Spring Security + JWT          | —         |

### 2.2 Frontend

| Componente       | Tecnologia                        | Versão |
| ---------------- | --------------------------------- | ------ |
| Framework        | Angular                           | 21     |
| UI Components    | Angular Material                  | 21     |
| Reatividade      | RxJS                              | 7.x    |
| State Management | Angular Signals + NgRx (opcional) | —      |
| HTTP             | Angular HttpClient                | —      |
| Roteamento       | Angular Router com Guards         | —      |
| Build            | Angular CLI                       | —      |

### 2.3 Infraestrutura

| Componente            | Tecnologia                               |
| --------------------- | ---------------------------------------- |
| Banco de Dados        | PostgreSQL (Docker ou servidor dedicado) |
| Servidor de Aplicação | Embutido (Tomcat via Spring Boot)        |
| Ambiente Dev          | Docker Compose                           |
| CI/CD (futuro)        | GitHub Actions                           |

---

## 3. Arquitetura do Sistema

### 3.1 Visão Macro

```
┌─────────────────────────────────────────────────────────────┐
│                        FRONTEND (Angular)                    │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────────┐  │
│  │   PDV    │ │ Estoque  │ │Relatórios│ │  Cadastros   │  │
│  └──────────┘ └──────────┘ └──────────┘ └──────────────┘  │
└──────────────────────────┬──────────────────────────────────┘
                           │ REST API (JSON/HTTPS)
┌──────────────────────────▼──────────────────────────────────┐
│                      BACKEND (Spring Boot)                   │
│  ┌──────────────────────────────────────────────────────┐   │
│  │              API Layer (Controllers REST)             │   │
│  ├──────────────────────────────────────────────────────┤   │
│  │              Service Layer (Regras de Negócio)        │   │
│  ├──────────────────────────────────────────────────────┤   │
│  │              Repository Layer (Spring Data JPA)       │   │
│  └──────────────────────────────────────────────────────┘   │
│                    Integrações Externas                       │
│  ┌────────────┐  ┌──────────────┐  ┌─────────────────┐     │
│  │  SEFAZ     │  │  Receita Fed.│  │  Gateway PIX    │     │
│  │  (NFC-e)   │  │  (CNPJ/CPF)  │  │  (futuro)       │     │
│  └────────────┘  └──────────────┘  └─────────────────┘     │
└──────────────────────────┬──────────────────────────────────┘
                           │
┌──────────────────────────▼──────────────────────────────────┐
│                      PostgreSQL                              │
└─────────────────────────────────────────────────────────────┘
```

### 3.2 Padrão de Arquitetura Backend (Clean Architecture + DDD)

Cada domínio (módulo de negócio) segue a estrutura de pacotes:

```
br.com.suaempresa.pdv
│
├── [dominio]/
│   ├── controller/        ← Endpoints REST, entrada/saída via DTOs
│   ├── service/           ← Regras de negócio, orquestração
│   ├── repository/        ← Interfaces Spring Data JPA
│   ├── entity/            ← Entidades JPA (mapeamento banco)
│   ├── dto/               ← Request/Response DTOs
│   │   ├── request/
│   │   └── response/
│   ├── mapper/            ← MapStruct: Entity ↔ DTO
│   ├── validator/         ← Validações customizadas
│   └── exception/         ← Exceções específicas do domínio
│
├── shared/
│   ├── exception/         ← GlobalExceptionHandler, ApiError
│   ├── config/            ← Beans de configuração Spring
│   └── util/              ← Utilitários genéricos
│
└── PdvApplication.java
```

---

## 4. Módulos e Funcionalidades

### 4.1 Módulo: Caixa (PDV)

**Responsabilidade:** Interface de venda, seleção de produtos, cálculo de totais, pagamento e emissão de NFC-e.

**Funcionalidades:**

- Abertura e fechamento de caixa (com valor inicial informado)
- Busca de produto por código de barras, nome ou código interno
- Adição/remoção de itens na venda em andamento
- Aplicação de desconto por item ou por venda total (permissão de gerente)
- Seleção de forma de pagamento (múltiplas formas por venda)
- Cálculo de troco para pagamento em dinheiro
- Registro de cliente para venda no crediário (nome obrigatório)
- Impressão/envio de NFC-e após confirmação
- Cancelamento de venda (com justificativa)
- Consulta de vendas do turno atual

**Fluxo principal de uma venda:**

```
Abrir Caixa → Adicionar Itens → Selecionar Pagamento → Confirmar Venda
     → Emitir NFC-e → Baixar Estoque → Registrar no Balanço → Imprimir/Exibir Cupom
```

---

### 4.2 Módulo: Produtos

**Responsabilidade:** Cadastro e manutenção do catálogo de produtos.

**Funcionalidades:**

- CRUD de produtos (criar, listar, editar, inativar)
- Campos: código interno, código de barras (EAN-13/EAN-8), nome, descrição, categoria, unidade de medida, preço de custo, preço de venda, margem calculada, NCM (obrigatório para NFC-e), CEST (quando aplicável), CFOP padrão, alíquotas fiscais (ICMS, PIS, COFINS)
- Upload de imagem do produto (opcional)
- Vínculo com fornecedor
- Histórico de preços
- Filtragem e busca por múltiplos critérios
- Inativação lógica (não exclusão física)

**Campos Fiscais Obrigatórios para NFC-e:**

| Campo         | Descrição                                              |
| ------------- | ------------------------------------------------------ |
| NCM           | Nomenclatura Comum do Mercosul (8 dígitos)             |
| CEST          | Código Especificador da Substituição Tributária        |
| CFOP          | Código Fiscal de Operações e Prestações                |
| CST/CSOSN     | Situação Tributária (Regime Normal / Simples Nacional) |
| Alíquota ICMS | Percentual de ICMS aplicável                           |
| PIS/COFINS    | Código e alíquota                                      |

---

### 4.3 Módulo: Estoque

**Responsabilidade:** Controle de entradas, saídas e posição de estoque.

**Funcionalidades:**

- Visualização de posição atual de estoque por produto
- Entrada de estoque manual (compra, devolução de cliente)
- Saída de estoque manual (perda, ajuste)
- Baixa automática ao confirmar venda
- Alertas de estoque mínimo (configurável por produto)
- Histórico de movimentações (entrada/saída, data, motivo, usuário)
- Inventário (contagem física com geração de ajuste)
- Relatório de produtos abaixo do estoque mínimo

**Tipos de Movimentação:**

| Tipo              | Sinal | Origem                |
| ----------------- | ----- | --------------------- |
| ENTRADA_COMPRA    | +     | Manual                |
| ENTRADA_DEVOLUCAO | +     | Cancelamento de venda |
| SAIDA_VENDA       | -     | Automático            |
| SAIDA_PERDA       | -     | Manual                |
| AJUSTE_INVENTARIO | +/-   | Inventário            |

---

### 4.4 Módulo: Clientes

**Responsabilidade:** Cadastro básico de clientes, focado em suporte ao crediário.

**Funcionalidades:**

- Cadastro de cliente: nome, CPF (opcional), telefone, endereço (opcional)
- Consulta de saldo devedor no crediário
- Histórico de compras do cliente
- Registro de pagamentos de crediário (entrada de dinheiro sem emissão de NFC-e — apenas recibo)
- Limite de crédito configurável por cliente

---

### 4.5 Módulo: Crediário / Fiado

**Responsabilidade:** Controle de vendas a prazo.

**Funcionalidades:**

- Registro de venda no fiado vinculada ao cliente
- Parcelamento simples (número de parcelas + datas de vencimento)
- Registro de pagamentos parciais ou totais
- Relatório de contas a receber (vencidas e a vencer)
- Notificação interna de parcelas vencidas

---

### 4.6 Módulo: Balanço de Caixa

**Responsabilidade:** Controle financeiro das entradas e saídas do caixa físico.

**Funcionalidades:**

- Abertura de caixa com saldo inicial informado
- Registro de entradas (vendas em dinheiro, recebimento de crediário)
- Registro de saídas manuais (sangria, despesas operacionais)
- Fechamento de caixa com apuração:
    - Saldo esperado (calculado pelo sistema)
    - Saldo informado (contado fisicamente)
    - Diferença (sobra/falta)
- Histórico de aberturas/fechamentos
- Consolidado por forma de pagamento

---

### 4.7 Módulo: Fiscal (NFC-e)

**Responsabilidade:** Geração, transmissão e armazenamento de NFC-e.

**Funcionalidades:**

- Geração do XML da NFC-e conforme layout SEFAZ
- Assinatura digital do XML (certificado A1 ou A3)
- Transmissão para SEFAZ estadual via WebService
- Tratamento de retorno: autorização, rejeição, contingência
- Armazenamento do XML autorizado e do DANFE (PDF)
- Cancelamento de NFC-e (dentro do prazo legal — 30 minutos)
- Emissão em modo de contingência (NFC-e off-line)
- Consulta de status de NFC-e emitidas

**Estados de uma NFC-e:**

```
PENDENTE → AGUARDANDO_SEFAZ → AUTORIZADA
                            → REJEITADA → PENDENTE (correção)
                            → CONTINGENCIA → AUTORIZADA (quando voltar online)
AUTORIZADA → CANCELADA (dentro do prazo)
```

---

### 4.8 Módulo: Relatórios

**Responsabilidade:** Geração de relatórios gerenciais e operacionais.

**Relatórios disponíveis:**

| Relatório                         | Filtros            | Formato    |
| --------------------------------- | ------------------ | ---------- |
| Vendas por período                | Data inicial/final | Tela / PDF |
| Vendas do dia                     | —                  | Tela / PDF |
| Vendas por forma de pagamento     | Período            | Tela / PDF |
| Vendas por produto                | Período            | Tela / PDF |
| Ranking de produtos mais vendidos | Período, top N     | Tela / PDF |
| Posição de estoque                | Categoria, status  | Tela / PDF |
| Produtos abaixo do mínimo         | —                  | Tela / PDF |
| Movimentação de estoque           | Produto, período   | Tela       |
| Balanço de caixa                  | Data               | Tela / PDF |
| Contas a receber (crediário)      | Vencimento         | Tela / PDF |
| Fluxo de caixa                    | Período            | Tela       |

---

### 4.9 Módulo: Configurações

**Responsabilidade:** Configurações da empresa e do sistema.

**Funcionalidades:**

- Dados da empresa (Razão Social, CNPJ, Inscrição Estadual, endereço)
- Certificado digital A1 (upload e gerenciamento)
- Configuração fiscal (regime tributário: Simples Nacional / Lucro Presumido / Real)
- Ambiente SEFAZ (homologação / produção)
- Série da NFC-e e número sequencial
- Configuração de impressora térmica (IP/porta para impressão de cupom)
- Usuários e perfis de acesso
- Configuração de alerta de estoque mínimo global

---

## 5. Modelo de Domínio (DDD)

### 5.1 Bounded Contexts

```
┌─────────────────┐   ┌─────────────────┐   ┌─────────────────┐
│    VENDAS       │   │    ESTOQUE      │   │    FISCAL       │
│                 │   │                 │   │                 │
│ Venda           │──▶│ Movimentacao    │   │ NotaFiscal      │
│ ItemVenda       │   │ EstoqueAtual    │   │ ItemNotaFiscal  │
│ Pagamento       │   │                 │   │                 │
└────────┬────────┘   └─────────────────┘   └─────────────────┘
         │
         ▼
┌─────────────────┐   ┌─────────────────┐   ┌─────────────────┐
│   PRODUTOS      │   │    CAIXA        │   │    CLIENTES     │
│                 │   │                 │   │                 │
│ Produto         │   │ Caixa           │   │ Cliente         │
│ Categoria       │   │ LancamentoCaixa │   │ Crediario       │
│ Fornecedor      │   │                 │   │ ParcelaCrediar. │
└─────────────────┘   └─────────────────┘   └─────────────────┘
```

### 5.2 Entidades Principais

#### `Produto`

```
- id: UUID
- codigoInterno: String
- codigoBarras: String (EAN)
- nome: String
- descricao: String
- categoria: Categoria
- unidadeMedida: UnidadeMedida (enum)
- precoCusto: BigDecimal
- precoVenda: BigDecimal
- ncm: String
- cest: String
- cfop: String
- situacaoTributaria: SituacaoTributaria
- aliquotaIcms: BigDecimal
- ativo: Boolean
- createdAt, updatedAt
```

#### `Venda`

```
- id: UUID
- numero: Long (sequencial)
- dataHora: LocalDateTime
- operador: Usuario
- caixa: Caixa
- cliente: Cliente (nullable)
- itens: List<ItemVenda>
- pagamentos: List<Pagamento>
- valorBruto: BigDecimal
- valorDesconto: BigDecimal
- valorTotal: BigDecimal
- status: StatusVenda (enum: EM_ANDAMENTO, CONFIRMADA, CANCELADA)
- notaFiscal: NotaFiscal (nullable)
```

#### `ItemVenda`

```
- id: UUID
- venda: Venda
- produto: Produto
- quantidade: BigDecimal
- precoUnitario: BigDecimal
- desconto: BigDecimal
- valorTotal: BigDecimal
```

#### `Pagamento`

```
- id: UUID
- venda: Venda
- formaPagamento: FormaPagamento (enum)
- valor: BigDecimal
- troco: BigDecimal
- nsu: String (cartão)
- autorizacao: String (cartão)
```

#### `EstoqueAtual`

```
- id: UUID
- produto: Produto (unique)
- quantidadeAtual: BigDecimal
- quantidadeMinima: BigDecimal
- updatedAt: LocalDateTime
```

#### `MovimentacaoEstoque`

```
- id: UUID
- produto: Produto
- tipo: TipoMovimentacao (enum)
- quantidade: BigDecimal
- quantidadeAnterior: BigDecimal
- quantidadeResultante: BigDecimal
- motivo: String
- referencia: String (id da venda, nota de compra, etc.)
- usuario: Usuario
- dataHora: LocalDateTime
```

#### `Caixa`

```
- id: UUID
- operador: Usuario
- dataAbertura: LocalDateTime
- dataFechamento: LocalDateTime (nullable)
- valorAbertura: BigDecimal
- valorFechamentoSistema: BigDecimal
- valorFechamentoFisico: BigDecimal
- diferenca: BigDecimal
- status: StatusCaixa (ABERTO, FECHADO)
```

#### `NotaFiscal`

```
- id: UUID
- venda: Venda
- numero: Long
- serie: Integer
- chaveAcesso: String (44 dígitos)
- dataEmissao: LocalDateTime
- xmlAutorizado: Text
- urlDanfe: String
- status: StatusNfe (enum)
- mensagemRetorno: String
- protocolo: String
```

#### `Cliente`

```
- id: UUID
- nome: String
- cpf: String (nullable)
- telefone: String
- limiteCredito: BigDecimal
- saldoDevedor: BigDecimal
- ativo: Boolean
```

---

## 6. Modelagem do Banco de Dados

### 6.1 Convenções

- Chaves primárias: `UUID` (tipo `uuid` no PostgreSQL)
- Nomes de tabelas: `snake_case` no plural (ex: `produtos`, `itens_venda`)
- Soft delete via coluna `ativo BOOLEAN DEFAULT TRUE`
- Timestamps: `created_at` e `updated_at` em todas as tabelas
- Índices: criados para colunas frequentemente filtradas

### 6.2 Diagrama de Tabelas (simplificado)

```
produtos              categorias           fornecedores
---------             ----------           ------------
id (PK)               id (PK)              id (PK)
codigo_interno        nome                 nome
codigo_barras         descricao            cnpj
nome                  ativo                telefone
descricao             created_at           email
categoria_id (FK) ──▶ updated_at           ativo
fornecedor_id (FK) ───────────────────────▶
preco_custo
preco_venda
ncm
cest
cfop
situacao_tributaria
aliquota_icms
aliquota_pis
aliquota_cofins
ativo
created_at
updated_at

estoque_atual         movimentacoes_estoque
-------------         ---------------------
id (PK)               id (PK)
produto_id (FK, UQ)   produto_id (FK)
quantidade_atual      tipo
quantidade_minima     quantidade
updated_at            quantidade_anterior
                      quantidade_resultante
                      motivo
                      referencia
                      usuario_id (FK)
                      data_hora

usuarios              perfis
--------              ------
id (PK)               id (PK)
nome                  nome
email                 permissoes (jsonb)
senha_hash
perfil_id (FK) ──────▶
ativo
created_at

caixas                lancamentos_caixa
------                -----------------
id (PK)               id (PK)
operador_id (FK)      caixa_id (FK)
data_abertura         tipo (ENTRADA/SAIDA)
data_fechamento       forma_pagamento
valor_abertura        valor
valor_fechamento_sis  descricao
valor_fechamento_fis  referencia_id
status                data_hora
                      usuario_id (FK)

vendas                itens_venda           pagamentos
------                -----------           ----------
id (PK)               id (PK)               id (PK)
numero                venda_id (FK) ────────▶
operador_id (FK)      produto_id (FK)       venda_id (FK)
caixa_id (FK)         quantidade            forma_pagamento
cliente_id (FK, null) preco_unitario        valor
data_hora             desconto              troco
valor_bruto           valor_total           nsu
valor_desconto                              autorizacao
valor_total
status
created_at

clientes              crediario             parcelas_crediario
--------              ---------             ------------------
id (PK)               id (PK)               id (PK)
nome                  cliente_id (FK)       crediario_id (FK)
cpf                   venda_id (FK)         numero_parcela
telefone              valor_total           valor
limite_credito        valor_pago            data_vencimento
saldo_devedor         status                data_pagamento
ativo                 created_at            valor_pago
created_at                                  status

notas_fiscais
-------------
id (PK)
venda_id (FK, UQ)
numero
serie
chave_acesso
data_emissao
xml_autorizado (TEXT)
url_danfe
status
mensagem_retorno
protocolo
ambiente (HOMOLOGACAO/PRODUCAO)
created_at
```

---

## 7. Backend — Estrutura de Camadas

### 7.1 Controller

Responsável apenas por receber a requisição HTTP, delegar para o Service e retornar o ResponseEntity. **Sem regra de negócio.**

```java
@RestController
@RequestMapping("/api/v1/produtos")
@RequiredArgsConstructor
public class ProdutoController {

    private final ProdutoService produtoService;

    @GetMapping
    public ResponseEntity<Page<ProdutoResponse>> listar(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(produtoService.listar(PageRequest.of(page, size)));
    }

    @PostMapping
    public ResponseEntity<ProdutoResponse> criar(@Valid @RequestBody ProdutoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(produtoService.criar(request));
    }
}
```

### 7.2 Service

Contém as regras de negócio, orquestra chamadas ao Repository, aplica validações de domínio e dispara eventos.

```java
@Service
@Transactional
@RequiredArgsConstructor
public class ProdutoService {

    private final ProdutoRepository produtoRepository;
    private final ProdutoMapper produtoMapper;

    public ProdutoResponse criar(ProdutoRequest request) {
        validarCodigoBarrasDuplicado(request.codigoBarras());
        Produto produto = produtoMapper.toEntity(request);
        return produtoMapper.toResponse(produtoRepository.save(produto));
    }
}
```

### 7.3 Repository

Interface Spring Data JPA com queries customizadas quando necessário.

```java
public interface ProdutoRepository extends JpaRepository<Produto, UUID> {
    Optional<Produto> findByCodigoBarras(String codigoBarras);
    Page<Produto> findByAtivoTrue(Pageable pageable);

    @Query("SELECT p FROM Produto p WHERE p.nome ILIKE %:termo% AND p.ativo = true")
    List<Produto> buscarPorNome(@Param("termo") String termo);
}
```

### 7.4 DTOs

```java
// Request
public record ProdutoRequest(
    @NotBlank String codigoInterno,
    @NotBlank String codigoBarras,
    @NotBlank @Size(max = 200) String nome,
    @NotNull UUID categoriaId,
    @NotNull @Positive BigDecimal precoVenda,
    @NotBlank @Size(min = 8, max = 8) String ncm,
    // ... demais campos fiscais
) {}

// Response
public record ProdutoResponse(
    UUID id,
    String codigoInterno,
    String codigoBarras,
    String nome,
    String categoriaNome,
    BigDecimal precoVenda,
    BigDecimal quantidadeEstoque,
    Boolean ativo
) {}
```

### 7.5 MapStruct Mapper

```java
@Mapper(componentModel = "spring", uses = {CategoriaMapper.class})
public interface ProdutoMapper {
    Produto toEntity(ProdutoRequest request);
    ProdutoResponse toResponse(Produto produto);
    List<ProdutoResponse> toResponseList(List<Produto> produtos);
}
```

### 7.6 Exception Handler Global

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(EntityNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiError(HttpStatus.NOT_FOUND, ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex) {
        List<String> erros = ex.getBindingResult().getFieldErrors()
                .stream().map(e -> e.getField() + ": " + e.getDefaultMessage())
                .toList();
        return ResponseEntity.badRequest().body(new ApiError(HttpStatus.BAD_REQUEST, erros));
    }
}
```

### 7.7 Estrutura de Testes

```
src/test/java/br/com/suaempresa/pdv/
├── produto/
│   ├── ProdutoServiceTest.java       ← JUnit 5 + Mockito (unitário)
│   └── ProdutoControllerTest.java    ← @WebMvcTest (slice test)
├── venda/
│   ├── VendaServiceTest.java
│   └── VendaFluxoIntegrationTest.java  ← @SpringBootTest (integração)
└── shared/
    └── TestDataBuilder.java           ← Builders para dados de teste
```

---

## 8. Frontend — Estrutura Angular

### 8.1 Organização de Módulos

```
src/app/
│
├── core/                          ← Singleton services, guards, interceptors
│   ├── auth/
│   │   ├── auth.service.ts
│   │   ├── auth.guard.ts
│   │   └── role.guard.ts
│   ├── interceptors/
│   │   ├── jwt.interceptor.ts
│   │   └── error.interceptor.ts
│   └── services/
│       └── notification.service.ts
│
├── shared/                        ← Componentes reutilizáveis
│   ├── components/
│   │   ├── data-table/
│   │   ├── confirm-dialog/
│   │   ├── loading-spinner/
│   │   └── page-header/
│   ├── directives/
│   └── pipes/
│
├── features/                      ← Módulos de domínio (lazy-loaded)
│   ├── pdv/                       ← Tela de caixa
│   │   ├── components/
│   │   │   ├── busca-produto/
│   │   │   ├── carrinho/
│   │   │   ├── pagamento/
│   │   │   └── resumo-venda/
│   │   ├── services/
│   │   │   └── venda.service.ts
│   │   ├── state/
│   │   │   └── venda.signal.ts    ← Signal-based state
│   │   └── pdv.routes.ts
│   │
│   ├── produtos/
│   │   ├── components/
│   │   │   ├── lista-produtos/
│   │   │   ├── form-produto/
│   │   │   └── detalhe-produto/
│   │   ├── services/
│   │   │   └── produto.service.ts
│   │   └── produtos.routes.ts
│   │
│   ├── estoque/
│   ├── caixa/
│   ├── clientes/
│   ├── relatorios/
│   └── configuracoes/
│
├── layout/                        ← Shell da aplicação
│   ├── sidebar/
│   ├── navbar/
│   └── app-layout.component.ts
│
└── app.routes.ts                  ← Rotas raiz com lazy loading
```

### 8.2 Rotas e Guards

```typescript
// app.routes.ts
export const routes: Routes = [
    {
        path: "login",
        loadComponent: () => import("./auth/login/login.component"),
    },
    {
        path: "",
        component: AppLayoutComponent,
        canActivate: [AuthGuard],
        children: [
            {
                path: "pdv",
                loadChildren: () => import("./features/pdv/pdv.routes"),
                canActivate: [RoleGuard],
                data: { roles: ["ADMIN", "OPERADOR", "GERENTE"] },
            },
            {
                path: "produtos",
                loadChildren: () =>
                    import("./features/produtos/produtos.routes"),
                canActivate: [RoleGuard],
                data: { roles: ["ADMIN", "GERENTE"] },
            },
            {
                path: "relatorios",
                loadChildren: () =>
                    import("./features/relatorios/relatorios.routes"),
                canActivate: [RoleGuard],
                data: { roles: ["ADMIN", "GERENTE"] },
            },
            // ...
        ],
    },
];
```

### 8.3 State Management com Signals

```typescript
// venda.signal.ts
export interface VendaState {
    itens: ItemVenda[];
    cliente: Cliente | null;
    pagamentos: Pagamento[];
    status: "VAZIA" | "EM_ANDAMENTO" | "AGUARDANDO_PAGAMENTO";
}

export class VendaStore {
    private readonly _state = signal<VendaState>({
        itens: [],
        cliente: null,
        pagamentos: [],
        status: "VAZIA",
    });

    readonly state = this._state.asReadonly();
    readonly totalItens = computed(() => this._state().itens.length);
    readonly valorTotal = computed(() =>
        this._state().itens.reduce((sum, item) => sum + item.valorTotal, 0),
    );

    adicionarItem(item: ItemVenda): void {
        this._state.update((s) => ({
            ...s,
            itens: [...s.itens, item],
            status: "EM_ANDAMENTO",
        }));
    }

    limpar(): void {
        this._state.set({
            itens: [],
            cliente: null,
            pagamentos: [],
            status: "VAZIA",
        });
    }
}
```

### 8.4 API Service Padrão

```typescript
// produto.service.ts
@Injectable({ providedIn: "root" })
export class ProdutoService {
    private readonly http = inject(HttpClient);
    private readonly baseUrl = `${environment.apiUrl}/produtos`;

    listar(params: ProdutoFiltro): Observable<PageResponse<Produto>> {
        return this.http.get<PageResponse<Produto>>(this.baseUrl, {
            params: { ...params },
        });
    }

    buscarPorCodigoBarras(codigo: string): Observable<Produto> {
        return this.http.get<Produto>(
            `${this.baseUrl}/codigo-barras/${codigo}`,
        );
    }

    criar(produto: ProdutoRequest): Observable<Produto> {
        return this.http.post<Produto>(this.baseUrl, produto);
    }
}
```

---

## 9. Integração com APIs Governamentais

### 9.1 SEFAZ — NFC-e (NF-e modelo 65)

**Especificação:** Nota Técnica 2019.001 (NFC-e) do Portal Nacional da NF-e

**Bibliotecas Java recomendadas:**

- `focusnfe-java` (cliente REST para API Focus NF-e) — recomendado para simplificar
- Ou implementação direta via `java.net.http.HttpClient` com WSDL do WebService SEFAZ

**Fluxo de emissão:**

```
1. Montar XML NFC-e (campos obrigatórios + dados da venda)
2. Assinar XML com certificado digital (A1: arquivo PFX / A3: token)
3. Transmitir para WebService SEFAZ estadual
4. Receber retorno:
   a. Código 100: Uso Autorizado → salvar XML + protocolo
   b. Código 2xx: Rejeição → analisar motivo, corrigir, reenviar
   c. Timeout → entrar em modo contingência
5. Gerar DANFE NFC-e (QR Code obrigatório)
6. Disponibilizar para impressão/e-mail
```

**Campos XML obrigatórios:**

| Grupo                  | Campo                                      | Observação                 |
| ---------------------- | ------------------------------------------ | -------------------------- |
| Emitente               | CNPJ, IE, Razão Social, Endereço           | Da configuração do sistema |
| Destinatário           | CPF (opcional para NFC-e)                  | Informar quando disponível |
| Produto                | NCM, CFOP, CST, quantidade, valor unitário | Por item                   |
| Tributação             | ICMS, PIS, COFINS                          | Por item                   |
| Pagamento              | forma_pagamento, valor                     | Por forma utilizada        |
| Total                  | valor_produtos, valor_total, valor_icms    | Calculado                  |
| Transporte             | modalidade_frete                           | 9 = Sem Frete (balcão)     |
| Informações adicionais | QR Code URL, URL consulta                  | Gerado pela SEFAZ          |

**Contingência off-line:**

- Quando SEFAZ indisponível, emitir com `tpEmis = 9` (Contingência Off-line)
- Gravar XML localmente com status `CONTINGENCIA`
- Sincronizar automaticamente quando conexão retornar
- Prazo máximo: emitir o mesmo número de série em até 24h online

### 9.2 Receita Federal — Consulta de CPF/CNPJ

**Objetivo:** Validar CPF informado para cliente e preencher dados da NFC-e.

**Opções de integração:**

| Opção                            | Custo                  | Facilidade |
| -------------------------------- | ---------------------- | ---------- |
| API ReceitaWS (receitaws.com.br) | Gratuita (limite reqs) | Alta       |
| BrasilAPI (brasilapi.com.br)     | Gratuita               | Alta       |
| CNPJ.ws                          | Gratuita               | Alta       |
| Acesso direto Receita Federal    | Requer convênio        | Baixa      |

**Uso recomendado:** BrasilAPI (CNPJ) + validação local de CPF (algoritmo próprio, sem API).

### 9.3 Tabela NCM

**Fonte:** Receita Federal — Tabela NCM completa (atualização periódica)

**Estratégia:** Importar a tabela NCM localmente no banco de dados para auto-complete no cadastro de produto, sem dependência de API em tempo real.

### 9.4 Certificado Digital

- **Tipo A1:** Arquivo PFX armazenado de forma segura (criptografado no servidor)
- **Tipo A3:** Via token/cartão USB — requer JCA provider (ex: `SafeNet`, `eToken`)
- Validade: monitorar vencimento e alertar com antecedência de 60 dias

---

## 10. Formas de Pagamento

### 10.1 Suportadas na v1.0

| Forma             | Código SEFAZ | Observações                                                    |
| ----------------- | ------------ | -------------------------------------------------------------- |
| Dinheiro          | 01           | Calcula troco automaticamente                                  |
| Cartão de Débito  | 04           | Registro manual de NSU/autorização                             |
| Cartão de Crédito | 03           | Registro manual de NSU/autorização                             |
| PIX               | 17           | Exibir QR Code / chave PIX para pagamento manual               |
| Crediário/Fiado   | 99           | Sem emissão de NFC-e imediata (ou emitir e controlar separado) |

### 10.2 PIX

Na v1.0, o PIX será **manual** (operador informa que recebeu, confirma no sistema). Integração com gateway PIX (ex: Efí Bank, Mercado Pago, Asaas) prevista para v1.5, com geração automática de QR Code e webhook de confirmação.

### 10.3 Pagamento Parcial / Múltiplas Formas

Uma venda pode ser quitada com mais de uma forma de pagamento (ex: parte no cartão, parte em dinheiro). O sistema deve suportar adicionar múltiplos pagamentos até o valor total ser coberto.

---

## 11. Relatórios

### 11.1 Tecnologias

- **Backend:** JasperReports ou iText para geração de PDF
- **Frontend:** Exibição de tabelas com Angular Material Table + paginação; exportação via chamada ao backend

### 11.2 Métricas do Dashboard Principal

- Total de vendas do dia (valor e quantidade)
- Total de vendas do mês
- Ticket médio
- Forma de pagamento mais utilizada
- Top 5 produtos do dia
- Alertas de estoque crítico
- Status do caixa atual (aberto/fechado + saldo parcial)

---

## 12. Segurança e Autenticação

### 12.1 Autenticação

- Login com e-mail/senha
- Geração de JWT (Access Token: 8h, Refresh Token: 7 dias)
- Refresh automático pelo frontend (interceptor Angular)
- Logout invalida o refresh token (armazenado no banco)

### 12.2 Autorização

- RBAC (Role-Based Access Control) via Spring Security
- Roles: `ROLE_ADMIN`, `ROLE_GERENTE`, `ROLE_OPERADOR`
- Proteção nos endpoints com `@PreAuthorize`
- Guards no frontend por role

### 12.3 Outras Medidas

- HTTPS obrigatório em produção
- Senhas com BCrypt (fator 12)
- Rate limiting nos endpoints de login
- Log de auditoria para operações críticas (cancelamento de venda, ajuste de estoque, fechamento de caixa)
- Certificado A1 armazenado criptografado (AES-256)

---

## 13. Roadmap e Fases de Entrega

### Fase 1 — MVP (3–4 meses)

- [x] Infraestrutura base (Spring Boot + Angular + PostgreSQL + Docker)
- [x] Autenticação e gerenciamento de usuários
- [x] CRUD de Categorias e Produtos
- [x] Controle de Estoque (entrada/saída manual)
- [x] Tela de PDV básica (adicionar itens, calcular total)
- [x] Pagamento em Dinheiro e Cartão (manual)
- [x] Emissão de NFC-e em homologação
- [x] Abertura e fechamento de caixa
- [x] Relatório de vendas do dia

### Fase 2 — Consolidação (2–3 meses) ✅

- [x] PIX manual ✅ (QR Code automático previsto para v1.5)
- [x] Crediário / Fiado com controle de parcelas
- [x] Inventário de estoque
- [x] Todos os relatórios planejados (incl. exportação PDF e DANFE)
- [x] NFC-e em produção (ambiente real) — via Focus NF-e
- [x] Contingência off-line NFC-e — `FiscalScheduler` com reenvio automático
- [x] Dashboard gerencial completo (com gráficos dinâmicos)

### Fase 3 — Expansão (2–3 meses)

- [ ] Suporte a múltiplos caixas
- [ ] Integração PIX automático (gateway)
- [ ] Módulo de fornecedores e ordens de compra
- [ ] Integração com balança (protocolo Toledo/Filizola)
- [ ] App mobile para consulta de estoque

### Fase 4 — Futuro

- [ ] E-commerce integrado
- [ ] NF-e de entrada (nota de compra)
- [ ] Módulo de funcionários e comissões
- [ ] Integração com contabilidade

---

## 14. Glossário

| Termo     | Definição                                                                                 |
| --------- | ----------------------------------------------------------------------------------------- |
| **NFC-e** | Nota Fiscal de Consumidor Eletrônica — modelo 65, para venda a consumidor final em balcão |
| **DANFE** | Documento Auxiliar da Nota Fiscal Eletrônica                                              |
| **SEFAZ** | Secretaria da Fazenda estadual — responsável por autorizar as notas fiscais               |
| **NCM**   | Nomenclatura Comum do Mercosul — código de 8 dígitos que classifica o produto             |
| **CFOP**  | Código Fiscal de Operações e Prestações — indica a natureza da operação                   |
| **CST**   | Código de Situação Tributária                                                             |
| **CSOSN** | Código de Situação da Operação no Simples Nacional                                        |
| **PDV**   | Ponto de Venda — terminal/sistema de caixa                                                |
| **EAN**   | European Article Number — padrão de código de barras de produto                           |
| **NSU**   | Número Sequencial Único — número de identificação de transação no cartão                  |
| **PIX**   | Sistema de pagamento instantâneo do Banco Central do Brasil                               |
| **JWT**   | JSON Web Token — padrão de autenticação stateless                                         |
| **RBAC**  | Role-Based Access Control — controle de acesso baseado em papéis                          |
| **DDD**   | Domain-Driven Design — metodologia de modelagem de software orientada ao domínio          |
| **DTO**   | Data Transfer Object — objeto usado para transferência de dados entre camadas             |

---

_Documento gerado em Abril de 2026 — sujeito a revisões conforme evolução do projeto._
