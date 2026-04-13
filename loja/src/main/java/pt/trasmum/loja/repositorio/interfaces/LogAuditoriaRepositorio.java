package pt.trasmum.loja.repositorio.interfaces;

import pt.trasmum.loja.dominio.core.LogAuditoria;

import java.util.List;

public interface LogAuditoriaRepositorio {
    void guardar(LogAuditoria log);
    void atualizar(LogAuditoria log);
    List<LogAuditoria> buscarPendentes();
}
