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

## 2026-04-20 — Suporte a produtos sem data de validade

**Área**: Domínio | Repositório | UI-Contract

**O quê**:
- `Produto` passa a ter o atributo `boolean temValidade` (default `true`). Construtor adicional `(codigoBarras, nome, categoria, precoBase, stockMinimo, temValidade)`.
- Schema: `Produto.temValidade BIT NOT NULL DEFAULT 1`; `Lote.dataValidade` e `LinhaRemessa.dataValidade` passaram de `NOT NULL` para `NULL`.
- `ProdutoDTO` (record do serviço) ganhou o campo `temValidade`.
- `CatalogoController`: diálogo Criar/Editar produto tem agora checkbox "Tem data de validade".
- `RemessaController`: no ecrã de "Registar Chegada", o `DatePicker` da linha é desativado quando o produto não tem validade; a `LinhaRemessa` é gravada com `dataValidade = null`.
- Repositórios (`ProdutoRepositorioImpl`, `LoteRepositorioImpl`, `RemessaRepositorioImpl`) escrevem/lêem `dataValidade` de forma null-safe (via `setNull`/`rs.getDate` com verificação).
- FEFO (`buscarLotesFEFO`): a ordenação passa a `ORDER BY dataValidade IS NULL, dataValidade ASC` — lotes sem validade ficam no fim, para que lotes com validade sejam consumidos primeiro.
- Alertas de validade (`Catálogo`): a query já filtra `dataValidade <= ...`, pelo que lotes sem validade nunca geram alertas (comportamento pretendido).

**Porquê**: nem todos os artigos da loja têm prazo de validade (ex: pilhas, produtos não-perecíveis). Obrigar a introduzir uma data artificial corrompia os lotes e gerava alertas falsos.

**Impacto no design**:
- Diagrama de classes: `Produto` ganha `temValidade:boolean`; nos diagramas de DB, `Lote.dataValidade` e `LinhaRemessa.dataValidade` deixam de ser obrigatórios.
- `Devolucao.dataValidadeEmbalagem` mantém-se `NOT NULL` por agora — devolver um produto sem validade não foi reconsiderado nesta alteração. Caso seja relevante, fica como trabalho futuro (tornar nullable e adaptar o fluxo de devolução no `VendaController`).

---

## 2026-04-20 — Consolidação do menu de navegação do gestor

**Área**: UI-Contract

**O quê**: Reorganização da barra lateral (`MainView`) para reduzir a carga cognitiva do perfil GESTOR/CEO. O menu passa de 9 → 7 itens:
- Novas vistas agregadoras: `AprovisionamentoView` (TabPane com Remessas + Fornecedores) e `AdministracaoView` (TabPane com Utilizadores + Auditoria). Ambas reutilizam as vistas e controladores existentes via `<fx:include>`; não foram introduzidos novos controladores.
- Itens do menu do gestor: Venda, Catálogo, Caixa, Aprovisionamento, Pagamentos, Administração, Fecho de Dia.
- Itens do menu do funcionário: Venda, Catálogo, Caixa (inalterado).

**Porquê**: O menu tornou-se denso com 9 opções para o gestor, muitas das quais representam funcionalidades relacionadas (remessas ↔ fornecedores; utilizadores ↔ auditoria).

**Impacto no design**: o diagrama de componentes/UI da camada de apresentação deve refletir as duas vistas agregadoras `AprovisionamentoView` / `AdministracaoView` como compostas pelas vistas originais. Não há alterações de domínio, serviços ou repositórios.

Adicionalmente: o registo de chegada de remessa deixou de ser um `Dialog` modal — `RemessaView` ganhou um terceiro painel interno (`painelRegistarChegada`), alinhado com o padrão já existente em "Novo Pedido". O fluxo passa a alternar painéis (gestão ↔ novo pedido ↔ registar chegada) sem pop-ups.

---

## 2026-04-19 — `PacoteFechoDTO` reescrito para o contrato do servidor central

**Área**: UI-Contract | Serviço

**O quê**: Os DTOs em `pt.trasmum.loja.sincronizacao.PacoteFechoDTO` (e o agregador em `FechoDiaServico`) passaram a produzir o payload denormalizado esperado pelo servidor central:
- `VendaDTO`: agora apenas `idOriginalLoja, dataHora, totalFaturado, metodoPagamento, linhas`. Removidos `id, idLoja, idUtilizador, estado, numeroFatura, nifCliente`.
- `LinhaVendaDTO`: passou a `idOriginalLoja, nomeProduto, categoria, quantidade, precoUnitario, subtotal`. O nome e categoria são resolvidos a partir de `Lote → Produto`.
- `RemessaDTO`: `idOriginalLoja, nomeFornecedor, dataRecepcao, valorTotalGuia, estadoPagamento`. `nomeFornecedor` é resolvido via `FornecedorRepositorio`. `estadoPagamento` é derivado dos `Pagamento` confirmados (`PAGA` se há pagamento `PAGO` para a remessa, caso contrário `PENDENTE_PAGAMENTO`). Linhas de remessa deixaram de ser enviadas.
- `DevolucaoDTO`: `idOriginalLoja, idOriginalVenda, dataHora, quantidade, valorRestituido`. `idOriginalVenda` mapeia para o `idFatura` local.
- `PagamentoDTO`: `idOriginalLoja, idOriginalRemessa, valor, dataPagamento, tipoPagamento`. `tipoPagamento` é enviado a `null` (não há esse atributo no domínio Loja).
- `SessaoCaixaDTO`: `idOriginalLoja, idUtilizador, saldoFinal, dataAbertura, dataEncerramento`. `saldoFinal` mapeia para `SessaoCaixa.saldoAtual`.
- `LogAuditoriaDTO`: `idOriginalLoja, acao, dataHora, nomeUtilizador`. Nome resolvido via `UtilizadorRepositorio`.
- `dataFecho` no pacote passou de `LocalDate` a `String` (ISO).

**Porquê**: O servidor central usa um modelo de analytics denormalizado (não tem as tabelas de `Produto`, `Fornecedor`, `Utilizador` da loja, e não as quer ter). Receber IDs locais seria inútil — precisa dos nomes/categorias resolvidos no envio. Esta era a divergência registada como pendente em `ServidorCentral/DESIGN_CHANGES_SERVER.md` (entrada de 2026-04-19 sobre incompatibilidade de DTOs); a presente entrada fecha esse pendente.

**Impacto no design**: O diagrama de componentes / contrato de sincronização precisa de refletir o novo payload. `FechoDiaServico` ganha quatro novas dependências de repositório (`ProdutoRepositorio`, `LoteRepositorio`, `FornecedorRepositorio`, `UtilizadorRepositorio`) usadas apenas para denormalização no envio.

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

---

## 2026-04-15 — Ecrã de Devoluções eliminado; funcionalidade integrada em Venda

**Área**: UI-Contract / Arquitetura
**O quê**: Removida a vista/controlador independente `DevolucaoView.fxml` / `DevolucaoController`. A lógica de devolução foi integrada em `VendaController` e `VendaView.fxml` como um segundo separador ("Devoluções") dentro de um `TabPane`. O botão "Devolução" do menu lateral de `MainView` foi eliminado.
**Porquê**: O caso de uso de devolução está intimamente ligado ao contexto de venda (pesquisa por número de fatura, devolução de artigos de uma venda anterior). Ter uma página separada para uma função acessória fragmentava desnecessariamente a navegação e aumentava a superfície de manutenção sem benefício funcional.
**Impacto no design**: Diagrama de componentes — remover o componente `DevolucaoView`/`DevolucaoController` como elemento autónomo; representar as devoluções como parte do componente de Venda. Diagrama de casos de uso / navegação — o CU "Efetuar Devolução" deixa de ter ponto de entrada próprio no menu e passa a ser acedido a partir do ecrã de Venda.

---

## 2026-04-15 — Operação de abate de lote

**Área**: Serviço / UI-Contract
**O quê**: Adicionada operação "Abater lote" acessível a partir da tabela de lotes no painel de detalhes do catálogo. Cada linha de lote passa a ter um botão "Abater" que permite reduzir a quantidade do lote (total ou parcialmente). Se a quantidade chegar a zero o lote deixa de aparecer em vendas (`buscarLotesFEFO` já filtra `quantidade > 0`). A operação é restrita a GESTOR/CEO, registada em auditoria, e os alertas de validade são recarregados após o abate.
**Porquê**: O sistema não tinha forma de registar a retirada física de lotes danificados ou expirados — o stock ficava inflacionado mesmo quando os produtos eram removidos da prateleira. Necessário para manter a integridade do stock e para dar seguimento aos alertas de "fora de validade".
**Impacto no design**: Diagrama de classes de serviços — adicionar operação `abaterLote(Utilizador, int idLote, int quantidade)` a `ICatalogoServico`. Diagrama de sequência do catálogo — novo fluxo "Abater lote". Diagrama de casos de uso — novo CU "Abater lote" (actor: GESTOR/CEO).

---

## 2026-04-17 — Registo de auditoria para todas as ações críticas

**Área**: Domínio / Serviço / Repositório
**O quê**: Adicionado registo de auditoria para todas as ações críticas do sistema: vendas, devoluções, alterações de preços, aplicação de descontos, gestão de utilizadores, alterações ao catálogo local e fecho de dia. O enum `TipoAcao` já continha os tipos necessários (`VENDA`, `DEVOLUCAO`, `ALTERACAO_PRECO`, `APLICACAO_DESCONTO`, `GESTAO_UTILIZADOR`, `ALTERACAO_CATALOGO`, `FECHO_DIA`). O `FechoDiaServico.executarFecho()` passou a registar auditoria após fecho bem-sucedido.
**Porquê**: O requisito "Todas as ações críticas devem ser registadas em logs de auditoria" exigia garantir que cada operação crítica gerasse um registo. O código já registava a maioria das ações; faltava apenas o fecho de dia.
**Impacto no design**: Diagrama de classes de domínio — confirmar que `LogAuditoria` e `TipoAcao` cobrem todas as ações. Diagrama de sequência do fecho de dia — adicionar passo de registo de auditoria.

---

## 2026-04-17 — View e Controller de Logs de Auditoria

**Área**: UI-Contract / Repositório / Serviço
**O quê**: 
- Novos métodos no repositório `LogAuditoriaRepositorio`: `buscarTodos()` e `buscarPorTipo(TipoAcao tipo)` para suporte à UI.
- Novos métodos no serviço `IAuditoriaServico` / `AuditoriaServico`: `obterTodosLogs()` e `obterLogsPorTipo(TipoAcao tipo)`.
- Nova vista `LogAuditoriaView.fxml` com `TableView` mostrando colunas: Data/Hora, Tipo, Entidade, ID Entidade, ID Utilizador, Estado.
- Novo controlador `LogAuditoriaController` com filtro por tipo de ação (ComboBox) e botão Atualizar.
- Novo item de menu "Auditoria" em `MainView.fxml` (visível apenas para GESTOR/CEO).
**Porquê**: O sistema precisava de uma interface para visualização dos logs de auditoria, permitindo aos gestores verificar o histórico de operações. A ordenação é automática do mais recente para o mais antigo via `ORDER BY dataHora DESC`.
**Impacto no design**: Diagrama de classes de repositórios — adicionar `buscarTodos()` e `buscarPorTipo()` a `LogAuditoriaRepositorio`. Diagrama de classes de serviços — adicionar métodos correspondentes a `IAuditoriaServico`. Diagrama de componentes — novo componente `LogAuditoriaView`/`LogAuditoriaController`. Diagrama de casos de uso — novo CU "Consultar logs de auditoria" (actor: GESTOR/CEO).

---

## 2026-04-18 — Filtro por categoria nas views de Venda e Catálogo

**Área**: UI-Contract
**O quê**: 
- Na view de Venda: adicionado `ComboBox` ao lado da search bar para filtrar produtos por categoria. O filtro combina pesquisa por texto + categoria selecionada.
- Na view de Catálogo: adicionado `ComboBox` ao lado do botão "Desconto" para filtrar produtos por categoria na tabela.
- Ambos os controladores (`VendaController` e `CatalogoController`) foram alterados para extrair categorias distintas dos produtos ativos e popular o `ComboBox` dinamicamente.
**Porquê**: O requisito de filtrar produtos por categoria melhorou a usabilidade em ambas as views, permitindo aos utilizadores encontrar produtos mais rapidamente.
**Impacto no design**: Diagrama de componentes — atualizar `VendaView` e `CatalogoView` para incluir o elemento de filtro. Sem alteração ao modelo de domínio ou serviços.
