package pt.trasmum.servidor.repositorio.interfaces;

import pt.trasmum.servidor.dominio.DevolucaoCentral;

import java.sql.Connection;
import java.time.LocalDate;
import java.util.List;

public interface DevolucaoCentralRepositorio {
    void guardar(DevolucaoCentral devolucao);
    void guardar(Connection conn, DevolucaoCentral devolucao);
    List<DevolucaoCentral> buscarPorLoja(String idLoja);
    double totalDevolucoesPorPeriodo(LocalDate inicio, LocalDate fim);
    double totalDevolucoesPorLojaEPeriodo(String idLoja, LocalDate inicio, LocalDate fim);
}
