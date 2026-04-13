package pt.trasmum.loja.dominio.fornecedores;

public class Fornecedor {

    private int id;
    private String nome;
    private String nif;
    private String morada;
    private String telefone;
    private String email;
    private String iban;

    public Fornecedor() {}

    public Fornecedor(String nome, String nif) {
        this.nome = nome;
        this.nif = nif;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getNif() { return nif; }
    public void setNif(String nif) { this.nif = nif; }

    public String getMorada() { return morada; }
    public void setMorada(String morada) { this.morada = morada; }

    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getIban() { return iban; }
    public void setIban(String iban) { this.iban = iban; }
}
