package pt.trasmum.loja.dominio.core;

public class Utilizador {

    private int id;
    private String nomeUtilizador;
    private String hashPalavraPasse;
    private PerfilUtilizador perfil;
    private boolean ativo;
    private boolean emSessao;

    public Utilizador() {}

    public Utilizador(String nomeUtilizador, String hashPalavraPasse, PerfilUtilizador perfil) {
        this.nomeUtilizador = nomeUtilizador;
        this.hashPalavraPasse = hashPalavraPasse;
        this.perfil = perfil;
        this.ativo = true;
        this.emSessao = false;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNomeUtilizador() { return nomeUtilizador; }
    public void setNomeUtilizador(String nomeUtilizador) { this.nomeUtilizador = nomeUtilizador; }

    public String getHashPalavraPasse() { return hashPalavraPasse; }
    public void setHashPalavraPasse(String hashPalavraPasse) { this.hashPalavraPasse = hashPalavraPasse; }

    public PerfilUtilizador getPerfil() { return perfil; }
    public void setPerfil(PerfilUtilizador perfil) { this.perfil = perfil; }

    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }

    public boolean isEmSessao() { return emSessao; }
    public void setEmSessao(boolean emSessao) { this.emSessao = emSessao; }
}
