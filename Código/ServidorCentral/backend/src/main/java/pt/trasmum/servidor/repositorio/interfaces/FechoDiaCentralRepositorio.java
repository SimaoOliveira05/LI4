package pt.trasmum.servidor.repositorio.interfaces;

import pt.trasmum.servidor.dominio.FechoDiaCentral;

import java.sql.Connection;
import java.time.LocalDate;
import java.util.List;

public interface FechoDiaCentralRepositorio {
    void guardar(FechoDiaCentral fecho);
    void guardar(Connection conn, FechoDiaCentral fecho);
    List<FechoDiaCentral> buscarPorData(LocalDate data);
    FechoDiaCentral buscarPorLojaEData(String idLoja, LocalDate data);
    List<FechoDiaCentral> listarTodos();
    List<FechoDiaCentral> buscarPorLoja(String idLoja);

}
