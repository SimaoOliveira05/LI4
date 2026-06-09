package pt.trasmum.loja.repositorio.interfaces;

import pt.trasmum.loja.dominio.catalogo.Produto;

import java.util.List;

public interface ProdutoRepositorio {
    void guardar(Produto produto);
    void atualizar(Produto produto);
    Produto buscarPorCodigoBarras(String codigo);
    Produto buscarPorId(int id);
    List<Produto> buscarAbaixoStockMinimo();
    List<Produto> pesquisar(String query);
    List<Produto> listarAtivos();
}
