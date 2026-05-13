#!/usr/bin/env bash
# Drop + recreate uma ou ambas as bases de dados e carrega o schema.
# Uso:  ./db-reset.sh [loja|servidor]   (sem argumentos = ambas)
# Vars: DB_USER (default trasmum), DB_PASS (default trasmum), DB_HOST (default localhost)

set -euo pipefail

DB_USER="${DB_USER:-trasmum}"
DB_PASS="${DB_PASS:-trasmum}"
DB_HOST="${DB_HOST:-localhost}"

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
ALVO="${1:-ambas}"

reset_db() {
    local nome="$1"
    local schema="$2"
    echo "==> A repor base de dados '${nome}'"
    mysql -h "$DB_HOST" -u "$DB_USER" -p"$DB_PASS" -e \
        "DROP DATABASE IF EXISTS \`${nome}\`; CREATE DATABASE \`${nome}\` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
    mysql -h "$DB_HOST" -u "$DB_USER" -p"$DB_PASS" "${nome}" < "${schema}"
    echo "    schema carregado de ${schema}"
}

load_sql() {
    local nome="$1"
    local file="$2"
    echo "==> A carregar ${file} em '${nome}'"
    mysql -h "$DB_HOST" -u "$DB_USER" -p"$DB_PASS" "${nome}" < "${file}"
}

case "$ALVO" in
    loja)
        reset_db trasmum_loja "${ROOT}/Loja/src/main/resources/schema.sql"
        load_sql trasmum_loja "${ROOT}/Loja/src/main/resources/mock_data.sql"
        ;;
    servidor)
        reset_db trasmum_servidor "${ROOT}/ServidorCentral/backend/db/schema.sql"
        ;;
    ambas)
        reset_db trasmum_loja "${ROOT}/Loja/src/main/resources/schema.sql"
        load_sql trasmum_loja "${ROOT}/Loja/src/main/resources/mock_data.sql"
        reset_db trasmum_servidor "${ROOT}/ServidorCentral/backend/db/schema.sql"
        ;;
    *)
        echo "Uso: $0 [loja|servidor]   (sem argumentos = ambas)" >&2
        exit 1
        ;;
esac

echo "Done."
