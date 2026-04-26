package pt.trasmum.loja.servico.impl;

import pt.trasmum.loja.dominio.core.ConfiguracaoTerminal;
import pt.trasmum.loja.dominio.core.Utilizador;
import pt.trasmum.loja.dominio.core.TipoAcao;
import pt.trasmum.loja.dominio.exceptions.ExcecoesCaixa.SangriaInvalidaException;
import pt.trasmum.loja.dominio.exceptions.ExcecoesCaixa.SessaoCaixaJaAbertaException;
import pt.trasmum.loja.dominio.tesouraria.DetalheNumerario;
import pt.trasmum.loja.dominio.tesouraria.Sangria;
import pt.trasmum.loja.dominio.tesouraria.SessaoCaixa;
import pt.trasmum.loja.repositorio.interfaces.SessaoCaixaRepositorio;
import pt.trasmum.loja.servico.interfaces.IAuditoriaServico;
import pt.trasmum.loja.servico.interfaces.ICaixaServico;

import java.time.LocalDateTime;
import java.util.List;

public class CaixaServico implements ICaixaServico {

    private final SessaoCaixaRepositorio sessaoCaixaRepo;
    private final IAuditoriaServico auditoriaServico;

    public CaixaServico(SessaoCaixaRepositorio sessaoCaixaRepo, IAuditoriaServico auditoriaServico) {
        this.sessaoCaixaRepo = sessaoCaixaRepo;
        this.auditoriaServico = auditoriaServico;
    }

    @Override
    public SessaoCaixa abrirSessao(Utilizador utilizador, double fundo, ConfiguracaoTerminal configuracao) {
        SessaoCaixa existente = sessaoCaixaRepo.buscarSessaoAtiva(utilizador.getId());
        if (existente != null) {
            throw new SessaoCaixaJaAbertaException(
                    "O utilizador '" + utilizador.getNomeUtilizador() + "' já tem uma sessão de caixa aberta.");
        }
        SessaoCaixa sessao = new SessaoCaixa(configuracao.getIdLoja(), utilizador.getId(), fundo);
        sessaoCaixaRepo.guardar(sessao);
        return sessao;
    }

    @Override
    public Sangria registarSangria(SessaoCaixa sessao, Utilizador utilizador, List<DetalheNumerario> valor) {
        Sangria sangria = new Sangria(sessao.getId(), valor);
        if (sangria.getTotal() <= 0)
            throw new SangriaInvalidaException("O valor da sangria tem de ser superior a zero.");
        if (sangria.getTotal() > sessao.getSaldoAtual())
            throw new SangriaInvalidaException(String.format(
                    "Valor da sangria (%.2f €) excede o saldo disponível na caixa (%.2f €).",
                    sangria.getTotal(), sessao.getSaldoAtual()));
        sessao.registarSangria(sangria);
        sessaoCaixaRepo.guardarSangria(sangria);
        sessaoCaixaRepo.atualizar(sessao);
        return sangria;
    }

    @Override
    public void fecharSessao(SessaoCaixa sessao, Utilizador utilizador, List<DetalheNumerario> contagemFinal) {
        double saldoContado = contagemFinal.stream().mapToDouble(DetalheNumerario::getSubtotal).sum();
        double diferenca = saldoContado - sessao.getSaldoAtual();
        sessao.setSaldoContado(saldoContado);
        sessao.setDiferencaFecho(diferenca);
        sessao.setDataEncerramento(LocalDateTime.now());
        sessaoCaixaRepo.atualizar(sessao);
        if (Math.abs(diferenca) >= 0.01) {
            auditoriaServico.registar(utilizador, TipoAcao.FECHO_DIA, "SessaoCaixa", sessao.getId(),
                    String.format("Diferença de caixa no fecho: %+.2f € (esperado %.2f €, contado %.2f €)",
                            diferenca, sessao.getSaldoAtual(), saldoContado));
        }
    }

    @Override
    public boolean verificarLimite(SessaoCaixa sessao, ConfiguracaoTerminal configuracao) {
        return sessao.getSaldoAtual() > configuracao.getLimiteMaximoCaixa();
    }

    @Override
    public SessaoCaixa buscarSessaoAtiva(int idUtilizador) {
        return sessaoCaixaRepo.buscarSessaoAtiva(idUtilizador);
    }

    @Override
    public List<SessaoCaixa> obterSessoesAbertas() {
        return sessaoCaixaRepo.buscarTodasAbertas();
    }

    @Override
    public List<SessaoCaixa> obterSessoesPendentes() {
        return sessaoCaixaRepo.buscarPendentes();
    }
}
