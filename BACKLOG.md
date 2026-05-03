# Backlog — Issues Identificadas na Análise de Gaps

> **Criado em:** 03 de Maio de 2026
> **Origem:** Análise cruzada entre `especificacao_tecnica.md` × código real
> **Prioridade:** Estes itens NÃO bloqueiam produção, mas representam gaps entre a spec e a implementação.

---

## 🔧 Issues Técnicas

### ISSUE-001 — Verificar dependência Testcontainers no `pom.xml`

**Prioridade:** 🔴 Alta (bloqueia Passo 5)
**Status:** `[x]` Concluído ✅

O `AbstractIntegrationTest.java` importa `org.testcontainers.*` e `org.springframework.boot.testcontainers.*`. As dependências foram adicionadas ao `pom.xml`.

**Ação:**
- [x] Adicionar `spring-boot-testcontainers`, `postgresql` (testcontainers) e `junit-jupiter` (testcontainers) ao `pom.xml`.

**Arquivo:** `api/pom.xml`

---

### ISSUE-002 — Enum `UnidadeMedida` ausente na entity `Produto`

**Prioridade:** 🟡 Média
**Status:** `[x]` Concluído ✅

A entity `Produto` agora possui o campo `unidadeMedida` (enum).

**Ação:**
1. [x] Criar enum `UnidadeMedida` em `domain/produto/`
2. [x] Adicionar campo na entity `Produto`
3. [x] Criar migration `V13__add_unidade_medida_to_produtos.sql`
4. [x] Atualizar DTOs e mappers
5. [x] Atualizar formulário de produto no frontend

**Arquivos:** `Produto.java`, `ProdutoRequest`, `ProdutoResponse`, mappers, migration, frontend form

---

### ISSUE-003 — Busca de produtos limitada a apenas nome

**Prioridade:** 🟡 Média
**Status:** `[x]` Concluído ✅

Implementada a busca `findByMultiCriteria` que pesquisa por Nome, Código de Barras ou Código Interno.

**Ação:**
1. [x] Criar query `findByMultiCriteria` no `ProdutoRepository`
2. [x] Atualizar `ProdutoService.listarProdutos()` para usar a nova busca
3. [ ] Atualizar frontend para exibir filtros avançados (Opcional, busca global já funciona)

**Arquivos:** `ProdutoController.java`, `ProdutoService.java`, `ProdutoRepository.java`

---

### ISSUE-004 — Tabela NCM para auto-complete no cadastro de produtos

**Prioridade:** 🟢 Baixa
**Status:** `[x]` Concluído ✅

Criada a tabela `ncm` com dados iniciais e endpoint de busca.

**Ação:**
1. [x] Criar entity `Ncm` e migration para tabela `ncm`
2. [x] Popular via migration com dados de exemplo
3. [x] Criar `NcmController` com endpoint `GET /api/v1/ncm/search?query=`
4. [ ] Adicionar auto-complete no campo NCM do formulário de produto

**Arquivos:** `Ncm.java`, `NcmRepository.java`, `NcmController.java`, migration V14

---

### ISSUE-005 — Consulta CPF/CNPJ via API externa (BrasilAPI)

**Prioridade:** 🟢 Baixa
**Status:** `[x]` Concluído ✅

Implementado `BrasilApiService` para consulta de CNPJ e CEP.

**Ação:**
1. [x] Criar `BrasilApiService` usando `RestTemplate`
2. [x] Criar `ExternalApiController` para expor as consultas
3. [ ] Integrar no frontend para preenchimento automático de cadastros

**Arquivos:** `BrasilApiService.java`, `ExternalApiController.java`

---

### ISSUE-006 — PIX com QR Code para Pagamento (v1.5)

**Prioridade:** 🟢 Baixa
**Status:** `[ ]` Pendente

A spec prevê integração com PIX (QR Code estático/dinâmico) na versão 1.5.

**Ação:**
1. Estudar APIs de bancos (Inter, Gerencianet, etc.) ou Gerador de QR Code PIX (Copy & Paste)
2. Criar `PixService`
3. Exibir QR Code na tela de fechamento de venda (PDV)

**Arquivos:** Novo service, PDV Component

---

### ISSUE-007 — `@MockBean` depreciado no Spring Boot 4.x

**Prioridade:** 🟡 Média
**Status:** `[x]` Concluído ✅

Migrado de `@MockBean` para `@MockitoBean`.

**Ação:**
1. [x] Migrar para `@MockitoBean` no `ProdutoControllerTest.java`

**Arquivo:** `ProdutoControllerTest.java`

---

## 📊 Resumo

| ID | Descrição | Prioridade | Bloqueia | Status |
|----|-----------|-----------|----------|--------|
| ISSUE-001 | Testcontainers dependency | 🔴 Alta | Passo 5 | ✅ |
| ISSUE-002 | Enum UnidadeMedida | 🟡 Média | — | ✅ |
| ISSUE-003 | Busca multi-critério | 🟡 Média | — | ✅ |
| ISSUE-004 | Tabela NCM auto-complete | 🟢 Baixa | — | ✅ |
| ISSUE-005 | Consulta CPF/CNPJ | 🟢 Baixa | — | ✅ |
| ISSUE-006 | PIX QR Code (v1.5) | 🟢 Baixa | — | `[ ]` |
| ISSUE-007 | @MockBean depreciado | 🟡 Média | Passo 5 | ✅ |

---

_Atualize este documento conforme as issues forem resolvidas._
