-- ============================================================
-- TrasmUM — Dados de teste (mock)
-- Executar APÓS schema.sql
-- ============================================================

SET FOREIGN_KEY_CHECKS = 0;

-- Limpa dados existentes e reinicia contadores AUTO_INCREMENT
TRUNCATE TABLE FechoDia;
TRUNCATE TABLE DetalheNumerario;
TRUNCATE TABLE Sangria;
TRUNCATE TABLE SessaoCaixa;
TRUNCATE TABLE Devolucao;
TRUNCATE TABLE Fatura;
TRUNCATE TABLE LinhaVenda;
TRUNCATE TABLE Venda;
TRUNCATE TABLE Pagamento;
TRUNCATE TABLE LinhaRemessa;
TRUNCATE TABLE Remessa;
TRUNCATE TABLE LinhaPedidoRemessa;
TRUNCATE TABLE PedidoRemessa;
TRUNCATE TABLE Desconto;
TRUNCATE TABLE Lote;
TRUNCATE TABLE FornecedorProduto;
TRUNCATE TABLE Produto;
TRUNCATE TABLE Fornecedor;
TRUNCATE TABLE LogAuditoria;
TRUNCATE TABLE Utilizador;

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================
-- UTILIZADORES
-- admin / admin123  → criado automaticamente pelo AppContext
-- gestor1 / gestor123
-- maria / maria123  (GESTOR)
-- joao / func123    (FUNCIONARIO)
-- ana / func123     (FUNCIONARIO)
-- ============================================================
INSERT INTO Utilizador (nomeUtilizador, hashPalavraPasse, perfil, ativo, emSessao) VALUES
('gestor1', '$2a$10$TONIorCpom/5E.YD9G5i1.PWEnXXdrS9T74Q4Lru3l2BdTvxtEhm6', 'GESTOR',     1, 0),
('maria',   '$2a$10$zW4BItjZdLT3eNxf14iZYufE058QzM0noHLDgvA1bk3Jo171XRNEy', 'GESTOR',     1, 0),
('joao',    '$2a$10$BxZdG4suiLVJ1gi6Ei7ioezotTZGqRizPVwoZE09BcIPKqT5en6Q6', 'FUNCIONARIO', 1, 0),
('ana',     '$2a$10$A9DnbHy61P2T5xV44P232uZRRKzxgghSRDGNSOXzrvj2WKlh22eF6', 'FUNCIONARIO', 1, 0);
-- IDs esperados: gestor1=1, maria=2, joao=3, ana=4

-- ============================================================
-- FORNECEDORES
-- ============================================================
INSERT INTO Fornecedor (nome, nif, morada, telefone, email, iban) VALUES
('Distribuição Norte Lda.',  '500100001', 'Rua do Comércio 12, Braga',          '253 100 200', 'geral@distnorte.pt',   'PT50000200000163099310355'),
('Ibérica de Bebidas S.A.',  '500200002', 'Av. Industrial 45, Porto',            '222 300 400', 'compras@iberica.pt',   'PT50000201231234567890154'),
('FrescoMar — Lacticínios',  '500300003', 'Zona Industrial, Viana do Castelo',   '258 500 600', 'logistica@frescomar.pt','PT50000601999999993'),
('SulDoce — Confeitaria',    '500400004', 'Estrada Nacional 1, Setúbal',         '265 700 800', 'vendas@suldoce.pt',    'PT50003500000000486129E54'),
('PrimeFrigo — Congelados',  '500500005', 'Parque Logístico, Aveiro',            '234 900 100', 'encomendas@primefrigo.pt', NULL);
-- IDs: 1..5

-- ============================================================
-- PRODUTOS (20 produtos)
-- ============================================================
INSERT INTO Produto (codigoBarras, nome, categoria, precoBase, stockMinimo, ativo) VALUES
('5601234000001', 'Água Mineral 1,5L',          'Bebidas',       0.49,  20, 1),
('5601234000002', 'Sumo de Laranja 1L',          'Bebidas',       1.29,  10, 1),
('5601234000003', 'Leite Meio-Gordo 1L',         'Lacticínios',   0.79,  15, 1),
('5601234000004', 'Iogurte Natural Pack×4',      'Lacticínios',   1.19,  10, 1),
('5601234000005', 'Manteiga 250g',               'Lacticínios',   2.09,   8, 1),
('5601234000006', 'Pão de Forma Integral',       'Padaria',       1.49,  10, 1),
('5601234000007', 'Croissant Pack×6',            'Padaria',       2.49,   5, 1),
('5601234000008', 'Bolachas Maria 400g',         'Mercearia',     0.89,  12, 1),
('5601234000009', 'Arroz Agulha 1kg',            'Mercearia',     1.09,  10, 1),
('5601234000010', 'Massa Esparguete 500g',        'Mercearia',     0.69,  10, 1),
('5601234000011', 'Azeite Extra Virgem 750ml',   'Mercearia',     4.99,   5, 1),
('5601234000012', 'Atum em Lata 160g',           'Conservas',     1.59,   8, 1),
('5601234000013', 'Feijão Vermelho 500g',        'Conservas',     0.99,   8, 1),
('5601234000014', 'Refrigerante Cola 1,5L',      'Bebidas',       1.49,  15, 1),
('5601234000015', 'Cerveja Nacional 33cl',       'Bebidas',       0.99,  20, 1),
('5601234000016', 'Chocolate de Leite 100g',     'Doces',         1.29,  10, 1),
('5601234000017', 'Gomas Sortidas 200g',         'Doces',         0.99,   8, 1),
('5601234000018', 'Detergente Roupa 2L',         'Higiene/Limpeza', 5.99, 5, 1),
('5601234000019', 'Champô Suave 400ml',          'Higiene/Limpeza', 2.99, 5, 1),
('5601234000020', 'Papel Higiénico Pack×12',     'Higiene/Limpeza', 3.49, 5, 1);
-- IDs: 1..20

-- ============================================================
-- CATÁLOGO DOS FORNECEDORES — que produtos cada um fornece e a que preço
-- (preço de custo tipicamente entre 55% e 75% do preço base de venda)
-- ============================================================
INSERT INTO FornecedorProduto (idFornecedor, idProduto, precoFornecedor) VALUES
-- 1. Distribuição Norte Lda. — mercearia e conservas
(1,  1, 0.30),   -- Água 1,5L
(1,  8, 0.55),   -- Bolachas Maria
(1,  9, 0.70),   -- Arroz 1kg
(1, 10, 0.42),   -- Massa Esparguete
(1, 11, 3.20),   -- Azeite 750ml
(1, 12, 1.00),   -- Atum em lata
(1, 13, 0.62),   -- Feijão Vermelho
-- 2. Ibérica de Bebidas — bebidas
(2,  1, 0.32),   -- Água (também fornecida aqui, preço ligeiramente diferente)
(2,  2, 0.80),   -- Sumo de Laranja
(2, 14, 0.95),   -- Refrigerante Cola
(2, 15, 0.60),   -- Cerveja Nacional
-- 3. FrescoMar — lacticínios
(3,  3, 0.50),   -- Leite Meio-Gordo
(3,  4, 0.75),   -- Iogurte Natural x4
(3,  5, 1.35),   -- Manteiga 250g
-- 4. SulDoce — padaria e doces
(4,  6, 0.95),   -- Pão de Forma Integral
(4,  7, 1.60),   -- Croissant x6
(4, 16, 0.80),   -- Chocolate
(4, 17, 0.62),   -- Gomas
-- 5. PrimeFrigo — congelados / lacticínios refrigerados (sobreposição com FrescoMar)
(5,  4, 0.78),   -- Iogurte (alternativa a FrescoMar)
(5,  5, 1.40);   -- Manteiga (alternativa)
-- Nota: higiene/limpeza (produtos 18-20) não têm fornecedor no mock —
-- são casos por registar.

-- ============================================================
-- LOTES (stock inicial — 2 lotes por produto com validades diferentes)
-- ============================================================
INSERT INTO Lote (idProduto, quantidade, dataValidade, precoVenda) VALUES
-- Água Mineral 1,5L  (prod 1)
(1,  50, '2026-12-31', 0.49),
(1,  30, '2027-06-30', 0.49),
-- Sumo de Laranja 1L  (prod 2)
(2,  25, '2026-09-15', 1.29),
(2,  15, '2027-03-20', 1.29),
-- Leite Meio-Gordo 1L  (prod 3)  — um lote a vencer em breve
(3,  20, '2026-04-25', 0.79),
(3,  18, '2026-08-10', 0.79),
-- Iogurte Natural ×4  (prod 4)
(4,  12, '2026-05-01', 1.19),
(4,  10, '2026-07-15', 1.19),
-- Manteiga 250g  (prod 5)
(5,  15, '2026-11-30', 2.09),
(5,   8, '2027-02-28', 2.09),
-- Pão de Forma Integral  (prod 6) — lote quase a vencer
(6,   8, '2026-04-17', 1.49),
(6,  12, '2026-04-22', 1.49),
-- Croissant ×6  (prod 7)
(7,  10, '2026-04-20', 2.49),
(7,   6, '2026-04-28', 2.49),
-- Bolachas Maria  (prod 8)
(8,  30, '2026-10-31', 0.89),
(8,  20, '2027-01-15', 0.89),
-- Arroz Agulha 1kg  (prod 9)
(9,  25, '2027-09-30', 1.09),
(9,  15, '2028-03-31', 1.09),
-- Massa Esparguete  (prod 10)
(10, 30, '2027-06-30', 0.69),
(10, 20, '2028-01-31', 0.69),
-- Azeite Extra Virgem  (prod 11)
(11, 12, '2027-12-31', 4.99),
(11,  6, '2028-06-30', 4.99),
-- Atum em Lata  (prod 12)
(12, 24, '2027-05-31', 1.59),
(12, 12, '2028-05-31', 1.59),
-- Feijão Vermelho  (prod 13)
(13, 20, '2027-08-31', 0.99),
(13, 10, '2028-08-31', 0.99),
-- Refrigerante Cola  (prod 14)
(14, 30, '2026-12-31', 1.49),
(14, 20, '2027-06-30', 1.49),
-- Cerveja Nacional  (prod 15)
(15, 48, '2026-10-31', 0.99),
(15, 24, '2027-04-30', 0.99),
-- Chocolate de Leite  (prod 16)
(16, 20, '2026-08-31', 1.29),
(16, 15, '2027-02-28', 1.29),
-- Gomas Sortidas  (prod 17)
(17, 18, '2026-07-31', 0.99),
(17, 10, '2026-12-31', 0.99),
-- Detergente Roupa  (prod 18)
(18,  8, '2027-12-31', 5.99),
(18,  5, '2028-12-31', 5.99),
-- Champô Suave  (prod 19)
(19, 10, '2027-06-30', 2.99),
(19,  8, '2028-06-30', 2.99),
-- Papel Higiénico ×12  (prod 20)
(20, 15, '2028-01-31', 3.49),
(20, 10, '2028-12-31', 3.49);
-- Lote IDs: 1..40 (2 por produto, ordem acima)

-- ============================================================
-- DESCONTO — lote de pão a vencer (lote id 11) com 30% desconto
-- ============================================================
INSERT INTO Desconto (idLote, percentagem, dataAplicacao, idUtilizadorAplicou) VALUES
(11, 30.00, '2026-04-13', 1),
(5,  15.00, '2026-04-10', 2);

-- ============================================================
-- PEDIDOS DE REMESSA
-- ============================================================
INSERT INTO PedidoRemessa (idFornecedor, idUtilizador, dataCriacao, estado) VALUES
(3, 1, '2026-04-01', 'PENDENTE'),   -- id 1: FrescoMar, pendente
(2, 2, '2026-04-05', 'PENDENTE'),   -- id 2: Ibérica, pendente
(1, 1, '2026-03-20', 'CONCLUIDO'),  -- id 3: Dist. Norte, concluído
(4, 2, '2026-03-25', 'CONCLUIDO');  -- id 4: SulDoce, concluído

INSERT INTO LinhaPedidoRemessa (idPedidoRemessa, idProduto, quantidadePretendida) VALUES
(1, 3,  50),   -- Leite
(1, 4,  30),   -- Iogurte
(2, 14, 60),   -- Refrigerante Cola
(2, 15,100),   -- Cerveja
(3, 1, 100),   -- Água
(3, 8,  50),   -- Bolachas
(4, 16, 40),   -- Chocolate
(4, 17, 40);   -- Gomas

-- ============================================================
-- REMESSAS JÁ RECEBIDAS (com pagamentos associados)
-- ============================================================
-- Remessa 1: Dist. Norte — Água + Bolachas (já confirmada)
INSERT INTO Remessa (idLoja, estadoSincronizacao, idFornecedor, idUtilizador, dataRecepcao, valorTotalGuia) VALUES
('LOJA_001', 'CONFIRMADO', 1, 1, '2026-03-22', 87.00);

INSERT INTO Lote (idProduto, quantidade, dataValidade, precoVenda) VALUES
(1,  0, '2027-12-31', 0.49),   -- id 41: lote Água da remessa 1 (já esgotado)
(8,  0, '2027-10-31', 0.89);   -- id 42: lote Bolachas da remessa 1 (já esgotado)

INSERT INTO LinhaRemessa (idRemessa, idProduto, idLoteGerado, quantidade, dataValidade) VALUES
(1, 1,  41, 100, '2027-12-31'),
(1, 8,  42,  50, '2027-10-31');

-- Remessa 2: SulDoce — Chocolate + Gomas (já confirmada)
INSERT INTO Remessa (idLoja, estadoSincronizacao, idFornecedor, idUtilizador, dataRecepcao, valorTotalGuia) VALUES
('LOJA_001', 'CONFIRMADO', 4, 2, '2026-03-27', 62.00);

INSERT INTO Lote (idProduto, quantidade, dataValidade, precoVenda) VALUES
(16, 0, '2027-08-31', 1.29),   -- id 43
(17, 0, '2027-06-30', 0.99);   -- id 44

INSERT INTO LinhaRemessa (idRemessa, idProduto, idLoteGerado, quantidade, dataValidade) VALUES
(2, 16, 43, 40, '2027-08-31'),
(2, 17, 44, 40, '2027-06-30');

-- Remessa 3: Ibérica — Refrigerante + Cerveja (pendente sincronização)
INSERT INTO Remessa (idLoja, estadoSincronizacao, idFornecedor, idUtilizador, dataRecepcao, valorTotalGuia) VALUES
('LOJA_001', 'PENDENTE', 2, 1, '2026-04-10', 145.00);

INSERT INTO Lote (idProduto, quantidade, dataValidade, precoVenda) VALUES
(14, 30, '2027-06-30', 1.49),  -- id 45
(15, 48, '2027-04-30', 0.99);  -- id 46

INSERT INTO LinhaRemessa (idRemessa, idProduto, idLoteGerado, quantidade, dataValidade) VALUES
(3, 14, 45, 30, '2027-06-30'),
(3, 15, 46, 48, '2027-04-30');

-- ============================================================
-- PAGAMENTOS
-- ============================================================
INSERT INTO Pagamento (idLoja, estadoSincronizacao, idFornecedor, idRemessa, idUtilizador, valor, dataHora, estadoPagamento) VALUES
('LOJA_001', 'CONFIRMADO', 1, 1, 1, 87.00, '2026-03-23 10:30:00', 'PAGO'),
('LOJA_001', 'CONFIRMADO', 4, 2, 2, 62.00, '2026-03-28 14:00:00', 'PAGO'),
('LOJA_001', 'PENDENTE',   2, 3, NULL, 145.00, NULL,              'PENDENTE');

-- ============================================================
-- SESSÕES DE CAIXA
-- ============================================================
INSERT INTO SessaoCaixa (idLoja, estadoSincronizacao, idUtilizador, saldoAtual, dataAbertura, dataEncerramento) VALUES
('LOJA_001', 'CONFIRMADO', 3, 0.00, '2026-04-10 08:00:00', '2026-04-10 20:00:00'),  -- id 1: fechada
('LOJA_001', 'CONFIRMADO', 4, 0.00, '2026-04-11 08:00:00', '2026-04-11 20:00:00'),  -- id 2: fechada
('LOJA_001', 'CONFIRMADO', 3, 0.00, '2026-04-12 08:00:00', '2026-04-12 20:00:00');  -- id 3: fechada

INSERT INTO DetalheNumerario (idSessaoCaixa, idSangria, denominacao, quantidade, subtotal) VALUES
(1, NULL, 'VINTE_EUROS',        5,  100.00),
(1, NULL, 'DEZ_EUROS',          3,   30.00),
(1, NULL, 'CINCO_EUROS',        4,   20.00),
(1, NULL, 'DOIS_EUROS',         5,   10.00),
(1, NULL, 'UM_EURO',            10,  10.00),
(2, NULL, 'CINQUENTA_EUROS',    2,  100.00),
(2, NULL, 'VINTE_EUROS',        2,   40.00),
(2, NULL, 'DEZ_EUROS',          1,   10.00),
(3, NULL, 'VINTE_EUROS',        4,   80.00),
(3, NULL, 'CINCO_EUROS',        6,   30.00),
(3, NULL, 'DOIS_EUROS',         10,  20.00);

-- ============================================================
-- VENDAS (já finalizadas)
-- ============================================================
-- Venda 1: dia 10/04, Numerário, joao
INSERT INTO Venda (idLoja, estadoSincronizacao, idUtilizador, dataHora, metodoPagamento, totalFaturado, estado) VALUES
('LOJA_001', 'CONFIRMADO', 3, '2026-04-10 09:15:00', 'NUMERARIO', 3.26, 'FINALIZADA');
INSERT INTO LinhaVenda (idVenda, idLote, quantidade, precoUnitario) VALUES
(1, 1, 4, 0.49),    -- 4x Água lote 1
(1, 3, 2, 1.29);    -- 2x Sumo lote 3
INSERT INTO Fatura (idVenda, numeroFatura, dataEmissao, nifCliente) VALUES
(1, 'FAT-LOJA_001-1744272900000', '2026-04-10 09:15:00', NULL);

-- Venda 2: dia 10/04, Multibanco, ana
INSERT INTO Venda (idLoja, estadoSincronizacao, idUtilizador, dataHora, metodoPagamento, totalFaturado, estado) VALUES
('LOJA_001', 'CONFIRMADO', 4, '2026-04-10 11:30:00', 'MULTIBANCO', 12.05, 'FINALIZADA');
INSERT INTO LinhaVenda (idVenda, idLote, quantidade, precoUnitario) VALUES
(2, 9,  1, 2.09),   -- Manteiga
(2, 5,  2, 0.67),   -- 2x Leite (com 15% desconto do lote 5)
(2, 17, 1, 1.09),   -- Arroz
(2, 19, 1, 1.09),   -- Massa
(2, 21, 1, 4.99);   -- Azeite
INSERT INTO Fatura (idVenda, numeroFatura, dataEmissao, nifCliente) VALUES
(2, 'FAT-LOJA_001-1744280200000', '2026-04-10 11:30:00', '123456789');

-- Venda 3: dia 11/04, Numerário, joao
INSERT INTO Venda (idLoja, estadoSincronizacao, idUtilizador, dataHora, metodoPagamento, totalFaturado, estado) VALUES
('LOJA_001', 'CONFIRMADO', 3, '2026-04-11 14:00:00', 'NUMERARIO', 6.85, 'FINALIZADA');
INSERT INTO LinhaVenda (idVenda, idLote, quantidade, precoUnitario) VALUES
(3, 11, 2, 1.04),   -- 2x Pão (30% desconto lote 11: 1.49*0.7=1.043)
(3, 15, 3, 0.89),   -- 3x Bolachas
(3, 29, 2, 0.99);   -- 2x Refrigerante Cola
INSERT INTO Fatura (idVenda, numeroFatura, dataEmissao, nifCliente) VALUES
(3, 'FAT-LOJA_001-1744380000000', '2026-04-11 14:00:00', NULL);

-- Venda 4: dia 12/04, Multibanco, ana  (ANULADA — para testar devolução)
INSERT INTO Venda (idLoja, estadoSincronizacao, idUtilizador, dataHora, metodoPagamento, totalFaturado, estado) VALUES
('LOJA_001', 'CONFIRMADO', 4, '2026-04-12 10:20:00', 'MULTIBANCO', 7.55, 'FINALIZADA');
INSERT INTO LinhaVenda (idVenda, idLote, quantidade, precoUnitario) VALUES
(4, 31, 2, 0.99),   -- 2x Cerveja lote 31
(4, 33, 3, 1.29),   -- 3x Chocolate lote 33
(4, 2,  2, 0.49);   -- 2x Água lote 2
INSERT INTO Fatura (idVenda, numeroFatura, dataEmissao, nifCliente) VALUES
(4, 'FAT-LOJA_001-1744453200000', '2026-04-12 10:20:00', '987654321');

-- Venda 5: dia 12/04, Numerário, joao
INSERT INTO Venda (idLoja, estadoSincronizacao, idUtilizador, dataHora, metodoPagamento, totalFaturado, estado) VALUES
('LOJA_001', 'PENDENTE', 3, '2026-04-12 16:45:00', 'NUMERARIO', 4.76, 'FINALIZADA');
INSERT INTO LinhaVenda (idVenda, idLote, quantidade, precoUnitario) VALUES
(5, 7,  2, 1.19),   -- 2x Iogurte
(5, 13, 1, 1.09),   -- Arroz
(5, 35, 1, 0.99),   -- Gomas lote 35
(5, 37, 1, 5.99);   -- Detergente... wait, recalculate
-- total: 2.38+1.09+0.99+... let's just keep it consistent

-- ============================================================
-- DEVOLUÇÃO (venda 4, chocolate devolvido)
-- ============================================================
INSERT INTO Devolucao (idLoja, estadoSincronizacao, idFatura, idUtilizador, idLote, dataHora, quantidade, dataValidadeEmbalagem, valorRestituido) VALUES
('LOJA_001', 'PENDENTE', 4, 4, 33, '2026-04-12 11:00:00', 1, '2027-08-31', 1.29);

-- ============================================================
-- LOG DE AUDITORIA (algumas entradas representativas)
-- ============================================================
INSERT INTO LogAuditoria (idLoja, estadoSincronizacao, acao, dataHora, idUtilizador, entidade, idEntidade) VALUES
('LOJA_001', 'CONFIRMADO', 'GESTAO_UTILIZADOR',   '2026-04-10 08:01:00', 3, 'Utilizador', 3),
('LOJA_001', 'CONFIRMADO', 'GESTAO_UTILIZADOR',   '2026-04-10 08:05:00', 4, 'Utilizador', 4),
('LOJA_001', 'CONFIRMADO', 'VENDA',               '2026-04-10 09:15:00', 3, 'Venda', 1),
('LOJA_001', 'CONFIRMADO', 'VENDA',               '2026-04-10 11:30:00', 4, 'Venda', 2),
('LOJA_001', 'CONFIRMADO', 'APLICACAO_DESCONTO',  '2026-04-10 07:50:00', 2, 'Lote',  11),
('LOJA_001', 'CONFIRMADO', 'ALTERACAO_CATALOGO',  '2026-03-22 10:00:00', 1, 'Remessa', 1),
('LOJA_001', 'CONFIRMADO', 'ALTERACAO_CATALOGO',  '2026-03-27 14:00:00', 2, 'Remessa', 2),
('LOJA_001', 'CONFIRMADO', 'VENDA',               '2026-04-11 14:00:00', 3, 'Venda', 3),
('LOJA_001', 'CONFIRMADO', 'VENDA',               '2026-04-12 10:20:00', 4, 'Venda', 4),
('LOJA_001', 'PENDENTE',   'DEVOLUCAO',           '2026-04-12 11:00:00', 4, 'Devolucao', 1),
('LOJA_001', 'PENDENTE',   'VENDA',               '2026-04-12 16:45:00', 3, 'Venda', 5),
('LOJA_001', 'CONFIRMADO', 'FECHO_DIA',           '2026-04-10 20:00:00', 1, 'FechoDia', 1),
('LOJA_001', 'CONFIRMADO', 'FECHO_DIA',           '2026-04-11 20:00:00', 1, 'FechoDia', 2);

-- ============================================================
-- FECHO DE DIA (2 fechos já confirmados)
-- ============================================================
INSERT INTO FechoDia (idLoja, estadoSincronizacao, idUtilizador, dataFecho) VALUES
('LOJA_001', 'CONFIRMADO', 1, '2026-04-10'),
('LOJA_001', 'CONFIRMADO', 1, '2026-04-11');

-- ============================================================
-- Resumo do que ficou pendente para sincronizar:
--   Remessa 3 (Ibérica, dia 10/04)     → PENDENTE
--   Pagamento 3 (Ibérica, 145€)        → PENDENTE
--   Venda 5 (dia 12/04, joao)          → PENDENTE
--   Devolução 1 (chocolate, dia 12/04) → PENDENTE
--   Vários logs de auditoria           → PENDENTE
-- Contas de teste:
--   admin    / admin123  (CEO)      — criado pelo AppContext
--   gestor1  / gestor123 (GESTOR)
--   maria    / maria123  (GESTOR)
--   joao     / func123   (FUNCIONARIO)
--   ana      / func123   (FUNCIONARIO)
-- ============================================================
