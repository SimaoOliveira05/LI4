package pt.trasmum.loja.servico.interfaces;

import pt.trasmum.loja.dominio.catalogo.Desconto;
import pt.trasmum.loja.dominio.catalogo.Lote;
import pt.trasmum.loja.dominio.catalogo.Produto;
import pt.trasmum.loja.dominio.core.Utilizador;
import pt.trasmum.loja.servico.ProdutoDTO;

import java.util.List;

public interface ICatalogoServico {
    Produto criarProduto(Utilizador utilizador, ProdutoDTO dados);
    void editarProduto(Utilizador utilizador, int id, ProdutoDTO dados);
    void desativarProduto(Utilizador utilizador, int id);
    Desconto aplicarDesconto(Utilizador utilizador, int idLote, double percentagem);
    void abaterLote(Utilizador utilizador, int idLote, int quantidade);
    List<Produto> gerarAlertasStockMinimo();
    List<Lote> gerarAlertasValidade(int diasLimite);
    List<Produto> pesquisarProduto(String query);
}
