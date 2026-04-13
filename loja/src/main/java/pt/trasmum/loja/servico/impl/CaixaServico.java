package pt.trasmum.loja.servico.impl;

import pt.trasmum.loja.dominio.SessaoCaixaJaAbertaException;
import pt.trasmum.loja.dominio.core.ConfiguracaoTerminal;
import pt.trasmum.loja.dominio.core.Utilizador;
import pt.trasmum.loja.dominio.tesouraria.DetalheNumerario;
import pt.trasmum.loja.dominio.tesouraria.Sangria;
import pt.trasmum.loja.dominio.tesouraria.SessaoCaixa;
import pt.trasmum.loja.repositorio.interfaces.SessaoCaixaRepositorio;
import pt.trasmum.loja.servico.interfaces.ICaixaServico;

import java.time.LocalDateTime;
import java.util.List;

public class CaixaServico implements ICaixaServico {

    private final SessaoCaixaRepositorio sessaoCaixaRepo;

    public CaixaServico(SessaoCaixaRepositorio sessaoCaixaRepo) {
        this.sessaoCaixaRepo = sessaoCaixaRepo;
    }

    @Override
    public SessaoCaixa abrirSessao(Utilizador utilizador, List<DetalheNumerario> fundo,
                                    ConfiguracaoTerminal configuracao) {
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
        sessao.registarSangria(sangria);
        sessaoCaixaRepo.guardarSangria(sangria);
        sessaoCaixaRepo.atualizar(sessao);
        return sangria;
    }

    @Override
    public void fecharSessao(SessaoCaixa sessao) {
        sessao.setDataEncerramento(LocalDateTime.now());
        sessaoCaixaRepo.atualizar(sessao);
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
    public List<SessaoCaixa> obterSessoesPendentes() {
        return sessaoCaixaRepo.buscarPendentes();
    }
}
