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
- [ ] **Refresh automático no frontend**: O `jwt.interceptor.ts` existe em `core/interceptors/` mas precisa verificar se faz refresh transparente antes de expirar
- [ ] **AuditService**: Atualmente é apenas um logger simples. Expandir para persistir em tabela `audit_log` com entidade, id, usuario, ação, timestamp
- [ ] **Armazenamento seguro do certificado A1**: Implementar upload criptografado (AES-256) e gestão de validade

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
| Busca por código de barras (endpoint dedicado) | ⚠️ | Query existe no repo, mas falta endpoint `/codigo-barras/{codigo}` |

### Itens pendentes de Produtos:
- [ ] **Endpoint de busca por código de barras**: Criar `GET /api/v1/produtos/codigo-barras/{codigo}` no controller
- [ ] **Upload de imagem**: Endpoint multipart + armazenamento (filesystem ou S3)
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
| Inventário (contagem física) | ❌ | Não há endpoint/fluxo de inventário |
| Relatório produtos abaixo do mínimo | ✅ | `GET /estoque/abaixo-minimo` |

### Itens pendentes de Estoque:
- [ ] **Inventário**: Criar endpoint `POST /api/v1/estoque/inventario` que recebe uma lista de `{produtoId, quantidadeContada}` e gera ajustes (`AJUSTE_INVENTARIO`) automaticamente

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

### Itens pendentes de Caixa:
- Nenhum item crítico pendente neste módulo. ✅ Completo.

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
| Emissão de NFC-e mock após confirmação | ⚠️ | Endpoint existe mas **não é chamado automaticamente** ao finalizar |

### Itens pendentes de Vendas:
- [ ] **Emissão automática de NFC-e**: Chamar `notaFiscalService.gerarNotaFiscalMock()` automaticamente no final de `finalizarVenda()` (atualmente é manual via `POST /vendas/{id}/nota-fiscal`)

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

### Itens pendentes de Clientes:
- Nenhum item crítico pendente. ✅ Completo.

---

## 8. Módulo: Crediário / Fiado (Seção 4.5)

| Funcionalidade | Estado | Detalhes |
|----------------|--------|----------|
| Registro de venda no fiado | ✅ | `processarCrediario()` em `VendaService` |
| Criação de parcela (padrão: 1 parcela, 30 dias) | ✅ | `ParcelaCrediario` entity |
| Liquidar parcela | ✅ | `POST /crediarios/parcelas/{id}/liquidar` |
| Listagem de parcelas (filtro por cliente/status) | ✅ | `GET /crediarios/parcelas` |
| Parcelamento customizado (N parcelas) | ❌ | Sempre cria 1 parcela |
| Relatório de contas a receber | ⚠️ | Dados disponíveis, mas sem endpoint dedicado |
| Notificação interna de parcelas vencidas | ❌ | Não há scheduler para verificar vencimentos |

### Itens pendentes de Crediário:
- [ ] **Parcelamento customizado**: Aceitar `numeroParcelas` na venda/crediário e gerar N parcelas com datas de vencimento mensais
- [ ] **Endpoint de contas a receber**: `GET /api/v1/crediarios/contas-a-receber?vencidas=true` com totais
- [x] **Scheduler de vencimentos**: `@Scheduled` que marca parcelas como `VENCIDA` quando `dataVencimento < hoje` e `status == PENDENTE`

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
- [ ] **Integração real SEFAZ**: Avaliar uso do `focusnfe-java` (API Focus NF-e) ou implementação direta via WebService SEFAZ
- [ ] **Assinatura digital**: Implementar leitura de certificado A1 (.pfx) e assinatura XML
- [ ] **Status como Enum**: Trocar `String status` na `NotaFiscal` para `@Enumerated StatusNfe`
- [ ] **Contingência off-line**: Lógica de fallback quando SEFAZ está indisponível
- [ ] **DANFE**: Geração de PDF com QR Code (JasperReports ou iText)
- [ ] **Upload de certificado**: Endpoint para upload do .pfx com armazenamento criptografado

---

## 10. Módulo: Relatórios (Seção 4.8 e 11)

| Relatório (spec) | Estado | Detalhes |
|-------------------|--------|----------|
| Vendas por período | ✅ | `GET /relatorios/vendas?inicio=&fim=` |
| Vendas do dia | ⚠️ | Possível via `vendas?inicio=&fim=` mas sem endpoint específico |
| Vendas por forma de pagamento | ⚠️ | Dados no `DashboardStats.faturamentoPorForma` mas sem endpoint dedicado |
| Vendas por produto | ❌ | Não implementado |
| Ranking de produtos mais vendidos | ⚠️ | `TopProduto` no DTO mas retorna `List.of()` (TODO) |
| Posição de estoque | ✅ | `GET /relatorios/estoque/posicao` |
| Produtos abaixo do mínimo | ✅ | `GET /estoque/abaixo-minimo` |
| Movimentação de estoque | ✅ | `GET /estoque/movimentacoes` |
| Balanço de caixa | ✅ | `GET /relatorios/caixa/balanco/{caixaId}` |
| Contas a receber (crediário) | ❌ | Sem endpoint dedicado |
| Fluxo de caixa | ❌ | Não implementado |
| Dashboard gerencial | ✅ | `GET /relatorios/dashboard/stats` |
| Exportação em PDF | ❌ | JasperReports/iText não integrado |

### Itens pendentes de Relatórios:
- [x] **Top produtos (ranking)**: Implementar query e retornar no `getDashboardStats()` (atualmente é `List.of()`)
- [ ] **Vendas por forma de pagamento**: Endpoint dedicado `GET /relatorios/vendas/por-forma-pagamento?inicio=&fim=`
- [ ] **Vendas por produto**: Endpoint `GET /relatorios/vendas/por-produto?inicio=&fim=`
- [ ] **Contas a receber**: Endpoint agregado com totais (vencidas, a vencer, total)
- [ ] **Fluxo de caixa**: Entradas e saídas consolidadas por período
- [ ] **Exportação PDF**: Integrar JasperReports ou iText para gerar PDF dos relatórios

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
| Upload de certificado digital | ❌ | Não há upload de .pfx |
| Gestão de usuários e perfis | ❌ | Sem CRUD de usuários via API (apenas seed) |

### Itens pendentes de Configurações:
- [ ] **CRUD de Usuários**: Criar `UsuarioController` com endpoints para criar, editar, listar e inativar usuários com seus perfis
- [ ] **Upload de certificado digital**: Endpoint para upload e armazenamento seguro

---

## 12. Frontend — Análise por Módulo

| Módulo Frontend | Estado | Arquivos existentes |
|-----------------|--------|---------------------|
| **Auth / Login** | ✅ | `features/auth/pages/login.component` |
| **Dashboard** | ⚠️ | Existe mas referencia `stats()?.series` (corrigido para `vendasRecentemente`) |
| **PDV (Ponto de Venda)** | ✅ | `features/pdv/pages/pos.component` |
| **Caixa** | ✅ | `features/caixa/pages/caixa.component` |
| **Produtos** | ✅ | `features/produtos/` (list + create/edit) |
| **Estoque** | ✅ | `features/estoque/pages/estoque-list.component` |
| **Clientes** | ✅ | `features/clientes/` (list + details) |
| **Relatórios** | ✅ | `features/relatorios/pages/relatorios.component` |
| **Configurações** | ✅ | `features/configuracoes/` (pages + services) — recém criado |
| **Crediário** | ✅ | `features/crediario/` (pages + services) — recém criado |
| **Forbidden (403)** | ✅ | `features/forbidden/` |
| **Layout (sidebar + navbar)** | ✅ | `layout/` com links para todos os módulos |

### Itens pendentes do Frontend:
- [x] **Build quebrado**: Existem arquivos duplicados em `pages/` e `services/` (raíz) que foram removidos, mas podem haver referências restantes. Garantir que `ng build` passe sem erros
- [x] **Dashboard chart**: Componente `LineChartComponent` precisa aceitar `VendasPorDia[]` e não `SeriesPoint[]`
- [ ] **Gestão de Usuários**: Criar tela admin para CRUD de usuários (atualmente só seed)
- [ ] **NFC-e UI**: Tela para visualizar notas fiscais emitidas, status, e opção de cancelamento

---

## 13. Testes

| Área | Estado | Detalhes |
|------|--------|----------|
| `VendaServiceTest.java` | ✅ | Testes unitários com Mockito |
| `CaixaServiceTest.java` | ✅ | Testes unitários com Mockito |
| `EstoqueServiceTest.java` | ✅ | Testes unitários com Mockito |
| `ApiApplicationTests.java` | ✅ | Smoke test do contexto Spring |
| Testes de integração (`@SpringBootTest`) | ❌ | Nenhum teste de integração |
| Testes de Controller (`@WebMvcTest`) | ❌ | Nenhum slice test |
| Testes de frontend (Jasmine/Karma) | ❌ | Apenas o `app.spec.ts` padrão |
| `TestDataBuilder` | ❌ | Não há builders para dados de teste |

### Itens pendentes de Testes:
- [ ] **Controller slice tests**: Criar `@WebMvcTest` para os controllers principais (ProdutoController, VendaController, CaixaController)
- [ ] **Integration tests**: Criar `@SpringBootTest` com Testcontainers para PostgreSQL
- [ ] **TestDataBuilder**: Builders para `Venda`, `Produto`, `Caixa`, `Usuario` etc.
- [ ] **Frontend tests**: Testes de componentes com `TestBed` para os módulos críticos (PDV, Caixa)

---

## 14. Priorização Sugerida (Próximos Passos)

### 🔴 Prioridade Alta (Bloqueantes para produção)

1. ~~**Corrigir build do frontend** — Garantir `ng build` sem erros~~ (Concluído)
2. **CRUD de Usuários** — Sem isso, novos operadores só entram via seed
3. ~~**Top produtos no Dashboard** — Implementar a query (atualmente retorna lista vazia)~~ (Concluído)
4. ~~**Scheduler de parcelas vencidas** — Parcelas nunca mudam para `VENCIDA`~~ (Concluído)

### 🟡 Prioridade Média (Funcionalidades importantes)

5. **Parcelamento customizado no crediário** — Loja real precisa de N parcelas
6. **Relatórios adicionais** — Vendas por produto, por forma de pagamento, contas a receber
7. **Emissão automática de NFC-e na finalização** — Atualmente é manual
8. **Busca de produto por código de barras** — Endpoint dedicado para leitor
9. **Inventário de estoque** — Contagem física e ajuste

### 🟢 Prioridade Baixa (Melhorias / Fase 2+)

10. **Integração real SEFAZ** (NFC-e produção, assinatura digital, contingência)
11. **Exportação PDF de relatórios** (JasperReports)
12. **Upload de imagem de produto**
13. **Histórico de preços**
14. **Auditoria persistida em banco**
15. **Testes de integração e E2E**
16. **CI/CD com GitHub Actions**

---

_Este roadmap deve ser atualizado conforme os itens forem concluídos._
