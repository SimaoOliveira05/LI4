package pt.trasmum.loja.dominio.catalogo;

import java.time.LocalDate;

public class Lote {

    private int id;
    private int idProduto;
    private int quantidade;
    private LocalDate dataValidade;
    private double precoVenda;
    private Desconto desconto;

    public Lote() {}

    public Lote(int idProduto, int quantidade, LocalDate dataValidade, double precoVenda) {
        this.idProduto = idProduto;
        this.quantidade = quantidade;
        this.dataValidade = dataValidade;
        this.precoVenda = precoVenda;
    }

    public void aplicarDesconto(Desconto desconto) {
        this.desconto = desconto;
    }

    public double getPrecoFinal() {
        if (desconto != null) {
            return precoVenda * (1.0 - desconto.getPercentagem() / 100.0);
        }
        return precoVenda;
    }

    public boolean temDesconto() {
        return desconto != null;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getIdProduto() { return idProduto; }
    public void setIdProduto(int idProduto) { this.idProduto = idProduto; }

    public int getQuantidade() { return quantidade; }
    public void setQuantidade(int quantidade) { this.quantidade = quantidade; }

    public LocalDate getDataValidade() { return dataValidade; }
    public void setDataValidade(LocalDate dataValidade) { this.dataValidade = dataValidade; }

    public double getPrecoVenda() { return precoVenda; }
    public void setPrecoVenda(double precoVenda) { this.precoVenda = precoVenda; }

    public Desconto getDesconto() { return desconto; }
    public void setDesconto(Desconto desconto) { this.desconto = desconto; }
}
