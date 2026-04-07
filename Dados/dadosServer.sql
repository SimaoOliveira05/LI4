-- ============================================================
-- TrasmUM — Software de Servidor Central
-- Schema MySQL
-- ============================================================

SET FOREIGN_KEY_CHECKS = 0;

-- ------------------------------------------------------------
-- CORE E AUTENTICAÇÃO
-- ------------------------------------------------------------

CREATE TABLE CEO (
    id                  INT             NOT NULL AUTO_INCREMENT,
    nomeUtilizador      VARCHAR(100)    NOT NULL UNIQUE,
    hashPalavraPasse    VARCHAR(60)     NOT NULL,
    tentativasLogin     INT             NOT NULL DEFAULT 0,
    bloqueadoAte        DATETIME        NULL,
    PRIMARY KEY (id),
    CONSTRAINT chk_ceo_tentativas
        CHECK (tentativasLogin >= 0)
);

CREATE TABLE Loja (
    idLoja      VARCHAR(50)     NOT NULL,
    nomeLoja    VARCHAR(100)    NOT NULL,
    PRIMARY KEY (idLoja)
);

CREATE TABLE FechoDiaCentral (
    id                  INT             NOT NULL AUTO_INCREMENT,
    idLoja              VARCHAR(50)     NOT NULL,
    dataFecho           DATE            NOT NULL,
    estadoFecho         ENUM(
                            'COMPLETO',
                            'PARCIAL'
                        )               NOT NULL DEFAULT 'PARCIAL',
    dataRecepcao        DATETIME        NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_fechodiacentral_loja
        FOREIGN KEY (idLoja)
        REFERENCES Loja(idLoja),
    CONSTRAINT uq_fechodiacentral
        UNIQUE (idLoja, dataFecho)
);

CREATE TABLE PedidoFecho (
    id                      INT             NOT NULL AUTO_INCREMENT,
    dataPedido              DATE            NOT NULL,
    janelaTemporalMinutos   INT             NOT NULL,
    estadoConsolidacao      ENUM(
                                'PENDENTE',
                                'CONSOLIDADO',
                                'ERRO'
                            )               NOT NULL DEFAULT 'PENDENTE',
    PRIMARY KEY (id),
    CONSTRAINT chk_pedidofecho_janela
        CHECK (janelaTemporalMinutos > 0)
);

CREATE TABLE PedidoFecho_FechoDiaCentral (
    idPedidoFecho       INT     NOT NULL,
    idFechoDiaCentral   INT     NOT NULL,
    PRIMARY KEY (idPedidoFecho, idFechoDiaCentral),
    CONSTRAINT fk_pf_fdc_pedido
        FOREIGN KEY (idPedidoFecho)
        REFERENCES PedidoFecho(id),
    CONSTRAINT fk_pf_fdc_fecho
        FOREIGN KEY (idFechoDiaCentral)
        REFERENCES FechoDiaCentral(id)
);

-- ------------------------------------------------------------
-- ENTIDADES TRANSACIONAIS
-- ------------------------------------------------------------

CREATE TABLE VendaCentral (
    id                  INT             NOT NULL AUTO_INCREMENT,
    idOriginalLoja      INT             NOT NULL,
    idLoja              VARCHAR(50)     NOT NULL,
    dataHora            DATETIME        NOT NULL,
    totalFaturado       DECIMAL(10,2)   NOT NULL,
    metodoPagamento     ENUM(
                            'NUMERARIO',
                            'MULTIBANCO'
                        )               NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uq_vendacentral
        UNIQUE (idLoja, idOriginalLoja),
    CONSTRAINT fk_vendacentral_loja
        FOREIGN KEY (idLoja)
        REFERENCES Loja(idLoja),
    CONSTRAINT chk_vendacentral_total
        CHECK (totalFaturado >= 0)
);

CREATE TABLE LinhaVendaCentral (
    id              INT             NOT NULL AUTO_INCREMENT,
    idVendaCentral  INT             NOT NULL,
    idOriginalLoja  INT             NOT NULL,
    idLoja          VARCHAR(50)     NOT NULL,
    nomeProduto     VARCHAR(150)    NOT NULL,
    categoria       VARCHAR(100)    NOT NULL,
    quantidade      INT             NOT NULL,
    precoUnitario   DECIMAL(10,2)   NOT NULL,
    subtotal        DECIMAL(10,2)   NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uq_linhavendacentral
        UNIQUE (idLoja, idOriginalLoja),
    CONSTRAINT fk_linhavendacentral_venda
        FOREIGN KEY (idVendaCentral)
        REFERENCES VendaCentral(id),
    CONSTRAINT chk_linhavenda_quantidade
        CHECK (quantidade > 0),
    CONSTRAINT chk_linhavenda_preco
        CHECK (precoUnitario >= 0),
    CONSTRAINT chk_linhavenda_subtotal
        CHECK (subtotal >= 0)
);

CREATE TABLE DevolucaoCentral (
    id                  INT             NOT NULL AUTO_INCREMENT,
    idOriginalLoja      INT             NOT NULL,
    idOriginalVenda     INT             NOT NULL,
    idLoja              VARCHAR(50)     NOT NULL,
    dataHora            DATETIME        NOT NULL,
    quantidade          INT             NOT NULL,
    valorRestituido     DECIMAL(10,2)   NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uq_devolucaocentral
        UNIQUE (idLoja, idOriginalLoja),
    CONSTRAINT fk_devolucaocentral_loja
        FOREIGN KEY (idLoja)
        REFERENCES Loja(idLoja),
    CONSTRAINT chk_devolucao_quantidade
        CHECK (quantidade > 0),
    CONSTRAINT chk_devolucao_valor
        CHECK (valorRestituido >= 0)
);

CREATE TABLE RemessaCentral (
    id                  INT             NOT NULL AUTO_INCREMENT,
    idOriginalLoja      INT             NOT NULL,
    idLoja              VARCHAR(50)     NOT NULL,
    nomeFornecedor      VARCHAR(150)    NOT NULL,
    dataRecepcao        DATE            NOT NULL,
    valorTotalGuia      DECIMAL(10,2)   NOT NULL,
    estadoPagamento     ENUM(
                            'PENDENTE_PAGAMENTO',
                            'PAGA'
                        )               NOT NULL DEFAULT 'PENDENTE_PAGAMENTO',
    PRIMARY KEY (id),
    CONSTRAINT uq_remessacentral
        UNIQUE (idLoja, idOriginalLoja),
    CONSTRAINT fk_remessacentral_loja
        FOREIGN KEY (idLoja)
        REFERENCES Loja(idLoja),
    CONSTRAINT chk_remessa_valor
        CHECK (valorTotalGuia >= 0)
);

CREATE TABLE PagamentoCentral (
    id                  INT             NOT NULL AUTO_INCREMENT,
    idOriginalLoja      INT             NOT NULL,
    idOriginalRemessa   INT             NOT NULL,
    idLoja              VARCHAR(50)     NOT NULL,
    valor               DECIMAL(10,2)   NOT NULL,
    dataPagamento       DATE            NULL,
    tipoPagamento       ENUM(
                            'TRANSFERENCIA',
                            'NUMERARIO',
                            'CHEQUE'
                        )               NULL,
    PRIMARY KEY (id),
    CONSTRAINT uq_pagamentocentral
        UNIQUE (idLoja, idOriginalLoja),
    CONSTRAINT fk_pagamentocentral_loja
        FOREIGN KEY (idLoja)
        REFERENCES Loja(idLoja),
    CONSTRAINT chk_pagamento_valor
        CHECK (valor >= 0)
);

CREATE TABLE SessaoCaixaCentral (
    id                  INT             NOT NULL AUTO_INCREMENT,
    idOriginalLoja      INT             NOT NULL,
    idLoja              VARCHAR(50)     NOT NULL,
    idUtilizador        INT             NOT NULL,
    saldoFinal          DECIMAL(10,2)   NOT NULL,
    dataAbertura        DATETIME        NOT NULL,
    dataEncerramento    DATETIME        NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uq_sessaocaixacentral
        UNIQUE (idLoja, idOriginalLoja),
    CONSTRAINT fk_sessaocaixacentral_loja
        FOREIGN KEY (idLoja)
        REFERENCES Loja(idLoja),
    CONSTRAINT chk_sessaocaixa_saldo
        CHECK (saldoFinal >= 0),
    CONSTRAINT chk_sessaocaixa_datas
        CHECK (dataEncerramento > dataAbertura)
);

CREATE TABLE LogAuditoriaCentral (
    id              INT             NOT NULL AUTO_INCREMENT,
    idOriginalLoja  INT             NOT NULL,
    idLoja          VARCHAR(50)     NOT NULL,
    acao            ENUM(
                        'VENDA',
                        'DEVOLUCAO',
                        'ALTERACAO_PRECO',
                        'APLICACAO_DESCONTO',
                        'GESTAO_UTILIZADOR',
                        'ALTERACAO_CATALOGO',
                        'FECHO_DIA'
                    )               NOT NULL,
    dataHora        DATETIME        NOT NULL,
    nomeUtilizador  VARCHAR(100)    NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uq_logauditoriacentral
        UNIQUE (idLoja, idOriginalLoja),
    CONSTRAINT fk_logauditoriacentral_loja
        FOREIGN KEY (idLoja)
        REFERENCES Loja(idLoja)
);

SET FOREIGN_KEY_CHECKS = 1;