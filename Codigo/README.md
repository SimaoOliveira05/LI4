# TrasmUM — Sistema de Gestão de Cadeia de Lojas de Conveniência

Sistema composto por dois softwares distintos que comunicam entre si via HTTP:

- **`Loja/`** — terminal de ponto de venda em JavaFX (Java 21 + Maven + MySQL `trasmum_loja`). Corre em cada terminal físico de loja.
- **`Servidor_Central/backend/`** — servidor central em Javalin (Java 21 + Maven + MySQL `trasmum_servidor`). Escuta na porta `:8080` e recebe os pacotes de fecho de dia de cada Loja.
- **`Servidor_Central/frontend/`** — _dashboard_ de monitorização e gestão em Vue 3 + Vite. Comunica com o _backend_ via HTTP.

Os _schemas_ SQL encontram-se em `Loja/src/main/resources/schema.sql` e `Servidor_Central/backend/db/schema.sql`.

---

## Pré-requisitos

| Componente | Versão mínima | Finalidade | Modo |
|---|---|---|---|
| Java (JDK) | 21 LTS | Executar o Software de Loja e o _backend_ local | Ambos |
| Maven | 3.9 | Compilar e executar os projetos Java | Ambos |
| MySQL | 8.0 | Base de dados local (sem Docker) | Local |
| Node.js | 20 LTS | Compilar e executar o _frontend_ sem Docker | Local |
| Docker | 24.0 | Orquestrar os serviços do Servidor Central | Docker |
| Docker Compose | 2.20 | Orquestrar os serviços do Servidor Central | Docker |

---

## Instalação e Arranque via Docker Compose (recomendado para o Servidor Central)

Pressupõe Docker e Docker Compose instalados e as portas 3307, 8080 e 3000 disponíveis.

```bash
# 1. Aceder ao directório do Servidor Central
cd Servidor_Central

# 2. Criar o ficheiro de configuração de ambiente a partir do modelo
cp .env.example .env
# Editar .env se necessário (ver secção Configuração)

# 3. Construir e arrancar os serviços (MySQL + backend + frontend)
docker compose up --build
```

O _frontend_ fica disponível em `http://localhost:3000` e o _backend_ em `http://localhost:8080`.

```bash
# Parar sem remover dados
docker compose down

# Parar e remover volumes (apaga todos os dados da base de dados central)
docker compose down -v
```

---

## Instalação e Arranque Local (sem Docker)

### 1. Preparação da base de dados

Criar o utilizador MySQL e conceder permissões:

```sql
CREATE USER 'trasmum'@'localhost' IDENTIFIED BY 'trasmum';
GRANT ALL PRIVILEGES ON trasmum_loja.*     TO 'trasmum'@'localhost';
GRANT ALL PRIVILEGES ON trasmum_servidor.* TO 'trasmum'@'localhost';
FLUSH PRIVILEGES;
```

### 2. Inicialização das bases de dados

```bash
scripts/db-reset.sh            # ambas as bases de dados
scripts/db-reset.sh loja       # apenas a base de dados da Loja
scripts/db-reset.sh servidor   # apenas a base de dados do Servidor Central
```

Variáveis de ambiente aceites: `DB_USER`, `DB_PASS`, `DB_HOST` (valores por omissão: `trasmum/trasmum/localhost`).

### 3. Arranque do Servidor Central

```bash
# Backend (:8080) e frontend (:5173) em paralelo — terminar com Ctrl-C
scripts/dev-up.sh

# Ou individualmente:
cd Servidor_Central/backend  && mvn compile && mvn exec:java
cd Servidor_Central/frontend && npm install && npm run dev
```

### 4. Arranque do Software de Loja

```bash
scripts/loja-up.sh
# Ou directamente:
cd Loja && mvn javafx:run
```

**Sequência correcta de arranque:** base de dados → _backend_ → _frontend_ → Software de Loja.

O Software de Loja pode funcionar em modo de venda sem ligação ao servidor; a ligação só é necessária no momento do fecho de dia.

---

## Configuração

### Software de Loja

Ficheiro: `Loja/src/main/resources/config.properties`

| Parâmetro | Descrição | Valor por omissão |
|---|---|---|
| `loja.id` | Identificador único da loja (incluído em todos os pacotes de fecho) | `LOJA_002` |
| `loja.nome` | Nome comercial da loja (aparece nas faturas) | — |
| `loja.morada` | Morada da loja (aparece nas faturas) | — |
| `loja.localidade` | Localidade da loja (aparece nas faturas) | — |
| `loja.nif` | NIF da loja (aparece nas faturas) | — |
| `loja.email` | Endereço de correio eletrónico de contacto | — |
| `loja.limiteMaximoCaixa` | Limite máximo de numerário em caixa (euros) | `500.00` |
| `servidor.url` | URL base do _backend_ do Servidor Central | `http://localhost:8080` |
| `servidor.alertaValidade.dias` | Dias de antecedência para alertas de validade | `7` |
| `db.url` | URL de ligação à base de dados local da Loja | MySQL em `localhost:3306` |
| `db.user` | Utilizador da base de dados local | `trasmum` |
| `db.password` | Palavra-passe da base de dados local | `trasmum` |

Cada instância de loja deve ter um `loja.id` único na cadeia.

### Software do Servidor Central

**Via Docker:** editar `Servidor_Central/.env` (usar `Servidor_Central/.env.example` como modelo).

**Via execução local:** editar `Servidor_Central/backend/src/main/resources/config.properties`.

As variáveis de ambiente sobrepõem-se sempre aos valores do ficheiro de configuração.

| Parâmetro / Variável de ambiente | Descrição | Valor por omissão |
|---|---|---|
| `db.url` / `DB_URL` | URL de ligação à base de dados central | MySQL em `localhost:3306` |
| `db.user` / `DB_USER` | Utilizador da base de dados central | `trasmum` |
| `db.password` / `DB_PASSWORD` | Palavra-passe da base de dados central | `trasmum` |
| `frontend.origem` / `FRONTEND_ORIGEM` | Origem permitida para pedidos CORS do _frontend_ | `http://localhost:5173` |
| `servidor.porta` / `SERVER_PORT` | Porta de escuta do _backend_ | `8080` |
| `VITE_API_URL` | URL do _backend_ visível ao navegador (injetada em tempo de _build_) | `http://localhost:8080` |
| `db.retry.maxTentativas` / `DB_RETRY_MAX_TENTATIVAS` | Número máximo de tentativas de ligação à BD no arranque | `10` |
| `db.retry.delayMs` / `DB_RETRY_DELAY_MS` | Intervalo entre tentativas de ligação à BD (ms) | `2000` |

---

## Credenciais e Primeiro Acesso

Ambos os softwares criam automaticamente, no primeiro arranque, uma conta de administração com as credenciais **`admin` / `admin123`**. Esta conta é eliminada assim que um utilizador com nome de utilizador diferente efectua o primeiro _login_ com sucesso.

**Recomendação:** após a configuração inicial, criar imediatamente uma conta definitiva com palavra-passe segura e efectuar o primeiro _login_ com essa conta.

---

## Operação Corrente

### Ciclo diário

1. O gestor autentica-se no terminal de Loja com perfil de Gestor ou CEO.
2. Acede ao menu de fecho de dia e inicia o processo.
3. O sistema agrega as transações do dia num pacote, calcula o _hash_ SHA-256 e envia-o para `<servidor.url>/fecho`.
4. O Servidor Central valida o _hash_, persiste os dados e devolve confirmação.
5. O terminal actualiza o estado das transações para _confirmado_.

O pacote de fecho pode ser reenviado se a ligação estiver indisponível no momento do fecho.

### Monitorização

O _dashboard_ (`http://localhost:3000` via Docker ou `http://localhost:5173` em desenvolvimento local) disponibiliza visão global de vendas, estado de sincronização de cada loja, gestão de remessas e relatórios por categoria e período.

---

## Manutenção

### Cópia de segurança

```bash
# Base de dados da Loja
mysqldump -u trasmum -ptrasmum trasmum_loja > backup_loja_$(date +%Y%m%d).sql

# Base de dados do Servidor Central (execução local)
mysqldump -u trasmum -ptrasmum trasmum_servidor > backup_servidor_$(date +%Y%m%d).sql

# Base de dados do Servidor Central (via Docker, com os serviços a correr)
docker exec mysql_data mysqldump -u trasmum -ptrasmum trasmum_servidor \
  > backup_servidor_$(date +%Y%m%d).sql
```

### Restauro

```bash
mysql -u trasmum -ptrasmum trasmum_loja     < backup_loja_YYYYMMDD.sql
mysql -u trasmum -ptrasmum trasmum_servidor < backup_servidor_YYYYMMDD.sql
```

### Actualização

```bash
git pull

# Via Docker — recompila e substitui os containers
cd Servidor_Central && docker compose up --build

# Execução local
cd Servidor_Central/backend  && mvn compile
cd Servidor_Central/frontend && npm install && npm run build
cd Loja                      && mvn clean package
```

### Testes

```bash
# Software de Loja (4 classes: Autorizacao, Devolucao, FechoDia, Venda)
cd Loja && mvn test

# Backend do Servidor Central (2 classes: AutenticacaoCEO, Ingestao)
cd Servidor_Central/backend && mvn test

# Classe individual
mvn test -Dtest=NomeDaClasse

# Método individual
mvn test -Dtest=NomeDaClasse#nomeDoMetodo
```

O _frontend_ não tem testes automatizados.

---

## Resolução de Problemas Comuns

| Sintoma | Causa provável | Solução |
|---|---|---|
| _Backend_ não arranca: `Config obrigatória em falta: db.url` | `config.properties` sem valores de BD e sem variáveis de ambiente definidas | Verificar `.env` (Docker) ou `config.properties` (local) |
| _Backend_ não arranca: `Port already in use: 8080` | Outra instância ou container já a utilizar a porta | Terminar o processo em conflito ou parar o container Docker |
| Fecho de dia falha: `Connection refused` | _Backend_ não acessível no endereço em `servidor.url` | Verificar se o _backend_ está em execução e se `servidor.url` está correcto |
| Fecho de dia falha: `Hash inválido` | Corrupção dos dados durante o envio | Repetir o fecho; se persistir, verificar a integridade da BD local da Loja |
| Software de Loja não arranca: erro de ligação à BD | MySQL não está em execução ou credenciais incorrectas | Verificar se o MySQL está activo e os parâmetros `db.*` em `config.properties` |
| _Dashboard_ não apresenta dados após fecho confirmado | _Frontend_ a apontar para URL do _backend_ incorrecto | Verificar `VITE_API_URL` no `.env` e reconstruir com `docker compose up --build` |

---

## Desvios ao design original

Cada subprojeto mantém o seu registo de desvios ao design original:

- `Loja/DESIGN_CHANGES_LOJA.md`
- `Servidor_Central/DESIGN_CHANGES_SERVER.md`
