package pt.trasmum.loja.repositorio.interfaces;

import pt.trasmum.loja.dominio.fornecedores.EstadoPedido;
import pt.trasmum.loja.dominio.fornecedores.PedidoRemessa;

import java.util.List;

public interface PedidoRemessaRepositorio {
    void guardar(PedidoRemessa pedido);
    void atualizar(PedidoRemessa pedido);
    List<PedidoRemessa> buscarPorEstado(EstadoPedido estado);
    List<PedidoRemessa> buscarPendentesPorFornecedor(int idFornecedor);
}
