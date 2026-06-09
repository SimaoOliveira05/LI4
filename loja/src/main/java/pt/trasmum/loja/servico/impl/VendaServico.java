package pt.trasmum.loja.servico.impl;

import pt.trasmum.loja.dominio.catalogo.Lote;
import pt.trasmum.loja.dominio.catalogo.Produto;
import pt.trasmum.loja.dominio.core.ConfiguracaoTerminal;
import pt.trasmum.loja.dominio.core.TipoAcao;
import pt.trasmum.loja.dominio.core.Utilizador;
import pt.trasmum.loja.dominio.exceptions.ExcecoesCaixa.SessaoCaixaNaoEncontradaException;
import pt.trasmum.loja.dominio.exceptions.ExcecoesCatalogo.ProdutoInativoException;
import pt.trasmum.loja.dominio.exceptions.ExcecoesCatalogo.ProdutoNaoEncontradoException;
import pt.trasmum.loja.dominio.exceptions.ExcecoesVenda.StockInsuficienteException;
import pt.trasmum.loja.dominio.vendas.*;
import pt.trasmum.loja.dominio.vendas.Venda.MetodoPagamento;
import pt.trasmum.loja.dominio.vendas.Venda.EstadoVenda;
import pt.trasmum.loja.repositorio.interfaces.LoteRepositorio;
import pt.trasmum.loja.repositorio.interfaces.ProdutoRepositorio;
import pt.trasmum.loja.repositorio.interfaces.SessaoCaixaRepositorio;
import pt.trasmum.loja.repositorio.interfaces.VendaRepositorio;
import pt.trasmum.loja.servico.interfaces.IAuditoriaServico;
import pt.trasmum.loja.servico.interfaces.IVendaServico;

import java.util.List;

public class VendaServico implements IVendaServico {

    private final VendaRepositorio vendaRepo;
    private final ProdutoRepositorio produtoRepo;
    private final LoteRepositorio loteRepo;
    private final SessaoCaixaRepositorio sessaoCaixaRepo;
    private final IAuditoriaServico auditoriaServico;
    private final ConfiguracaoTerminal configuracao;

    public VendaServico(VendaRepositorio vendaRepo, ProdutoRepositorio produtoRepo,
                        LoteRepositorio loteRepo, SessaoCaixaRepositorio sessaoCaixaRepo,
                        IAuditoriaServico auditoriaServico, ConfiguracaoTerminal configuracao) {
        this.vendaRepo = vendaRepo;
        this.produtoRepo = produtoRepo;
        this.loteRepo = loteRepo;
        this.sessaoCaixaRepo = sessaoCaixaRepo;
        this.auditoriaServico = auditoriaServico;
        this.configuracao = configuracao;
    }

    @Override
    public Venda iniciarVenda(Utilizador utilizador) {
        var sessao = sessaoCaixaRepo.buscarSessaoAtiva(utilizador.getId());
        if (sessao == null) {
            throw new SessaoCaixaNaoEncontradaException("Abra uma sessão de caixa antes de iniciar uma venda.");
        }
        return new Venda(configuracao.getIdLoja(), utilizador.getId(), MetodoPagamento.NUMERARIO);
    }

    @Override
    public LinhaVenda adicionarLinha(Venda venda, String codigoBarras, int quantidade) {
        Produto produto = produtoRepo.buscarPorCodigoBarras(codigoBarras);
        if (produto == null) throw new ProdutoNaoEncontradoException("Produto não encontrado: " + codigoBarras);
        if (!produto.isAtivo()) throw new ProdutoInativoException("Produto inativo: " + produto.getNome());

        List<Lote> lotesFEFO = loteRepo.buscarLotesFEFO(produto.getId());
        int totalDisponivel = lotesFEFO.stream().mapToInt(Lote::getQuantidade).sum();
        if (totalDisponivel < quantidade) {
            throw new StockInsuficienteException(
                    "Stock insuficiente para '" + produto.getNome() + "'. Disponível: " + totalDisponivel);
        }

        // FEFO: decrementa dos lotes mais antigos primeiro
        // Cria uma LinhaVenda por lote consumido; retorna a primeira
        LinhaVenda primeira = null;
        int restante = quantidade;
        for (Lote lote : lotesFEFO) {
            if (restante <= 0) break;
            int retirar = Math.min(lote.getQuantidade(), restante);
            lote.setQuantidade(lote.getQuantidade() - retirar);
            loteRepo.atualizar(lote);

            LinhaVenda linha = new LinhaVenda(lote.getId(), retirar, lote.getPrecoFinal());
            venda.adicionarLinha(linha);
            if (primeira == null) primeira = linha;
            restante -= retirar;
        }
        return primeira;
    }

    @Override
    public Fatura finalizarVenda(Venda venda, MetodoPagamento metodo, double valorEntregue) {
        double total = venda.getLinhas().stream().mapToDouble(LinhaVenda::calcularSubtotal).sum();
        if (metodo == MetodoPagamento.NUMERARIO && valorEntregue < total) {
            throw new IllegalArgumentException("Valor entregue insuficiente.");
        }

        var sessao = sessaoCaixaRepo.buscarSessaoAtiva(venda.getIdUtilizador());
        if (sessao == null) {
            throw new SessaoCaixaNaoEncontradaException("Não é possível finalizar a venda: abra uma sessão de caixa antes de registar a venda.");
        }

        venda.setMetodoPagamento(metodo);
        venda.setTotalFaturado(total);
        venda.setEstado(EstadoVenda.FINALIZADA);

        // Emite fatura anónima se ainda não emitida
        if (venda.getFatura() == null) {
            venda.emitirFatura(null);
        }

        vendaRepo.guardar(venda);

        // Atualiza saldo da sessão de caixa (só para pagamentos em numerário)
        if (metodo == MetodoPagamento.NUMERARIO) {
            sessao.setSaldoAtual(sessao.getSaldoAtual() + total);
            sessaoCaixaRepo.atualizar(sessao);
        }

        auditoriaServico.registar(
                new UtilizadorPlaceholder(venda.getIdUtilizador()),
                TipoAcao.VENDA, "Venda", venda.getId(),
                String.format("Venda de %.2f € via %s (%d artigo(s))", total, metodo.name(), venda.getLinhas().size()));
        return venda.getFatura();
    }

    @Override
    public void anularVenda(Venda venda) {
        // Repõe stock nos lotes consumidos
        for (LinhaVenda linha : venda.getLinhas()) {
            Lote lote = loteRepo.buscarPorId(linha.getIdLote());
            if (lote != null) {
                lote.setQuantidade(lote.getQuantidade() + linha.getQuantidade());
                loteRepo.atualizar(lote);
            }
        }
        venda.setEstado(EstadoVenda.ANULADA);
        vendaRepo.guardar(venda);
        auditoriaServico.registar(
                new UtilizadorPlaceholder(venda.getIdUtilizador()),
                TipoAcao.VENDA, "Venda", venda.getId(),
                "Anulou venda #" + venda.getId());
    }

    @Override
    public double calcularTroco(double total, double valorEntregue) {
        if (valorEntregue < total) throw new IllegalArgumentException("Valor entregue inferior ao total.");
        return valorEntregue - total;
    }

    @Override
    public List<Venda> obterVendasPendentes() {
        return vendaRepo.buscarPendentes();
    }

    @Override
    public Venda buscarPorNumeroFatura(String numeroFatura) {
        return vendaRepo.buscarPorNumeroFatura(numeroFatura);
    }

    // Classe auxiliar para criar um Utilizador a partir apenas do id (para auditoria)
    private static class UtilizadorPlaceholder extends pt.trasmum.loja.dominio.core.Utilizador {
        UtilizadorPlaceholder(int id) { setId(id); }
    }
}
