package pt.trasmum.servidor.dominio;

import java.time.LocalDateTime;

public class CEO {
    private int id;
    private String nomeUtilizador;
    private String hashPalavraPasse;
    private int tentativasLogin;
    private LocalDateTime bloqueadoAte;

    public CEO() {}

    public CEO(int id, String nomeUtilizador, String hashPalavraPasse, int tentativasLogin, LocalDateTime bloqueadoAte) {
        this.id = id;
        this.nomeUtilizador = nomeUtilizador;
        this.hashPalavraPasse = hashPalavraPasse;
        this.tentativasLogin = tentativasLogin;
        this.bloqueadoAte = bloqueadoAte;
    }

    public CEO(String nomeUtilizador, String hashPalavraPasse, int tentativasLogin, LocalDateTime bloqueadoAte) {
        this(0, nomeUtilizador, hashPalavraPasse, tentativasLogin, bloqueadoAte);
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNomeUtilizador() { return nomeUtilizador; }
    public void setNomeUtilizador(String nomeUtilizador) { this.nomeUtilizador = nomeUtilizador; }
    public String getHashPalavraPasse() { return hashPalavraPasse; }
    public void setHashPalavraPasse(String hashPalavraPasse) { this.hashPalavraPasse = hashPalavraPasse; }
    public int getTentativasLogin() { return tentativasLogin; }
    public void setTentativasLogin(int tentativasLogin) { this.tentativasLogin = tentativasLogin; }
    public LocalDateTime getBloqueadoAte() { return bloqueadoAte; }
    public void setBloqueadoAte(LocalDateTime bloqueadoAte) { this.bloqueadoAte = bloqueadoAte; }
}
