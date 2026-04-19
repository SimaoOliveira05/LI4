package pt.trasmum.loja.servico.impl;

import pt.trasmum.loja.dominio.catalogo.Produto;
import pt.trasmum.loja.dominio.core.PerfilUtilizador;
import pt.trasmum.loja.dominio.core.TipoAcao;
import pt.trasmum.loja.dominio.core.Utilizador;
import pt.trasmum.loja.dominio.exceptions.ExcecoesCatalogo.ProdutoInativoException;
import pt.trasmum.loja.dominio.exceptions.ExcecoesCatalogo.ProdutoNaoEncontradoException;
import pt.trasmum.loja.dominio.fornecedores.Fornecedor;
import pt.trasmum.loja.dominio.fornecedores.FornecedorProduto;
import pt.trasmum.loja.repositorio.interfaces.FornecedorProdutoRepositorio;
import pt.trasmum.loja.repositorio.interfaces.FornecedorRepositorio;
import pt.trasmum.loja.repositorio.interfaces.ProdutoRepositorio;
import pt.trasmum.loja.servico.interfaces.IAuditoriaServico;
import pt.trasmum.loja.servico.interfaces.IAutorizacaoServico;
import pt.trasmum.loja.servico.interfaces.IFornecedorServico;

import java.util.List;

public class FornecedorServico implements IFornecedorServico {

    private final FornecedorRepositorio fornecedorRepo;
    private final FornecedorProdutoRepositorio fornecedorProdutoRepo;
    private final ProdutoRepositorio produtoRepo;
    private final IAutorizacaoServico autorizacaoServico;
    private final IAuditoriaServico auditoriaServico;

    public FornecedorServico(FornecedorRepositorio fornecedorRepo,
                             FornecedorProdutoRepositorio fornecedorProdutoRepo,
                             ProdutoRepositorio produtoRepo,
                             IAutorizacaoServico autorizacaoServico,
                             IAuditoriaServico auditoriaServico) {
        this.fornecedorRepo = fornecedorRepo;
        this.fornecedorProdutoRepo = fornecedorProdutoRepo;
        this.produtoRepo = produtoRepo;
        this.autorizacaoServico = autorizacaoServico;
        this.auditoriaServico = auditoriaServico;
    }

    @Override
    public Fornecedor criarFornecedor(Utilizador utilizador, Fornecedor fornecedor) {
        autorizacaoServico.exigirPerfil(utilizador, PerfilUtilizador.GESTOR, PerfilUtilizador.CEO);
        fornecedorRepo.guardar(fornecedor);
        auditoriaServico.registar(utilizador, TipoAcao.ALTERACAO_CATALOGO, "Fornecedor", fornecedor.getId());
        return fornecedor;
    }

    @Override
    public void editarFornecedor(Utilizador utilizador, Fornecedor fornecedor) {
        autorizacaoServico.exigirPerfil(utilizador, PerfilUtilizador.GESTOR, PerfilUtilizador.CEO);
        fornecedorRepo.atualizar(fornecedor);
        auditoriaServico.registar(utilizador, TipoAcao.ALTERACAO_CATALOGO, "Fornecedor", fornecedor.getId());
    }

    @Override
    public Fornecedor buscarPorId(int id) {
        return fornecedorRepo.buscarPorId(id);
    }

    @Override
    public List<Fornecedor> listarTodos() {
        return fornecedorRepo.listarTodos();
    }

    @Override
    public void associarProduto(Utilizador utilizador, int idFornecedor, int idProduto, double precoFornecedor) {
        autorizacaoServico.exigirPerfil(utilizador, PerfilUtilizador.GESTOR, PerfilUtilizador.CEO);
        if (precoFornecedor < 0) throw new IllegalArgumentException("Preço do fornecedor não pode ser negativo.");
        Produto p = produtoRepo.buscarPorId(idProduto);
        if (p == null) throw new ProdutoNaoEncontradoException("Produto não encontrado: " + idProduto);
        if (!p.isAtivo()) throw new ProdutoInativoException("Produto inativo: " + p.getNome());
        if (fornecedorRepo.buscarPorId(idFornecedor) == null)
            throw new IllegalArgumentException("Fornecedor não encontrado: " + idFornecedor);
        fornecedorProdutoRepo.associar(idFornecedor, idProduto, precoFornecedor);
        auditoriaServico.registar(utilizador, TipoAcao.ALTERACAO_CATALOGO, "FornecedorProduto", idFornecedor);
    }

    @Override
    public void desassociarProduto(Utilizador utilizador, int idFornecedor, int idProduto) {
        autorizacaoServico.exigirPerfil(utilizador, PerfilUtilizador.GESTOR, PerfilUtilizador.CEO);
        fornecedorProdutoRepo.desassociar(idFornecedor, idProduto);
        auditoriaServico.registar(utilizador, TipoAcao.ALTERACAO_CATALOGO, "FornecedorProduto", idFornecedor);
    }

    @Override
    public void atualizarPrecoFornecedor(Utilizador utilizador, int idFornecedor, int idProduto, double novoPreco) {
        autorizacaoServico.exigirPerfil(utilizador, PerfilUtilizador.GESTOR, PerfilUtilizador.CEO);
        if (novoPreco < 0) throw new IllegalArgumentException("Preço do fornecedor não pode ser negativo.");
        fornecedorProdutoRepo.atualizarPreco(idFornecedor, idProduto, novoPreco);
        auditoriaServico.registar(utilizador, TipoAcao.ALTERACAO_PRECO, "FornecedorProduto", idFornecedor);
    }

    @Override
    public List<Produto> listarProdutosFornecidos(int idFornecedor) {
        return fornecedorProdutoRepo.listarProdutosDoFornecedor(idFornecedor);
    }

    @Override
    public List<FornecedorProduto> listarCatalogoDetalhado(int idFornecedor) {
        return fornecedorProdutoRepo.listarDetalhado(idFornecedor);
    }

    @Override
    public boolean fornece(int idFornecedor, int idProduto) {
        return fornecedorProdutoRepo.fornece(idFornecedor, idProduto);
    }

    @Override
    public double precoDoFornecedor(int idFornecedor, int idProduto) {
        return fornecedorProdutoRepo.precoDoFornecedor(idFornecedor, idProduto);
    }
}
