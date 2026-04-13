package pt.trasmum.loja.repositorio.interfaces;

import pt.trasmum.loja.dominio.fornecedores.Pagamento;

import java.util.List;

public interface PagamentoRepositorio {
    void guardar(Pagamento pagamento);
    List<Pagamento> buscarPendentes();
    void atualizar(Pagamento pagamento);
}
