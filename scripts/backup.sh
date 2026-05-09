#!/bin/bash
# backup.sh — Faz dump do PostgreSQL e envia para o Google Drive via rclone
#
# Pré-requisitos:
#   1. rclone instalado na máquina host (https://rclone.org/install/)
#   2. rclone configurado com um remote do Google Drive:
#        rclone config
#      Siga o assistente, escolha "Google Drive" e nomeie o remote igual
#      ao valor de RCLONE_REMOTE no .env (padrão: gdrive)
#
# Uso manual:
#   chmod +x scripts/backup.sh
#   ./scripts/backup.sh
#
# Agendamento automático (cron) — roda todo dia às 02:00:
#   crontab -e
#   0 2 * * * /caminho/absoluto/para/scripts/backup.sh >> /var/log/erp-backup.log 2>&1

set -euo pipefail

# ─── Carrega variáveis do .env ────────────────────────────────────────────────
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ENV_FILE="$SCRIPT_DIR/../.env"

if [[ ! -f "$ENV_FILE" ]]; then
  echo "[ERRO] Arquivo .env não encontrado em $ENV_FILE"
  exit 1
fi

set -a
source "$ENV_FILE"
set +a

# ─── Configuração ─────────────────────────────────────────────────────────────
CONTAINER_NAME="erp_db"
BACKUP_DIR="$SCRIPT_DIR/../backups"
TIMESTAMP=$(date +"%Y-%m-%d_%H-%M-%S")
BACKUP_FILE="$BACKUP_DIR/erp_backup_$TIMESTAMP.sql.gz"
RCLONE_REMOTE="${RCLONE_REMOTE:-gdrive}"
RCLONE_GDRIVE_FOLDER="${RCLONE_GDRIVE_FOLDER:-backups/erp}"
RETENTION_DAYS=7   # Apaga backups locais com mais de 7 dias

# ─── Cria pasta local de backups se não existir ───────────────────────────────
mkdir -p "$BACKUP_DIR"

echo "[$(date)] Iniciando backup..."

# ─── Dump do banco ───────────────────────────────────────────────────────────
docker exec "$CONTAINER_NAME" \
  pg_dump -U "$POSTGRES_USER" "$POSTGRES_DB" \
  | gzip > "$BACKUP_FILE"

echo "[$(date)] Dump gerado: $BACKUP_FILE"

# ─── Envia para Google Drive ─────────────────────────────────────────────────
rclone copy "$BACKUP_FILE" "$RCLONE_REMOTE:$RCLONE_GDRIVE_FOLDER" \
  --progress \
  --log-level INFO

echo "[$(date)] Backup enviado para $RCLONE_REMOTE:$RCLONE_GDRIVE_FOLDER"

# ─── Remove backups locais antigos ───────────────────────────────────────────
find "$BACKUP_DIR" -name "erp_backup_*.sql.gz" -mtime +$RETENTION_DAYS -delete
echo "[$(date)] Backups locais com mais de $RETENTION_DAYS dias removidos"

echo "[$(date)] Backup concluído com sucesso."