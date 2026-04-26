package pt.trasmum.loja.repositorio.interfaces;

import pt.trasmum.loja.dominio.core.Loja;

public interface LojaRepositorio {
    Loja obter(String idLoja);
    void guardar(Loja loja);
    void atualizar(Loja loja);
}
