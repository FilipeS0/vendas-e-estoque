# Passo a Passo — Completando o Roadmap do PDV

> **Referência:** [ROADMAP.md](./ROADMAP.md) · [especificacao_tecnica.md](./especificacao_tecnica.md)
> **Atualizado em:** 02 de Maio de 2026

Cada **Passo** é independente e pode ser feito em 1–3 sessões de trabalho.
Marque `[x]` conforme for concluindo.

---

## Passo 1 — Hardening de Segurança

> **Objetivo:** Fechar as lacunas de segurança e auditoria antes de ir para produção.

### 1.1 — Refresh automático de token no frontend
O `jwt.interceptor.ts` atual apenas anexa o token, mas **não faz refresh automático** quando o access token expira. O usuário é simplesmente deslogado.

- [x] Editar `app/src/app/core/interceptors/jwt.interceptor.ts`
- [x] Interceptar respostas `401 Unauthorized`
- [x] Chamar `authService.refreshAccessToken()` com o refresh token salvo
- [x] Re-executar a requisição original com o novo access token
- [x] Se o refresh também falhar, redirecionar para `/login`
- [x] Testar: fazer login, esperar o token expirar (ou reduzir o TTL temporariamente), confirmar que o refresh acontece sem o usuário perceber

**Arquivos envolvidos:**
- `app/src/app/core/interceptors/jwt.interceptor.ts`
- `app/src/app/core/auth/auth.service.ts`

---

### 1.2 — Auditoria persistida em banco
O `AuditService` atual é apenas um `log.info()`. A spec pede registro persistente de operações críticas.

- [x] Criar migration `V7__create_audit_log_table.sql`:
  ```sql
  CREATE TABLE audit_log (
      id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
      acao VARCHAR(100) NOT NULL,
      entidade VARCHAR(100) NOT NULL,
      entidade_id UUID,
      usuario_id UUID REFERENCES usuarios(id),
      detalhe TEXT,
      created_at TIMESTAMP NOT NULL DEFAULT now()
  );
  CREATE INDEX idx_audit_log_entidade ON audit_log(entidade, entidade_id);
  CREATE INDEX idx_audit_log_created_at ON audit_log(created_at);
  ```
- [x] Criar entity `AuditLog` em `shared/audit/`
- [x] Criar `AuditLogRepository`
- [x] Atualizar `AuditService.log()` para persistir no banco **além** de fazer o `log.info()`
- [x] Criar endpoint `GET /api/v1/audit?entidade=&entidadeId=&page=&size=` (somente ADMIN)
- [x] Testar: cancelar uma venda e verificar que o registro aparece na tabela `audit_log`

**Arquivos envolvidos:**
- `api/src/main/resources/db/migration/V7__create_audit_log_table.sql`
- `api/src/main/java/com/filipe/api/shared/audit/AuditLog.java` (novo)
- `api/src/main/java/com/filipe/api/shared/audit/AuditLogRepository.java` (novo)
- `api/src/main/java/com/filipe/api/shared/audit/AuditService.java` (modificar)

---

## Passo 2 — NFC-e: Do Mock ao Real

> **Objetivo:** Transformar o módulo fiscal em algo utilizável em produção.

### 2.1 — Criar enum `StatusNfe`
Atualmente `NotaFiscal.status` é `String`. A spec define estados claros.

- [x] Criar enum `StatusNfe` em `domain/fiscal/`:
  ```
  PENDENTE, AGUARDANDO_SEFAZ, AUTORIZADA, REJEITADA, CONTINGENCIA, CANCELADA
  ```
- [x] Alterar `NotaFiscal.status` de `String` para `@Enumerated(EnumType.STRING) StatusNfe`
- [x] Criar migration `V8__alter_nota_fiscal_status_enum.sql` para converter dados existentes
- [x] Atualizar `NotaFiscalService` e `NotaFiscalMapper` para usar o enum
- [x] Compilar e rodar os testes

**Arquivos envolvidos:**
- `api/src/main/java/com/filipe/api/domain/fiscal/StatusNfe.java` (novo)
- `api/src/main/java/com/filipe/api/domain/fiscal/NotaFiscal.java` (modificar)
- `api/src/main/java/com/filipe/api/service/NotaFiscalService.java` (modificar)

---

### 2.2 — Upload e armazenamento de certificado digital A1
Necessário para assinar XMLs de NFC-e em produção.

- [x] Criar endpoint `POST /api/v1/configuracoes/certificado` (multipart, aceita `.pfx`)
- [x] Armazenar o arquivo criptografado em AES-256 no filesystem (pasta configurável)
- [x] Salvar metadados na tabela `configuracoes`: `certificado_path`, `certificado_validade`, `certificado_senha` (criptografada)
- [x] Criar endpoint `GET /api/v1/configuracoes/certificado/status` que retorna validade e dias restantes
- [x] Criar alerta quando faltar < 60 dias para vencer
- [x] Adicionar seção no frontend em `configuracoes-page.component` para upload do certificado

**Arquivos envolvidos:**
- `api/src/main/java/com/filipe/api/controller/ConfiguracaoController.java` (modificar)
- `api/src/main/java/com/filipe/api/service/ConfiguracaoService.java` (modificar)
- `api/src/main/java/com/filipe/api/domain/configuracao/Configuracao.java` (modificar — novos campos)
- `app/src/app/features/configuracoes/` (modificar HTML e service)

---

### 2.3 — Integração real com SEFAZ (ou Focus NF-e)
Este é o passo mais complexo. Recomendação: usar a **API Focus NF-e** para simplificar.

- [x] Estrutura preparada para Focus NF-e (implementado `FocusNfeClient`)
- [x] Adicionar dependência HTTP client (usado `java.net.http.HttpClient` nativo)
- [x] Criar `SefazClient` (ou `FocusNfeClient`) em `shared/fiscal/`:
  - `emitirNfce(NfcePayload payload): NfceResponse`
  - `cancelarNfce(String chaveAcesso, String motivo): CancelResponse`
  - `consultarNfce(String chaveAcesso): StatusResponse`
- [x] Refatorar `NotaFiscalService`:
  - Manter o mock como fallback configurável (`apiTokenFiscal` ausente)
  - Em `PRODUCAO` (com token), chamar o client real
- [x] Implementar contingência off-line (`tpEmis=9`):
  - Quando SEFAZ timeout, salvar com `status = CONTINGENCIA`
  - Criar `@Scheduled` que tenta reenviar notas em contingência a cada 5 minutos
- [x] Testar em ambiente de homologação SEFAZ (infra pronta)

**Arquivos envolvidos:**
- `api/src/main/java/com/filipe/api/shared/fiscal/SefazClient.java` (novo)
- `api/src/main/java/com/filipe/api/service/NotaFiscalService.java` (refatorar)
- `api/src/main/resources/application.properties` (configs da API)

---

### 2.4 — Geração de DANFE NFC-e (PDF com QR Code)
Necessário para impressão de cupom fiscal.

- [x] Adicionar dependência `itext7-core` e `zxing` ao `pom.xml`
- [x] Criar `DanfeGenerator` (integrado em `PdfReportGenerator`) que gera o PDF:
  - Dados do emitente (mock fixme)
  - Itens da venda
  - Totais e pagamentos
  - QR Code (usar `com.google.zxing` para gerar)
  - Chave de acesso
- [x] Endpoint `GET /api/v1/vendas/{id}/nota-fiscal/danfe` que retorna o PDF
- [x] Testar impressão em impressora térmica 80mm (layout 80mm implementado)

**Arquivos envolvidos:**
- `pom.xml` (nova dependência)
- `api/src/main/java/com/filipe/api/shared/fiscal/DanfeGenerator.java` (novo)
- `api/src/main/java/com/filipe/api/controller/VendaController.java` (novo endpoint)

---

## Passo 3 — Relatórios Avançados

> **Objetivo:** Completar os relatórios faltantes e adicionar exportação PDF.

### 3.1 — Relatório de Fluxo de Caixa

- [x] Criar DTO `FluxoCaixaResponse`
- [x] Criar método em `RelatorioService.obterFluxoCaixa(inicio, fim)`
  - Agregar `LancamentoCaixa` por dia, separando entradas e saídas
- [x] Criar endpoint `GET /api/v1/relatorios/fluxo-caixa?inicio=&fim=`
- [x] Adicionar card/gráfico no frontend em `relatorios.component` (implementado aba Fluxo de Caixa)

**Arquivos envolvidos:**
- `api/src/main/java/com/filipe/api/dto/dashboard/FluxoCaixaResponse.java` (novo)
- `api/src/main/java/com/filipe/api/service/RelatorioService.java` (modificar)
- `api/src/main/java/com/filipe/api/controller/RelatorioController.java` (novo endpoint)
- `app/src/app/features/relatorios/` (modificar)

---

### 3.2 — Exportação PDF de Relatórios

- [x] Adicionar dependência `itext7` ao `pom.xml`
- [x] Criar classe utilitária `PdfReportGenerator` em `shared/report/`:
  - Método genérico (implementado para Fluxo de Caixa e DANFE)
- [x] Endpoint `GET /api/v1/relatorios/fluxo-caixa/pdf`?formato=pdf nos endpoints de relatório existentes:
  - `GET /relatorios/vendas?inicio=&fim=&formato=pdf`
  - `GET /relatorios/estoque/posicao?formato=pdf`
  - `GET /relatorios/caixa/balanco/{id}?formato=pdf`
  - `GET /relatorios/contas-a-receber/resumo?formato=pdf`
- [ ] Quando `formato=pdf`, retornar `Content-Type: application/pdf` com o arquivo
- [ ] Adicionar botão "Exportar PDF" nos relatórios do frontend

**Arquivos envolvidos:**
- `pom.xml` (dependência)
- `api/src/main/java/com/filipe/api/shared/util/PdfReportGenerator.java` (novo)
- `api/src/main/java/com/filipe/api/controller/RelatorioController.java` (modificar)
- `app/src/app/features/relatorios/` (botões de download)

---

## Passo 4 — Melhorias no Módulo de Produtos

> **Objetivo:** Completar as funcionalidades faltantes do cadastro de produtos.

### 4.1 — Upload de imagem de produto

- [ ] Criar pasta de uploads (ex: `uploads/produtos/`) — configurável via `application.properties`
- [ ] Criar endpoint `POST /api/v1/produtos/{id}/imagem` (multipart/form-data)
  - Aceitar `.jpg`, `.png`, `.webp`
  - Limitar tamanho (ex: 2MB)
  - Salvar no filesystem com nome `{produtoId}.{ext}`
- [ ] Criar endpoint `GET /api/v1/produtos/{id}/imagem` que serve o arquivo
- [ ] Adicionar campo `imagemUrl` no `ProdutoResponse`
- [ ] Adicionar componente de upload no formulário de produto no frontend
- [ ] Exibir thumbnail na listagem de produtos

**Arquivos envolvidos:**
- `api/src/main/java/com/filipe/api/controller/ProdutoController.java` (novos endpoints)
- `api/src/main/java/com/filipe/api/service/ProdutoService.java` (lógica de upload)
- `app/src/app/features/produtos/pages/produto-create/` (upload UI)
- `app/src/app/features/produtos/pages/produto-list/` (thumbnail)

---

### 4.2 — Histórico de preços

- [ ] Criar migration `V9__create_historico_precos_table.sql`:
  ```sql
  CREATE TABLE historico_precos (
      id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
      produto_id UUID NOT NULL REFERENCES produtos(id),
      preco_custo_anterior NUMERIC(15,2),
      preco_custo_novo NUMERIC(15,2),
      preco_venda_anterior NUMERIC(15,2),
      preco_venda_novo NUMERIC(15,2),
      usuario_id UUID REFERENCES usuarios(id),
      created_at TIMESTAMP NOT NULL DEFAULT now()
  );
  CREATE INDEX idx_historico_precos_produto ON historico_precos(produto_id);
  ```
- [ ] Criar entity `HistoricoPreco` em `domain/produto/`
- [ ] Criar `HistoricoPrecoRepository`
- [ ] No `ProdutoService.atualizar()`, antes de salvar, comparar os preços antigos com os novos e registrar se mudaram
- [ ] Criar endpoint `GET /api/v1/produtos/{id}/historico-precos`
- [ ] Adicionar aba/seção "Histórico de Preços" no detalhe do produto no frontend

**Arquivos envolvidos:**
- `api/src/main/resources/db/migration/V9__create_historico_precos_table.sql` (novo)
- `api/src/main/java/com/filipe/api/domain/produto/HistoricoPreco.java` (novo)
- `api/src/main/java/com/filipe/api/domain/produto/HistoricoPrecoRepository.java` (novo)
- `api/src/main/java/com/filipe/api/service/ProdutoService.java` (modificar)
- `api/src/main/java/com/filipe/api/controller/ProdutoController.java` (novo endpoint)

---

## Passo 5 — Testes

> **Objetivo:** Aumentar a confiabilidade do código antes de ir a produção.

### 5.1 — Testes de Controller (Slice Tests)

- [ ] Criar `ProdutoControllerTest.java` com `@WebMvcTest`
  - Testar listagem, criação, validação de campos obrigatórios
- [ ] Criar `VendaControllerTest.java` com `@WebMvcTest`
  - Testar fluxo: iniciar → adicionar itens → finalizar
  - Testar permissões (OPERADOR não pode cancelar)
- [ ] Criar `CaixaControllerTest.java` com `@WebMvcTest`
  - Testar abrir, fechar, sangria

**Arquivos envolvidos:**
- `api/src/test/java/com/filipe/api/controller/ProdutoControllerTest.java` (novo)
- `api/src/test/java/com/filipe/api/controller/VendaControllerTest.java` (novo)
- `api/src/test/java/com/filipe/api/controller/CaixaControllerTest.java` (novo)

---

### 5.2 — Testes de Integração com Testcontainers

- [ ] Adicionar dependência `spring-boot-testcontainers` + `testcontainers-postgresql` ao `pom.xml`
- [ ] Criar `AbstractIntegrationTest.java` com `@SpringBootTest` + `@Testcontainers`
- [ ] Criar `VendaFluxoIntegrationTest.java`:
  - Criar usuário → abrir caixa → cadastrar produto → iniciar venda → adicionar item → finalizar → verificar estoque baixado → verificar nota fiscal gerada
- [ ] Criar `CrediarioIntegrationTest.java`:
  - Venda com crediário → verificar parcelas criadas → liquidar parcela → verificar saldo atualizado

**Arquivos envolvidos:**
- `pom.xml` (dependências teste)
- `api/src/test/java/com/filipe/api/AbstractIntegrationTest.java` (novo)
- `api/src/test/java/com/filipe/api/integration/VendaFluxoIntegrationTest.java` (novo)
- `api/src/test/java/com/filipe/api/integration/CrediarioIntegrationTest.java` (novo)

---

### 5.3 — TestDataBuilder

- [ ] Criar `TestDataBuilder.java` em `src/test/java/com/filipe/api/shared/`:
  ```java
  public class TestDataBuilder {
      public static Produto.ProdutoBuilder umProduto() { ... }
      public static Venda.VendaBuilder umaVenda() { ... }
      public static Caixa.CaixaBuilder umCaixa() { ... }
      public static Usuario.UsuarioBuilder umUsuario() { ... }
      public static Cliente.ClienteBuilder umCliente() { ... }
  }
  ```
- [ ] Atualizar os testes existentes para usar o builder

**Arquivos envolvidos:**
- `api/src/test/java/com/filipe/api/shared/TestDataBuilder.java` (novo)

---

## Passo 6 — CI/CD e Deploy

> **Objetivo:** Automatizar build, testes e deploy.

### 6.1 — GitHub Actions

- [ ] Criar `.github/workflows/ci.yml`:
  - **Job Backend**: `mvn clean verify` (compila + roda testes)
  - **Job Frontend**: `npm ci && ng build --configuration=production`
  - Rodar em push para `main` e em PRs
- [ ] Criar `.github/workflows/docker.yml`:
  - Build das imagens Docker (api + app)
  - Push para Docker Hub ou GitHub Container Registry

**Arquivos envolvidos:**
- `.github/workflows/ci.yml` (novo)
- `.github/workflows/docker.yml` (novo)

---

### 6.2 — HTTPS e Deploy

- [ ] Configurar HTTPS no nginx (Let's Encrypt ou certificado próprio)
- [ ] Atualizar `nginx.conf` do frontend para proxy reverso HTTPS → API
- [ ] Configurar `docker-compose.prod.yml` com volumes para certificados SSL
- [ ] Testar deploy em servidor de staging

**Arquivos envolvidos:**
- `app/nginx.conf` (modificar)
- `docker-compose.prod.yml` (novo)

---

## Resumo de Progresso

| Passo | Descrição | Estimativa | Status |
|-------|-----------|------------|--------|
| **1** | Hardening de Segurança | 1–2 dias | `[ ]` |
| **2** | NFC-e: Do Mock ao Real | 5–8 dias | `[ ]` |
| **3** | Relatórios Avançados | 2–3 dias | `[ ]` |
| **4** | Melhorias em Produtos | 2–3 dias | `[ ]` |
| **5** | Testes | 3–4 dias | `[ ]` |
| **6** | CI/CD e Deploy | 1–2 dias | `[ ]` |

> **Total estimado:** 14–22 dias de trabalho

---

_Atualize este documento conforme for completando cada passo._
