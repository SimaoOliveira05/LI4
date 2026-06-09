package pt.trasmum.loja.dominio.core;

public class Loja {

    private String id;
    private String nome;
    private String morada;
    private String localidade;
    private String nif;
    private String email;
    private double limiteMaximoCaixa;
    private int diasAlertaValidade;

    public Loja(String id, String nome, String morada, String localidade, String nif, String email,
                double limiteMaximoCaixa, int diasAlertaValidade) {
        this.id = id;
        this.nome = nome;
        this.morada = morada;
        this.localidade = localidade;
        this.nif = nif;
        this.email = email;
        this.limiteMaximoCaixa = limiteMaximoCaixa;
        this.diasAlertaValidade = diasAlertaValidade;
    }

    public String getId()                  { return id; }
    public String getNome()                { return nome; }
    public String getMorada()              { return morada; }
    public String getLocalidade()          { return localidade; }
    public String getNif()                 { return nif; }
    public String getEmail()               { return email; }
    public double getLimiteMaximoCaixa()   { return limiteMaximoCaixa; }
    public int getDiasAlertaValidade()     { return diasAlertaValidade; }

    public void setNome(String nome)                           { this.nome = nome; }
    public void setMorada(String morada)                       { this.morada = morada; }
    public void setLocalidade(String localidade)               { this.localidade = localidade; }
    public void setNif(String nif)                             { this.nif = nif; }
    public void setEmail(String email)                         { this.email = email; }
    public void setLimiteMaximoCaixa(double limiteMaximoCaixa) { this.limiteMaximoCaixa = limiteMaximoCaixa; }
    public void setDiasAlertaValidade(int diasAlertaValidade)  { this.diasAlertaValidade = diasAlertaValidade; }
}
