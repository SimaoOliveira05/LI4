# Registo de Decisões de Design — Servidor Central

Entradas em ordem cronológica inversa (mais recente no topo).

## 2026-05-16 — Especificação OpenAPI servida como ficheiro estático em vez do plugin `javalin-openapi`

**Área**: Backend  
**O quê**: A especificação OpenAPI é servida como ficheiro estático `openapi.yaml` (classpath resource) através de dois endpoints adicionados manualmente em `ServerMain`: `GET /openapi.yaml` (devolve o YAML com `Content-Type: application/yaml; charset=UTF-8`) e `GET /swagger-ui` (devolve HTML com a Swagger UI embutida via CDN). Não foi utilizado o plugin `javalin-openapi` nem a anotação `@OpenApi` nos handlers.  
**Porquê**: O plugin `javalin-openapi` em versão compatível com Javalin 6 requeria dependências de processamento de anotações (`javalin-openapi-plugin`, `swagger-core`) que conflituavam com a versão de Javalin utilizada no projecto e introduziam complexidade desnecessária de configuração de _annotation processors_ no Maven. A abordagem estática é mais simples, previsível e sem dependências adicionais.  
**Impacto no design**: O ficheiro `openapi.yaml` tem de ser mantido manualmente em sincronização com os handlers sempre que a API for alterada. A especificação não é gerada automaticamente a partir das anotações do código.

---

## 2026-05-13 — Endpoint `/api/lojas` adicional (ausente no swagger.txt)

**Área**: Backend  
**O quê**: Acrescentado `GET /api/lojas` que devolve a lista de todas as lojas registadas na rede (`List<Loja>`). Requer autenticação Bearer. Documentado na especificação OpenAPI em `openapi.yaml`.  
**Porquê**: O frontend precisa de popular seletores de loja nos filtros do dashboard, relatórios e remessas. Sem este endpoint seria necessário codificar as lojas no cliente ou reutilizar as respostas de outros endpoints (acoplamento indesejado). Não constava do swagger.txt porque esse documento foi elaborado antes da implementação do frontend.  
**Impacto no design**: Acrescenta um ponto de entrada à API pública. Implementado em `LojaHandler.listar()` usando `LojaRepositorio.listarTodas()`, que já existia para uso interno.

---

## 2026-05-13 — Divergências de URL entre o swagger.txt e a implementação

**Área**: Backend  
**O quê**: Os caminhos dos endpoints implementados divergem dos caminhos descritos no swagger.txt em três aspectos:

| swagger.txt | Implementado | Motivo |
|---|---|---|
| `/login`, `/logout` | `/api/auth/login`, `/api/auth/logout` | Agrupamento sob `/api/auth/` para distinguir claramente da rota de ingestão `/fecho`, que não requer sessão CEO. |
| `/dashboard/metricas` | `/api/dashboard/global` | O nome `global` é mais preciso — a rota agrega toda a rede; `metricas` era ambíguo face à rota por loja. |
| `/dashboard/metricas/{idLoja}` | `/api/dashboard/loja/{idLoja}` | Consistência com o prefixo `/api/dashboard/` e clareza semântica. |
| `/consolidacao/{dataFecho}` (parâmetro de caminho) | `/api/monitor?data=` (parâmetro de query) | A data é um filtro opcional com default para hoje, não um identificador de recurso; query param é mais adequado. O recurso foi renomeado para `monitor` por refletir melhor o caso de uso "monitorização da rede". |

**Porquê**: O swagger.txt foi escrito antes da implementação e os nomes foram revistos durante o desenvolvimento para maior consistência e clareza semântica.  
**Impacto no design**: O Software de Loja usa apenas `/fecho` e `/ping` (não afetados). O frontend já consome os caminhos implementados. A especificação `openapi.yaml` documenta os caminhos reais.

---

## 2026-04-21 — Logs de auditoria com descrição textual

**Área**: Backend + Frontend  
**O quê**: O campo `descricao` foi adicionado a `LogAuditoriaCentral` e à tabela correspondente. O `PacoteFechoDTO` enviado pela Loja passa a incluir este campo por log. O dashboard de loja no frontend passou a exibir a descrição textual no lugar do enum `TipoAcao`, tornando os logs legíveis sem necessidade de interpretação técnica.  
**Porquê**: Os logs recebidos anteriormente mostravam apenas o tipo de ação (ex.: `GESTAO_UTILIZADOR`), sem contexto sobre o que foi feito. A descrição livre permite auditoria real pelo CEO/gestor.  
**Impacto no design**: Acrescenta coluna `descricao VARCHAR(255)` à tabela `LogAuditoriaCentral`; altera `LogAuditoriaCentralRepositorioImpl` (INSERT e mapear); o DTO `LogAuditoriaDTO` dentro do `PacoteFechoDTO` recebe o campo `descricao`; o tipo frontend `DashboardLoja.logsAuditoria` recebe o campo.

---

## 2026-04-21 — Cálculo de despesas e lucro líquido no dashboard

**Área**: Backend + Frontend  
**O quê**: O dashboard global e o dashboard por loja passaram a expor dois novos campos: `totalDespesas` (soma de `valorTotalGuia` das remessas recebidas no período) e `lucroLiquido` (= vendas totais − devoluções − despesas). A tabela de decomposição por loja foi igualmente estendida com `despesas` e `lucro` por loja. No frontend, foram adicionados cards dedicados em ambos os dashboards com coloração dinâmica (verde se ≥ 0, vermelho se negativo).  
**Porquê**: Os dados de remessas já eram recebidos e guardados em `RemessaCentral` mas nunca eram usados analiticamente. A visibilidade do lucro é necessária para suporte à tomada de decisão pelo CEO/gestor.  
**Impacto no design**: Acrescenta métodos de agregação a `RemessaCentralRepositorio` (`totalDespesasPorPeriodo`, `totalDespesasPorLojaEPeriodo`); altera os DTOs `DashboardGlobalDTO`, `DashboardLojaDTO` e `DecomposicaoLojaDTO`; `DashboardServico` passa a receber `RemessaCentralRepositorio` como dependência adicional.

---

## 2026-04-19 — Estrutura do projeto dividida em `frontend/` e `backend/`
**Área**: Infra
**O quê**: A pasta `ServidorCentral/` passou a ter dois subprojectos: `frontend/` (Vue 3 + Vite existente, movido sem alterações estruturais) e `backend/` (Maven + Javalin criado de raiz). O `README.md` original do Vite foi movido para dentro de `frontend/`.
**Porquê**: Manter os dois projetos completamente isolados em termos de build, dependências e arranque, conforme instrução do utilizador.
**Impacto no design**: Nenhum — a organização externa dos artefactos não está fixada no design original.

---

## 2026-04-19 — Assinaturas dos repositórios com overload `Connection`
**Área**: Backend
**O quê**: Cada interface de repositório envolvida na ingestão passou a expor duas variantes de `guardar`/`criarSeNaoExistir`: a descrita no prompt (que abre a sua própria ligação) e uma variante adicional `guardar(Connection conn, Entity)` / `criarSeNaoExistir(Connection conn, Loja)`.
**Porquê**: O prompt exige que todo o processamento de um `PacoteFechoDTO` seja executado numa única transação JDBC. Sem aceitar a `Connection` externa em cada repositório, teria que se abrir uma ligação nova por tabela, quebrando a transação.
**Impacto no design**: Acrescenta métodos às interfaces de repositório. A variante sem `Connection` mantém-se para uso fora de transações agregadas.

---

## 2026-04-19 — Método de verificação de hash do pacote
**Área**: Backend
**O quê**: `IngestaoServico.verificarHash` limpa o campo `hashIntegridade`, serializa o pacote com o mesmo `Gson` usado pelo Loja (serializadores `LocalDate`/`LocalDateTime` via `toString`), calcula SHA-256 hexadecimal e compara com o valor recebido.
**Porquê**: Espelhar exactamente o procedimento em `HttpSincronizacaoGateway` do Loja para que o hash coincida.
**Impacto no design**: Nenhum — implementação interna do contrato já definido.

---

## 2026-04-19 — Tipos do frontend preservados para evitar mexer em componentes
**Área**: UI-Contract
**O quê**: `src/api/mockData.ts` passou a re-exportar apenas os tipos `Store` e `Shipment` a partir de `src/api/types.ts`. `networkService.ts` foi reescrito para chamar a API real e adaptar as respostas do servidor ao formato `Store` / `Shipment` que os componentes já consomem. Campos que o servidor central não produz (`dailySales`, `stockAlerts`) são preenchidos com `0`; `documentNumber` passa a ser `#<id>`.
**Porquê**: O prompt exige que não se altere a estrutura visual nem a lógica dos componentes. Manter os tipos legados com um adaptador garante isso.
**Impacto no design**: Os campos `dailySales` e `stockAlerts` do frontend deixam de ter significado real — são sempre `0`. Se o design do dashboard de monitor vier a precisar destes valores, serão necessários endpoints dedicados.

---

## 2026-04-19 — Módulo `http.ts` com interceptor de autenticação
**Área**: Frontend
**O quê**: Criado `src/api/http.ts` com `request`/`get`/`post` que adicionam automaticamente `Authorization: Bearer <token>` (lido de `localStorage`). Em 401, limpa o token e redireciona para `/login`.
**Porquê**: O prompt pede um interceptor único e persistência do token em `localStorage`.
**Impacto no design**: Nenhum — camada técnica.

---

## 2026-04-19 — `LoginView.vue`, navigation guard e ocultação da sidebar
**Área**: Frontend
**O quê**: Nova rota `/login`, `LoginView.vue` simples (utilizador + palavra-passe, mensagens de erro). Guard `router.beforeEach` redirecciona para `/login` se não houver token, excepto em rotas com `meta.publica`. `App.vue` passa a esconder a `Sidebar` em rotas públicas ou quando não autenticado. O botão "Terminar Sessão" da sidebar está ligado a `authService.logout()` seguido de redirect.
**Porquê**: Conformidade com os requisitos de autenticação do prompt mantendo a estrutura visual existente.
**Impacto no design**: Introduz estado de autenticação no frontend, que não estava descrito no design visual original.

---

## 2026-04-19 — Conta `admin` bootstrap — condição para remoção
**Área**: Backend
**O quê**: `AutenticacaoCEOServico` remove a conta `admin` de bootstrap apenas após um login bem-sucedido com uma conta **diferente** (qualquer conta cujo `nomeUtilizador` não seja `admin`). Enquanto a única conta for a bootstrap, continua a funcionar.
**Porquê**: O prompt diz que a conta deve ser eliminada "após o primeiro login bem-sucedido com uma conta definitiva". Se fosse removida no primeiro login da própria conta `admin`, não seria possível entrar novamente antes de existir outra conta.
**Impacto no design**: Fluxo de onboarding requer uma conta CEO adicional; até lá o `admin/admin123` continua válido.

---

## 2026-04-19 — Endpoint `/ping` adicional
**Área**: Backend
**O quê**: Acrescentado `GET /ping` que devolve `{"ok": true}`.
**Porquê**: O Software de Loja já usa `/ping` no `HttpSincronizacaoGateway` para verificar conectividade antes de enviar o fecho. Não estava listado no prompt, mas é necessário para compatibilidade.
**Impacto no design**: Nenhum — endpoint trivial de health-check.

---

## 2026-04-19 — DTOs de ingestão incompatíveis com os do Software de Loja actual
**Área**: UI-Contract
**O quê**: Os DTOs de ingestão definidos no prompt (`VendaDTO` com `idOriginalLoja`, `nomeProduto`, `categoria`, `subtotal`; `RemessaDTO` com `nomeFornecedor`; etc.) divergem do que `pt.trasmum.loja.sincronizacao.PacoteFechoDTO` envia actualmente no Software de Loja (campos `id`, `idLote`, `idFornecedor`, etc.). O backend foi implementado de acordo com o prompt.
**Porquê**: O prompt é a referência normativa para o servidor. A reconciliação com o Loja fica pendente.
**Impacto no design**: O `HttpSincronizacaoGateway` e os DTOs em `Loja/src/main/java/pt/trasmum/loja/sincronizacao/` precisam de ser actualizados para produzir o payload acima antes de o `/fecho` ser utilizável end-to-end. Recomenda-se adicionar esta tarefa ao `Loja/DESIGN_CHANGES.md`.

---

## 2026-04-19 — `VITE_API_URL` como variável de ambiente opcional
**Área**: Frontend
**O quê**: `http.ts` usa `import.meta.env.VITE_API_URL` se definido, caindo para `http://localhost:8080` por defeito.
**Porquê**: Facilita apontar o frontend para uma instância remota do backend sem recompilar.
**Impacto no design**: Nenhum.
