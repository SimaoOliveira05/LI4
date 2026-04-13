package pt.trasmum.loja.repositorio.interfaces;

import pt.trasmum.loja.dominio.fornecedores.Remessa;

import java.util.List;

import pt.trasmum.loja.dominio.core.EstadoSincronizacao;

public interface RemessaRepositorio {
    void guardar(Remessa remessa);
    void atualizarSincronizacao(int id, EstadoSincronizacao estado);
    List<Remessa> buscarPendentes();
}
