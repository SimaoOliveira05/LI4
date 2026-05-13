package pt.trasmum.servidor.repositorio.interfaces;

import pt.trasmum.servidor.dominio.SessaoCaixaCentral;

import java.sql.Connection;
import java.util.List;

public interface SessaoCaixaCentralRepositorio {
    void guardar(SessaoCaixaCentral sessao);
    void guardar(Connection conn, SessaoCaixaCentral sessao);
    List<SessaoCaixaCentral> buscarPorLoja(String idLoja);
}
