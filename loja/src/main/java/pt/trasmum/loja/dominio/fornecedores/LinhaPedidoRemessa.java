package pt.trasmum.loja.dominio.fornecedores;

public class LinhaPedidoRemessa {

    private int id;
    private int idPedidoRemessa;
    private int idProduto;
    private int quantidadePretendida;

    public LinhaPedidoRemessa() {}

    public LinhaPedidoRemessa(int idPedidoRemessa, int idProduto, int quantidadePretendida) {
        this.idPedidoRemessa = idPedidoRemessa;
        this.idProduto = idProduto;
        this.quantidadePretendida = quantidadePretendida;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getIdPedidoRemessa() { return idPedidoRemessa; }
    public void setIdPedidoRemessa(int idPedidoRemessa) { this.idPedidoRemessa = idPedidoRemessa; }

    public int getIdProduto() { return idProduto; }
    public void setIdProduto(int idProduto) { this.idProduto = idProduto; }

    public int getQuantidadePretendida() { return quantidadePretendida; }
    public void setQuantidadePretendida(int quantidadePretendida) { this.quantidadePretendida = quantidadePretendida; }
}
