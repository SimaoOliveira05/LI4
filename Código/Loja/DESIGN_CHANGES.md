# DESIGN_CHANGES.md

Registo de desvios relativamente aos artefactos de design originais (UML, diagramas de componentes, especificação).

---

## 2026-04-21 — Logs de auditoria com descrição textual

**Área:** `dominio/core/LogAuditoria`, `repositorio/LogAuditoriaRepositorio`, `servico/IAuditoriaServico`, todos os call sites de `registar()`

**O que mudou:**

- `LogAuditoria` recebeu um campo `descricao` (`String`) que contém uma mensagem legível do que foi feito (ex.: `"Criou utilizador 'joao.silva'"`, `"Alterou preço de 'Leite UHT' de 1,20€ para 1,35€"`).
- A tabela `LogAuditoria` no schema recebeu a coluna `descricao VARCHAR(255)`.
- `IAuditoriaServico.registar()` passou a aceitar `descricao` como parâmetro adicional.
- Todos os call sites nos serviços foram atualizados para passar uma mensagem descritiva contextual.
- O ecrã de logs na Loja foi atualizado para mostrar a descrição em vez do nome do enum `TipoAcao`.
- O campo é incluído no `PacoteFechoDTO` e enviado ao servidor central, que o apresenta no dashboard de loja.

**Porquê:**

Os logs existentes apenas registavam o tipo de ação (`GESTAO_UTILIZADOR`, `ALTERACAO_CATALOGO`, etc.) sem indicar o que concretamente foi feito, tornando-os pouco úteis para auditoria real. O design original não especificava o nível de detalhe do log.

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

---

## 2026-04-26 — Entidade Loja persistida em base de dados local

**Área:** `dominio/core/Loja`, `repositorio/LojaRepositorio`, `servico/ILojaServico`, `AppContext`, `AdministracaoView`, `schema.sql`

**O que mudou:**

- Nova entidade de domínio `Loja` (`id`, `nome`, `morada`, `localidade`, `nif`, `email`, `limiteMaximoCaixa`, `diasAlertaValidade`) adicionada ao pacote `dominio/core`.
- Nova tabela `ConfiguracaoLoja` no schema local (`schema.sql`) que persiste os dados de identidade e os parâmetros operacionais da loja (`limiteMaximoCaixa`, `diasAlertaValidade`).
- Nova interface `LojaRepositorio` e implementação JDBC `LojaRepositorioImpl`.
- Novo serviço `ILojaServico` / `LojaServico` com operações `obter()` e `atualizar()` (requer perfil GESTOR ou CEO); registos de auditoria gerados em cada alteração.
- `AppContext` instancia o repositório e serviço; na primeira execução efetua bootstrap da tabela a partir dos valores de `config.properties` (se a linha ainda não existir); em arranques seguintes, os valores de `limiteMaximoCaixa` e `diasAlertaValidade` provenientes da BD sobrepõem-se aos de `config.properties` na instância em memória de `ConfiguracaoTerminal`.
- `ConfiguracaoTerminal` tornou-se parcialmente mutável: `limiteMaximoCaixa` e `diasAlertaValidade` deixaram de ser `final` e receberam setters, para que alterações via UI tomem efeito imediato sem reiniciar a aplicação.
- Ecrã de Administração ganhou um separador "Loja" com `LojaView.fxml` / `LojaController`, que apresenta os dados atuais e permite guardá-los.

**Porquê:**

Os dados de identidade da loja (nome, morada, NIF, e-mail) e os parâmetros operacionais (limite de caixa, dias de alerta de validade) estavam apenas em `config.properties`, ficheiro de texto sem interface de edição. Criar uma entidade em BD permite editá-los via UI, auditá-los, e garantir que as alterações tomam efeito imediatamente (sem reiniciar a aplicação) e se mantêm entre sessões.
