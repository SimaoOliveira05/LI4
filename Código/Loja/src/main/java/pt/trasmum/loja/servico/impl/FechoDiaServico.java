package pt.trasmum.loja.servico.impl;

import pt.trasmum.loja.dominio.catalogo.Lote;
import pt.trasmum.loja.dominio.catalogo.Produto;
import pt.trasmum.loja.dominio.core.ConfiguracaoTerminal;
import pt.trasmum.loja.dominio.core.LogAuditoria;
import pt.trasmum.loja.dominio.core.PerfilUtilizador;
import pt.trasmum.loja.dominio.core.TipoAcao;
import pt.trasmum.loja.dominio.core.Utilizador;
import pt.trasmum.loja.dominio.exceptions.EnvioFechoFalhouException;
import pt.trasmum.loja.dominio.fornecedores.Fornecedor;
import pt.trasmum.loja.dominio.fornecedores.Pagamento;
import pt.trasmum.loja.dominio.fornecedores.Remessa;
import pt.trasmum.loja.dominio.tesouraria.FechoDia;
import pt.trasmum.loja.dominio.tesouraria.SessaoCaixa;
import pt.trasmum.loja.dominio.vendas.Devolucao;
import pt.trasmum.loja.dominio.vendas.Venda;
import pt.trasmum.loja.repositorio.interfaces.*;
import pt.trasmum.loja.servico.interfaces.*;
import pt.trasmum.loja.sincronizacao.PacoteFechoDTO;
import pt.trasmum.loja.sincronizacao.PacoteFechoDTO.*;
import pt.trasmum.loja.sincronizacao.SincronizacaoGateway;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FechoDiaServico implements IFechoDiaServico {

    private final IVendaServico vendaServico;
    private final IDevolucaoServico devolucaoServico;
    private final ICaixaServico caixaServico;
    private final IRemessaServico remessaServico;
    private final IAuditoriaServico auditoriaServico;
    private final IAutorizacaoServico autorizacaoServico;
    private final FechoDiaRepositorio fechoDiaRepo;
    private final VendaRepositorio vendaRepo;
    private final DevolucaoRepositorio devolucaoRepo;
    private final RemessaRepositorio remessaRepo;
    private final PagamentoRepositorio pagamentoRepo;
    private final SessaoCaixaRepositorio sessaoCaixaRepo;
    private final LogAuditoriaRepositorio logRepo;
    private final ProdutoRepositorio produtoRepo;
    private final LoteRepositorio loteRepo;
    private final FornecedorRepositorio fornecedorRepo;
    private final UtilizadorRepositorio utilizadorRepo;
    private final SincronizacaoGateway sincronizacaoGateway;
    private final ConfiguracaoTerminal configuracao;

    public FechoDiaServico(IVendaServico vendaServico, IDevolucaoServico devolucaoServico,
                            ICaixaServico caixaServico, IRemessaServico remessaServico,
                            IAuditoriaServico auditoriaServico, IAutorizacaoServico autorizacaoServico,
                            FechoDiaRepositorio fechoDiaRepo, VendaRepositorio vendaRepo,
                            DevolucaoRepositorio devolucaoRepo, RemessaRepositorio remessaRepo,
                            PagamentoRepositorio pagamentoRepo, SessaoCaixaRepositorio sessaoCaixaRepo,
                            LogAuditoriaRepositorio logRepo, ProdutoRepositorio produtoRepo,
                            LoteRepositorio loteRepo, FornecedorRepositorio fornecedorRepo,
                            UtilizadorRepositorio utilizadorRepo,
                            SincronizacaoGateway sincronizacaoGateway,
                            ConfiguracaoTerminal configuracao) {
        this.vendaServico = vendaServico;
        this.devolucaoServico = devolucaoServico;
        this.caixaServico = caixaServico;
        this.remessaServico = remessaServico;
        this.auditoriaServico = auditoriaServico;
        this.autorizacaoServico = autorizacaoServico;
        this.fechoDiaRepo = fechoDiaRepo;
        this.vendaRepo = vendaRepo;
        this.devolucaoRepo = devolucaoRepo;
        this.remessaRepo = remessaRepo;
        this.pagamentoRepo = pagamentoRepo;
        this.sessaoCaixaRepo = sessaoCaixaRepo;
        this.logRepo = logRepo;
        this.produtoRepo = produtoRepo;
        this.loteRepo = loteRepo;
        this.fornecedorRepo = fornecedorRepo;
        this.utilizadorRepo = utilizadorRepo;
        this.sincronizacaoGateway = sincronizacaoGateway;
        this.configuracao = configuracao;
    }

    @Override
    public FechoDia executarFecho(Utilizador utilizador, ConfiguracaoTerminal config) {
        autorizacaoServico.exigirPerfil(utilizador, PerfilUtilizador.GESTOR, PerfilUtilizador.CEO);

        List<Venda> vendas = vendaServico.obterVendasPendentes();
        List<Devolucao> devolucoes = devolucaoServico.obterDevolucoesPendentes();
        List<SessaoCaixa> sessoes = caixaServico.obterSessoesPendentes();
        List<Remessa> remessas = remessaServico.obterRemessasPendentes();
        List<Pagamento> pagamentos = remessaServico.obterPagamentosPendentes();
        List<LogAuditoria> logs = auditoriaServico.obterLogsPendentes();

        vendas.forEach(v -> { v.marcarEmTransito(); atualizarSincVenda(v); });
        devolucoes.forEach(d -> { d.marcarEmTransito(); atualizarSincDevolucao(d); });
        sessoes.forEach(s -> { s.marcarEmTransito(); sessaoCaixaRepo.atualizar(s); });
        remessas.forEach(r -> { r.marcarEmTransito(); atualizarSincRemessa(r); });
        pagamentos.forEach(p -> { p.marcarEmTransito(); pagamentoRepo.atualizar(p); });
        logs.forEach(l -> { l.marcarEmTransito(); logRepo.atualizar(l); });

        PacoteFechoDTO pacote = construirPacote(config, vendas, devolucoes, remessas, pagamentos, sessoes, logs);

        boolean sucesso = sincronizacaoGateway.enviarFechoDia(pacote);

        FechoDia fecho = new FechoDia(config.getIdLoja(), utilizador.getId(), LocalDate.now());

        if (!sucesso) {
            reverterEmTransito();
            fechoDiaRepo.guardar(fecho);
            throw new EnvioFechoFalhouException("Falha no envio do fecho de dia ao servidor central.");
        }

        vendas.forEach(v -> { v.marcarConfirmado(); atualizarSincVenda(v); });
        devolucoes.forEach(d -> { d.marcarConfirmado(); atualizarSincDevolucao(d); });
        sessoes.forEach(s -> { s.marcarConfirmado(); sessaoCaixaRepo.atualizar(s); });
        remessas.forEach(r -> { r.marcarConfirmado(); atualizarSincRemessa(r); });
        pagamentos.forEach(p -> { p.marcarConfirmado(); pagamentoRepo.atualizar(p); });
        logs.forEach(l -> { l.marcarConfirmado(); logRepo.atualizar(l); });

        fecho.marcarConfirmado();
        fechoDiaRepo.guardar(fecho);

        auditoriaServico.registar(utilizador, TipoAcao.FECHO_DIA, "FechoDia", fecho.getId());

        return fecho;
    }

    @Override
    public boolean reenviar(int idFecho) {
        reverterEmTransito();
        Utilizador utilizadorAtual = pt.trasmum.loja.app.AppContext.getInstance().utilizadorAtual;
        if (utilizadorAtual == null) return false;
        try {
            executarFecho(utilizadorAtual, configuracao);
            return true;
        } catch (EnvioFechoFalhouException e) {
            return false;
        }
    }

    @Override
    public void reverterEmTransito() {
        vendaRepo.buscarPendentes();
        devolucaoRepo.buscarPendentes();
        sessaoCaixaRepo.buscarPendentes();
        remessaRepo.buscarPendentes();
        pagamentoRepo.buscarPendentes();
        logRepo.buscarPendentes();
        fechoDiaRepo.buscarPendentes();
    }

    private PacoteFechoDTO construirPacote(ConfiguracaoTerminal config,
            List<Venda> vendas, List<Devolucao> devolucoes, List<Remessa> remessas,
            List<Pagamento> pagamentos, List<SessaoCaixa> sessoes, List<LogAuditoria> logs) {
        PacoteFechoDTO p = new PacoteFechoDTO();
        p.idLoja = config.getIdLoja();
        p.nomeLoja = config.getNomeLoja();
        p.dataFecho = LocalDate.now().toString();
        p.vendas = mapVendas(vendas);
        p.devolucoes = mapDevolucoes(devolucoes);
        p.remessas = mapRemessas(remessas, pagamentos);
        p.pagamentos = mapPagamentos(pagamentos);
        p.sessoesCaixa = mapSessoes(sessoes);
        p.logs = mapLogs(logs);
        return p;
    }

    private List<VendaDTO> mapVendas(List<Venda> vendas) {
        Map<Integer, Produto> produtoCache = new HashMap<>();
        Map<Integer, Lote> loteCache = new HashMap<>();
        List<VendaDTO> dtos = new ArrayList<>();
        for (Venda v : vendas) {
            VendaDTO dto = new VendaDTO();
            dto.idOriginalLoja = v.getId();
            dto.dataHora = v.getDataHora() != null ? v.getDataHora().toString() : null;
            dto.totalFaturado = v.getTotalFaturado();
            dto.metodoPagamento = v.getMetodoPagamento() != null ? v.getMetodoPagamento().name() : null;
            dto.linhas = new ArrayList<>();
            for (var linha : v.getLinhas()) {
                LinhaVendaDTO l = new LinhaVendaDTO();
                l.idOriginalLoja = linha.getId();
                l.quantidade = linha.getQuantidade();
                l.precoUnitario = linha.getPrecoUnitario();
                l.subtotal = linha.calcularSubtotal();
                Lote lote = loteCache.computeIfAbsent(linha.getIdLote(), loteRepo::buscarPorId);
                if (lote != null) {
                    Produto prod = produtoCache.computeIfAbsent(lote.getIdProduto(), produtoRepo::buscarPorId);
                    if (prod != null) {
                        l.nomeProduto = prod.getNome();
                        l.categoria = prod.getCategoria();
                    }
                }
                dto.linhas.add(l);
            }
            dtos.add(dto);
        }
        return dtos;
    }

    private List<DevolucaoDTO> mapDevolucoes(List<Devolucao> devs) {
        List<DevolucaoDTO> dtos = new ArrayList<>();
        for (Devolucao d : devs) {
            DevolucaoDTO dto = new DevolucaoDTO();
            dto.idOriginalLoja = d.getId();
            dto.idOriginalVenda = d.getIdFatura();
            dto.dataHora = d.getDataHora() != null ? d.getDataHora().toString() : null;
            dto.quantidade = d.getQuantidade();
            dto.valorRestituido = d.getValorRestituido();
            dtos.add(dto);
        }
        return dtos;
    }

    private List<RemessaDTO> mapRemessas(List<Remessa> remessas, List<Pagamento> pagamentos) {
        Map<Integer, Fornecedor> fornCache = new HashMap<>();
        Map<Integer, Boolean> pagaPorRemessa = new HashMap<>();
        for (Pagamento p : pagamentos) {
            if (p.getEstadoPagamento() == Pagamento.EstadoPagamento.PAGO) {
                pagaPorRemessa.put(p.getIdRemessa(), true);
            }
        }
        List<RemessaDTO> dtos = new ArrayList<>();
        for (Remessa r : remessas) {
            RemessaDTO dto = new RemessaDTO();
            dto.idOriginalLoja = r.getId();
            Fornecedor f = fornCache.computeIfAbsent(r.getIdFornecedor(), fornecedorRepo::buscarPorId);
            dto.nomeFornecedor = f != null ? f.getNome() : "Desconhecido";
            dto.dataRecepcao = r.getDataRecepcao() != null ? r.getDataRecepcao().toString() : null;
            dto.valorTotalGuia = r.getValorTotalGuia();
            dto.estadoPagamento = pagaPorRemessa.getOrDefault(r.getId(), false) ? "PAGA" : "PENDENTE_PAGAMENTO";
            dtos.add(dto);
        }
        return dtos;
    }

    private List<PagamentoDTO> mapPagamentos(List<Pagamento> pagamentos) {
        List<PagamentoDTO> dtos = new ArrayList<>();
        for (Pagamento p : pagamentos) {
            PagamentoDTO dto = new PagamentoDTO();
            dto.idOriginalLoja = p.getId();
            dto.idOriginalRemessa = p.getIdRemessa();
            dto.valor = p.getValor();
            dto.dataPagamento = p.getDataHora() != null ? p.getDataHora().toString() : null;
            dto.tipoPagamento = null;
            dtos.add(dto);
        }
        return dtos;
    }

    private List<SessaoCaixaDTO> mapSessoes(List<SessaoCaixa> sessoes) {
        List<SessaoCaixaDTO> dtos = new ArrayList<>();
        for (SessaoCaixa s : sessoes) {
            SessaoCaixaDTO dto = new SessaoCaixaDTO();
            dto.idOriginalLoja = s.getId();
            dto.idUtilizador = s.getIdUtilizador();
            dto.saldoFinal = s.getSaldoAtual();
            dto.dataAbertura = s.getDataAbertura() != null ? s.getDataAbertura().toString() : null;
            dto.dataEncerramento = s.getDataEncerramento() != null ? s.getDataEncerramento().toString() : null;
            dtos.add(dto);
        }
        return dtos;
    }

    private List<LogAuditoriaDTO> mapLogs(List<LogAuditoria> logs) {
        Map<Integer, Utilizador> userCache = new HashMap<>();
        List<LogAuditoriaDTO> dtos = new ArrayList<>();
        for (LogAuditoria l : logs) {
            LogAuditoriaDTO dto = new LogAuditoriaDTO();
            dto.idOriginalLoja = l.getId();
            dto.acao = l.getAcao() != null ? l.getAcao().name() : null;
            dto.dataHora = l.getDataHora() != null ? l.getDataHora().toString() : null;
            Utilizador u = userCache.computeIfAbsent(l.getIdUtilizador(), utilizadorRepo::buscarPorId);
            dto.nomeUtilizador = u != null ? u.getNomeUtilizador() : "Desconhecido";
            dtos.add(dto);
        }
        return dtos;
    }

    private void atualizarSincVenda(Venda v) {
        vendaRepo.atualizarSincronizacao(v.getId(), v.getEstadoSincronizacao());
    }

    private void atualizarSincDevolucao(Devolucao d) {
        devolucaoRepo.atualizarSincronizacao(d.getId(), d.getEstadoSincronizacao());
    }

    private void atualizarSincRemessa(Remessa r) {
        remessaRepo.atualizarSincronizacao(r.getId(), r.getEstadoSincronizacao());
    }
}
