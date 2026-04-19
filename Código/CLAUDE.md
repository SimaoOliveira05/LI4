# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Repository Layout

This is the LI4 group project — a two-tier convenience-store system with two independent subprojects:

- **`Loja/`** — JavaFX desktop POS application (Java 21, Maven, MySQL). Runs on the store terminal; has its own `CLAUDE.md` with detailed architecture notes — read it before working in that tree.
- **`ServidorCentral/`** — Vue 3 + TypeScript + Vite SPA (analytics / monitoring dashboard for the central server side). Currently consumes mock data from `src/api/mockData.ts` via `src/api/networkService.ts`; no real backend wired in yet.

The two subprojects are designed to interact at runtime: `Loja` POSTs daily closing packets (`/fecho`, `/ping`) over HTTPS to a central server (`https://localhost:8443` by default), and `ServidorCentral` is the planned dashboard for that server. Treat them as separate codebases for build/test purposes.

## Common Commands

`Loja/` (run from inside `Loja/`):
```bash
mvn clean package      # build
mvn javafx:run         # run the desktop app
mvn compile            # compile only
```
No automated tests in `Loja/`.

`ServidorCentral/` (run from inside `ServidorCentral/`):
```bash
npm install
npm run dev            # vite dev server
npm run build          # vue-tsc type-check + vite build
npm run preview        # preview built bundle
```

## ServidorCentral Architecture (high level)

- Vue 3 `<script setup>` SFCs, Pinia (`src/stores/network.ts`) for shared state, Vue Router (`src/router/index.ts`) for navigation between dashboard views in `src/views/`.
- UI uses Tailwind CSS with shadcn-style primitives via `radix-vue` / `reka-ui`; `components.json` configures the shadcn generator. `src/lib/` holds `cn()` and other utilities.
- Charts: `chart.js` + `vue-chartjs`.
- All data currently comes from `src/api/mockData.ts`; `networkService.ts` simulates async fetches with `setTimeout`. Replace these with real HTTP calls when the backend is connected.

## Loja Architecture

See `Loja/CLAUDE.md` — covers the layered architecture (`apresentacao` / `servico` / `repositorio` / `dominio`), the `AppContext` composition root, `Navigator`-based scene transitions, role-based authorisation (`FUNCIONARIO` / `GESTOR` / `CEO`), the fecho-de-dia synchronisation protocol, and the raw-JDBC data layer.

## Documenting Design Deviations

`Loja/` is built against pre-defined design artifacts (UML, component diagrams). Any change that adds, removes, or alters domain classes, attributes, relationships, enums, service contracts, or architectural layers must be appended to `Loja/DESIGN_CHANGES.md` (date, area, what, why). Pure implementation tweaks (bug fixes, UI/CSS, SQL adjustments, internal refactors) do not need to be logged. This convention applies inside `Loja/`; `ServidorCentral/` has no equivalent design log.

## Language

The `Loja/` codebase is in Portuguese — class names, variables, and domain terms (e.g. `Venda`, `Caixa`, `FechoDia`, `Utilizador`). Follow that convention when extending it. `ServidorCentral/` is in English.
