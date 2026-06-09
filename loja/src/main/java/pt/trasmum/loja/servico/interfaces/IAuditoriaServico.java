package pt.trasmum.loja.servico.interfaces;

import pt.trasmum.loja.dominio.core.LogAuditoria;
import pt.trasmum.loja.dominio.core.TipoAcao;
import pt.trasmum.loja.dominio.core.Utilizador;

import java.util.List;

public interface IAuditoriaServico {
    void registar(Utilizador utilizador, TipoAcao acao, String entidade, int idEntidade, String descricao);
    List<LogAuditoria> obterLogsPendentes();
    List<LogAuditoria> obterTodosLogs();
    List<LogAuditoria> obterLogsPorTipo(TipoAcao tipo);
}
