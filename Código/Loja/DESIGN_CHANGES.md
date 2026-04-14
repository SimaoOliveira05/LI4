# DESIGN_CHANGES.md

Registo de desvios entre o **design original** (diagramas UML, diagrama de componentes, especificação) e a **implementação**. Cada entrada aqui representa algo que deve, mais tarde, ser refletido nos artefactos de design para que estes voltem a estar coerentes com o código.

**Não registar aqui**: correções de bugs, ajustes de CSS, alterações a queries SQL, refactors internos, ou qualquer coisa que não altere o modelo de domínio / contratos de serviço / arquitetura.

**Formato de cada entrada**:

```
## YYYY-MM-DD — Título curto
**Área**: Domínio | Serviço | Repositório | Arquitetura | UI-Contract
**O quê**: descrição sucinta do que foi adicionado/removido/alterado.
**Porquê**: motivação (gap do design, requisito implícito, incoerência detetada, etc.).
**Impacto no design**: que diagramas/documentos precisam de ser atualizados.
```

---

## 2026-04-13 — Operações de sincronização acrescentadas aos repositórios

**Área**: Repositório
**O quê**: Adicionado `atualizarSincronizacao(int id, EstadoSincronizacao estado)` nas interfaces `VendaRepositorio`, `DevolucaoRepositorio` e `RemessaRepositorio` (e respetivas implementações).
**Porquê**: O `FechoDiaServico` precisa de marcar registos como `EM_TRANSITO` antes de enviar e como `CONFIRMADO` após resposta do servidor. O design original não previa este método explicitamente.
**Impacto no design**: Diagrama de classes do pacote `repositorio.interfaces` — adicionar a operação nas três interfaces em causa.

---

## 2026-04-13 — Persistência de sangrias no repositório de sessão de caixa

**Área**: Repositório
**O quê**: Adicionado `guardarSangria(Sangria sangria)` a `SessaoCaixaRepositorio`. Insere na tabela `Sangria` e persiste os `DetalheNumerario` associados à sangria.
**Porquê**: As sangrias são parte da sessão de caixa mas têm o seu próprio ciclo de vida; o design não tinha operação dedicada para as persistir.
**Impacto no design**: Diagrama de classes do pacote `repositorio.interfaces` e, eventualmente, revisitar se `Sangria` deveria ter o seu próprio repositório.

---

## 2026-04-13 — Consulta de sessão ativa no serviço de caixa

**Área**: Serviço
**O quê**: Adicionado `buscarSessaoAtiva(int idUtilizador)` a `ICaixaServico`.
**Porquê**: `VendaController` precisa de consultar a sessão ativa do utilizador (para verificar limite de caixa); `CaixaController` também o usa para refrescar o estado da UI.
**Impacto no design**: Diagrama de classes de `servico.interfaces` — adicionar a operação.

---

## 2026-04-13 — Consulta de stock total por produto

**Área**: Repositório / Contrato UI
**O quê**: Adicionado `Map<Integer,Integer> stockPorProduto()` a `LoteRepositorio`. A coluna "Stock" do catálogo passou a mostrar o somatório das quantidades dos lotes (em vez de `stockMinimo`, que era o valor errado).
**Porquê**: A UI do catálogo precisava de mostrar o stock atual (soma dos lotes). O design/dominio não expunha essa agregação.
**Impacto no design**: Diagrama de classes de `LoteRepositorio` — adicionar a operação de agregação.

---

## 2026-04-13 — Limpeza de sessões órfãs no arranque

**Área**: Repositório / Arquitetura
**O quê**: Adicionado `limparSessoesAtivas()` a `UtilizadorRepositorio`; invocado pelo `AppContext` no arranque para colocar todos os utilizadores fora de sessão.
**Porquê**: Se a aplicação for encerrada sem logout (kill, crash, fechar janela), o flag `emSessao` fica a `1` e o utilizador não consegue voltar a entrar (erro "Sessão Duplicada"). A recuperação é análoga à reversão de `EM_TRANSITO → PENDENTE` para sincronização.
**Impacto no design**: Diagrama de classes de `UtilizadorRepositorio` e — talvez — a especificação deveria documentar a responsabilidade "recuperação de sessões órfãs" como função do composition root.

---

## 2026-04-14 — Criação de pedidos de remessa exposta na UI

**Área**: UI-Contract (não altera modelo de domínio, mas altera fluxo esperado)
**O quê**: Adicionado botão "Novo Pedido" no tab "Pedidos de Remessa" e o respetivo diálogo em `RemessaController.onNovoPedido()`.
**Porquê**: O caso de uso "Gestor cria pedido de remessa a fornecedor" existia no design mas não tinha ponto de entrada na UI — o serviço `criarPedidoRemessa` estava implementado mas inacessível.
**Impacto no design**: Diagrama de sequência do caso de uso "Criar Pedido de Remessa" — confirmar que o ator inicia a partir do ecrã de Remessas. Sem alteração de classes.

---

## 2026-04-14 — Detalhes de lotes no catálogo expostos em novos métodos

**Área**: UI-Contract
**O quê**: Adicionados `CatalogoController.onVerDetalhesProduto()` e `CatalogoController.mostrarDetalhesLotes(Produto, List<Lote>)` para exibir um diálogo com os lotes FEFO do produto seleccionado.
**Porquê**: A vista do catálogo precisava de um ponto de entrada para inspecionar os lotes de um produto sem sair do ecrã principal.
**Impacto no design**: Diagrama de sequência do catálogo de produtos para o fluxo "Ver detalhes de produto"; actualizar o diagrama de classes de apresentação para incluir os novos métodos do controlador.

---

## 2026-04-14 — Relação Fornecedor ↔ Produto (catálogo do fornecedor)

**Área**: Domínio / Serviço / Repositório / UI-Contract
**O quê**:
- Nova entidade associativa `FornecedorProduto` (N:N) com atributos `idFornecedor`, `idProduto`, `precoFornecedor`. Nova tabela homónima na BD com chave composta e `ON DELETE CASCADE` para `Fornecedor` e `Produto`.
- Nova exceção `ProdutoNaoFornecidoException`.
- Novo repositório `FornecedorProdutoRepositorio` (associar, desassociar, atualizar preço, listar produtos do fornecedor, verificar fornece, obter preço).
- Novo serviço `IFornecedorServico` / `FornecedorServico` com CRUD de fornecedores e gestão do seu catálogo (restrito a GESTOR/CEO).
- `RemessaServico.registarRemessa` e `criarPedidoRemessa` passam a validar que cada linha corresponde a um produto do catálogo do fornecedor; caso contrário lançam `ProdutoNaoFornecidoException`.
- Nova vista/controlador `FornecedorView.fxml` + `FornecedorController` com duas zonas: lista de fornecedores (CRUD) e catálogo do fornecedor seleccionado (associar produtos, ajustar preço, remover).
- Novo item de menu "Fornecedores" em `MainView` (visível a GESTOR/CEO).
- No fluxo "Novo Pedido" de `RemessaController`, a grelha de produtos passa a ser filtrada pelo fornecedor seleccionado (mostra apenas os produtos do seu catálogo).
- No diálogo "Registar Chegada" há agora uma label "Sugerido: X,XX €" que soma `precoFornecedor × qtdRecebida` para ajudar o utilizador a validar o valor da guia; o valor continua introduzido manualmente.

**Porquê**: O modelo original permitia que qualquer fornecedor fornecesse qualquer produto, o que não reflecte a realidade de uma loja (o fornecedor de leite não traz arroz) e permitia gerar pedidos inválidos. A validação explícita evita erros humanos e dá base para, no futuro, automatizar o cálculo do `valorTotalGuia`.

**Impacto no design**:
- **Diagrama de classes de domínio**: adicionar `FornecedorProduto` e a associação N:N entre `Fornecedor` e `Produto`. Adicionar exceção `ProdutoNaoFornecidoException`.
- **Diagrama de classes de repositórios**: adicionar `FornecedorProdutoRepositorio`.
- **Diagrama de classes de serviços**: adicionar `IFornecedorServico` e actualizar assinatura do construtor de `RemessaServico` (nova dependência).
- **Diagrama de casos de uso**: novo CU "Gerir catálogo do fornecedor" (actor: GESTOR/CEO). Ajustar o CU "Criar pedido de remessa" e "Registar chegada" para incluir validação contra catálogo.
- **Diagrama de componentes**: a vista Fornecedor é um novo elemento do módulo de apresentação, já previsto como componente.
