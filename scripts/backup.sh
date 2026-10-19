#!/bin/sh
# BLK Burguer - backup do banco PostgreSQL (#42), para Linux/macOS.
# Uso: ./backup.sh [banco]   (padrão: blkburguer)
# Conexão: PGHOST, PGPORT, PGUSER (padrão localhost, 5432, postgres); senha pelo pg_dump, PGPASSWORD ou ~/.pgpass.
set -e
BANCO="${1:-blkburguer}"
PASTA="$(dirname "$0")/../backups"
mkdir -p "$PASTA"
ARQUIVO="$PASTA/${BANCO}_$(date +%Y-%m-%d_%H%M).backup"
echo "Gerando backup de \"$BANCO\"..."
pg_dump -h "${PGHOST:-localhost}" -p "${PGPORT:-5432}" -U "${PGUSER:-postgres}" -F c -f "$ARQUIVO" "$BANCO"
echo "Backup salvo em: $ARQUIVO"
