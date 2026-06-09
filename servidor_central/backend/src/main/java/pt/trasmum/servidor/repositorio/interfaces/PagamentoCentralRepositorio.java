package pt.trasmum.servidor.repositorio.interfaces;

import pt.trasmum.servidor.dominio.PagamentoCentral;

import java.sql.Connection;
import java.util.List;

public interface PagamentoCentralRepositorio {
    void guardar(PagamentoCentral pagamento);
    void guardar(Connection conn, PagamentoCentral pagamento);
    List<PagamentoCentral> listarTodos();
}
