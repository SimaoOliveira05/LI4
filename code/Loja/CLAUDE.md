# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**TrasmUM — Software de Loja** is a JavaFX desktop POS (point-of-sale) application for a convenience store terminal. It connects to a local MySQL database and periodically synchronises data with a remote server via HTTPS.

- Java 21, JavaFX 21, Maven
- MySQL database (local)
- Remote server synchronisation via HTTPS (`/fecho`, `/ping` endpoints)
- Portuguese-language codebase (class names, variable names, domain terms are all in PT)

## Common Commands

```bash
# Build
mvn clean package

# Run the application
mvn javafx:run

# Compile only (no packaging)
mvn compile
```

There are no automated tests in this project.

## Architecture

The application follows a clean layered architecture:

```
apresentacao/controllers/   ← JavaFX controllers (UI layer)
servico/interfaces+impl/    ← Business logic (service layer)
repositorio/interfaces+impl/← Data access (repository layer, raw JDBC)
dominio/                    ← Domain model (entities, enums, exceptions)
app/                        ← Composition root and infrastructure
sincronizacao/              ← HTTP gateway to remote server
```

### Composition Root

`AppContext` (`app/AppContext.java`) is the **singleton composition root** — it manually instantiates all repositories, services, and the sync gateway, then injects dependencies. There is no DI framework. All controllers obtain services by calling `AppContext.getInstance()`.

### Navigation

`Navigator` (`app/Navigator.java`) handles all scene transitions. The main layout is a `BorderPane`; internal navigation replaces the center pane via `navegarParaCentro(fxmlPath)`. Login/logout replace the entire scene.

### Configuration

`config.properties` (in `src/main/resources/`) holds all runtime configuration:
- `loja.id`, `loja.nome`, `loja.limiteMaximoCaixa` — terminal identity
- `servidor.url` — remote server base URL (default `https://localhost:8443`)
- `servidor.truststore.path/password` — optional JKS truststore for TLS; if blank, SSL verification is disabled (dev mode)
- `db.url`, `db.user`, `db.password` — MySQL connection

`ConfiguracaoTerminal` loads this file and is injected into services that need store identity or server URL.

### User Roles & Authorisation

Three roles defined in `PerfilUtilizador`: `FUNCIONARIO`, `GESTOR`, `CEO`.  
`AutorizacaoServico.exigirPerfil(utilizador, perfis...)` throws `AcessoNegadoException` if the user lacks the required role. Controllers retrieve the logged-in user from `AppContext.getInstance().getUtilizadorAtual()`.

On startup, `AppContext` auto-creates an `admin/admin123` (CEO) account if none exists.

### Synchronisation / Fecho de Dia

`FechoDiaServico` aggregates all daily data (vendas, devoluções, remessas, pagamentos, logs, sessão de caixa) into a `PacoteFechoDTO`, computes a SHA-256 integrity hash, and POSTs it to the server via `HttpSincronizacaoGateway`. Records that have been sent are marked `EM_TRANSITO`; on a successful response they become `CONFIRMADO`. On startup, any `EM_TRANSITO` records are rolled back to `PENDENTE` to recover from interrupted sends.

### Database

Schema is in `src/main/resources/schema.sql`. Seed data in `seed.sql` and `mock_data.sql`. Apply manually to the MySQL instance configured in `config.properties`. All DB access uses raw JDBC — no ORM.

### FXML Views

Each screen has a matching `*View.fxml` in `src/main/resources/fxml/` and a `*Controller.java` in `apresentacao/controllers/`. Controllers are instantiated by the `FXMLLoader` and must call `AppContext.getInstance()` themselves to access services.

## Documenting Design Deviations

This project has a pre-defined design (domain model, class diagrams, component diagrams produced in the design phase). When implementation reveals a gap, inconsistency, or missing relationship in that design — **any change that adds, removes or alters domain classes, attributes, relationships, enums, service contracts, or architectural layers** — it must be recorded in `DESIGN_CHANGES.md` at the project root.

**Mandatory every time** such a change is made:
1. Append a new entry to `DESIGN_CHANGES.md` with date, area affected, what was changed, and why.
2. Do not silently diverge from the design artifacts; the goal is to keep a running log so the design documents (UML, component diagrams, specification) can be reconciled later.

Purely implementation-level changes (bug fixes, UI tweaks, CSS, SQL query adjustments, refactors that do not change the public model) do **not** need to be logged — only deviations that the design documents would need to reflect.
