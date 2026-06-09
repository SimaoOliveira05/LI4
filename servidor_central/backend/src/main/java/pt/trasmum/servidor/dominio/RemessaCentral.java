package pt.trasmum.servidor.dominio;

import java.time.LocalDate;

public class RemessaCentral {
    private int id;
    private int idOriginalLoja;
    private String idLoja;
    private String nomeFornecedor;
    private LocalDate dataRecepcao;
    private double valorTotalGuia;
    private EstadoPagamentoRemessa estadoPagamento;

    public RemessaCentral() {}

    public RemessaCentral(int id, int idOriginalLoja, String idLoja, String nomeFornecedor,
                          LocalDate dataRecepcao, double valorTotalGuia, EstadoPagamentoRemessa estadoPagamento) {
        this.id = id;
        this.idOriginalLoja = idOriginalLoja;
        this.idLoja = idLoja;
        this.nomeFornecedor = nomeFornecedor;
        this.dataRecepcao = dataRecepcao;
        this.valorTotalGuia = valorTotalGuia;
        this.estadoPagamento = estadoPagamento;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getIdOriginalLoja() { return idOriginalLoja; }
    public void setIdOriginalLoja(int idOriginalLoja) { this.idOriginalLoja = idOriginalLoja; }
    public String getIdLoja() { return idLoja; }
    public void setIdLoja(String idLoja) { this.idLoja = idLoja; }
    public String getNomeFornecedor() { return nomeFornecedor; }
    public void setNomeFornecedor(String nomeFornecedor) { this.nomeFornecedor = nomeFornecedor; }
    public LocalDate getDataRecepcao() { return dataRecepcao; }
    public void setDataRecepcao(LocalDate dataRecepcao) { this.dataRecepcao = dataRecepcao; }
    public double getValorTotalGuia() { return valorTotalGuia; }
    public void setValorTotalGuia(double valorTotalGuia) { this.valorTotalGuia = valorTotalGuia; }
    public EstadoPagamentoRemessa getEstadoPagamento() { return estadoPagamento; }
    public void setEstadoPagamento(EstadoPagamentoRemessa estadoPagamento) { this.estadoPagamento = estadoPagamento; }
}
