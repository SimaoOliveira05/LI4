package pt.trasmum.loja.repositorio.interfaces;

import pt.trasmum.loja.dominio.tesouraria.FechoDia;

import java.util.List;

public interface FechoDiaRepositorio {
    void guardar(FechoDia fecho);
    void atualizar(FechoDia fecho);
    FechoDia buscarUltimoConfirmado();
    void reverterEmTransito();
    List<FechoDia> buscarPendentes();
}
