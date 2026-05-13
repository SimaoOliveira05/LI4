package pt.trasmum.servidor.repositorio.interfaces;

import pt.trasmum.servidor.dominio.Loja;

import java.sql.Connection;
import java.util.List;

public interface LojaRepositorio {
    void criarSeNaoExistir(Loja loja);
    void criarSeNaoExistir(Connection conn, Loja loja);
    List<Loja> listarTodas();
    Loja buscarPorId(String idLoja);
}
