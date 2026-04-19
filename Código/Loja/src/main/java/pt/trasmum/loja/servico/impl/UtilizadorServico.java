package pt.trasmum.loja.servico.impl;

import org.mindrot.jbcrypt.BCrypt;
import pt.trasmum.loja.dominio.core.PerfilUtilizador;
import pt.trasmum.loja.dominio.core.TipoAcao;
import pt.trasmum.loja.dominio.core.Utilizador;
import pt.trasmum.loja.repositorio.interfaces.UtilizadorRepositorio;
import pt.trasmum.loja.servico.UtilizadorDTO;
import pt.trasmum.loja.servico.interfaces.IAuditoriaServico;
import pt.trasmum.loja.servico.interfaces.IAutorizacaoServico;
import pt.trasmum.loja.servico.interfaces.IUtilizadorServico;

import java.util.List;

public class UtilizadorServico implements IUtilizadorServico {

    private final UtilizadorRepositorio utilizadorRepo;
    private final IAutorizacaoServico autorizacaoServico;
    private final IAuditoriaServico auditoriaServico;

    public UtilizadorServico(UtilizadorRepositorio utilizadorRepo,
                              IAutorizacaoServico autorizacaoServico,
                              IAuditoriaServico auditoriaServico) {
        this.utilizadorRepo = utilizadorRepo;
        this.autorizacaoServico = autorizacaoServico;
        this.auditoriaServico = auditoriaServico;
    }

    @Override
    public Utilizador criar(Utilizador gestor, UtilizadorDTO dados) {
        autorizacaoServico.exigirPerfil(gestor, PerfilUtilizador.GESTOR, PerfilUtilizador.CEO);

        // CEO cria GESTOR ou FUNCIONARIO; GESTOR só cria FUNCIONARIO
        if (gestor.getPerfil() == PerfilUtilizador.CEO && dados.perfil() != PerfilUtilizador.GESTOR && dados.perfil() != PerfilUtilizador.FUNCIONARIO) {
            throw new pt.trasmum.loja.dominio.exceptions.ExcecoesSeguranca.AcessoNegadoException("CEO só pode criar utilizadores com perfil GESTOR ou FUNCIONARIO.");
        }
        if (gestor.getPerfil() == PerfilUtilizador.GESTOR && dados.perfil() != PerfilUtilizador.FUNCIONARIO) {
            throw new pt.trasmum.loja.dominio.exceptions.ExcecoesSeguranca.AcessoNegadoException("GESTOR só pode criar utilizadores com perfil FUNCIONARIO.");
        }

        String hash = BCrypt.hashpw(dados.palavraPasse(), BCrypt.gensalt());
        Utilizador novo = new Utilizador(dados.nomeUtilizador(), hash, dados.perfil());
        utilizadorRepo.guardar(novo);
        auditoriaServico.registar(gestor, TipoAcao.GESTAO_UTILIZADOR, "Utilizador", novo.getId());
        return novo;
    }

    @Override
    public void desativar(Utilizador gestor, int idAlvo) {
        autorizacaoServico.exigirPerfil(gestor, PerfilUtilizador.GESTOR, PerfilUtilizador.CEO);
        Utilizador alvo = utilizadorRepo.buscarPorId(idAlvo);
        if (alvo == null) throw new IllegalArgumentException("Utilizador não encontrado: " + idAlvo);

        // Não pode desativar o único GESTOR ativo
        if (alvo.getPerfil() == PerfilUtilizador.GESTOR) {
            long gestoresAtivos = utilizadorRepo.listarAtivos().stream()
                    .filter(u -> u.getPerfil() == PerfilUtilizador.GESTOR)
                    .count();
            if (gestoresAtivos <= 1) {
                throw new IllegalStateException("Não é possível desativar o único gestor ativo na loja.");
            }
        }
        utilizadorRepo.desativar(idAlvo);
        auditoriaServico.registar(gestor, TipoAcao.GESTAO_UTILIZADOR, "Utilizador", idAlvo);
    }

    @Override
    public void editar(Utilizador gestor, int idAlvo, UtilizadorDTO dados) {
        autorizacaoServico.exigirPerfil(gestor, PerfilUtilizador.GESTOR, PerfilUtilizador.CEO);
        Utilizador alvo = utilizadorRepo.buscarPorId(idAlvo);
        if (alvo == null) throw new IllegalArgumentException("Utilizador não encontrado: " + idAlvo);
        alvo.setNomeUtilizador(dados.nomeUtilizador());
        if (dados.palavraPasse() != null && !dados.palavraPasse().isBlank()) {
            alvo.setHashPalavraPasse(BCrypt.hashpw(dados.palavraPasse(), BCrypt.gensalt()));
        }
        utilizadorRepo.atualizar(alvo);
        auditoriaServico.registar(gestor, TipoAcao.GESTAO_UTILIZADOR, "Utilizador", idAlvo);
    }

    @Override
    public void validarGestorAtivo(String idLoja) {
        List<Utilizador> ativos = utilizadorRepo.listarAtivos();
        boolean temGestor = ativos.stream()
                .anyMatch(u -> u.getPerfil() == PerfilUtilizador.GESTOR);
        if (!temGestor) {
            throw new IllegalStateException("Não existe nenhum gestor ativo na loja " + idLoja + ".");
        }
    }
}
