package pt.trasmum.servidor.dominio;

public class Loja {
    private String idLoja;
    private String nomeLoja;

    public Loja() {}

    public Loja(String idLoja, String nomeLoja) {
        this.idLoja = idLoja;
        this.nomeLoja = nomeLoja;
    }

    public String getIdLoja() { return idLoja; }
    public void setIdLoja(String idLoja) { this.idLoja = idLoja; }
    public String getNomeLoja() { return nomeLoja; }
    public void setNomeLoja(String nomeLoja) { this.nomeLoja = nomeLoja; }
}
