package pt.trasmum.loja.servico.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pt.trasmum.loja.dominio.catalogo.Lote;
import pt.trasmum.loja.dominio.catalogo.Produto;
import pt.trasmum.loja.dominio.core.ConfiguracaoTerminal;
import pt.trasmum.loja.dominio.core.Utilizador;
import pt.trasmum.loja.dominio.core.PerfilUtilizador;
import pt.trasmum.loja.dominio.exceptions.ExcecoesVenda.*;
import pt.trasmum.loja.dominio.vendas.Devolucao;
import pt.trasmum.loja.dominio.vendas.Fatura;
import pt.trasmum.loja.dominio.vendas.LinhaVenda;
import pt.trasmum.loja.dominio.vendas.Venda;
import pt.trasmum.loja.repositorio.interfaces.DevolucaoRepositorio;
import pt.trasmum.loja.repositorio.interfaces.LoteRepositorio;
import pt.trasmum.loja.repositorio.interfaces.ProdutoRepositorio;
import pt.trasmum.loja.repositorio.interfaces.VendaRepositorio;
import pt.trasmum.loja.servico.interfaces.IAuditoriaServico;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DevolucaoServicoTest {

    @Mock VendaRepositorio vendaRepo;
    @Mock DevolucaoRepositorio devolucaoRepo;
    @Mock LoteRepositorio loteRepo;
    @Mock ProdutoRepositorio produtoRepo;
    @Mock IAuditoriaServico auditoriaServico;
    @Mock ConfiguracaoTerminal configuracao;

    @InjectMocks
    DevolucaoServico servico;

    private static final String NUM_FATURA = "FAT-L01-001";
    private static final String EAN = "1234567890";
    private static final int ID_PRODUTO = 10;
    private static final int ID_LOTE = 1;

    // ── Guarda 1: fatura existe? ────────────────────────────────────

    @Test
    void processar_faturaNula_lancaFaturaNaoEncontradaException() {
        when(vendaRepo.buscarPorNumeroFatura(NUM_FATURA)).thenReturn(null);
        Utilizador u = utilizador();
        assertThrows(FaturaNaoEncontradaException.class,
                () -> servico.processar(u, NUM_FATURA, EAN, LocalDate.now(), 1));
    }

    @Test
    void processar_vendaSemFatura_lancaFaturaNaoEncontradaException() {
        Venda venda = new Venda();
        when(vendaRepo.buscarPorNumeroFatura(NUM_FATURA)).thenReturn(venda);
        Utilizador u = utilizador();
        assertThrows(FaturaNaoEncontradaException.class,
                () -> servico.processar(u, NUM_FATURA, EAN, LocalDate.now(), 1));
    }

    // ── Guarda 2: produto consta na fatura? ─────────────────────────

    @Test
    void processar_artigoNaoConstaNaFatura_lancaArtigoNaoConstaNaFaturaException() {
        Venda venda = vendaComFatura();
        when(vendaRepo.buscarPorNumeroFatura(NUM_FATURA)).thenReturn(venda);

        Produto produto = produto();
        when(produtoRepo.buscarPorCodigoBarras(EAN)).thenReturn(produto);
        // venda não tem linhas → encontrarLinhaComProduto retorna null diretamente

        assertThrows(ArtigoNaoConstaNaFaturaException.class,
                () -> servico.processar(utilizador(), NUM_FATURA, EAN, LocalDate.now(), 1));
    }

    // ── Guarda 3: quantidade já totalmente devolvida ────────────────

    @Test
    void processar_quantidadeTotalmenteDevolvida_lancaDevolucaoJaProcessadaException() {
        Venda venda = vendaComFaturaELinha(ID_LOTE, 2);
        when(vendaRepo.buscarPorNumeroFatura(NUM_FATURA)).thenReturn(venda);

        Produto produto = produto();
        when(produtoRepo.buscarPorCodigoBarras(EAN)).thenReturn(produto);

        Lote lote = lote(ID_LOTE, ID_PRODUTO);
        when(loteRepo.buscarPorProduto(ID_PRODUTO)).thenReturn(List.of(lote));

        // Já devolveu 2 (igual à quantidade vendida)
        Devolucao devPrev = new Devolucao();
        devPrev.setIdLote(ID_LOTE);
        devPrev.setQuantidade(2);
        when(devolucaoRepo.buscarPorFatura(NUM_FATURA)).thenReturn(List.of(devPrev));

        assertThrows(DevolucaoJaProcessadaException.class,
                () -> servico.processar(utilizador(), NUM_FATURA, EAN, LocalDate.now(), 1));
    }

    // ── Guarda 3b: quantidade a devolver excede disponível ──────────

    @Test
    void processar_quantidadeExcedeDisponivel_lancaQuantidadeDevolucaoExcedidaException() {
        Venda venda = vendaComFaturaELinha(ID_LOTE, 2);
        when(vendaRepo.buscarPorNumeroFatura(NUM_FATURA)).thenReturn(venda);

        Produto produto = produto();
        when(produtoRepo.buscarPorCodigoBarras(EAN)).thenReturn(produto);

        Lote lote = lote(ID_LOTE, ID_PRODUTO);
        when(loteRepo.buscarPorProduto(ID_PRODUTO)).thenReturn(List.of(lote));

        // Já devolveu 1, disponível = 1, pede devolver 2
        Devolucao devPrev = new Devolucao();
        devPrev.setIdLote(ID_LOTE);
        devPrev.setQuantidade(1);
        when(devolucaoRepo.buscarPorFatura(NUM_FATURA)).thenReturn(List.of(devPrev));

        assertThrows(QuantidadeDevolucaoExcedidaException.class,
                () -> servico.processar(utilizador(), NUM_FATURA, EAN, LocalDate.now(), 2));
    }

    // ── Guarda 4a: lote ainda válido → repõe stock ──────────────────

    @Test
    void processar_loteValido_repoeStockEPersisteDevolucao() {
        Venda venda = vendaComFaturaELinha(ID_LOTE, 3);
        when(vendaRepo.buscarPorNumeroFatura(NUM_FATURA)).thenReturn(venda);

        Produto produto = produto();
        when(produtoRepo.buscarPorCodigoBarras(EAN)).thenReturn(produto);

        Lote lote = lote(ID_LOTE, ID_PRODUTO);
        lote.setQuantidade(5);
        when(loteRepo.buscarPorProduto(ID_PRODUTO)).thenReturn(List.of(lote));
        when(devolucaoRepo.buscarPorFatura(NUM_FATURA)).thenReturn(List.of());
        when(loteRepo.buscarLotesFEFO(ID_PRODUTO)).thenReturn(List.of(lote));
        when(configuracao.getIdLoja()).thenReturn("L01");

        LocalDate validadeValida = LocalDate.now().plusDays(30);
        servico.processar(utilizador(), NUM_FATURA, EAN, validadeValida, 1);

        verify(loteRepo).atualizar(lote);
        verify(devolucaoRepo).guardar(any(Devolucao.class));
        assertEquals(6, lote.getQuantidade());
    }

    // ── Guarda 4b: lote expirado → não repõe stock ──────────────────

    @Test
    void processar_loteExpirado_naoRepoeStockMasPersisteDevolucao() {
        Venda venda = vendaComFaturaELinha(ID_LOTE, 3);
        when(vendaRepo.buscarPorNumeroFatura(NUM_FATURA)).thenReturn(venda);

        Produto produto = produto();
        when(produtoRepo.buscarPorCodigoBarras(EAN)).thenReturn(produto);

        Lote lote = lote(ID_LOTE, ID_PRODUTO);
        lote.setQuantidade(5);
        when(loteRepo.buscarPorProduto(ID_PRODUTO)).thenReturn(List.of(lote));
        when(devolucaoRepo.buscarPorFatura(NUM_FATURA)).thenReturn(List.of());
        when(loteRepo.buscarLotesFEFO(ID_PRODUTO)).thenReturn(List.of(lote));
        when(configuracao.getIdLoja()).thenReturn("L01");

        LocalDate validadeExpirada = LocalDate.now().minusDays(1);
        servico.processar(utilizador(), NUM_FATURA, EAN, validadeExpirada, 1);

        verify(loteRepo, never()).atualizar(any());
        verify(devolucaoRepo).guardar(any(Devolucao.class));
        assertEquals(5, lote.getQuantidade());
    }

    // ── obterDevolucoesPendentes ────────────────────────────────────

    @Test
    void obterDevolucoesPendentes_delegaParaRepositorio() {
        List<Devolucao> esperado = List.of(new Devolucao());
        when(devolucaoRepo.buscarPendentes()).thenReturn(esperado);
        assertSame(esperado, servico.obterDevolucoesPendentes());
    }

    // ── helpers ─────────────────────────────────────────────────────

    private Utilizador utilizador() {
        return new Utilizador("op", "hash", PerfilUtilizador.FUNCIONARIO);
    }

    private Produto produto() {
        Produto p = new Produto(EAN, "Produto Teste", "cat", 5.0, 1);
        p.setId(ID_PRODUTO);
        return p;
    }

    private Lote lote(int id, int idProduto) {
        Lote l = new Lote(idProduto, 10, LocalDate.now().plusDays(60), 5.0);
        l.setId(id);
        return l;
    }

    private Venda vendaComFatura() {
        Venda venda = new Venda();
        Fatura fatura = new Fatura();
        fatura.setId(1);
        fatura.setNumeroFatura(NUM_FATURA);
        venda.setFatura(fatura);
        return venda;
    }

    private Venda vendaComFaturaELinha(int idLote, int quantidade) {
        Venda venda = vendaComFatura();
        LinhaVenda linha = new LinhaVenda(idLote, quantidade, 5.0);
        venda.adicionarLinha(linha);
        return venda;
    }
}
