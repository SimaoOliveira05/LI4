package pt.trasmum.loja.servico.impl;

import pt.trasmum.loja.dominio.EnvioFechoFalhouException;
import pt.trasmum.loja.dominio.core.ConfiguracaoTerminal;
import pt.trasmum.loja.dominio.core.LogAuditoria;
import pt.trasmum.loja.dominio.core.PerfilUtilizador;
import pt.trasmum.loja.dominio.core.Utilizador;
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
import java.util.List;

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
    private final SincronizacaoGateway sincronizacaoGateway;
    private final ConfiguracaoTerminal configuracao;

    public FechoDiaServico(IVendaServico vendaServico, IDevolucaoServico devolucaoServico,
                            ICaixaServico caixaServico, IRemessaServico remessaServico,
                            IAuditoriaServico auditoriaServico, IAutorizacaoServico autorizacaoServico,
                            FechoDiaRepositorio fechoDiaRepo, VendaRepositorio vendaRepo,
                            DevolucaoRepositorio devolucaoRepo, RemessaRepositorio remessaRepo,
                            PagamentoRepositorio pagamentoRepo, SessaoCaixaRepositorio sessaoCaixaRepo,
                            LogAuditoriaRepositorio logRepo, SincronizacaoGateway sincronizacaoGateway,
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
        this.sincronizacaoGateway = sincronizacaoGateway;
        this.configuracao = configuracao;
    }

    @Override
    public FechoDia executarFecho(Utilizador utilizador, ConfiguracaoTerminal config) {
        autorizacaoServico.exigirPerfil(utilizador, PerfilUtilizador.GESTOR, PerfilUtilizador.CEO);

        // Recolhe pendentes
        List<Venda> vendas = vendaServico.obterVendasPendentes();
        List<Devolucao> devolucoes = devolucaoServico.obterDevolucoesPendentes();
        List<SessaoCaixa> sessoes = caixaServico.obterSessoesPendentes();
        List<Remessa> remessas = remessaServico.obterRemessasPendentes();
        List<Pagamento> pagamentos = remessaServico.obterPagamentosPendentes();
        List<LogAuditoria> logs = auditoriaServico.obterLogsPendentes();

        // Marca EM_TRANSITO
        vendas.forEach(v -> { v.marcarEmTransito(); atualizarSincVenda(v); });
        devolucoes.forEach(d -> { d.marcarEmTransito(); atualizarSincDevolucao(d); });
        sessoes.forEach(s -> { s.marcarEmTransito(); sessaoCaixaRepo.atualizar(s); });
        remessas.forEach(r -> { r.marcarEmTransito(); atualizarSincRemessa(r); });
        pagamentos.forEach(p -> { p.marcarEmTransito(); pagamentoRepo.atualizar(p); });
        logs.forEach(l -> { l.marcarEmTransito(); logRepo.atualizar(l); });

        // Constrói pacote
        PacoteFechoDTO pacote = construirPacote(config, vendas, devolucoes, remessas, pagamentos, sessoes, logs);

        boolean sucesso = sincronizacaoGateway.enviarFechoDia(pacote);

        FechoDia fecho = new FechoDia(config.getIdLoja(), utilizador.getId(), LocalDate.now());

        if (!sucesso) {
            // Reverte tudo para PENDENTE
            reverterEmTransito();
            fechoDiaRepo.guardar(fecho); // persiste com PENDENTE
            throw new EnvioFechoFalhouException("Falha no envio do fecho de dia ao servidor central.");
        }

        // Marca tudo como CONFIRMADO
        vendas.forEach(v -> { v.marcarConfirmado(); atualizarSincVenda(v); });
        devolucoes.forEach(d -> { d.marcarConfirmado(); atualizarSincDevolucao(d); });
        sessoes.forEach(s -> { s.marcarConfirmado(); sessaoCaixaRepo.atualizar(s); });
        remessas.forEach(r -> { r.marcarConfirmado(); atualizarSincRemessa(r); });
        pagamentos.forEach(p -> { p.marcarConfirmado(); pagamentoRepo.atualizar(p); });
        logs.forEach(l -> { l.marcarConfirmado(); logRepo.atualizar(l); });

        fecho.marcarConfirmado();
        fechoDiaRepo.guardar(fecho);
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
        // Chama buscarPendentes em todos os repos — cada um reverte EM_TRANSITO → PENDENTE
        vendaRepo.buscarPendentes();
        devolucaoRepo.buscarPendentes();
        sessaoCaixaRepo.buscarPendentes();
        remessaRepo.buscarPendentes();
        pagamentoRepo.buscarPendentes();
        logRepo.buscarPendentes();
        fechoDiaRepo.buscarPendentes();
    }

    // ---- Helpers de mapeamento -------------------------------------------

    private PacoteFechoDTO construirPacote(ConfiguracaoTerminal config,
            List<Venda> vendas, List<Devolucao> devolucoes, List<Remessa> remessas,
            List<Pagamento> pagamentos, List<SessaoCaixa> sessoes, List<LogAuditoria> logs) {
        PacoteFechoDTO p = new PacoteFechoDTO();
        p.idLoja = config.getIdLoja();
        p.nomeLoja = config.getNomeLoja();
        p.dataFecho = LocalDate.now();
        p.vendas = mapVendas(vendas);
        p.devolucoes = mapDevolucoes(devolucoes);
        p.remessas = mapRemessas(remessas);
        p.pagamentos = mapPagamentos(pagamentos);
        p.sessoesCaixa = mapSessoes(sessoes);
        p.logs = mapLogs(logs);
        return p;
    }

    private List<VendaDTO> mapVendas(List<Venda> vendas) {
        List<VendaDTO> dtos = new ArrayList<>();
        for (Venda v : vendas) {
            VendaDTO dto = new VendaDTO();
            dto.id = v.getId();
            dto.idLoja = v.getIdLoja();
            dto.idUtilizador = v.getIdUtilizador();
            dto.dataHora = v.getDataHora() != null ? v.getDataHora().toString() : null;
            dto.metodoPagamento = v.getMetodoPagamento() != null ? v.getMetodoPagamento().name() : null;
            dto.totalFaturado = v.getTotalFaturado();
            dto.estado = v.getEstado() != null ? v.getEstado().name() : null;
            if (v.getFatura() != null) {
                dto.numeroFatura = v.getFatura().getNumeroFatura();
                dto.nifCliente = v.getFatura().getNifCliente();
            }
            dto.linhas = new ArrayList<>();
            for (var linha : v.getLinhas()) {
                LinhaVendaDTO l = new LinhaVendaDTO();
                l.idLote = linha.getIdLote();
                l.quantidade = linha.getQuantidade();
                l.precoUnitario = linha.getPrecoUnitario();
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
            dto.id = d.getId();
            dto.idLoja = d.getIdLoja();
            dto.numeroFatura = d.getNumeroFatura();
            dto.idUtilizador = d.getIdUtilizador();
            dto.idLote = d.getIdLote();
            dto.dataHora = d.getDataHora() != null ? d.getDataHora().toString() : null;
            dto.quantidade = d.getQuantidade();
            dto.dataValidadeEmbalagem = d.getDataValidadeEmbalagem() != null ? d.getDataValidadeEmbalagem().toString() : null;
            dto.valorRestituido = d.getValorRestituido();
            dtos.add(dto);
        }
        return dtos;
    }

    private List<RemessaDTO> mapRemessas(List<Remessa> remessas) {
        List<RemessaDTO> dtos = new ArrayList<>();
        for (Remessa r : remessas) {
            RemessaDTO dto = new RemessaDTO();
            dto.id = r.getId();
            dto.idLoja = r.getIdLoja();
            dto.idFornecedor = r.getIdFornecedor();
            dto.idUtilizador = r.getIdUtilizador();
            dto.dataRecepcao = r.getDataRecepcao() != null ? r.getDataRecepcao().toString() : null;
            dto.valorTotalGuia = r.getValorTotalGuia();
            dto.linhas = new ArrayList<>();
            for (var linha : r.getLinhas()) {
                LinhaRemessaDTO l = new LinhaRemessaDTO();
                l.idProduto = linha.getIdProduto();
                l.idLoteGerado = linha.getIdLoteGerado();
                l.quantidade = linha.getQuantidade();
                l.dataValidade = linha.getDataValidade() != null ? linha.getDataValidade().toString() : null;
                dto.linhas.add(l);
            }
            dtos.add(dto);
        }
        return dtos;
    }

    private List<PagamentoDTO> mapPagamentos(List<Pagamento> pagamentos) {
        List<PagamentoDTO> dtos = new ArrayList<>();
        for (Pagamento p : pagamentos) {
            PagamentoDTO dto = new PagamentoDTO();
            dto.id = p.getId();
            dto.idLoja = p.getIdLoja();
            dto.idFornecedor = p.getIdFornecedor();
            dto.idRemessa = p.getIdRemessa();
            dto.valor = p.getValor();
            dto.estadoPagamento = p.getEstadoPagamento() != null ? p.getEstadoPagamento().name() : null;
            dto.dataHora = p.getDataHora() != null ? p.getDataHora().toString() : null;
            dtos.add(dto);
        }
        return dtos;
    }

    private List<SessaoCaixaDTO> mapSessoes(List<SessaoCaixa> sessoes) {
        List<SessaoCaixaDTO> dtos = new ArrayList<>();
        for (SessaoCaixa s : sessoes) {
            SessaoCaixaDTO dto = new SessaoCaixaDTO();
            dto.id = s.getId();
            dto.idLoja = s.getIdLoja();
            dto.idUtilizador = s.getIdUtilizador();
            dto.saldoAtual = s.getSaldoAtual();
            dto.dataAbertura = s.getDataAbertura() != null ? s.getDataAbertura().toString() : null;
            dto.dataEncerramento = s.getDataEncerramento() != null ? s.getDataEncerramento().toString() : null;
            dtos.add(dto);
        }
        return dtos;
    }

    private List<LogAuditoriaDTO> mapLogs(List<LogAuditoria> logs) {
        List<LogAuditoriaDTO> dtos = new ArrayList<>();
        for (LogAuditoria l : logs) {
            LogAuditoriaDTO dto = new LogAuditoriaDTO();
            dto.id = l.getId();
            dto.idLoja = l.getIdLoja();
            dto.acao = l.getAcao() != null ? l.getAcao().name() : null;
            dto.dataHora = l.getDataHora() != null ? l.getDataHora().toString() : null;
            dto.idUtilizador = l.getIdUtilizador();
            dto.entidade = l.getEntidade();
            dto.idEntidade = l.getIdEntidade();
            dtos.add(dto);
        }
        return dtos;
    }

    // ---- Helpers de atualização de sincronização -------------------------

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
