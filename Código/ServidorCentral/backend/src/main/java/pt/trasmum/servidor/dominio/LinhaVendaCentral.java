package pt.trasmum.servidor.dominio;

public class LinhaVendaCentral {
    private int id;
    private int idVendaCentral;
    private int idOriginalLoja;
    private String idLoja;
    private String nomeProduto;
    private String categoria;
    private int quantidade;
    private double precoUnitario;
    private double subtotal;

    public LinhaVendaCentral() {}

    public LinhaVendaCentral(int id, int idVendaCentral, int idOriginalLoja, String idLoja,
                             String nomeProduto, String categoria, int quantidade,
                             double precoUnitario, double subtotal) {
        this.id = id;
        this.idVendaCentral = idVendaCentral;
        this.idOriginalLoja = idOriginalLoja;
        this.idLoja = idLoja;
        this.nomeProduto = nomeProduto;
        this.categoria = categoria;
        this.quantidade = quantidade;
        this.precoUnitario = precoUnitario;
        this.subtotal = subtotal;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getIdVendaCentral() { return idVendaCentral; }
    public void setIdVendaCentral(int idVendaCentral) { this.idVendaCentral = idVendaCentral; }
    public int getIdOriginalLoja() { return idOriginalLoja; }
    public void setIdOriginalLoja(int idOriginalLoja) { this.idOriginalLoja = idOriginalLoja; }
    public String getIdLoja() { return idLoja; }
    public void setIdLoja(String idLoja) { this.idLoja = idLoja; }
    public String getNomeProduto() { return nomeProduto; }
    public void setNomeProduto(String nomeProduto) { this.nomeProduto = nomeProduto; }
    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }
    public int getQuantidade() { return quantidade; }
    public void setQuantidade(int quantidade) { this.quantidade = quantidade; }
    public double getPrecoUnitario() { return precoUnitario; }
    public void setPrecoUnitario(double precoUnitario) { this.precoUnitario = precoUnitario; }
    public double getSubtotal() { return subtotal; }
    public void setSubtotal(double subtotal) { this.subtotal = subtotal; }
}
