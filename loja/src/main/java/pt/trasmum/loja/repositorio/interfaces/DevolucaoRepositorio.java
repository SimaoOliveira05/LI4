package pt.trasmum.loja.repositorio.interfaces;

import pt.trasmum.loja.dominio.vendas.Devolucao;

import java.util.List;

import pt.trasmum.loja.dominio.core.EstadoSincronizacao;

public interface DevolucaoRepositorio {
    void guardar(Devolucao devolucao);
    void atualizarSincronizacao(int id, EstadoSincronizacao estado);
    List<Devolucao> buscarPorFatura(String numeroFatura);
    List<Devolucao> buscarPendentes();
}
