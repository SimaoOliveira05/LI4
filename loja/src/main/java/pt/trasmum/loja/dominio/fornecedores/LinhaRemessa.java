package pt.trasmum.loja.dominio.fornecedores;

import java.time.LocalDate;

public class LinhaRemessa {

    private int id;
    private int idRemessa;
    private int idProduto;
    private int idLoteGerado;
    private int quantidade;
    private LocalDate dataValidade;

    public LinhaRemessa() {}

    public LinhaRemessa(int idRemessa, int idProduto, int quantidade, LocalDate dataValidade) {
        this.idRemessa = idRemessa;
        this.idProduto = idProduto;
        this.quantidade = quantidade;
        this.dataValidade = dataValidade;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getIdRemessa() { return idRemessa; }
    public void setIdRemessa(int idRemessa) { this.idRemessa = idRemessa; }

    public int getIdProduto() { return idProduto; }
    public void setIdProduto(int idProduto) { this.idProduto = idProduto; }

    public int getIdLoteGerado() { return idLoteGerado; }
    public void setIdLoteGerado(int idLoteGerado) { this.idLoteGerado = idLoteGerado; }

    public int getQuantidade() { return quantidade; }
    public void setQuantidade(int quantidade) { this.quantidade = quantidade; }

    public LocalDate getDataValidade() { return dataValidade; }
    public void setDataValidade(LocalDate dataValidade) { this.dataValidade = dataValidade; }
}
