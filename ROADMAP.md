# Roadmap — Sistema PDV: Especificação × Estado Atual

> **Gerado em:** 02 de Maio de 2026
> **Referência:** [especificacao_tecnica.md](./especificacao_tecnica.md)

---

## Legenda

| Símbolo | Significado |
|---------|-------------|
| ✅ | Implementado e funcional |
| ⚠️ | Parcialmente implementado (lacunas descritas) |
| ❌ | Não implementado |

---

## 1. Infraestrutura & DevOps

| Item | Backend | Frontend | Status |
|------|---------|----------|--------|
| Spring Boot 4.0.6 + Java 21 | `pom.xml` | — | ✅ |
| Angular 21 + Angular Material | — | `package.json` | ✅ |
| PostgreSQL + Docker Compose | `Dockerfile` | `Dockerfile` | ✅ |
| Flyway migrations (6 scripts) | `V1` → `V6` | — | ✅ |
| SpringDoc / Swagger UI | `springdoc-openapi` | — | ✅ |
| MapStruct + Lombok | `pom.xml` | — | ✅ |
| CI/CD (GitHub Actions) | — | — | ❌ Previsto para futuro |

---

## 2. Segurança & Autenticação (Seção 12 da spec)

| Funcionalidade | Spec | Estado Atual | Status |
|----------------|------|--------------|--------|
| Login e-mail/senha | JWT | `AuthController.login()` | ✅ |
| Access Token (JWT) | 8h | `TokenService.gerarToken()` | ✅ |
| Refresh Token | 7 dias, salvo no banco | `RefreshToken` entity + `TokenService.renovarAccessToken()` | ✅ |
| Logout (revogar refresh) | Invalidar no banco | `AuthController.logout()` | ✅ |
| Endpoint `/auth/me` | Perfil do usuário logado | `AuthController.getMe()` | ✅ |
| RBAC (3 roles) | `ADMIN, GERENTE, OPERADOR` | `@PreAuthorize` nos controllers + `roleGuard` no Angular | ✅ |
| Rate limiting no login | Bucket4j | `RateLimiterConfig` | ✅ (fix aplicado) |
| Senhas com BCrypt | Fator 12 | `SecurityConfig` | ✅ |
| HTTPS em produção | Obrigatório | Configuração de deploy | ❌ Pendente de deploy |
| Log de auditoria | Operações críticas | `AuditService` (apenas log simples) | ⚠️ |
| Certificado A1 criptografado | AES-256 | — | ❌ |

### Itens pendentes de Segurança:
- [x] **Refresh automático no frontend**: Implementado via `jwt.interceptor.ts` com captura de `401` e retry automático. ✅
- [x] **AuditService**: Implementada persistência em banco de dados na tabela `audit_log` com suporte a filtros. ✅
- [x] **Armazenamento seguro do certificado A1**: Implementado upload criptografado (AES-256) e gestão de validade. ✅

---

## 3. Módulo: Produtos (Seção 4.2)

| Funcionalidade | Estado | Detalhes |
|----------------|--------|----------|
| CRUD completo (criar, listar, editar) | ✅ | `ProdutoController` + `ProdutoService` |
| Inativação lógica (soft delete) | ✅ | Campo `ativo` na entidade |
| Campos fiscais (NCM, CEST, CFOP, CSOSN, CST PIS/COFINS, Origem) | ✅ | Entity + migrations V4, V5 |
| Alíquotas (ICMS, PIS, COFINS) | ✅ | Campos na entidade |
| Vínculo com Categoria | ✅ | `@ManyToOne` |
| Vínculo com Fornecedor | ✅ | `@ManyToOne` |
| Upload de imagem do produto | ❌ | Não implementado |
| Histórico de preços | ❌ | Não há tabela/entidade para isso |
| Busca por código de barras (endpoint dedicado) | ✅ | `GET /api/v1/produtos/codigo-barras/{codigo}` |

### Itens pendentes de Produtos:
- [ ] **Upload de imagem**: Endpoint multipart + armazenamento (filesystem or S3)
- [ ] **Histórico de preços**: Criar entidade `HistoricoPreco` e listener JPA para registrar mudanças

---

## 4. Módulo: Estoque (Seção 4.3)

| Funcionalidade | Estado | Detalhes |
|----------------|--------|----------|
| Posição atual de estoque | ✅ | `EstoqueAtual` entity |
| Entrada manual (compra) | ✅ | `EstoqueController.registrarMovimentacao()` |
| Saída manual (perda/ajuste) | ✅ | `EstoqueController.registrarSaidaManual()` |
| Baixa automática na venda | ✅ | `VendaService.finalizarVenda()` → `estoqueService.moverEstoque()` |
| Reposição automática no cancelamento | ✅ | `VendaService.cancelarVenda()` → `reverterEstoque()` |
| Alerta de estoque mínimo (configurável) | ✅ | `EstoqueAtualRepository.findAbaixoMinimo()` |
| Atualizar estoque mínimo por produto | ✅ | `PUT /estoque/{produtoId}/minimo` |
| Histórico de movimentações | ✅ | `MovimentacaoEstoque` entity + endpoint |
| Inventário (contagem física e ajuste) | ✅ | `POST /api/v1/estoque/ajuste-inventario` |
| Relatório produtos abaixo do mínimo | ✅ | `GET /estoque/abaixo-minimo` |

### Itens pendentes de Estoque:
- Nenhum item crítico pendente. ✅ Completo.

---

## 5. Módulo: Caixa / PDV (Seções 4.1 e 4.6)

| Funcionalidade | Estado | Detalhes |
|----------------|--------|----------|
| Abertura de caixa com valor inicial | ✅ | `POST /caixas/abrir` |
| Fechamento de caixa (esperado × físico × diferença) | ✅ | `POST /caixas/{id}/fechar` |
| Entradas/Saídas manuais (sangria/suprimento) | ✅ | Endpoints dedicados |
| Registro automático de entradas de vendas | ✅ | `caixaService.registrarEntradaAutomaticaVenda()` |
| Estorno automático em cancelamento | ✅ | `caixaService.registrarEstornoVenda()` |
| Listagem de caixas (filtro status/data) | ✅ | `GET /caixas` |
| Listagem de lançamentos por caixa | ✅ | `GET /caixas/{id}/lancamentos` |
| Consolidado por forma de pagamento | ✅ | `relatorioBalancoCaixa()` |
| Histórico de aberturas/fechamentos | ✅ | Via `GET /caixas` |

---

## 6. Módulo: Vendas (Seção 4.1)

| Funcionalidade | Estado | Detalhes |
|----------------|--------|----------|
| Iniciar venda (EM_ANDAMENTO) | ✅ | `POST /vendas` |
| Adicionar/remover itens | ✅ | `POST /vendas/{id}/itens`, `DELETE /vendas/{id}/itens/{itemId}` |
| Desconto por item | ✅ | Campo `desconto` no `ItemVendaRequest` |
| Desconto por venda total | ✅ | `PATCH /vendas/{id}/desconto` (requer GERENTE/ADMIN) |
| Múltiplas formas de pagamento | ✅ | Lista de `PagamentoRequest` no `FinalizarVendaRequest` |
| Cálculo de troco (dinheiro) | ✅ | `distribuirTroco()` |
| Cancelamento com justificativa | ✅ | `POST /vendas/{id}/cancelar` |
| Auditoria de cancelamento | ✅ | `AuditService.log()` |
| Consulta de vendas do turno (filtro por caixa) | ✅ | `GET /vendas?caixaId=` |
| Crediário (registro + parcela automática) | ✅ | `processarCrediario()` |
| Emissão automática de NFC-e (Mock) | ✅ | Automatizado no `finalizarVenda()` |

---

## 7. Módulo: Clientes (Seção 4.4)

| Funcionalidade | Estado | Detalhes |
|----------------|--------|----------|
| CRUD de clientes | ✅ | `ClienteController` |
| Campos: nome, CPF, telefone | ✅ | `Cliente` entity |
| Limite de crédito | ✅ | Campo `limiteCredito` |
| Saldo devedor | ✅ | Campo `saldoDevedor` (atualizado ao processar crediário) |
| Histórico de compras (extrato) | ✅ | `GET /clientes/{id}/extrato` |
| Consulta de saldo devedor | ✅ | Incluído no extrato |

---

## 8. Módulo: Crediário / Fiado (Seção 4.5)

| Funcionalidade | Estado | Detalhes |
|----------------|--------|----------|
| Registro de venda no fiado | ✅ | `processarCrediario()` em `VendaService` |
| Criação de parcelas (customizável ou padrão) | ✅ | `ParcelaCrediario` entity + loop de parcelas |
| Liquidar parcela | ✅ | `POST /crediarios/parcelas/{id}/liquidar` |
| Listagem de parcelas (filtro por cliente/status) | ✅ | `GET /crediarios/parcelas` |
| Parcelamento customizado (N parcelas) | ✅ | Aceita `numeroParcelas` no `PagamentoRequest` |
| Relatório de contas a receber | ✅ | `GET /api/v1/relatorios/contas-a-receber/resumo` |
| Scheduler de parcelas vencidas | ✅ | `@Scheduled` em `CrediarioService` |

---

## 9. Módulo: Fiscal / NFC-e (Seção 4.7)

| Funcionalidade | Estado | Detalhes |
|----------------|--------|----------|
| Geração de NFC-e (mock/homologação) | ✅ | `NotaFiscalService.gerarNotaFiscalMock()` |
| Cancelamento de NFC-e | ✅ | `cancelarNotaFiscalMock()` |
| Consulta de NFC-e por venda | ✅ | `GET /vendas/{id}/nota-fiscal` |
| XML mock com dados básicos | ✅ | `montarXmlMock()` |
| **Integração real com SEFAZ** | ❌ | Apenas mock |
| Assinatura digital (certificado A1/A3) | ❌ | Não implementado |
| Contingência off-line (tpEmis=9) | ❌ | Não implementado |
| DANFE NFC-e com QR Code | ❌ | Não implementado |
| Upload de certificado digital | ❌ | Não implementado |
| Status enum (PENDENTE→AUTORIZADA→CANCELADA) | ⚠️ | `status` é String, não enum |

### Itens pendentes de Fiscal:
- [x] **Integração real SEFAZ**: Implementado `FocusNfeClient` com suporte a emissão, cancelamento e contingência automática. ✅
- [x] **Assinatura digital**: Implementado upload de certificado A1 (.pfx) com armazenamento seguro (AES-256) e extração automática de validade. ✅
- [x] **Status como Enum**: Trocado `String status` na `NotaFiscal` para `@Enumerated StatusNfe`. ✅
- [ ] **DANFE**: Geração de PDF com QR Code (JasperReports ou iText)

---

## 10. Módulo: Relatórios (Seção 4.8 e 11)

| Relatório (spec) | Estado | Detalhes |
|-------------------|--------|----------|
| Vendas por período | ✅ | `GET /relatorios/vendas?inicio=&fim=` |
| Vendas por forma de pagamento | ✅ | `GET /relatorios/vendas/por-forma-pagamento` |
| Vendas por produto | ✅ | `GET /relatorios/vendas/por-produto` |
| Ranking de produtos mais vendidos | ✅ | `TopProduto` no `DashboardStats` |
| Posição de estoque | ✅ | `GET /relatorios/estoque/posicao` |
| Produtos abaixo do mínimo | ✅ | `GET /estoque/abaixo-minimo` |
| Movimentação de estoque | ✅ | `GET /estoque/movimentacoes` |
| Balanço de caixa | ✅ | `GET /relatorios/caixa/balanco/{caixaId}` |
| Contas a receber (crediário) | ✅ | `GET /relatorios/contas-a-receber/resumo` |
| Fluxo de caixa | ✅ | `GET /relatorios/fluxo-caixa` |
| Dashboard gerencial | ✅ | `GET /relatorios/dashboard/stats` |
| Exportação em PDF | ✅ | Integrado iText 7 |

### Itens pendentes de Relatórios:
- [x] **Relatório de Fluxo de Caixa**: Visão consolidada de entradas e saídas por período. ✅
- [x] **Exportação PDF**: Implementado motor iText 7 para exportação de relatórios e documentos. ✅
- [x] **DANFE**: Geração de PDF com QR Code em layout térmico (80mm) para NFC-e. ✅
- [ ] **Dashboard com Gráficos**: Adicionar gráficos dinâmicos (Chart.js ou Ngx-Charts) no dashboard.

---

## 11. Módulo: Configurações (Seção 4.9)

| Funcionalidade | Estado | Detalhes |
|----------------|--------|----------|
| Dados da empresa (CNPJ, Razão Social, IE, Endereço) | ✅ | `Configuracao` entity + endpoints |
| Regime tributário | ✅ | Campo `regimeTributario` |
| Ambiente SEFAZ (homologação/produção) | ✅ | Campo `ambienteSefaz` |
| Série e número sequencial NFC-e | ✅ | Campos na entity |
| Configuração impressora térmica | ✅ | Campos IP/porta |
| Alerta estoque mínimo global | ✅ | Campo `alertaEstoqueMinimoGlobal` |
| Gestão de usuários e perfis | ✅ | `UsuarioController` + `UsuarioFormDialogComponent` |

---

## 12. Frontend — Análise por Módulo

| Módulo Frontend | Estado | Arquivos existentes |
|-----------------|--------|---------------------|
| **Auth / Login** | ✅ | `features/auth/pages/login.component` |
| **Dashboard** | ✅ | `features/dashboard/pages/dashboard.component` |
| **PDV (Ponto de Venda)** | ✅ | `features/pdv/pages/pos.component` |
| **Caixa** | ✅ | `features/caixa/pages/caixa.component` |
| **Produtos** | ✅ | `features/produtos/` |
| **Estoque** | ✅ | `features/estoque/pages/estoque-list.component` |
| **Clientes** | ✅ | `features/clientes/` |
| **Relatórios** | ✅ | `features/relatorios/pages/relatorios.component` |
| **Configurações** | ✅ | `features/configuracoes/` |
| **Crediário** | ✅ | `features/crediario/` |
| **Usuários (Gestão)** | ✅ | `features/usuarios/` (Novo CRUD) |
| **Layout** | ✅ | Sidebar atualizada com link Usuários |

---

## 13. Priorização Sugerida (Estado Atual)

### 🔴 Prioridade Alta (Próximos Passos)
1. **Integração real SEFAZ** — Transição do Mock para produção.
2. **Exportação PDF de relatórios** — Requisito comum para contabilidade.
3. **Auditoria persistida em banco** — Atualmente é apenas log de texto.

### 🟡 Prioridade Média
4. **Upload de imagem de produto**
5. **Histórico de preços**
6. **Fluxo de caixa consolidado**

### 🟢 Prioridade Baixa
7. **Testes de integração (Testcontainers)**
8. **CI/CD com GitHub Actions**
9. **Manual do usuário / Documentação de API**

---

_Este roadmap deve ser atualizado conforme os itens forem concluídos._
