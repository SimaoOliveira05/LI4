package pt.trasmum.loja.servico.interfaces;

import pt.trasmum.loja.dominio.catalogo.Produto;
import pt.trasmum.loja.dominio.core.Utilizador;
import pt.trasmum.loja.dominio.fornecedores.Fornecedor;
import pt.trasmum.loja.dominio.fornecedores.FornecedorProduto;

import java.util.List;

public interface IFornecedorServico {
    Fornecedor criarFornecedor(Utilizador utilizador, Fornecedor fornecedor);
    void editarFornecedor(Utilizador utilizador, Fornecedor fornecedor);
    Fornecedor buscarPorId(int id);
    List<Fornecedor> listarTodos();

    void associarProduto(Utilizador utilizador, int idFornecedor, int idProduto, double precoFornecedor);
    void desassociarProduto(Utilizador utilizador, int idFornecedor, int idProduto);
    void atualizarPrecoFornecedor(Utilizador utilizador, int idFornecedor, int idProduto, double novoPreco);

    List<Produto> listarProdutosFornecidos(int idFornecedor);
    List<FornecedorProduto> listarCatalogoDetalhado(int idFornecedor);
    boolean fornece(int idFornecedor, int idProduto);
    double precoDoFornecedor(int idFornecedor, int idProduto);
}
