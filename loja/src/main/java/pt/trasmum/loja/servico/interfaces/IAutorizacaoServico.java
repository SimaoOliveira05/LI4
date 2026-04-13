package pt.trasmum.loja.servico.interfaces;

import pt.trasmum.loja.dominio.core.PerfilUtilizador;
import pt.trasmum.loja.dominio.core.Utilizador;

public interface IAutorizacaoServico {
    void exigirPerfil(Utilizador utilizador, PerfilUtilizador... perfisPermitidos);
    boolean temPerfil(Utilizador utilizador, PerfilUtilizador perfil);
}
