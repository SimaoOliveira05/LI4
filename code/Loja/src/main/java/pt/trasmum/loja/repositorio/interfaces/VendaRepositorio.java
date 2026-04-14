package pt.trasmum.loja.repositorio.interfaces;

import pt.trasmum.loja.dominio.core.EstadoSincronizacao;
import pt.trasmum.loja.dominio.vendas.Venda;

import java.util.List;

public interface VendaRepositorio {
    void guardar(Venda venda);
    void atualizarSincronizacao(int id, EstadoSincronizacao estado);
    Venda buscarPorId(int id);
    Venda buscarPorNumeroFatura(String numeroFatura);
    List<Venda> buscarPendentes();
    void marcarConfirmadas(List<Integer> ids);
}
