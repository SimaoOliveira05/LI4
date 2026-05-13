package pt.trasmum.loja.repositorio.interfaces;

import pt.trasmum.loja.dominio.catalogo.Produto;
import pt.trasmum.loja.dominio.fornecedores.FornecedorProduto;

import java.util.List;

public interface FornecedorProdutoRepositorio {
    void associar(int idFornecedor, int idProduto, double precoFornecedor);
    void desassociar(int idFornecedor, int idProduto);
    void atualizarPreco(int idFornecedor, int idProduto, double novoPreco);
    List<Produto> listarProdutosDoFornecedor(int idFornecedor);
    List<FornecedorProduto> listarDetalhado(int idFornecedor);
    boolean fornece(int idFornecedor, int idProduto);
    /** Devolve o preço do fornecedor para um produto, ou -1 se não fornecido. */
    double precoDoFornecedor(int idFornecedor, int idProduto);
}
