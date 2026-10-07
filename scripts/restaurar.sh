#!/bin/sh
# BLK Burguer - restauração de backup (#42), para Linux/macOS.
# Uso: ./restaurar.sh <arquivo.backup> [banco_destino]   (padrão: blkburguer_restaurado, um banco separado)
set -e
if [ -z "$1" ] || [ ! -f "$1" ]; then
    echo "Uso: ./restaurar.sh <arquivo.backup> [banco_destino]"
    exit 1
fi
DESTINO="${2:-blkburguer_restaurado}"
CONEXAO="-h ${PGHOST:-localhost} -p ${PGPORT:-5432} -U ${PGUSER:-postgres}"
if [ "$DESTINO" = "blkburguer" ]; then
    printf 'ATENÇÃO: os dados atuais de "%s" serão substituídos. Digite SIM para continuar: ' "$DESTINO"
    read -r CONFIRMA
    [ "$CONFIRMA" = "SIM" ] || { echo "Cancelado."; exit 1; }
fi
createdb $CONEXAO "$DESTINO" 2>/dev/null || true
pg_restore $CONEXAO --clean --if-exists --no-owner -d "$DESTINO" "$1"
echo "Restaurado em \"$DESTINO\"."
