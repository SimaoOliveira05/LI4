package pt.trasmum.loja.servico.impl;

import pt.trasmum.loja.dominio.core.PerfilUtilizador;
import pt.trasmum.loja.dominio.core.Utilizador;
import pt.trasmum.loja.dominio.exceptions.ExcecoesSeguranca.AcessoNegadoException;
import pt.trasmum.loja.servico.interfaces.IAutorizacaoServico;

public class AutorizacaoServico implements IAutorizacaoServico {

    @Override
    public void exigirPerfil(Utilizador utilizador, PerfilUtilizador... perfisPermitidos) {
        for (PerfilUtilizador p : perfisPermitidos) {
            if (utilizador.getPerfil() == p) return;
        }
        throw new AcessoNegadoException(
                "Acesso negado. Perfil '" + utilizador.getPerfil() + "' não tem permissão para esta operação.");
    }

    @Override
    public boolean temPerfil(Utilizador utilizador, PerfilUtilizador perfil) {
        return utilizador.getPerfil() == perfil;
    }
}
