# 🎯 PDV Project Tracking — Unified Roadmap & Tasks

> **Referência Principal:** [especificacao_tecnica.md](./especificacao_tecnica.md)
> **Última Atualização:** 03 de Maio de 2026

---

## 📊 1. Dashboard de Módulos (Roadmap)

| Símbolo | Significado |
|---------|-------------|
| ✅ | Implementado e funcional |
| ⚠️ | Parcialmente implementado / Em testes |
| ❌ | Não implementado |

| Módulo | Status | Principais Tecnologias / Detalhes |
|:-------|:------:|:----------------------------------|
| **Infra & DevOps** | ⚠️ | Spring Boot 4, Java 21, Angular 21, Docker. (Pendente: CI/CD) |
| **Segurança** | ✅ | JWT, Refresh Token, BCrypt, Auditoria persistida, Certificado A1 AES-256. |
| **Produtos** | ✅ | CRUD, Soft Delete, Fiscais, Imagem (FS), Histórico Preços, UnidadeMedida. |
| **Estoque** | ✅ | Movimentação, Baixa automática, Inventário, Alerta mínimo. |
| **Caixa / PDV** | ✅ | Abertura/Fechamento, Sangria/Suprimento, Fluxo consolidado. |
| **Vendas** | ✅ | Itens, Descontos, Múltiplos Pagos, Troco, Cancelamento, Crediário. |
| **Clientes** | ✅ | CRUD, CPF, Limite Crédito, Saldo Devedor, Extrato. |
| **Crediário** | ✅ | Registro Fiado, Parcelas Automáticas, Liquidação, Scheduler Vencimento. |
| **Fiscal / NFC-e** | ✅ | FocusNfeClient, Certificado A1, Contingência, DANFE 80mm. |
| **Relatórios** | ✅ | iText 7 (PDF), Vendas, Estoque, Caixa, Fluxo de Caixa, Dashboard. |

---

## 🚀 2. Plano de Execução (Passo a Passo)

### 🟦 Passo 5 — Testes (EM PROGRESSO)
**Objetivo:** Garantir a estabilidade e evitar regressões.

- [ ] **5.1 — Testes de Controller (MockMvc)**
  - [x] `ProdutoControllerTest.java` (Listagem, Inativação, RBAC)
  - [ ] `ProdutoControllerTest.java` (Expandir: Criação e Validação)
  - [ ] `VendaControllerTest.java` (Fluxo: Iniciar → Itens → Finalizar)
  - [ ] `CaixaControllerTest.java` (Abrir, Fechar, Sangria)
- [ ] **5.2 — Testes de Integração (Testcontainers)**
  - [x] Configurar infraestrutura (`AbstractIntegrationTest.java` + Docker)
  - [ ] `VendaFluxoIntegrationTest.java` (Fluxo E2E: Venda + Baixa Estoque + Nota Fiscal)
  - [ ] `CrediarioIntegrationTest.java` (Venda Fiado + Liquidação Parcela)
- [ ] **5.3 — TestDataBuilder**
  - [x] `createCategoria()`, `createFornecedor()`, `createProduto()`
  - [ ] `createVenda()`, `createCaixa()`, `createUsuario()`, `createCliente()`

---

### ⬜ Passo 6 — CI/CD e Deploy (PRÓXIMO)
**Objetivo:** Automatizar o ciclo de vida da aplicação.

- [ ] **6.1 — GitHub Actions**
  - [ ] Pipeline de Build e Testes (Backend/Frontend)
  - [ ] Build e Push de Imagens Docker
- [ ] **6.2 — HTTPS e Deploy Real**
  - [ ] nginx + SSL (Let's Encrypt)
  - [ ] `docker-compose.prod.yml`

---

## 📋 3. Backlog & Technical Debt

| ID | Descrição | Prioridade | Status |
|:---|:----------|:----------:|:------:|
| ISSUE-006 | **PIX QR Code (v1.5)** — Integração via Gateway ou Copy/Paste | 🟢 Baixa | `[ ]` |
| ISSUE-004.2 | **NCM Auto-complete** — Integrar busca no formulário de produto | 🟢 Baixa | `[ ]` |
| ISSUE-005.2 | **BrasilAPI Integration** — Usar no cadastro de Clientes/Fornecedores | 🟢 Baixa | `[ ]` |
| UI-001 | **Gráficos Dinâmicos** — Refinar visual do Dashboard (Ngx-Charts) | 🟡 Média | `[ ]` |

---

## 📜 4. Histórico de Conquistas (Completed)

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
