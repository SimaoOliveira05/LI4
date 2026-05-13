# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Repository Layout

This repo is the LI4 group project (TrasmUM — convenience-store software). All source lives under `Codigo/`; the rest of the repo is academic deliverables:

- `Codigo/` — all code. See `Codigo/CLAUDE.md` for build commands and `Codigo/Loja/CLAUDE.md` for POS architecture details.
- `Dados/` — reference SQL dumps (`dadosLoja.sql`, `dadosServer.sql`); copies live next to each subproject.
- `Diagramas_Classe/`, `Diagrama_Componentes/`, `Diagramas_Sequencia/`, `Diagramas_Maquin_Estado/`, `Diagramas_Use_Case/` — UML artefacts produced in the design phase. **Loja and the server backend are implemented against these**; deviations must be logged in `Codigo/Loja/DESIGN_CHANGES_LOJA.md` or `Codigo/Servidor_Central/DESIGN_CHANGES_SERVER.md` (only model/contract/architecture changes — not bug fixes or UI tweaks).
- `Requisitos/` — requirements docs.
- `report.typ` — Typst report; `trasmum_mockup.pen` — Penpot mockup.

## Three runnable subprojects

Inside `Codigo/`:

- `Loja/` — JavaFX 21 desktop POS, Maven, raw JDBC to MySQL `trasmum_loja`. Portuguese codebase. POSTs daily closings (`/fecho`, `/ping`) to the central server over HTTPS.
- `Servidor_Central/backend/` — Javalin 6 REST server, Maven, raw JDBC to MySQL `trasmum_servidor`. Listens on `:8080`. Receives `/fecho` ingestion from each `Loja`.
- `Servidor_Central/frontend/` — Vue 3 + TS + Vite + Tailwind + shadcn-style primitives (`radix-vue`/`reka-ui`); Pinia for state, `chart.js`+`vue-chartjs` for charts. Talks to the backend over HTTP.

## Common Commands

From `Codigo/`:

```bash
scripts/db-reset.sh            # drops + recreates both DBs (or: loja | servidor)
scripts/dev-up.sh              # backend (:8080) + frontend (:5173) in parallel
scripts/loja-up.sh             # JavaFX POS terminal
```

`db-reset.sh` honours `DB_USER`/`DB_PASS`/`DB_HOST` (defaults `trasmum/trasmum/localhost`).

Note: `dev-up.sh` and `loja-up.sh` currently reference `ServidorCentral/` but the actual directory is `Servidor_Central/` — fix the path in the script if you need to use it, or run the subprojects directly:

```bash
# Backend
cd Codigo/Servidor_Central/backend && mvn compile && mvn exec:java
# Frontend
cd Codigo/Servidor_Central/frontend && npm install && npm run dev   # build: npm run build (vue-tsc + vite)
# Loja
cd Codigo/Loja && mvn javafx:run                                    # build: mvn clean package
```

Or via Docker (full stack incl. MySQL on `:3307`):

```bash
cd Codigo/Servidor_Central && docker compose up --build              # backend :8080, frontend :3000
```

There are no automated tests in any subproject.

Default dashboard login: `admin` / `admin123` (deleted after the first real CEO login).

## Cross-project contract

`Loja` aggregates a day's vendas/devoluções/remessas/pagamentos/logs/sessão de caixa into a `PacoteFechoDTO`, hashes it with SHA-256, and POSTs to `servidor.url` (default `https://localhost:8443`, configurable in `Loja/src/main/resources/config.properties`). The server-side ingestion endpoints live under `Servidor_Central/backend/.../handler` and `.../dto/ingestao`. When changing the wire format, update both sides and the DTO classes together.

## Language conventions

`Loja/` and `Servidor_Central/backend/` are Portuguese (class/variable/domain names: `Venda`, `Caixa`, `FechoDia`, `Utilizador`, `Loja`). The frontend is English. Follow the existing convention in each subtree.
