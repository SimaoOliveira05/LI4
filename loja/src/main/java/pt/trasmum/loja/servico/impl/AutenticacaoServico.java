package pt.trasmum.loja.servico.impl;

import pt.trasmum.loja.dominio.core.TipoAcao;
import pt.trasmum.loja.dominio.core.Utilizador;
import pt.trasmum.loja.dominio.exceptions.ExcecoesSeguranca.CredenciaisInvalidasException;
import pt.trasmum.loja.dominio.exceptions.ExcecoesSeguranca.SessaoDuplicadaException;
import pt.trasmum.loja.repositorio.interfaces.UtilizadorRepositorio;
import pt.trasmum.loja.servico.interfaces.IAuditoriaServico;
import pt.trasmum.loja.servico.interfaces.IAutenticacaoServico;

public class AutenticacaoServico implements IAutenticacaoServico {

    private final UtilizadorRepositorio utilizadorRepo;
    private final IAuditoriaServico auditoriaServico;

    public AutenticacaoServico(UtilizadorRepositorio utilizadorRepo, IAuditoriaServico auditoriaServico) {
        this.utilizadorRepo = utilizadorRepo;
        this.auditoriaServico = auditoriaServico;
    }

    @Override
    public Utilizador autenticar(String nomeUtilizador, String palavraPasse) {
        // Repositório faz BCrypt.checkpw internamente
        Utilizador utilizador = utilizadorRepo.buscarPorCredenciais(nomeUtilizador, palavraPasse);
        if (utilizador == null) {
            throw new CredenciaisInvalidasException("Nome de utilizador ou palavra-passe incorretos.");
        }
        if (verificarSessaoDuplicada(utilizador)) {
            throw new SessaoDuplicadaException("O utilizador '" + nomeUtilizador + "' já tem uma sessão ativa.");
        }
        utilizador.setEmSessao(true);
        utilizadorRepo.atualizar(utilizador);
        auditoriaServico.registar(utilizador, TipoAcao.GESTAO_UTILIZADOR, "Utilizador", utilizador.getId(),
                "Login de '" + utilizador.getNomeUtilizador() + "'");
        return utilizador;
    }

    @Override
    public void encerrarSessao(Utilizador utilizador) {
        utilizador.setEmSessao(false);
        utilizadorRepo.atualizar(utilizador);
    }

    @Override
    public boolean verificarSessaoDuplicada(Utilizador utilizador) {
        return utilizadorRepo.verificarSessaoAtiva(utilizador.getId());
    }
}
