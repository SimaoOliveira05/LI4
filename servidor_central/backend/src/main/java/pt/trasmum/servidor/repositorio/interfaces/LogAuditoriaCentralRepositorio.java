package pt.trasmum.servidor.repositorio.interfaces;

import pt.trasmum.servidor.dominio.LogAuditoriaCentral;

import java.sql.Connection;
import java.time.LocalDate;
import java.util.List;

public interface LogAuditoriaCentralRepositorio {
    void guardar(LogAuditoriaCentral log);
    void guardar(Connection conn, LogAuditoriaCentral log);
    List<LogAuditoriaCentral> buscarPorLoja(String idLoja);
    List<LogAuditoriaCentral> buscarPorLojaEPeriodo(String idLoja, LocalDate inicio, LocalDate fim);
}
