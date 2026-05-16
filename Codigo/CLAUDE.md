# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Repository Layout

This is the LI4 group project — a convenience-store system with three runnable subprojects:

- **`Loja/`** — JavaFX desktop POS (Java 21, Maven, MySQL `trasmum_loja`). Runs on the store terminal. Has its own `CLAUDE.md` with detailed architecture notes — read it before working in that tree.
- **`Servidor_Central/backend/`** — Javalin 6 REST server (Java 21, Maven, MySQL `trasmum_servidor`). Listens on `:8080`. Receives daily-closing packets from each `Loja`.
- **`Servidor_Central/frontend/`** — Vue 3 + TypeScript + Vite SPA (analytics / monitoring dashboard). Talks to the backend over HTTP via `src/api/http.ts`.

`Loja` POSTs daily closings (`/fecho`, `/ping`) over HTTPS to the backend (`https://localhost:8443` by default, configurable in `Loja/src/main/resources/config.properties`). Treat each subproject as an independent codebase for build/test purposes.

## Common Commands

Convenience scripts in `scripts/`:

```bash
scripts/db-reset.sh            # drop + recreate both DBs (or: loja | servidor)
scripts/dev-up.sh              # backend (:8080) + frontend (:5173) in parallel
scripts/loja-up.sh             # JavaFX POS terminal
```

`db-reset.sh` honours `DB_USER` / `DB_PASS` / `DB_HOST` (defaults `trasmum/trasmum/localhost`).

```bash
# Loja (run from Loja/)
mvn javafx:run                 # run
mvn clean package              # build
mvn compile                    # compile only

# Backend (run from Servidor_Central/backend/)
mvn compile && mvn exec:java   # listens on :8080

# Frontend (run from Servidor_Central/frontend/)
npm install
npm run dev                    # vite dev server (:5173)
npm run build                  # vue-tsc type-check + vite build
npm run preview
```

Full stack via Docker (from `Servidor_Central/`):

```bash
docker compose up --build      # MySQL :3307, backend :8080, frontend :3000
```

## Tests

Both Maven subprojects have unit tests using **JUnit 5 + Mockito**. No database or running server needed — all dependencies are mocked.

```bash
# Loja (4 test classes: Autorizacao, Devolucao, FechoDia, Venda)
cd Loja && mvn test

# Backend (2 test classes: AutenticacaoCEO, Ingestao)
cd Servidor_Central/backend && mvn test

# Run a single test class
mvn test -Dtest=NomeDaClasse

# Run a single test method
mvn test -Dtest=NomeDaClasse#nomeDoMetodo
```

The frontend has no automated tests.

Default dashboard login: `admin` / `admin123` (deleted on the first real CEO login).

## Servidor_Central — backend (high level)

Javalin server under `pt.trasmum.servidor`, layered:

```
app/                ← entry point, wiring
handler/            ← Javalin route handlers
servico/{interfaces,impl}/   ← business logic
repositorio/{interfaces,impl}/ ← raw JDBC
dominio/            ← entities/enums
dto/                ← API DTOs (dto/api) and Loja ingestion DTOs (dto/ingestao)
infra/              ← cross-cutting infra (DB, config, etc.)
```

Auth uses jBCrypt; JSON via Gson. Schema is in `backend/db/schema.sql`.

## Servidor_Central — frontend (high level)

- Vue 3 `<script setup>` SFCs, Pinia (`src/stores/`) for shared state, Vue Router (`src/router/index.ts`) for navigation between dashboard views in `src/views/`.
- UI uses Tailwind CSS with shadcn-style primitives via `radix-vue` / `reka-ui`; `components.json` configures the shadcn generator. `src/lib/` holds `cn()` and other utilities.
- Charts: `chart.js` + `vue-chartjs`.
- Real backend integration: `src/api/http.ts` is a thin `fetch` wrapper; `networkService.ts` and `authService.ts` call backend endpoints. `VITE_API_URL` (build/dev arg) configures the base URL.

## Loja architecture

See `Loja/CLAUDE.md` — covers the layered architecture (`apresentacao` / `servico` / `repositorio` / `dominio`), the `AppContext` composition root, `Navigator`-based scene transitions, role-based authorisation (`FUNCIONARIO` / `GESTOR` / `CEO`), the fecho-de-dia synchronisation protocol, and the raw-JDBC data layer.

## Documenting Design Deviations

`Loja/` and `Servidor_Central/backend/` are built against pre-defined design artifacts (UML, component diagrams in the repo root). Any change that adds, removes, or alters domain classes, attributes, relationships, enums, service contracts, or architectural layers must be appended to:

- `Loja/DESIGN_CHANGES_LOJA.md` for changes inside `Loja/`
- `Servidor_Central/DESIGN_CHANGES_SERVER.md` for backend changes

Pure implementation tweaks (bug fixes, UI/CSS, SQL adjustments, internal refactors) do not need to be logged. The frontend has no equivalent design log.

## Language

`Loja/` and the backend are written in Portuguese — class names, variables, and domain terms (`Venda`, `Caixa`, `FechoDia`, `Utilizador`, `Loja`). Follow that convention when extending them. The frontend is in English.
