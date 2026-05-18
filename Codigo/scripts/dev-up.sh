#!/usr/bin/env bash
# Arranca backend (Javalin :8080) e frontend (Vite :5173) em paralelo.
# Ctrl-C mata ambos.

set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
BACKEND_DIR="${ROOT}/Servidor_Central/backend"
FRONTEND_DIR="${ROOT}/Servidor_Central/frontend"

PIDS=()
cleanup() {
    echo
    echo "==> A parar processos..."
    for pid in "${PIDS[@]}"; do
        kill -TERM "$pid" 2>/dev/null || true
    done
    wait 2>/dev/null || true
}
trap cleanup INT TERM EXIT

prefix() {
    local tag="$1"
    sed -u "s/^/[${tag}] /"
}

echo "==> A libertar porta 8080 (se ocupada)..."
fuser -k 8080/tcp 2>/dev/null || true

echo "==> Backend  (mvn compile + exec:java) em ${BACKEND_DIR}"
( cd "$BACKEND_DIR" && mvn -q compile && mvn -q exec:java 2>&1 | prefix "backend" ) &
PIDS+=($!)

echo "==> Frontend (npm run dev) em ${FRONTEND_DIR}"
if [ ! -d "${FRONTEND_DIR}/node_modules" ]; then
    echo "    a instalar dependências..."
    ( cd "$FRONTEND_DIR" && npm install ) | prefix "frontend"
fi
( cd "$FRONTEND_DIR" && npm run dev 2>&1 | prefix "frontend" ) &
PIDS+=($!)

echo
echo "Backend:  http://localhost:8080"
echo "Frontend: http://localhost:5173"
echo "Ctrl-C para parar ambos."
echo

wait
