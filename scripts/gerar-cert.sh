#!/bin/bash
# gerar-cert.sh — Gera certificado SSL autoassinado para uso local
#
# Gera os arquivos:
#   certs/cert.pem  → certificado
#   certs/key.pem   → chave privada
#
# Uso:
#   chmod +x scripts/gerar-cert.sh
#   ./scripts/gerar-cert.sh
#
# Após rodar, suba o ambiente de produção local:
#   docker compose -f docker-compose.prod.yml up -d --build
#
# ⚠️  O navegador vai exibir aviso de certificado não confiável — é normal
#     para certificados autoassinados. Clique em "Avançado > Continuar".

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
CERTS_DIR="$SCRIPT_DIR/../certs"

mkdir -p "$CERTS_DIR"

openssl req -x509 -nodes -days 365 \
  -newkey rsa:2048 \
  -keyout "$CERTS_DIR/key.pem" \
  -out "$CERTS_DIR/cert.pem" \
  -subj "/C=BR/ST=Local/L=Local/O=ERP/CN=localhost" \
  -addext "subjectAltName=DNS:localhost,IP:127.0.0.1"

echo ""
echo "✅ Certificado gerado em $CERTS_DIR"
echo "   cert.pem → certificado"
echo "   key.pem  → chave privada"
echo ""
echo "⚠️  A pasta certs/ está no .gitignore — as chaves não serão commitadas."