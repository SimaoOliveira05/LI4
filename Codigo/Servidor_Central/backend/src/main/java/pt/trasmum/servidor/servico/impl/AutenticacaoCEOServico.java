package pt.trasmum.servidor.servico.impl;

import org.mindrot.jbcrypt.BCrypt;
import pt.trasmum.servidor.dominio.CEO;
import pt.trasmum.servidor.dominio.ContaBloqueadaException;
import pt.trasmum.servidor.dominio.ContaCEOJaExisteException;
import pt.trasmum.servidor.dominio.CredenciaisInvalidasException;
import pt.trasmum.servidor.repositorio.interfaces.CEORepositorio;
import pt.trasmum.servidor.servico.interfaces.IAutenticacaoCEOServico;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

public class AutenticacaoCEOServico implements IAutenticacaoCEOServico {
    private final CEORepositorio repo;
    private final int tentativasMaximas;
    private final int duracaoBloqueioMinutos;

    public AutenticacaoCEOServico(CEORepositorio repo, Properties config) {
        this.repo = repo;
        this.tentativasMaximas = Integer.parseInt(config.getProperty("auth.tentativasMaximas", "5"));
        this.duracaoBloqueioMinutos = Integer.parseInt(config.getProperty("auth.duracaoBloqueioMinutos", "5"));
    }

    @Override
    public CEO autenticar(String nomeUtilizador, String palavraPasse) {
        CEO ceo = repo.buscarPorNomeUtilizador(nomeUtilizador);
        if (ceo == null) {
            throw new CredenciaisInvalidasException("Credenciais inválidas");
        }
        if (estaBloqueado(ceo)) {
            String ate = ceo.getBloqueadoAte().format(DateTimeFormatter.ofPattern("HH:mm"));
            throw new ContaBloqueadaException("Conta bloqueada até " + ate);
        }
        if (!BCrypt.checkpw(palavraPasse, ceo.getHashPalavraPasse())) {
            registarFalhado(ceo);
            throw new CredenciaisInvalidasException("Credenciais inválidas");
        }
        limparBloqueio(ceo);
        apagarBootstrapSeNecessario(ceo);
        return ceo;
    }

    @Override
    public void criar(String nomeUtilizador, String palavraPasse) {
        if (nomeUtilizador == null || nomeUtilizador.isBlank())
            throw new IllegalArgumentException("Nome de utilizador obrigatório");
        if (palavraPasse == null || palavraPasse.isBlank())
            throw new IllegalArgumentException("Palavra-passe obrigatória");
        if (repo.existeContaDefinitiva())
            throw new ContaCEOJaExisteException("Já existe uma conta CEO registada no sistema. Não é possível criar outra.");
        repo.criar(new CEO(nomeUtilizador, BCrypt.hashpw(palavraPasse, BCrypt.gensalt()), 0, null));
    }

    @Override
    public void registarFalhado(CEO ceo) {
        ceo.setTentativasLogin(ceo.getTentativasLogin() + 1);
        if (ceo.getTentativasLogin() >= tentativasMaximas) {
            ceo.setBloqueadoAte(LocalDateTime.now().plusMinutes(duracaoBloqueioMinutos));
        }
        repo.atualizar(ceo);
    }

    @Override
    public void limparBloqueio(CEO ceo) {
        ceo.setTentativasLogin(0);
        ceo.setBloqueadoAte(null);
        repo.atualizar(ceo);
    }

    @Override
    public boolean estaBloqueado(CEO ceo) {
        return ceo.getBloqueadoAte() != null && ceo.getBloqueadoAte().isAfter(LocalDateTime.now());
    }

    private void apagarBootstrapSeNecessario(CEO ceoAutenticado) {
        if ("admin".equals(ceoAutenticado.getNomeUtilizador())) return;
        // Se existe outra conta autenticada e a conta admin de bootstrap ainda existe, removê-la.
        CEO bootstrap = repo.buscarPorNomeUtilizador("admin");
        if (bootstrap != null) {
            repo.apagarPorNomeUtilizador("admin");
        }
    }
}
