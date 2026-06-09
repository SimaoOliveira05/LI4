package pt.trasmum.loja.servico.impl;

import pt.trasmum.loja.dominio.core.PerfilUtilizador;
import pt.trasmum.loja.dominio.core.Utilizador;
import pt.trasmum.loja.dominio.fornecedores.Pagamento;
import pt.trasmum.loja.dominio.fornecedores.Pagamento.EstadoPagamento;
import pt.trasmum.loja.repositorio.interfaces.PagamentoRepositorio;
import pt.trasmum.loja.servico.interfaces.IAutorizacaoServico;
import pt.trasmum.loja.servico.interfaces.IPagamentoServico;

import java.time.LocalDateTime;
import java.util.List;

public class PagamentoServico implements IPagamentoServico {

    private final PagamentoRepositorio pagamentoRepo;
    private final IAutorizacaoServico autorizacaoServico;

    public PagamentoServico(PagamentoRepositorio pagamentoRepo, IAutorizacaoServico autorizacaoServico) {
        this.pagamentoRepo = pagamentoRepo;
        this.autorizacaoServico = autorizacaoServico;
    }

    @Override
    public List<Pagamento> listarPendentes() {
        return pagamentoRepo.buscarPendentes();
    }

    @Override
    public List<Pagamento> listarNaoPagos() {
        return pagamentoRepo.buscarNaoPagos();
    }

    @Override
    public void liquidar(Utilizador utilizador, int idPagamento) {
        autorizacaoServico.exigirPerfil(utilizador, PerfilUtilizador.GESTOR, PerfilUtilizador.CEO);
        List<Pagamento> pendentes = pagamentoRepo.buscarNaoPagos();
        Pagamento pagamento = pendentes.stream()
                .filter(p -> p.getId() == idPagamento)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Pagamento não encontrado: " + idPagamento));
        pagamento.setEstadoPagamento(EstadoPagamento.PAGO);
        pagamento.setIdUtilizador(utilizador.getId());
        pagamento.setDataHora(LocalDateTime.now());
        pagamento.reverterParaPendente();
        pagamentoRepo.atualizar(pagamento);
    }
}
