package pt.trasmum.loja.servico.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pt.trasmum.loja.dominio.catalogo.Lote;
import pt.trasmum.loja.dominio.catalogo.Produto;
import pt.trasmum.loja.dominio.core.*;
import pt.trasmum.loja.dominio.exceptions.EnvioFechoFalhouException;
import pt.trasmum.loja.dominio.exceptions.ExcecoesSeguranca.AcessoNegadoException;
import pt.trasmum.loja.dominio.fornecedores.Fornecedor;
import pt.trasmum.loja.dominio.fornecedores.Pagamento;
import pt.trasmum.loja.dominio.fornecedores.Remessa;
import pt.trasmum.loja.dominio.tesouraria.SessaoCaixa;
import pt.trasmum.loja.dominio.vendas.Devolucao;
import pt.trasmum.loja.dominio.vendas.LinhaVenda;
import pt.trasmum.loja.dominio.vendas.Venda;
import pt.trasmum.loja.repositorio.interfaces.*;
import pt.trasmum.loja.servico.interfaces.*;
import pt.trasmum.loja.sincronizacao.SincronizacaoGateway;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FechoDiaServicoTest {

    @Mock IVendaServico vendaServico;
    @Mock IDevolucaoServico devolucaoServico;
    @Mock ICaixaServico caixaServico;
    @Mock IRemessaServico remessaServico;
    @Mock IAuditoriaServico auditoriaServico;
    @Mock IAutorizacaoServico autorizacaoServico;
    @Mock FechoDiaRepositorio fechoDiaRepo;
    @Mock VendaRepositorio vendaRepo;
    @Mock DevolucaoRepositorio devolucaoRepo;
    @Mock RemessaRepositorio remessaRepo;
    @Mock PagamentoRepositorio pagamentoRepo;
    @Mock SessaoCaixaRepositorio sessaoCaixaRepo;
    @Mock LogAuditoriaRepositorio logRepo;
    @Mock ProdutoRepositorio produtoRepo;
    @Mock LoteRepositorio loteRepo;
    @Mock FornecedorRepositorio fornecedorRepo;
    @Mock UtilizadorRepositorio utilizadorRepo;
    @Mock SincronizacaoGateway sincronizacaoGateway;
    @Mock ConfiguracaoTerminal configuracao;

    @InjectMocks
    FechoDiaServico servico;

    // ── executarFecho ───────────────────────────────────────────────

    @Test
    void executarFecho_perfilFuncionario_lancaAcessoNegadoEGatewayNuncaInvocado() {
        Utilizador funcionario = new Utilizador("func", "h", PerfilUtilizador.FUNCIONARIO);
        doThrow(new AcessoNegadoException("Acesso negado"))
                .when(autorizacaoServico).exigirPerfil(
                        any(Utilizador.class),
                        any(PerfilUtilizador.class),
                        any(PerfilUtilizador.class));

        assertThrows(AcessoNegadoException.class,
                () -> servico.executarFecho(funcionario, configuracao));

        verify(sincronizacaoGateway, never()).enviarFechoDia(any());
    }

    @Test
    void executarFecho_envioFalha_lancaExcecaoEReverte() {
        Utilizador gestor = gestorComPerfil();
        configurarListasPendentesVazias();
        when(configuracao.getIdLoja()).thenReturn("L01");
        when(configuracao.getNomeLoja()).thenReturn("Loja Teste");
        when(sincronizacaoGateway.enviarFechoDia(any())).thenReturn(false);

        assertThrows(EnvioFechoFalhouException.class,
                () -> servico.executarFecho(gestor, configuracao));

        verify(vendaRepo).reverterEmTransito();
    }

    @Test
    void executarFecho_sucesso_guardaFechoERegistaAuditoria() {
        Utilizador gestor = gestorComPerfil();
        configurarListasPendentesVazias();
        when(configuracao.getIdLoja()).thenReturn("L01");
        when(configuracao.getNomeLoja()).thenReturn("Loja Teste");
        when(sincronizacaoGateway.enviarFechoDia(any())).thenReturn(true);

        servico.executarFecho(gestor, configuracao);

        verify(fechoDiaRepo).guardar(any());
        verify(auditoriaServico).registar(eq(gestor), eq(TipoAcao.FECHO_DIA), any(), anyInt(), any());
    }

    // ── executarFecho com listas não-vazias (cobre mapeadores e lambdas forEach) ──

    @Test
    void executarFecho_comListasNaoVazias_cobremMapeadoresELambdas() {
        Utilizador gestor = gestorComPerfil();

        Venda venda = new Venda("L01", 1, Venda.MetodoPagamento.NUMERARIO);
        LinhaVenda linha = new LinhaVenda(10, 2, 5.0);
        venda.adicionarLinha(linha);
        Lote lote = new Lote(20, 5, LocalDate.now().plusDays(30), 5.0);
        lote.setId(10);
        Produto produto = new Produto("111", "Prod A", "cat", 5.0, 1);
        produto.setId(20);
        when(loteRepo.buscarPorId(10)).thenReturn(lote);
        when(produtoRepo.buscarPorId(20)).thenReturn(produto);

        Devolucao devolucao = new Devolucao("L01", 1, "FAT-001", 1, 10, 1, LocalDate.now(), 5.0);
        SessaoCaixa sessao = new SessaoCaixa("L01", 1, 100.0);

        Remessa remessa = new Remessa("L01", 5, 1, LocalDate.now(), 200.0);
        remessa.setId(1);
        Fornecedor fornecedor = new Fornecedor("Forn A", "123456789");
        when(fornecedorRepo.buscarPorId(5)).thenReturn(fornecedor);

        Pagamento pagamento = new Pagamento("L01", 5, 1, 200.0);
        pagamento.setEstadoPagamento(Pagamento.EstadoPagamento.PAGO);

        LogAuditoria log = new LogAuditoria("L01", TipoAcao.VENDA, 1, "Venda", 1, "desc");
        Utilizador utilizador = new Utilizador("op", "h", PerfilUtilizador.FUNCIONARIO);
        when(utilizadorRepo.buscarPorId(1)).thenReturn(utilizador);

        when(vendaServico.obterVendasPendentes()).thenReturn(List.of(venda));
        when(devolucaoServico.obterDevolucoesPendentes()).thenReturn(List.of(devolucao));
        when(caixaServico.obterSessoesPendentes()).thenReturn(List.of(sessao));
        when(remessaServico.obterRemessasPendentes()).thenReturn(List.of(remessa));
        when(remessaServico.obterPagamentosPendentes()).thenReturn(List.of(pagamento));
        when(auditoriaServico.obterLogsPendentes()).thenReturn(List.of(log));
        when(configuracao.getIdLoja()).thenReturn("L01");
        when(configuracao.getNomeLoja()).thenReturn("Loja Teste");
        when(sincronizacaoGateway.enviarFechoDia(any())).thenReturn(true);

        servico.executarFecho(gestor, configuracao);

        verify(fechoDiaRepo).guardar(any());
        // atualizarSincronizacao chamado 2× por venda/devolucao/remessa (em_transito + confirmado)
        verify(vendaRepo, times(2)).atualizarSincronizacao(anyInt(), any());
    }

    // ── reenviar ────────────────────────────────────────────────────

    @Test
    void reenviar_utilizadorNulo_devolveFalse() {
        assertFalse(servico.reenviar(1, null));
    }

    @Test
    void reenviar_envioFalha_devolveFalse() {
        Utilizador gestor = gestorComPerfil();
        configurarListasPendentesVazias();
        when(configuracao.getIdLoja()).thenReturn("L01");
        when(configuracao.getNomeLoja()).thenReturn("Loja Teste");
        when(sincronizacaoGateway.enviarFechoDia(any())).thenReturn(false);

        assertFalse(servico.reenviar(1, gestor));
    }

    // ── reverterEmTransito ──────────────────────────────────────────

    @Test
    void reverterEmTransito_invocaReverterEmTodosOsRepositorios() {
        servico.reverterEmTransito();

        verify(vendaRepo).reverterEmTransito();
        verify(devolucaoRepo).reverterEmTransito();
        verify(sessaoCaixaRepo).reverterEmTransito();
        verify(remessaRepo).reverterEmTransito();
        verify(pagamentoRepo).reverterEmTransito();
        verify(logRepo).reverterEmTransito();
        verify(fechoDiaRepo).reverterEmTransito();
    }

    // ── helpers ─────────────────────────────────────────────────────

    private Utilizador gestorComPerfil() {
        return new Utilizador("gestor", "h", PerfilUtilizador.GESTOR);
    }

    private void configurarListasPendentesVazias() {
        when(vendaServico.obterVendasPendentes()).thenReturn(List.of());
        when(devolucaoServico.obterDevolucoesPendentes()).thenReturn(List.of());
        when(caixaServico.obterSessoesPendentes()).thenReturn(List.of());
        when(remessaServico.obterRemessasPendentes()).thenReturn(List.of());
        when(remessaServico.obterPagamentosPendentes()).thenReturn(List.of());
        when(auditoriaServico.obterLogsPendentes()).thenReturn(List.of());
    }
}
