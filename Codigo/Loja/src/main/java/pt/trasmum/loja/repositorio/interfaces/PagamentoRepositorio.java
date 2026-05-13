package pt.trasmum.loja.repositorio.interfaces;

import pt.trasmum.loja.dominio.fornecedores.Pagamento;

import java.util.List;

public interface PagamentoRepositorio {
    void guardar(Pagamento pagamento);
    void reverterEmTransito();
    List<Pagamento> buscarPendentes();
    List<Pagamento> buscarNaoPagos();
    void atualizar(Pagamento pagamento);
}
