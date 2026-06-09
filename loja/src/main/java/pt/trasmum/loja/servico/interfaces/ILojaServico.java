package pt.trasmum.loja.servico.interfaces;

import pt.trasmum.loja.dominio.core.Loja;
import pt.trasmum.loja.dominio.core.Utilizador;

public interface ILojaServico {
    Loja obter();
    void atualizar(Utilizador utilizador, Loja loja);
}
