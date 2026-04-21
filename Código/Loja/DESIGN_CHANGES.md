# DESIGN_CHANGES.md

Registo de desvios relativamente aos artefactos de design originais (UML, diagramas de componentes, especificação).

---

## 2026-04-21 — Fatura PDF: dados da loja, agrupamento por categoria e logótipo

**Área:** `apresentacao/GeradorFaturaPdf`, `dominio/core/ConfiguracaoTerminal`, `config.properties`

**O que mudou:**

- `ConfiguracaoTerminal` passou a carregar e expor quatro novos campos de identidade da loja: `morada`, `localidade`, `nif` e `email` (lidos de `config.properties`; fallback para string vazia se ausentes).
- `GeradorFaturaPdf` foi actualizado para:
  - Apresentar o logótipo da loja (`images/logo.png`) no topo da fatura, centrado e escalado para 80 × 80 px.
  - Mostrar morada, localidade, NIF e e-mail da loja no cabeçalho, abaixo do nome.
  - Agrupar os artigos por categoria do produto (campo `Produto.categoria`) em vez de os listar numa tabela plana; cada grupo tem um cabeçalho de secção com o nome da categoria; produtos sem categoria caem em "Outros".
- `config.properties` recebeu as propriedades `loja.morada`, `loja.localidade`, `loja.nif` e `loja.email`.

**Porquê:**

O levantamento de requisitos identificou que a fatura real da TrasmUM inclui morada, localidade, NIF e e-mail da loja, bem como logótipo, e que os artigos devem ser apresentados divididos por secção/categoria (ex.: Mercearia, Talho). O design original não contemplava estes campos nem este agrupamento.
