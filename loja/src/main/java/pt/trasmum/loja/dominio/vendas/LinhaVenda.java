package pt.trasmum.loja.dominio.vendas;

public class LinhaVenda {

    private int id;
    private int idVenda;
    private int idLote;
    private int quantidade;
    private double precoUnitario;

    public LinhaVenda() {}

    public LinhaVenda(int idLote, int quantidade, double precoUnitario) {
        this.idLote = idLote;
        this.quantidade = quantidade;
        this.precoUnitario = precoUnitario;
    }

    public double calcularSubtotal() {
        return quantidade * precoUnitario;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getIdVenda() { return idVenda; }
    public void setIdVenda(int idVenda) { this.idVenda = idVenda; }

    public int getIdLote() { return idLote; }
    public void setIdLote(int idLote) { this.idLote = idLote; }

    public int getQuantidade() { return quantidade; }
    public void setQuantidade(int quantidade) { this.quantidade = quantidade; }

    public double getPrecoUnitario() { return precoUnitario; }
    public void setPrecoUnitario(double precoUnitario) { this.precoUnitario = precoUnitario; }
}
