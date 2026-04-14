package pt.trasmum.loja.servico.impl;

import pt.trasmum.loja.dominio.catalogo.Desconto;
import pt.trasmum.loja.dominio.catalogo.Lote;
import pt.trasmum.loja.dominio.catalogo.Produto;
import pt.trasmum.loja.dominio.core.ConfiguracaoTerminal;
import pt.trasmum.loja.dominio.core.PerfilUtilizador;
import pt.trasmum.loja.dominio.core.TipoAcao;
import pt.trasmum.loja.dominio.core.Utilizador;
import pt.trasmum.loja.repositorio.interfaces.LoteRepositorio;
import pt.trasmum.loja.repositorio.interfaces.ProdutoRepositorio;
import pt.trasmum.loja.servico.ProdutoDTO;
import pt.trasmum.loja.servico.interfaces.IAuditoriaServico;
import pt.trasmum.loja.servico.interfaces.IAutorizacaoServico;
import pt.trasmum.loja.servico.interfaces.ICatalogoServico;

import java.time.LocalDate;
import java.util.List;

public class CatalogoServico implements ICatalogoServico {

    private final ProdutoRepositorio produtoRepo;
    private final LoteRepositorio loteRepo;
    private final IAutorizacaoServico autorizacaoServico;
    private final IAuditoriaServico auditoriaServico;
    private final ConfiguracaoTerminal configuracao;

    public CatalogoServico(ProdutoRepositorio produtoRepo, LoteRepositorio loteRepo,
                           IAutorizacaoServico autorizacaoServico, IAuditoriaServico auditoriaServico,
                           ConfiguracaoTerminal configuracao) {
        this.produtoRepo = produtoRepo;
        this.loteRepo = loteRepo;
        this.autorizacaoServico = autorizacaoServico;
        this.auditoriaServico = auditoriaServico;
        this.configuracao = configuracao;
    }

    @Override
    public Produto criarProduto(Utilizador utilizador, ProdutoDTO dados) {
        autorizacaoServico.exigirPerfil(utilizador, PerfilUtilizador.GESTOR, PerfilUtilizador.CEO);
        Produto produto = new Produto(dados.codigoBarras(), dados.nome(), dados.categoria(),
                                      dados.precoBase(), dados.stockMinimo());
        produtoRepo.guardar(produto);
        auditoriaServico.registar(utilizador, TipoAcao.ALTERACAO_CATALOGO, "Produto", produto.getId());
        return produto;
    }

    @Override
    public void editarProduto(Utilizador utilizador, int id, ProdutoDTO dados) {
        autorizacaoServico.exigirPerfil(utilizador, PerfilUtilizador.GESTOR, PerfilUtilizador.CEO);
        Produto produto = produtoRepo.buscarPorId(id);
        if (produto == null) throw new pt.trasmum.loja.dominio.ExcecoesCatalogo.ProdutoNaoEncontradoException("Produto não encontrado: " + id);
        boolean precoAlterado = Double.compare(produto.getPrecoBase(), dados.precoBase()) != 0;
        produto.setCodigoBarras(dados.codigoBarras());
        produto.setNome(dados.nome());
        produto.setCategoria(dados.categoria());
        produto.setPrecoBase(dados.precoBase());
        produto.setStockMinimo(dados.stockMinimo());
        produtoRepo.atualizar(produto);
        TipoAcao acao = precoAlterado ? TipoAcao.ALTERACAO_PRECO : TipoAcao.ALTERACAO_CATALOGO;
        auditoriaServico.registar(utilizador, acao, "Produto", produto.getId());
    }

    @Override
    public void desativarProduto(Utilizador utilizador, int id) {
        autorizacaoServico.exigirPerfil(utilizador, PerfilUtilizador.GESTOR, PerfilUtilizador.CEO);
        Produto produto = produtoRepo.buscarPorId(id);
        if (produto == null) throw new pt.trasmum.loja.dominio.ExcecoesCatalogo.ProdutoNaoEncontradoException("Produto não encontrado: " + id);
        produto.setAtivo(false);
        produtoRepo.atualizar(produto);
        auditoriaServico.registar(utilizador, TipoAcao.ALTERACAO_CATALOGO, "Produto", id);
    }

    @Override
    public Desconto aplicarDesconto(Utilizador utilizador, int idLote, double percentagem) {
        autorizacaoServico.exigirPerfil(utilizador, PerfilUtilizador.GESTOR, PerfilUtilizador.CEO);
        Desconto desconto = new Desconto(idLote, percentagem, LocalDate.now(), utilizador.getId());
        loteRepo.guardarDesconto(desconto, idLote);
        auditoriaServico.registar(utilizador, TipoAcao.APLICACAO_DESCONTO, "Lote", idLote);
        return desconto;
    }

    @Override
    public List<Produto> gerarAlertasStockMinimo() {
        return produtoRepo.buscarAbaixoStockMinimo();
    }

    @Override
    public List<Lote> gerarAlertasValidade(int diasLimite) {
        return loteRepo.buscarAbaixoDaValidade(diasLimite);
    }

    @Override
    public List<Produto> pesquisarProduto(String query) {
        return produtoRepo.pesquisar(query);
    }
}
