package pt.trasmum.loja.servico.impl;

import pt.trasmum.loja.dominio.core.Loja;
import pt.trasmum.loja.dominio.core.PerfilUtilizador;
import pt.trasmum.loja.dominio.core.TipoAcao;
import pt.trasmum.loja.dominio.core.Utilizador;
import pt.trasmum.loja.repositorio.interfaces.LojaRepositorio;
import pt.trasmum.loja.servico.interfaces.IAuditoriaServico;
import pt.trasmum.loja.servico.interfaces.IAutorizacaoServico;
import pt.trasmum.loja.servico.interfaces.ILojaServico;

public class LojaServico implements ILojaServico {

    private final LojaRepositorio lojaRepo;
    private final IAutorizacaoServico autorizacaoServico;
    private final IAuditoriaServico auditoriaServico;
    private final String idLoja;

    public LojaServico(LojaRepositorio lojaRepo, IAutorizacaoServico autorizacaoServico,
                       IAuditoriaServico auditoriaServico, String idLoja) {
        this.lojaRepo = lojaRepo;
        this.autorizacaoServico = autorizacaoServico;
        this.auditoriaServico = auditoriaServico;
        this.idLoja = idLoja;
    }

    @Override
    public Loja obter() {
        return lojaRepo.obter(idLoja);
    }

    @Override
    public void atualizar(Utilizador utilizador, Loja loja) {
        autorizacaoServico.exigirPerfil(utilizador, PerfilUtilizador.GESTOR, PerfilUtilizador.CEO);
        lojaRepo.atualizar(loja);
        auditoriaServico.registar(utilizador, TipoAcao.ALTERACAO_CATALOGO, "Loja", 0,
                "Atualizou informações da loja '" + loja.getNome() + "'");
    }
}
