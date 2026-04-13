package pt.trasmum.loja.servico.impl;

import pt.trasmum.loja.dominio.core.ConfiguracaoTerminal;
import pt.trasmum.loja.dominio.core.LogAuditoria;
import pt.trasmum.loja.dominio.core.TipoAcao;
import pt.trasmum.loja.dominio.core.Utilizador;
import pt.trasmum.loja.repositorio.interfaces.LogAuditoriaRepositorio;
import pt.trasmum.loja.servico.interfaces.IAuditoriaServico;

import java.util.List;

public class AuditoriaServico implements IAuditoriaServico {

    private final LogAuditoriaRepositorio logRepo;
    private final String idLoja;

    public AuditoriaServico(LogAuditoriaRepositorio logRepo, ConfiguracaoTerminal configuracao) {
        this.logRepo = logRepo;
        this.idLoja = configuracao.getIdLoja();
    }

    @Override
    public void registar(Utilizador utilizador, TipoAcao acao, String entidade, int idEntidade) {
        LogAuditoria log = new LogAuditoria(idLoja, acao, utilizador.getId(), entidade, idEntidade);
        logRepo.guardar(log);
    }

    @Override
    public List<LogAuditoria> obterLogsPendentes() {
        return logRepo.buscarPendentes();
    }
}
