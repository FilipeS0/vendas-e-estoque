# 🎯 PDV Project Tracking — Unified Roadmap & Tasks

> **Referência Principal:** [especificacao_tecnica.md](./especificacao_tecnica.md)
> **Última Atualização:** 09 de Maio de 2026

---

## 📊 1. Dashboard de Módulos (Roadmap)

| Símbolo | Significado |
|---------|-------------|
| ✅ | Implementado e funcional |
| ⚠️ | Parcialmente implementado / Em testes |
| ❌ | Não implementado |

| Módulo | Status | Principais Tecnologias / Detalhes |
|:-------|:------:|:----------------------------------|
| **Infra & DevOps** | ✅ | Spring Boot 4, Java 21, Angular 21, Docker, HTTPS + Deploy Real. |
| **Segurança** | ✅ | JWT, Refresh Token, BCrypt, Auditoria persistida, Certificado A1 AES-256. Rate-limiter pré-autenticação. |
| **Produtos** | ✅ | CRUD, Soft Delete, Fiscais, Imagem (FS), Histórico Preços, UnidadeMedida, Tela de Detalhes. |
| **Estoque** | ✅ | Movimentação, Baixa automática, Inventário, Alerta mínimo, Relatórios PDF. |
| **Caixa / PDV** | ✅ | Abertura/Fechamento, Sangria/Suprimento, Fluxo consolidado, Vendas Turno Atual. |
| **Vendas** | ✅ | Itens, Descontos, Múltiplos Pagos, Troco, Cancelamento, Crediário, PIX QR Code. |
| **Clientes** | ✅ | CRUD, CPF, Limite Crédito, Saldo Devedor, Extrato, BrasilAPI. |
| **Crediário** | ✅ | Registro Fiado, Parcelas Automáticas, Liquidação, Scheduler Vencimento. |
| **Fiscal / NFC-e** | ✅ | FocusNfeClient (emissão + cancelamento real), Certificado A1, Contingência, DANFE 80mm, campos fiscais dinâmicos, CNPJ dinâmico. |
| **Relatórios** | ✅ | iText 7 (PDF), Vendas, Estoque, Caixa, Fluxo de Caixa, Dashboard, Rankings, Contas a Receber. |
| **Fornecedores** | ✅ | CRUD + Paginação + @PreAuthorize + Soft Delete + Filtro inativos + BusinessException. |
| **Configurações** | ✅ | Dados da empresa, NFC-e, PIX (chave/beneficiário/cidade), Certificado A1 c/ scheduler de vencimento. |

---

## 📋 2. Backlog — Gap Analysis vs. Especificação Técnica

### 🔴 Alta Prioridade

| ID | Descrição | Módulo | Detalhes |
|:---|:----------|:------:|:---------|
| ~~GAP-001~~ | ~~**NFC-e: NCM e CFOP hardcoded na emissão real**~~ | Fiscal | `[x]` Corrigido em `NotaFiscalService` |
| ~~GAP-002~~ | ~~**FornecedorController sem segurança (@PreAuthorize)**~~ | Fornecedores | `[x]` Adicionadas anotações `@PreAuthorize` |
| ~~GAP-003~~ | ~~**FornecedorController sem validação de entrada**~~ | Fornecedores | `[x]` Adicionada anotação `@Valid` |
| ~~GAP-004~~ | ~~**Rate-limiter não bloqueia tentativas antes da autenticação**~~ | Segurança | `[x]` Checagem movida para antes da autenticação |
| ~~GAP-005~~ | ~~**Cancelamento de NFC-e sem validação de prazo**~~ | Fiscal | `[x]` Adicionada validação de 30 minutos |
| ~~GAP-006~~ | ~~**Dashboard stats: endpoint inexistente (404)**~~ | Relatórios | `[x]` Endpoint JSON adicionado em `RelatorioController` |
| ~~GAP-007~~ | ~~**Ranking de produtos: endpoint JSON inexistente (404)**~~ | Relatórios | `[x]` Endpoint JSON adicionado em `RelatorioController` |

### 🟡 Média Prioridade

| ID | Descrição | Módulo | Detalhes |
|:---|:----------|:------:|:---------|
| ~~GAP-008~~ | ~~**Relatório "Vendas do Dia" dedicado ausente**~~ | Relatórios | `[x]` Adicionado `/vendas/hoje` no `RelatorioController` |
| ~~GAP-009~~ | ~~**Relatório "Estoque abaixo do mínimo" sem PDF**~~ | Relatórios | `[x]` Endpoint `/estoque/abaixo-minimo/pdf` adicionado |
| ~~GAP-010~~ | ~~**Relatório "Movimentação de estoque" ausente em Relatórios**~~ | Relatórios | `[x]` Endpoint `/estoque/movimentacoes/pdf` adicionado |
| ~~GAP-011~~ | ~~**Configurações: PIX não aparece no formulário frontend**~~ | Config/UI | `[x]` Campos PIX adicionados no formulário do frontend e DTOs |
| ~~GAP-012~~ | ~~**Alerta de vencimento de Certificado Digital A1**~~ | Config | `[x]` Implementado `CertificadoScheduler` (cron) para logs de alerta 60 dias |
| ~~GAP-013~~ | ~~**Cliente sem campo `updatedAt`**~~ | Clientes | `[x]` Campo `updatedAt` adicionado na entidade `Cliente` |
| ~~GAP-014~~ | ~~**Dashboard: período fixo de 30 dias**~~ | Relatórios | `[x]` Parâmetro `dias` dinâmico adicionado no backend e frontend |
| ~~GAP-015~~ | ~~**Crediário: rota duplicada no app.routes.ts**~~ | Frontend | `[x]` Rota duplicada removida do `app.routes.ts` |
| ~~GAP-016~~ | ~~**FornecedorController usa exclusão física**~~ | Fornecedores | `[x]` Exclusão ajustada para `setAtivo(false)` (soft delete) |

### 🟢 Baixa Prioridade

| ID | Descrição | Módulo | Detalhes |
|:---|:----------|:------:|:---------|
| ~~GAP-017~~ | ~~**Relatório de "Contas a receber" sem filtro de vencimento**~~ | Relatórios | `[x]` Endpoint `/contas-a-receber/resumo` atualizado para receber `dataInicio` e `dataFim` |
| ~~GAP-018~~ | ~~**Frontend: Produto não tem tela de detalhe**~~ | Produtos/UI | `[x]` Rota e componente `produto-details` criados e integrados |
| ~~GAP-019~~ | ~~**FornecedorController sem paginação**~~ | Fornecedores | `[x]` Retorno do `listar()` alterado para `Page<FornecedorResponse>` |
| ~~GAP-020~~ | ~~**Consulta de vendas do turno atual inexistente**~~ | Vendas | `[x]` Adicionado endpoint `GET /vendas/turno-atual` |
| ~~GAP-021~~ | ~~**NfcePayload não inclui todos os campos tributários**~~ | Fiscal | `[x]` Resolvido junto com GAP-001 e mapeado os campos fiscais dinâmicos |

---

## 📋 2.1 Second Pass — Novos Gaps Encontrados

### 🔴 Alta Prioridade

| ID | Descrição | Módulo | Detalhes |
|:---|:----------|:------:|:--------|
| ~~GAP-022~~ | ~~**FocusNfeClient: CNPJ do emitente hardcoded como "FIXME"**~~ | Fiscal | `[x]` Agora lê dinamicamente de `Configuracao.cnpj`. |
| ~~GAP-023~~ | ~~**DANFE: Nome da empresa hardcoded "Empresa Exemplo LTDA"**~~ | Relatórios | `[x]` `PdfReportGenerator` agora recebe `empresaNome` da configuração. |
| ~~GAP-024~~ | ~~**FocusNfeClient: cancelarNfce() não implementado**~~ | Fiscal | `[x]` Implementado envio de DELETE para API da FocusNFe com justificativa. |

### 🟡 Média Prioridade

| ID | Descrição | Módulo | Detalhes |
|:---|:----------|:------:|:--------|
| ~~GAP-025~~ | ~~**FornecedorService.listar() retorna fornecedores inativos**~~ | Fornecedores | `[x]` Alterado para `findByAtivoTrue(pageable)`. |
| ~~GAP-026~~ | ~~**FornecedorService usa `RuntimeException` em vez de `BusinessException`**~~ | Fornecedores | `[x]` Padronizado para `BusinessException` (HTTP 422). |
| ~~GAP-027~~ | ~~**FocusNfeClient: ICMS e forma de pagamento hardcoded**~~ | Fiscal | `[x]` Mapeamento dinâmico de CSOSN, Origem e códigos de pagamento SEFAZ. |

### 🟢 Baixa Prioridade

| ID | Descrição | Módulo | Detalhes |
|:---|:----------|:------:|:--------|
| ~~GAP-028~~ | ~~**VendaService: `System.err.println` em vez de logger**~~ | Vendas | `[x]` Substituído por `log.error()` usando Slf4j. |

---

## 📋 2.2 Third Pass — Status Final

> ✅ **Codebase limpo.** Zero ocorrências de: `FIXME`, `TODO`, `HACK`, `System.err`, `e.printStackTrace()`, `RuntimeException` em services.

### 🟢 Housekeeping

| ID | Descrição | Módulo | Detalhes |
|:---|:----------|:------:|:--------|
| GAP-029 | **Arquivos de build log commitados no repositório** | Infra | `app/build_log.txt`, `app/build_log_2.txt`, `app/build_log_3.txt` — Arquivos temporários de debug que não deveriam estar no Git. Adicionar ao `.gitignore` e remover do tracking. |

---

## 🗺️ 5. Roadmap — Fase 3 (Expansão) & Fase 4 (Futuro)

> Itens da especificação técnica §13 ainda não implementados.

### Fase 3 — Expansão (Previsto)

| ID | Descrição | Status | Detalhes |
|:---|:----------|:------:|:--------|
| ROAD-001 | Suporte a múltiplos caixas | ❌ | Arquitetura já preparada (`Caixa` vinculado a `Venda`). Falta UI para gerenciar N caixas simultâneos. |
| ROAD-002 | Integração PIX automático (gateway) | ❌ | PIX estático (QR Code copia e cola) implementado. Falta integração com gateway para confirmação automática via webhooks. |
| ROAD-003 | Ordens de compra a fornecedores | ❌ | CRUD de fornecedores completo. Falta módulo de pedidos de compra com fluxo (rascunho → aprovado → recebido → estoque). |
| ROAD-004 | Integração com balança (Toledo/Filizola) | ❌ | Campo `UnidadeMedida` (KG, UN) já existe no produto. Falta integração serial/USB com protocolo de balança. |
| ROAD-005 | App mobile para consulta de estoque | ❌ | API REST já disponível. Falta app mobile (Flutter/React Native). |

### Fase 4 — Futuro

| ID | Descrição | Status |
|:---|:----------|:------:|
| ROAD-006 | E-commerce integrado | ❌ |
| ROAD-007 | NF-e de entrada (nota de compra) | ❌ |
| ROAD-008 | Módulo de funcionários e comissões | ❌ |
| ROAD-009 | Integração com contabilidade | ❌ |

## 🚀 3. Plano de Execução — Tarefas Concluídas (Arquivo)

### ✅ Passo 1–4 — Infraestrutura, Módulos Core, Fiscal (CONCLUÍDO)
### ✅ Passo 5 — Testes (CONCLUÍDO)
### ✅ Passo 6 — CI/CD e Deploy (CONCLUÍDO)

---

## 📜 4. Histórico de Conquistas (Completed)

- **Maio 09, 2026**:
  - Implementação de Auto-complete de NCM no cadastro de produtos.
  - Integração com BrasilAPI para busca de CEP e CNPJ (Clientes/Fornecedores).
  - Geração de QR Code PIX Estático (Copia e Cola) no PDV.
  - Refinamento visual do Dashboard com Ngx-Charts.
  - Gap analysis completa contra a especificação técnica — 22 issues levantados.
  - Correção de todas as 7 issues de **Alta Prioridade** (GAP-001 a GAP-007): Segurança em Fornecedores, Rate Limiting de Auth, campos fiscais dinâmicos, validação de cancelamento NFC-e e novos endpoints JSON para Dashboard.
  - Correção das issues de **Média Prioridade** (GAP-008 a GAP-016): Novos relatórios em PDF para Estoque e Vendas, campos PIX na UI, scheduler de validade de certificado, paginação e soft delete em Fornecedores.
  - Correção das issues de **Baixa Prioridade** (GAP-017 a GAP-021): Filtros por data no Contas a Receber, nova página de detalhes do Produto, e endpoint para vendas do turno atual.
- **Maio 03, 2026**:
  - Unificação do tracking de projeto.
  - Implementação do `UnidadeMedida` no módulo de Produtos.
  - Adição de Busca Multi-critério em Produtos.
  - Infraestrutura de Testcontainers (PostgreSQL 16) configurada.
  - Migração de `@MockBean` para `@MockitoBean`.
  - Integração inicial com BrasilAPI e Tabela NCM (V14).
- **Maio 02, 2026**:
  - Implementação de Auditoria persistente (`audit_log`).
  - Geração de DANFE NFC-e (80mm) com QR Code via iText/ZXing.
  - Integração real com SEFAZ via Focus NF-e Client.
  - Gestão segura de Certificado Digital A1 (AES-256).
  - Exportação de relatórios em PDF.
  - Implementação do Módulo de Crediário e Liquidação de Parcelas.
