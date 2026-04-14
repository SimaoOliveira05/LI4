package pt.trasmum.loja.servico.interfaces;

import pt.trasmum.loja.dominio.core.Utilizador;
import pt.trasmum.loja.dominio.fornecedores.*;
import pt.trasmum.loja.dominio.fornecedores.PedidoRemessa.EstadoPedido;

import java.util.List;

public interface IRemessaServico {
    Remessa registarRemessa(Utilizador utilizador, Fornecedor fornecedor,
                            List<LinhaRemessa> linhas, double valorTotalGuia);
    void associarPedidoPendente(Remessa remessa);
    PedidoRemessa criarPedidoRemessa(Utilizador utilizador, Fornecedor fornecedor,
                                     List<LinhaPedidoRemessa> linhas);
    List<PedidoRemessa> listarPedidos(EstadoPedido estado);
    List<Remessa> obterRemessasPendentes();
    List<Pagamento> obterPagamentosPendentes();
}
