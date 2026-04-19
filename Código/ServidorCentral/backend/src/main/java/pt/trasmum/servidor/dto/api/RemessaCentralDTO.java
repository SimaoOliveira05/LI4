package pt.trasmum.servidor.dto.api;

public class RemessaCentralDTO {
    public int id;
    public String idLoja;
    public String nomeLoja;
    public String nomeFornecedor;
    public String dataRecepcao;
    public double valorTotalGuia;
    public String estadoPagamento;

    public RemessaCentralDTO() {}

    public RemessaCentralDTO(int id, String idLoja, String nomeLoja, String nomeFornecedor,
                             String dataRecepcao, double valorTotalGuia, String estadoPagamento) {
        this.id = id;
        this.idLoja = idLoja;
        this.nomeLoja = nomeLoja;
        this.nomeFornecedor = nomeFornecedor;
        this.dataRecepcao = dataRecepcao;
        this.valorTotalGuia = valorTotalGuia;
        this.estadoPagamento = estadoPagamento;
    }
}
