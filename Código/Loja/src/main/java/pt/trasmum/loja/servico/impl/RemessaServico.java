package pt.trasmum.loja.servico.impl;

import pt.trasmum.loja.dominio.catalogo.Produto;
import pt.trasmum.loja.dominio.core.ConfiguracaoTerminal;
import pt.trasmum.loja.dominio.core.PerfilUtilizador;
import pt.trasmum.loja.dominio.core.TipoAcao;
import pt.trasmum.loja.dominio.core.Utilizador;
import pt.trasmum.loja.dominio.exceptions.RemessaSemLinhasException;
import pt.trasmum.loja.dominio.exceptions.ExcecoesCatalogo.ProdutoInativoException;
import pt.trasmum.loja.dominio.exceptions.ExcecoesCatalogo.ProdutoNaoEncontradoException;
import pt.trasmum.loja.dominio.exceptions.ExcecoesCatalogo.ProdutoNaoFornecidoException;
import pt.trasmum.loja.dominio.fornecedores.*;
import pt.trasmum.loja.dominio.fornecedores.PedidoRemessa.EstadoPedido;
import pt.trasmum.loja.repositorio.interfaces.*;
import pt.trasmum.loja.servico.interfaces.IAuditoriaServico;
import pt.trasmum.loja.servico.interfaces.IAutorizacaoServico;
import pt.trasmum.loja.servico.interfaces.IRemessaServico;

import java.time.LocalDate;
import java.util.List;

public class RemessaServico implements IRemessaServico {

    private final RemessaRepositorio remessaRepo;
    private final PedidoRemessaRepositorio pedidoRepo;
    private final PagamentoRepositorio pagamentoRepo;
    private final ProdutoRepositorio produtoRepo;
    private final FornecedorProdutoRepositorio fornecedorProdutoRepo;
    private final IAutorizacaoServico autorizacaoServico;
    private final IAuditoriaServico auditoriaServico;
    private final ConfiguracaoTerminal configuracao;

    public RemessaServico(RemessaRepositorio remessaRepo, PedidoRemessaRepositorio pedidoRepo,
                           PagamentoRepositorio pagamentoRepo, ProdutoRepositorio produtoRepo,
                           FornecedorProdutoRepositorio fornecedorProdutoRepo,
                           IAutorizacaoServico autorizacaoServico, IAuditoriaServico auditoriaServico,
                           ConfiguracaoTerminal configuracao) {
        this.remessaRepo = remessaRepo;
        this.pedidoRepo = pedidoRepo;
        this.pagamentoRepo = pagamentoRepo;
        this.produtoRepo = produtoRepo;
        this.fornecedorProdutoRepo = fornecedorProdutoRepo;
        this.autorizacaoServico = autorizacaoServico;
        this.auditoriaServico = auditoriaServico;
        this.configuracao = configuracao;
    }

    @Override
    public Remessa registarRemessa(Utilizador utilizador, Fornecedor fornecedor,
                                    List<LinhaRemessa> linhas, double valorTotalGuia) {
        autorizacaoServico.exigirPerfil(utilizador, PerfilUtilizador.GESTOR, PerfilUtilizador.CEO);
        if (linhas == null || linhas.isEmpty()) throw new RemessaSemLinhasException("A remessa deve ter pelo menos uma linha.");

        // Valida produtos
        for (LinhaRemessa linha : linhas) {
            Produto p = produtoRepo.buscarPorId(linha.getIdProduto());
            if (p == null) throw new ProdutoNaoEncontradoException("Produto não encontrado: " + linha.getIdProduto());
            if (!p.isAtivo()) throw new ProdutoInativoException("Produto inativo: " + p.getNome());
            if (!fornecedorProdutoRepo.fornece(fornecedor.getId(), p.getId()))
                throw new ProdutoNaoFornecidoException(
                        "Produto '" + p.getNome() + "' não é fornecido por " + fornecedor.getNome() + ".");
        }

        Remessa remessa = new Remessa(configuracao.getIdLoja(), fornecedor.getId(),
                utilizador.getId(), LocalDate.now(), valorTotalGuia);
        for (LinhaRemessa linha : linhas) remessa.adicionarLinha(linha);

        remessaRepo.guardar(remessa); // transação: cria remessa + lotes
        associarPedidoPendente(remessa);

        Pagamento pagamento = new Pagamento(configuracao.getIdLoja(), fornecedor.getId(),
                remessa.getId(), valorTotalGuia);
        pagamentoRepo.guardar(pagamento);

        auditoriaServico.registar(utilizador, TipoAcao.ALTERACAO_CATALOGO, "Remessa", remessa.getId(),
                String.format("Recebeu remessa do fornecedor '%s' (%.2f €, %d linhas)", fornecedor.getNome(), valorTotalGuia, linhas.size()));
        return remessa;
    }

    @Override
    public void associarPedidoPendente(Remessa remessa) {
        List<PedidoRemessa> pendentes = pedidoRepo.buscarPendentesPorFornecedor(remessa.getIdFornecedor());
        for (PedidoRemessa pedido : pendentes) {
            pedido.setEstado(EstadoPedido.CONCLUIDO);
            pedidoRepo.atualizar(pedido);
        }
    }

    @Override
    public PedidoRemessa criarPedidoRemessa(Utilizador utilizador, Fornecedor fornecedor,
                                             List<LinhaPedidoRemessa> linhas) {
        autorizacaoServico.exigirPerfil(utilizador, PerfilUtilizador.GESTOR, PerfilUtilizador.CEO);
        for (LinhaPedidoRemessa linha : linhas) {
            Produto p = produtoRepo.buscarPorId(linha.getIdProduto());
            if (p == null) throw new ProdutoNaoEncontradoException("Produto não encontrado: " + linha.getIdProduto());
            if (!p.isAtivo()) throw new ProdutoInativoException("Produto inativo: " + p.getNome());
            if (!fornecedorProdutoRepo.fornece(fornecedor.getId(), p.getId()))
                throw new ProdutoNaoFornecidoException(
                        "Produto '" + p.getNome() + "' não é fornecido por " + fornecedor.getNome() + ".");
        }
        PedidoRemessa pedido = new PedidoRemessa(fornecedor.getId(), utilizador.getId(), LocalDate.now());
        for (LinhaPedidoRemessa linha : linhas) pedido.adicionarLinha(linha);
        pedidoRepo.guardar(pedido);
        return pedido;
    }

    @Override
    public List<PedidoRemessa> listarPedidos(EstadoPedido estado) {
        return pedidoRepo.buscarPorEstado(estado);
    }

    @Override
    public List<Remessa> obterRemessasPendentes() {
        return remessaRepo.buscarPendentes();
    }

    @Override
    public List<Pagamento> obterPagamentosPendentes() {
        return pagamentoRepo.buscarPendentes();
    }
}
