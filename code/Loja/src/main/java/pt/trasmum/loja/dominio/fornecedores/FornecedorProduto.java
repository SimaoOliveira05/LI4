package pt.trasmum.loja.dominio.fornecedores;

/** Associação N:N entre Fornecedor e Produto com o preço a que o fornecedor vende à loja. */
public class FornecedorProduto {

    private int idFornecedor;
    private int idProduto;
    private double precoFornecedor;

    public FornecedorProduto() {}

    public FornecedorProduto(int idFornecedor, int idProduto, double precoFornecedor) {
        this.idFornecedor = idFornecedor;
        this.idProduto = idProduto;
        this.precoFornecedor = precoFornecedor;
    }

    public int getIdFornecedor() { return idFornecedor; }
    public void setIdFornecedor(int idFornecedor) { this.idFornecedor = idFornecedor; }

    public int getIdProduto() { return idProduto; }
    public void setIdProduto(int idProduto) { this.idProduto = idProduto; }

    public double getPrecoFornecedor() { return precoFornecedor; }
    public void setPrecoFornecedor(double precoFornecedor) { this.precoFornecedor = precoFornecedor; }
}
