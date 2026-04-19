package pt.trasmum.loja.servico.impl;

import pt.trasmum.loja.dominio.catalogo.Lote;
import pt.trasmum.loja.dominio.catalogo.Produto;
import pt.trasmum.loja.dominio.core.ConfiguracaoTerminal;
import pt.trasmum.loja.dominio.core.TipoAcao;
import pt.trasmum.loja.dominio.core.Utilizador;
import pt.trasmum.loja.dominio.exceptions.ExcecoesVenda.ArtigoNaoConstaNaFaturaException;
import pt.trasmum.loja.dominio.exceptions.ExcecoesVenda.DevolucaoJaProcessadaException;
import pt.trasmum.loja.dominio.exceptions.ExcecoesVenda.FaturaNaoEncontradaException;
import pt.trasmum.loja.dominio.exceptions.ExcecoesVenda.QuantidadeDevolucaoExcedidaException;
import pt.trasmum.loja.dominio.vendas.Devolucao;
import pt.trasmum.loja.dominio.vendas.LinhaVenda;
import pt.trasmum.loja.dominio.vendas.Venda;
import pt.trasmum.loja.repositorio.interfaces.DevolucaoRepositorio;
import pt.trasmum.loja.repositorio.interfaces.LoteRepositorio;
import pt.trasmum.loja.repositorio.interfaces.ProdutoRepositorio;
import pt.trasmum.loja.repositorio.interfaces.VendaRepositorio;
import pt.trasmum.loja.servico.interfaces.IAuditoriaServico;
import pt.trasmum.loja.servico.interfaces.IDevolucaoServico;

import java.time.LocalDate;
import java.util.List;

public class DevolucaoServico implements IDevolucaoServico {

    private final VendaRepositorio vendaRepo;
    private final DevolucaoRepositorio devolucaoRepo;
    private final LoteRepositorio loteRepo;
    private final ProdutoRepositorio produtoRepo;
    private final IAuditoriaServico auditoriaServico;
    private final ConfiguracaoTerminal configuracao;

    public DevolucaoServico(VendaRepositorio vendaRepo, DevolucaoRepositorio devolucaoRepo,
                             LoteRepositorio loteRepo, ProdutoRepositorio produtoRepo,
                             IAuditoriaServico auditoriaServico, ConfiguracaoTerminal configuracao) {
        this.vendaRepo = vendaRepo;
        this.devolucaoRepo = devolucaoRepo;
        this.loteRepo = loteRepo;
        this.produtoRepo = produtoRepo;
        this.auditoriaServico = auditoriaServico;
        this.configuracao = configuracao;
    }

    @Override
    public Devolucao processar(Utilizador utilizador, String numeroFatura, String codigoBarras,
                               LocalDate dataValidadeEmbalagem, int quantidade) {
        // Guarda 1: fatura existe?
        Venda venda = vendaRepo.buscarPorNumeroFatura(numeroFatura);
        if (venda == null || venda.getFatura() == null) {
            throw new FaturaNaoEncontradaException("Fatura não encontrada: " + numeroFatura);
        }

        // Guarda 2: produto consta na fatura?
        Produto produto = produtoRepo.buscarPorCodigoBarras(codigoBarras);
        if (produto == null) throw new FaturaNaoEncontradaException("Produto não encontrado: " + codigoBarras);

        LinhaVenda linhaFatura = encontrarLinhaComProduto(venda.getLinhas(), produto.getId());
        if (linhaFatura == null) {
            throw new ArtigoNaoConstaNaFaturaException(
                    "O artigo '" + produto.getNome() + "' não consta na fatura " + numeroFatura);
        }

        // Guarda 3: quantidades disponíveis
        int quantidadeVendida = quantidadeVendidaNaFatura(venda.getLinhas(), produto.getId());
        List<Devolucao> devolucoesPrevias = devolucaoRepo.buscarPorFatura(numeroFatura);
        int jaDevolvido = devolucoesPrevias.stream()
                .filter(d -> d.getIdLote() == linhaFatura.getIdLote())
                .mapToInt(Devolucao::getQuantidade)
                .sum();

        if (jaDevolvido >= quantidadeVendida) {
            throw new DevolucaoJaProcessadaException("Este artigo já foi totalmente devolvido para a fatura " + numeroFatura);
        }
        int disponivel = quantidadeVendida - jaDevolvido;
        if (quantidade > disponivel) {
            throw new QuantidadeDevolucaoExcedidaException(
                    "Quantidade a devolver (" + quantidade + ") excede o disponível (" + disponivel + ").");
        }

        // Guarda 4: repor stock se lote ainda válido
        List<Lote> lotesFEFO = loteRepo.buscarLotesFEFO(produto.getId());
        Lote loteOriginal = lotesFEFO.stream()
                .filter(l -> l.getId() == linhaFatura.getIdLote())
                .findFirst()
                .orElse(null);

        double valorRestituido = linhaFatura.getPrecoUnitario() * quantidade;

        if (loteOriginal != null && !dataValidadeEmbalagem.isBefore(LocalDate.now())) {
            loteOriginal.setQuantidade(loteOriginal.getQuantidade() + quantidade);
            loteRepo.atualizar(loteOriginal);
        }

        // Persiste devolução
        Devolucao devolucao = new Devolucao(
                configuracao.getIdLoja(),
                venda.getFatura().getId(),
                numeroFatura,
                utilizador.getId(),
                linhaFatura.getIdLote(),
                quantidade,
                dataValidadeEmbalagem,
                valorRestituido);
        devolucaoRepo.guardar(devolucao);
        auditoriaServico.registar(utilizador, TipoAcao.DEVOLUCAO, "Devolucao", devolucao.getId());
        return devolucao;
    }

    @Override
    public List<Devolucao> obterDevolucoesPendentes() {
        return devolucaoRepo.buscarPendentes();
    }

    private LinhaVenda encontrarLinhaComProduto(List<LinhaVenda> linhas, int idProduto) {
        for (LinhaVenda linha : linhas) {
            List<Lote> lotes = loteRepo.buscarLotesFEFO(idProduto);
            // Verifica se o lote da linha pertence ao produto
            // buscarLotesFEFO retorna só lotes com quantidade > 0; o lote pode estar esgotado após venda
            // usamos uma abordagem diferente: busca todos os lotes do produto incluindo esgotados
            // Para este fim, verificamos se o idLote pertence ao produto usando os lotes disponíveis
            // Se não encontrar, assumimos que o lote do produto foi consumido na venda
            if (lotesPertencemAoProduto(linha.getIdLote(), idProduto)) {
                return linha;
            }
        }
        return null;
    }

    private boolean lotesPertencemAoProduto(int idLote, int idProduto) {
        // Tenta encontrar o lote nos lotes FEFO do produto (inclui esgotados via busca diferente)
        // Estratégia: busca todos os lotes do produto (FEFO retorna só qty>0)
        // Para validação de devolução, usamos os lotes com ou sem stock via buscarAbaixoDaValidade(9999)
        // Abordagem pragmática: usa buscarAbaixoDaValidade com prazo longo para obter todos os lotes
        List<Lote> todos = loteRepo.buscarAbaixoDaValidade(36500); // 100 anos
        return todos.stream().anyMatch(l -> l.getId() == idLote && l.getIdProduto() == idProduto);
    }

    private int quantidadeVendidaNaFatura(List<LinhaVenda> linhas, int idProduto) {
        return linhas.stream()
                .filter(l -> lotesPertencemAoProduto(l.getIdLote(), idProduto))
                .mapToInt(LinhaVenda::getQuantidade)
                .sum();
    }
}
