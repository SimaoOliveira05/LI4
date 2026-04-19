# LI4 — TrasmUM

Sistema de loja de conveniência em duas pontas:

- **`Loja/`** — terminal POS em JavaFX (Java 21 + Maven + MySQL). Ver `Loja/CLAUDE.md`.
- **`ServidorCentral/backend/`** — servidor central de analytics em Javalin (Java 21 + Maven + MySQL).
- **`ServidorCentral/frontend/`** — dashboard de monitorização em Vue 3 + Vite.

Os schemas SQL de referência vivem em `../Dados/dadosLoja.sql` e `../Dados/dadosServer.sql`. Cópias prontas a carregar estão também em `Loja/db/schema.sql` e `ServidorCentral/backend/db/schema.sql`.

## Arrancar

```bash
scripts/db-reset.sh            # repõe ambas as DBs (ou: loja | servidor)
scripts/dev-up.sh              # backend (:8080) + frontend (:5173) em paralelo
scripts/loja-up.sh             # terminal POS (JavaFX)
```

`db-reset.sh` aceita as variáveis `DB_USER`, `DB_PASS`, `DB_HOST` (defaults `trasmum/trasmum/localhost`).

Login default no dashboard: `admin` / `admin123` (eliminado após o primeiro login com outra conta CEO).

## Desvios ao design

Cada projeto mantém o seu registo:

- `Loja/DESIGN_CHANGES.md`
- `ServidorCentral/DESIGN_CHANGES_SERVER.md`
