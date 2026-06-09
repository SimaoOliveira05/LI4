package pt.trasmum.loja.servico.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pt.trasmum.loja.dominio.catalogo.Lote;
import pt.trasmum.loja.dominio.catalogo.Produto;
import pt.trasmum.loja.dominio.core.ConfiguracaoTerminal;
import pt.trasmum.loja.dominio.core.PerfilUtilizador;
import pt.trasmum.loja.dominio.core.Utilizador;
import pt.trasmum.loja.dominio.exceptions.ExcecoesCaixa.SessaoCaixaNaoEncontradaException;
import pt.trasmum.loja.dominio.exceptions.ExcecoesCatalogo.ProdutoInativoException;
import pt.trasmum.loja.dominio.exceptions.ExcecoesCatalogo.ProdutoNaoEncontradoException;
import pt.trasmum.loja.dominio.exceptions.ExcecoesVenda.StockInsuficienteException;
import pt.trasmum.loja.dominio.tesouraria.SessaoCaixa;
import pt.trasmum.loja.dominio.vendas.LinhaVenda;
import pt.trasmum.loja.dominio.vendas.Venda;
import pt.trasmum.loja.dominio.vendas.Venda.MetodoPagamento;
import pt.trasmum.loja.repositorio.interfaces.LoteRepositorio;
import pt.trasmum.loja.repositorio.interfaces.ProdutoRepositorio;
import pt.trasmum.loja.repositorio.interfaces.SessaoCaixaRepositorio;
import pt.trasmum.loja.repositorio.interfaces.VendaRepositorio;
import pt.trasmum.loja.servico.interfaces.IAuditoriaServico;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VendaServicoTest {

    @Mock VendaRepositorio vendaRepo;
    @Mock ProdutoRepositorio produtoRepo;
    @Mock LoteRepositorio loteRepo;
    @Mock SessaoCaixaRepositorio sessaoCaixaRepo;
    @Mock IAuditoriaServico auditoriaServico;
    @Mock ConfiguracaoTerminal configuracao;

    @InjectMocks
    VendaServico servico;

    // ── adicionarLinha ──────────────────────────────────────────────

    @Test
    void adicionarLinha_produtoNaoEncontrado_lancaExcecao() {
        when(produtoRepo.buscarPorCodigoBarras("999")).thenReturn(null);
        Venda venda = new Venda();
        assertThrows(ProdutoNaoEncontradoException.class,
                () -> servico.adicionarLinha(venda, "999", 1));
    }

    @Test
    void adicionarLinha_produtoInativo_lancaExcecao() {
        Produto p = new Produto("111", "Produto A", "cat", 2.0, 1);
        p.setAtivo(false);
        when(produtoRepo.buscarPorCodigoBarras("111")).thenReturn(p);
        Venda venda = new Venda();
        assertThrows(ProdutoInativoException.class,
                () -> servico.adicionarLinha(venda, "111", 1));
    }

    @Test
    void adicionarLinha_stockInsuficiente_lancaExcecao() {
        Produto p = new Produto("222", "Produto B", "cat", 3.0, 1);
        p.setId(10);
        when(produtoRepo.buscarPorCodigoBarras("222")).thenReturn(p);
        Lote lote = new Lote(10, 2, LocalDate.now().plusDays(10), 3.0);
        lote.setId(1);
        when(loteRepo.buscarLotesFEFO(10)).thenReturn(List.of(lote));
        Venda venda = new Venda();
        assertThrows(StockInsuficienteException.class,
                () -> servico.adicionarLinha(venda, "222", 5));
    }

    @Test
    void adicionarLinha_fefoComDoisLotes_ambosOsLotesAtualizados() {
        Produto p = new Produto("333", "Produto C", "cat", 4.0, 1);
        p.setId(20);
        when(produtoRepo.buscarPorCodigoBarras("333")).thenReturn(p);

        Lote lote1 = new Lote(20, 3, LocalDate.now().plusDays(5), 4.0);
        lote1.setId(1);
        Lote lote2 = new Lote(20, 5, LocalDate.now().plusDays(30), 4.0);
        lote2.setId(2);
        when(loteRepo.buscarLotesFEFO(20)).thenReturn(List.of(lote1, lote2));

        Venda venda = new Venda();
        LinhaVenda linha = servico.adicionarLinha(venda, "333", 5);

        assertNotNull(linha);
        verify(loteRepo, times(2)).atualizar(any());
        assertEquals(0, lote1.getQuantidade());
        assertEquals(3, lote2.getQuantidade());
    }

    // ── finalizarVenda ──────────────────────────────────────────────

    @Test
    void finalizarVenda_numerarioInsuficiente_lancaIllegalArgumentException() {
        Venda venda = vendaComLinhas(10.0);
        assertThrows(IllegalArgumentException.class,
                () -> servico.finalizarVenda(venda, MetodoPagamento.NUMERARIO, 5.0));
    }

    @Test
    void finalizarVenda_semSessaoCaixa_lancaSessaoCaixaNaoEncontradaException() {
        Venda venda = vendaComLinhas(10.0);
        when(sessaoCaixaRepo.buscarSessaoAtiva(anyInt())).thenReturn(null);
        assertThrows(SessaoCaixaNaoEncontradaException.class,
                () -> servico.finalizarVenda(venda, MetodoPagamento.NUMERARIO, 20.0));
    }

    @Test
    void finalizarVenda_caminhoFeliz_guardaVendaAtualizaSessaoRegistaAuditoria() {
        Venda venda = vendaComLinhas(10.0);
        SessaoCaixa sessao = new SessaoCaixa("L01", 1, 50.0);
        when(sessaoCaixaRepo.buscarSessaoAtiva(anyInt())).thenReturn(sessao);

        servico.finalizarVenda(venda, MetodoPagamento.NUMERARIO, 15.0);

        verify(vendaRepo).guardar(venda);
        verify(sessaoCaixaRepo).atualizar(sessao);
        verify(auditoriaServico).registar(any(), any(), any(), anyInt(), any());
        assertEquals(60.0, sessao.getSaldoAtual(), 0.001);
    }

    @Test
    void finalizarVenda_saldoExcedeLimite_naoLancaExcecao() {
        // O alerta de sangria é responsabilidade do controller — o serviço não deve lançar exceção
        Venda venda = vendaComLinhas(10.0);
        SessaoCaixa sessao = new SessaoCaixa("L01", 1, 490.0);
        when(sessaoCaixaRepo.buscarSessaoAtiva(anyInt())).thenReturn(sessao);

        assertDoesNotThrow(() -> servico.finalizarVenda(venda, MetodoPagamento.NUMERARIO, 15.0));
        assertEquals(500.0, sessao.getSaldoAtual(), 0.001);
        verify(vendaRepo).guardar(venda);
    }

    // ── calcularTroco ───────────────────────────────────────────────

    @Test
    void calcularTroco_valorInferior_lancaIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> servico.calcularTroco(10.0, 5.0));
    }

    @Test
    void calcularTroco_valorSuperior_devolveDiferenca() {
        double troco = servico.calcularTroco(10.0, 15.0);
        assertEquals(5.0, troco, 0.001);
    }

    // ── iniciarVenda ────────────────────────────────────────────────

    @Test
    void iniciarVenda_semSessaoAtiva_lancaSessaoCaixaNaoEncontradaException() {
        Utilizador u = new Utilizador("func", "h", PerfilUtilizador.FUNCIONARIO);
        u.setId(5);
        when(sessaoCaixaRepo.buscarSessaoAtiva(5)).thenReturn(null);
        assertThrows(SessaoCaixaNaoEncontradaException.class, () -> servico.iniciarVenda(u));
    }

    @Test
    void iniciarVenda_caminhoFeliz_devolveVendaEmCurso() {
        Utilizador u = new Utilizador("func", "h", PerfilUtilizador.FUNCIONARIO);
        u.setId(5);
        when(sessaoCaixaRepo.buscarSessaoAtiva(5)).thenReturn(new SessaoCaixa());
        when(configuracao.getIdLoja()).thenReturn("L01");

        Venda venda = servico.iniciarVenda(u);

        assertNotNull(venda);
        assertEquals(Venda.EstadoVenda.EM_CURSO, venda.getEstado());
    }

    // ── anularVenda ─────────────────────────────────────────────────

    @Test
    void anularVenda_repoeStockEGuardaVendaAnulada() {
        Venda venda = new Venda();
        venda.setIdUtilizador(1);
        LinhaVenda linha = new LinhaVenda(1, 3, 5.0);
        venda.adicionarLinha(linha);

        Lote lote = new Lote(10, 7, LocalDate.now().plusDays(30), 5.0);
        lote.setId(1);
        when(loteRepo.buscarPorId(1)).thenReturn(lote);

        servico.anularVenda(venda);

        assertEquals(10, lote.getQuantidade());
        assertEquals(Venda.EstadoVenda.ANULADA, venda.getEstado());
        verify(vendaRepo).guardar(venda);
        verify(auditoriaServico).registar(any(), any(), any(), anyInt(), any());
    }

    // ── obterVendasPendentes / buscarPorNumeroFatura ────────────────

    @Test
    void obterVendasPendentes_delegaParaRepositorio() {
        List<Venda> esperado = List.of(new Venda());
        when(vendaRepo.buscarPendentes()).thenReturn(esperado);
        assertSame(esperado, servico.obterVendasPendentes());
    }

    @Test
    void buscarPorNumeroFatura_delegaParaRepositorio() {
        Venda v = new Venda();
        when(vendaRepo.buscarPorNumeroFatura("FAT-001")).thenReturn(v);
        assertSame(v, servico.buscarPorNumeroFatura("FAT-001"));
    }

    // ── helpers ─────────────────────────────────────────────────────

    private Venda vendaComLinhas(double precoUnitario) {
        Venda venda = new Venda();
        venda.setIdUtilizador(1);
        LinhaVenda linha = new LinhaVenda(1, 1, precoUnitario);
        venda.adicionarLinha(linha);
        return venda;
    }
}
