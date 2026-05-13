package pt.trasmum.loja.servico.interfaces;

import pt.trasmum.loja.dominio.core.ConfiguracaoTerminal;
import pt.trasmum.loja.dominio.core.Utilizador;
import pt.trasmum.loja.dominio.tesouraria.FechoDia;

public interface IFechoDiaServico {
    FechoDia executarFecho(Utilizador utilizador, ConfiguracaoTerminal configuracao);
    boolean reenviar(int idFecho, Utilizador utilizador);
    void reverterEmTransito();
}
