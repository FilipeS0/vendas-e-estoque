# Sistema ERP de Venda e Estoque Moderno

Um sistema completo de Gestão de Vendas e Estoque (ERP) projetado especificamente para operações de varejo e integração com PDV (Ponto de Venda). Este projeto utiliza os princípios de Domain-Driven Design (DDD) e uma stack tecnológica moderna para oferecer uma experiência de usuário segura, performática e reativa.

## 🚀 Stack Tecnológica

### Backend
- **Java 21 & Spring Boot 4.x**: Alta performance e produtividade.
- **Spring Security & OAuth2**: Autenticação stateless baseada em JWT.
- **Spring Data JPA & PostgreSQL 16**: Persistência robusta com migrações via Flyway.
- **AES-GCM Crypto**: Criptografia avançada para dados sensíveis.
- **Rate Limiting (Bucket4j)**: Proteção contra ataques de força bruta.

### Frontend
- **Angular 21**: Uso intensivo de Standalone Components e Signals.
- **Angular Material**: Interface premium e componentes reativos.
- **Ngx-Charts**: Visualização de métricas e performance em tempo real.
- **RxJS**: Gerenciamento de estado e fluxos assíncronos.

### Infraestrutura
- **Docker & Docker Compose**: Build multi-estágio e isolamento total.
- **CI/CD**: Pipelines configurados para validação de qualidade e build.

---

## 🌟 Funcionalidades Principais

- **Segurança Avançada**: Autenticação JWT com Spring Security, filtros de segurança e RBAC (ADMIN, GERENTE, OPERADOR).
- **PDV (Ponto de Venda)**: Interface de venda rápida com busca dinâmica, integração com PIX dinâmico e emissão de comprovantes.
- **Gestão de Produtos e Estoque**: 
  - Ciclo de vida completo (CRUD) com validações rigorosas (EAN, NCM).
  - Suporte a imagens de produtos com validação de MIME type.
  - Paginação nativa e filtros otimizados com RxJS.
  - **Inativação Lógica (Soft Delete)**: Preserva histórico de vendas inativando registros.
- **Gestão Financeira e Crediário**: Controle total de parcelas, liquidação e balanços de caixa.
- **Relatórios**: Geração de PDFs profissionais para vendas, orçamentos e balanços de estoque.
- **Provisionamento Automático**: `DataSeeder` injeta usuários, categorias e fornecedores padrão no primeiro acesso.

---

## 🛠️ Como Iniciar

### 1. Pré-requisitos
- [Docker](https://docs.docker.com/get-docker/) e [Docker Compose](https://docs.docker.com/compose/install/)
- Node.js (v20+) e npm
- JDK 21+

### 2. Configuração de Ambiente
Crie um arquivo `.env` na raiz do projeto baseado no `.env.example`.

### 3. Executar o Backend e Banco de Dados (via Docker)
```bash
docker compose up --build
```
A API estará disponível em `http://localhost:8080`.

### 4. Executar o Frontend
```bash
cd app
npm install
npm run start
```
O frontend estará disponível em `http://localhost:4200`.

---

## 🔐 Credenciais Padrão (Admin)

Após o provisionamento automático, você pode acessar o sistema com:

- **E-mail:** `admin@erp.com`
- **Senha:** `admin123`

---

## 🤝 Contribuição

Este é um projeto de código aberto! Sinta-se à vontade para enviar Pull Requests, sugerir melhorias na UI ou reportar bugs.

## 📝 Licença

Este projeto está licenciado sob a MIT License.

---
Desenvolvido por Filipe S.
