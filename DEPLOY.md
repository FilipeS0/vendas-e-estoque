# Deploy de Produção — HTTPS com Let's Encrypt

## Pré-requisitos

- Servidor com Docker + Docker Compose instalados
- Domínio apontando para o IP do servidor (registro A no DNS)
- Portas **80** e **443** abertas no firewall

---

## Estrutura de arquivos esperada no servidor

```
.
├── docker-compose.prod.yml
├── .env
├── nginx/
│   ├── nginx.init.conf   ← usado só na primeira emissão do certificado
│   └── nginx.prod.conf   ← usado em produção normal
├── api/
└── app/
```

---

## Passo 1 — Configurar o `.env`

Crie o arquivo `.env` na raiz do projeto com pelo menos:

```env
POSTGRES_DB=erp
POSTGRES_USER=erp_user
POSTGRES_PASSWORD=SENHA_FORTE_AQUI
DOMAIN=seudominio.com
```

> ⚠️ Nunca comite o `.env` no Git. Ele está (ou deve estar) no `.gitignore`.

---

## Passo 2 — Primeira emissão do certificado SSL

O Certbot precisa responder a um desafio HTTP antes de o certificado existir.
Por isso, na **primeira vez**, suba o nginx com a config temporária:

### 2a. Suba apenas o nginx com config HTTP simples

```bash
# Substitua temporariamente a config de produção pela de init
cp nginx/nginx.init.conf nginx/nginx.prod.conf

docker compose -f docker-compose.prod.yml up -d frontend
```

### 2b. Emita o certificado

```bash
docker compose -f docker-compose.prod.yml run --rm certbot \
  certbot certonly \
    --webroot \
    --webroot-path=/var/www/certbot \
    --email SEU@EMAIL.COM \
    --agree-tos \
    --no-eff-email \
    -d seudominio.com
```

> Substitua `SEU@EMAIL.COM` e `seudominio.com` pelos seus dados reais.

### 2c. Restaure a config de produção com SSL

```bash
# Restaure o nginx.prod.conf original (com SSL)
git checkout nginx/nginx.prod.conf
```

---

## Passo 3 — Subir tudo em produção

```bash
docker compose -f docker-compose.prod.yml up -d --build
```

Verifique que os serviços estão rodando:

```bash
docker compose -f docker-compose.prod.yml ps
```

---

## Renovação automática

O serviço `certbot` no compose já roda em loop e tenta renovar o certificado a cada 12h.
Os certificados Let's Encrypt expiram em **90 dias**; a renovação automática ocorre quando
restam menos de 30 dias.

Para renovar manualmente:

```bash
docker compose -f docker-compose.prod.yml run --rm certbot certbot renew
docker compose -f docker-compose.prod.yml restart frontend
```

---

## Comandos úteis

```bash
# Ver logs do nginx
docker logs erp_frontend -f

# Ver logs da API
docker logs erp_api -f

# Parar tudo
docker compose -f docker-compose.prod.yml down

# Recriar apenas a API após novo build
docker compose -f docker-compose.prod.yml up -d --build api
```