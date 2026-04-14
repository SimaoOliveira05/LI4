package pt.trasmum.loja.servico.interfaces;

import pt.trasmum.loja.dominio.core.Utilizador;
import pt.trasmum.loja.dominio.vendas.*;
import pt.trasmum.loja.dominio.vendas.Venda.MetodoPagamento;

import java.util.List;

public interface IVendaServico {
    Venda iniciarVenda(Utilizador utilizador);
    LinhaVenda adicionarLinha(Venda venda, String codigoBarras, int quantidade);
    Fatura finalizarVenda(Venda venda, MetodoPagamento metodo, double valorEntregue);
    void anularVenda(Venda venda);
    double calcularTroco(double total, double valorEntregue);
    List<Venda> obterVendasPendentes();
}
