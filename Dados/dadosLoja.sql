-- ============================================================
-- TrasmUM — Software de Loja
-- Schema MySQL
-- ============================================================

SET FOREIGN_KEY_CHECKS = 0;

-- ------------------------------------------------------------
-- CORE E SEGURANÇA
-- ------------------------------------------------------------ 
CREATE TABLE Utilizador (
    id                  INT             NOT NULL AUTO_INCREMENT,
    nomeUtilizador      VARCHAR(100)    NOT NULL UNIQUE,
    hashPalavraPasse    VARBINARY(64)   NOT NULL,
    perfil              ENUM(
                            'FUNCIONARIO',
                            'GESTOR',
                            'CEO'
                        )               NOT NULL,
    ativo               BIT             NOT NULL DEFAULT 1,
    emSessao            BIT             NOT NULL DEFAULT 0,
    PRIMARY KEY (id)
);

-- ------------------------------------------------------------
-- AUDITORIA
-- ------------------------------------------------------------

CREATE TABLE LogAuditoria (
    id                      INT             NOT NULL AUTO_INCREMENT,
    idLoja                  VARCHAR(50)     NOT NULL,
    estadoSincronizacao     ENUM(
                                'PENDENTE',
                                'EM_TRANSITO',
                                'CONFIRMADO'
                            )               NOT NULL DEFAULT 'PENDENTE',
    acao                    ENUM(
                                'VENDA',
                                'DEVOLUCAO',
                                'ALTERACAO_PRECO',
                                'APLICACAO_DESCONTO',
                                'GESTAO_UTILIZADOR',
                                'ALTERACAO_CATALOGO',
                                'FECHO_DIA'
                            )               NOT NULL,
    dataHora                DATETIME        NOT NULL,
    idUtilizador            INT             NOT NULL,
    entidade                VARCHAR(100)    NOT NULL,
    idEntidade              INT             NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_logauditoria_utilizador
        FOREIGN KEY (idUtilizador)
        REFERENCES Utilizador(id)
);

-- ------------------------------------------------------------
-- CATÁLOGO E INVENTÁRIO
-- ------------------------------------------------------------

CREATE TABLE Produto (
    id              INT             NOT NULL AUTO_INCREMENT,
    codigoBarras    VARCHAR(50)     NOT NULL UNIQUE,
    nome            VARCHAR(150)    NOT NULL,
    categoria       VARCHAR(100)    NOT NULL,
    precoBase       DECIMAL(10,2)   NOT NULL,
    stockMinimo     INT             NOT NULL DEFAULT 0,
    ativo           BIT             NOT NULL DEFAULT 1,
    PRIMARY KEY (id),
    CONSTRAINT chk_produto_precoBase
        CHECK (precoBase >= 0),
    CONSTRAINT chk_produto_stockMinimo
        CHECK (stockMinimo >= 0)
);

CREATE TABLE Lote (
    id              INT             NOT NULL AUTO_INCREMENT,
    idProduto       INT             NOT NULL,
    quantidade      INT             NOT NULL DEFAULT 0,
    dataValidade    DATE            NOT NULL,
    precoVenda      DECIMAL(10,2)   NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_lote_produto
        FOREIGN KEY (idProduto)
        REFERENCES Produto(id),
    CONSTRAINT chk_lote_quantidade
        CHECK (quantidade >= 0),
    CONSTRAINT chk_lote_precoVenda
        CHECK (precoVenda >= 0)
);

CREATE TABLE Desconto (
    id                      INT             NOT NULL AUTO_INCREMENT,
    idLote                  INT             NOT NULL UNIQUE,
    percentagem             DECIMAL(5,2)    NOT NULL,
    dataAplicacao           DATE            NOT NULL,
    idUtilizadorAplicou     INT             NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_desconto_lote
        FOREIGN KEY (idLote)
        REFERENCES Lote(id),
    CONSTRAINT fk_desconto_utilizador
        FOREIGN KEY (idUtilizadorAplicou)
        REFERENCES Utilizador(id),
    CONSTRAINT chk_desconto_percentagem
        CHECK (percentagem > 0 AND percentagem <= 100)
);

-- ------------------------------------------------------------
-- REMESSAS E FORNECEDORES
-- ------------------------------------------------------------

CREATE TABLE Fornecedor (
    id          INT             NOT NULL AUTO_INCREMENT,
    nome        VARCHAR(150)    NOT NULL,
    nif         VARCHAR(20)     NOT NULL UNIQUE,
    morada      VARCHAR(200)    NULL,
    telefone    VARCHAR(20)     NULL,
    email       VARCHAR(150)    NULL,
    iban        VARCHAR(34)     NULL,
    PRIMARY KEY (id)
);

CREATE TABLE PedidoRemessa (
    id              INT             NOT NULL AUTO_INCREMENT,
    idFornecedor    INT             NOT NULL,
    idUtilizador    INT             NOT NULL,
    dataCriacao     DATE            NOT NULL,
    estado          ENUM(
                        'PENDENTE',
                        'CONCLUIDO'
                    )               NOT NULL DEFAULT 'PENDENTE',
    PRIMARY KEY (id),
    CONSTRAINT fk_pedidoremessa_fornecedor
        FOREIGN KEY (idFornecedor)
        REFERENCES Fornecedor(id),
    CONSTRAINT fk_pedidoremessa_utilizador
        FOREIGN KEY (idUtilizador)
        REFERENCES Utilizador(id)
);

CREATE TABLE LinhaPedidoRemessa (
    id                      INT     NOT NULL AUTO_INCREMENT,
    idPedidoRemessa         INT     NOT NULL,
    idProduto               INT     NOT NULL,
    quantidadePretendida    INT     NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_linhapedidoremessa_pedido
        FOREIGN KEY (idPedidoRemessa)
        REFERENCES PedidoRemessa(id),
    CONSTRAINT fk_linhapedidoremessa_produto
        FOREIGN KEY (idProduto)
        REFERENCES Produto(id),
    CONSTRAINT chk_linhapedido_quantidade
        CHECK (quantidadePretendida > 0)
);

CREATE TABLE Remessa (
    id                  INT             NOT NULL AUTO_INCREMENT,
    idLoja              VARCHAR(50)     NOT NULL,
    estadoSincronizacao ENUM(
                            'PENDENTE',
                            'EM_TRANSITO',
                            'CONFIRMADO'
                        )               NOT NULL DEFAULT 'PENDENTE',
    idFornecedor        INT             NOT NULL,
    idUtilizador        INT             NOT NULL,
    dataRecepcao        DATE            NOT NULL,
    valorTotalGuia      DECIMAL(10,2)   NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_remessa_fornecedor
        FOREIGN KEY (idFornecedor)
        REFERENCES Fornecedor(id),
    CONSTRAINT fk_remessa_utilizador
        FOREIGN KEY (idUtilizador)
        REFERENCES Utilizador(id),
    CONSTRAINT chk_remessa_valorTotalGuia
        CHECK (valorTotalGuia >= 0)
);

CREATE TABLE LinhaRemessa (
    id              INT     NOT NULL AUTO_INCREMENT,
    idRemessa       INT     NOT NULL,
    idProduto       INT     NOT NULL,
    idLoteGerado    INT     NULL,
    quantidade      INT     NOT NULL,
    dataValidade    DATE    NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_linharemessa_remessa
        FOREIGN KEY (idRemessa)
        REFERENCES Remessa(id),
    CONSTRAINT fk_linharemessa_produto
        FOREIGN KEY (idProduto)
        REFERENCES Produto(id),
    CONSTRAINT fk_linharemessa_lote
        FOREIGN KEY (idLoteGerado)
        REFERENCES Lote(id),
    CONSTRAINT chk_linharemessa_quantidade
        CHECK (quantidade > 0)
);

CREATE TABLE Pagamento (
    id                  INT             NOT NULL AUTO_INCREMENT,
    idLoja              VARCHAR(50)     NOT NULL,
    estadoSincronizacao ENUM(
                            'PENDENTE',
                            'EM_TRANSITO',
                            'CONFIRMADO'
                        )               NOT NULL DEFAULT 'PENDENTE',
    idFornecedor        INT             NOT NULL,
    idRemessa           INT             NOT NULL,
    idUtilizador        INT             NULL,
    valor               DECIMAL(10,2)   NOT NULL,
    dataHora            DATETIME        NULL,
    estadoPagamento     ENUM(
                            'PENDENTE',
                            'PAGO'
                        )               NOT NULL DEFAULT 'PENDENTE',
    PRIMARY KEY (id),
    CONSTRAINT fk_pagamento_fornecedor
        FOREIGN KEY (idFornecedor)
        REFERENCES Fornecedor(id),
    CONSTRAINT fk_pagamento_remessa
        FOREIGN KEY (idRemessa)
        REFERENCES Remessa(id),
    CONSTRAINT fk_pagamento_utilizador
        FOREIGN KEY (idUtilizador)
        REFERENCES Utilizador(id),
    CONSTRAINT chk_pagamento_valor
        CHECK (valor >= 0)
);

-- ------------------------------------------------------------
-- VENDAS E FATURAS
-- ------------------------------------------------------------

CREATE TABLE Venda (
    id                  INT             NOT NULL AUTO_INCREMENT,
    idLoja              VARCHAR(50)     NOT NULL,
    estadoSincronizacao ENUM(
                            'PENDENTE',
                            'EM_TRANSITO',
                            'CONFIRMADO'
                        )               NOT NULL DEFAULT 'PENDENTE',
    idUtilizador        INT             NOT NULL,
    dataHora            DATETIME        NOT NULL,
    metodoPagamento     ENUM(
                            'NUMERARIO',
                            'MULTIBANCO'
                        )               NOT NULL,
    totalFaturado       DECIMAL(10,2)   NOT NULL,
    estado              ENUM(
                            'FINALIZADA',
                            'ANULADA'
                        )               NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_venda_utilizador
        FOREIGN KEY (idUtilizador)
        REFERENCES Utilizador(id),
    CONSTRAINT chk_venda_totalFaturado
        CHECK (totalFaturado >= 0)
);

CREATE TABLE LinhaVenda (
    id              INT             NOT NULL AUTO_INCREMENT,
    idVenda         INT             NOT NULL,
    idLote          INT             NOT NULL,
    quantidade      INT             NOT NULL,
    precoUnitario   DECIMAL(10,2)   NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_linhavenda_venda
        FOREIGN KEY (idVenda)
        REFERENCES Venda(id),
    CONSTRAINT fk_linhavenda_lote
        FOREIGN KEY (idLote)
        REFERENCES Lote(id),
    CONSTRAINT chk_linhavenda_quantidade
        CHECK (quantidade > 0),
    CONSTRAINT chk_linhavenda_precoUnitario
        CHECK (precoUnitario >= 0)
);

CREATE TABLE Fatura (
    id              INT             NOT NULL AUTO_INCREMENT,
    idVenda         INT             NOT NULL UNIQUE,
    numeroFatura    VARCHAR(50)     NOT NULL UNIQUE,
    dataEmissao     DATETIME        NOT NULL,
    nifCliente      VARCHAR(20)     NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_fatura_venda
        FOREIGN KEY (idVenda)
        REFERENCES Venda(id)
);

CREATE TABLE Devolucao (
    id                      INT             NOT NULL AUTO_INCREMENT,
    idLoja                  VARCHAR(50)     NOT NULL,
    estadoSincronizacao     ENUM(
                                'PENDENTE',
                                'EM_TRANSITO',
                                'CONFIRMADO'
                            )               NOT NULL DEFAULT 'PENDENTE',
    idFatura                INT             NOT NULL,
    idUtilizador            INT             NOT NULL,
    idLote                  INT             NOT NULL,
    dataHora                DATETIME        NOT NULL,
    quantidade              INT             NOT NULL,
    dataValidadeEmbalagem   DATE            NOT NULL,
    valorRestituido         DECIMAL(10,2)   NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_devolucao_fatura
        FOREIGN KEY (idFatura)
        REFERENCES Fatura(id),
    CONSTRAINT fk_devolucao_utilizador
        FOREIGN KEY (idUtilizador)
        REFERENCES Utilizador(id),
    CONSTRAINT fk_devolucao_lote
        FOREIGN KEY (idLote)
        REFERENCES Lote(id),
    CONSTRAINT chk_devolucao_quantidade
        CHECK (quantidade > 0),
    CONSTRAINT chk_devolucao_valorRestituido
        CHECK (valorRestituido >= 0)
);

-- ------------------------------------------------------------
-- TESOURARIA E CAIXA
-- ------------------------------------------------------------

CREATE TABLE SessaoCaixa (
    id                  INT             NOT NULL AUTO_INCREMENT,
    idLoja              VARCHAR(50)     NOT NULL,
    estadoSincronizacao ENUM(
                            'PENDENTE',
                            'EM_TRANSITO',
                            'CONFIRMADO'
                        )               NOT NULL DEFAULT 'PENDENTE',
    idUtilizador        INT             NOT NULL,
    saldoAtual          DECIMAL(10,2)   NOT NULL DEFAULT 0,
    dataAbertura        DATETIME        NOT NULL,
    dataEncerramento    DATETIME        NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_sessaocaixa_utilizador
        FOREIGN KEY (idUtilizador)
        REFERENCES Utilizador(id),
    CONSTRAINT chk_sessaocaixa_saldo
        CHECK (saldoAtual >= 0)
);

CREATE TABLE DetalheNumerario (
    id              INT             NOT NULL AUTO_INCREMENT,
    idSessaoCaixa   INT             NULL,
    idSangria       INT             NULL,
    denominacao     ENUM(
                        'CEM_EUROS',
                        'CINQUENTA_EUROS',
                        'VINTE_EUROS',
                        'DEZ_EUROS',
                        'CINCO_EUROS',
                        'DOIS_EUROS',
                        'UM_EURO',
                        'CINQUENTA_CENTIMOS',
                        'VINTE_CENTIMOS',
                        'DEZ_CENTIMOS',
                        'CINCO_CENTIMOS',
                        'DOIS_CENTIMOS',
                        'UM_CENTIMO'
                    )               NOT NULL,
    quantidade      INT             NOT NULL,
    subtotal        DECIMAL(10,2)   NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT chk_detalhe_quantidade
        CHECK (quantidade >= 0),
    CONSTRAINT chk_detalhe_subtotal
        CHECK (subtotal >= 0)
);

CREATE TABLE Sangria (
    id              INT             NOT NULL AUTO_INCREMENT,
    idSessaoCaixa   INT             NOT NULL,
    dataHora        DATETIME        NOT NULL,
    total           DECIMAL(10,2)   NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_sangria_sessaocaixa
        FOREIGN KEY (idSessaoCaixa)
        REFERENCES SessaoCaixa(id),
    CONSTRAINT chk_sangria_total
        CHECK (total > 0)
);

ALTER TABLE DetalheNumerario
    ADD CONSTRAINT fk_detalhe_sessaocaixa
        FOREIGN KEY (idSessaoCaixa)
        REFERENCES SessaoCaixa(id),
    ADD CONSTRAINT fk_detalhe_sangria
        FOREIGN KEY (idSangria)
        REFERENCES Sangria(id);

-- ------------------------------------------------------------
-- FECHO DE DIA
-- ------------------------------------------------------------

CREATE TABLE FechoDia (
    id                  INT             NOT NULL AUTO_INCREMENT,
    idLoja              VARCHAR(50)     NOT NULL,
    estadoSincronizacao ENUM(
                            'PENDENTE',
                            'EM_TRANSITO',
                            'CONFIRMADO'
                        )               NOT NULL DEFAULT 'PENDENTE',
    idUtilizador        INT             NOT NULL,
    dataFecho           DATE            NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_fechodia_utilizador
        FOREIGN KEY (idUtilizador)
        REFERENCES Utilizador(id)
);

SET FOREIGN_KEY_CHECKS = 1;\