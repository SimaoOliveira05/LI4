package pt.trasmum.loja.servico.interfaces;

import pt.trasmum.loja.dominio.core.ConfiguracaoTerminal;
import pt.trasmum.loja.dominio.core.Utilizador;
import pt.trasmum.loja.dominio.tesouraria.DetalheNumerario;
import pt.trasmum.loja.dominio.tesouraria.Sangria;
import pt.trasmum.loja.dominio.tesouraria.SessaoCaixa;

import java.util.List;

public interface ICaixaServico {
    SessaoCaixa abrirSessao(Utilizador utilizador, List<DetalheNumerario> fundo, ConfiguracaoTerminal configuracao);
    Sangria registarSangria(SessaoCaixa sessao, Utilizador utilizador, List<DetalheNumerario> valor);
    void fecharSessao(SessaoCaixa sessao);
    boolean verificarLimite(SessaoCaixa sessao, ConfiguracaoTerminal configuracao);
    SessaoCaixa buscarSessaoAtiva(int idUtilizador);
    List<SessaoCaixa> obterSessoesPendentes();
}
