package pt.trasmum.loja.servico.interfaces;

import pt.trasmum.loja.dominio.core.Utilizador;
import pt.trasmum.loja.servico.UtilizadorDTO;

public interface IUtilizadorServico {
    Utilizador criar(Utilizador gestor, UtilizadorDTO dados);
    void desativar(Utilizador gestor, int idAlvo);
    void editar(Utilizador gestor, int idAlvo, UtilizadorDTO dados);
    void validarGestorAtivo(String idLoja);
}
