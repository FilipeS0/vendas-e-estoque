# Sistema de Gestão de Vendas e Estoque - Backend API

API robusta e segura para gestão comercial, construída com Spring Boot 4.x e Java 21, seguindo as melhores práticas de arquitetura e segurança.

## 🚀 Stack Tecnológica
- **Java 21 & Spring Boot 4.0.6**: Base da aplicação.
- **Spring Security & OAuth2**: Proteção de endpoints e autenticação baseada em JWT.
- **Spring Data JPA & PostgreSQL**: Persistência de dados relacional.
- **Flyway**: Gerenciamento de migrações de banco de dados.
- **AES-GCM Cryptography**: Criptografia de dados sensíveis com vetores de inicialização randômicos.
- **Bucket4j**: Rate limiting para proteção contra ataques de força bruta e DoS.
- **iText7**: Geração de relatórios PDF complexos.
- **Testcontainers**: Testes de integração com bancos de dados reais em containers Docker.

## 🔐 Segurança e Confiabilidade
- **CORS Configurável**: Controle de origens através de variáveis de ambiente.
- **RBAC (Role Based Access Control)**: Controle de acesso refinado (ADMIN, GERENTE, OPERADOR).
- **Timeouts de API**: Proteção contra lentidão de serviços externos (FocusNFE, BrasilAPI).
- **MIME Type Validation**: Validação rigorosa de uploads para prevenir execução de scripts maliciosos.

## 🛠️ Instalação e Configuração

### Requisitos
- JDK 21+
- Docker (para Testcontainers e banco de dados)
- Maven 3.9+

### Configuração de Ambiente
Crie um arquivo `.env` baseado no `.env.example`:
```bash
DB_URL=jdbc:postgresql://localhost:5432/vendas_estoque
DB_USERNAME=postgres
DB_PASSWORD=sua_senha
CRYPTO_SECRET=sua_chave_secreta_de_32_chars
```

### Execução
```bash
mvn spring-boot:run
```

### Documentação da API
A documentação interativa (Swagger/OpenAPI) fica disponível em:
`http://localhost:8080/swagger-ui.html`

## 🧪 Testes
Executar a suíte de testes unitários e de integração:
```bash
mvn test
```

---
Desenvolvido por Filipe S.
