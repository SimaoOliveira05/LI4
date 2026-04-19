#!/usr/bin/env bash
# Arranca o terminal POS (JavaFX).
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "${ROOT}/Loja"
exec mvn javafx:run
