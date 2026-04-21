package pt.trasmum.loja.servico.interfaces;

import pt.trasmum.loja.dominio.core.Utilizador;
import pt.trasmum.loja.dominio.fornecedores.Pagamento;

import java.util.List;

public interface IPagamentoServico {
    List<Pagamento> listarPendentes();
    List<Pagamento> listarNaoPagos();
    void liquidar(Utilizador utilizador, int idPagamento);
}
