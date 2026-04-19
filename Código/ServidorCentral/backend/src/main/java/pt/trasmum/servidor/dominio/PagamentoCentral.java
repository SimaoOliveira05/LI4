package pt.trasmum.servidor.dominio;

import java.time.LocalDate;

public class PagamentoCentral {
    private int id;
    private int idOriginalLoja;
    private int idOriginalRemessa;
    private String idLoja;
    private double valor;
    private LocalDate dataPagamento;
    private TipoPagamento tipoPagamento;

    public PagamentoCentral() {}

    public PagamentoCentral(int id, int idOriginalLoja, int idOriginalRemessa, String idLoja,
                            double valor, LocalDate dataPagamento, TipoPagamento tipoPagamento) {
        this.id = id;
        this.idOriginalLoja = idOriginalLoja;
        this.idOriginalRemessa = idOriginalRemessa;
        this.idLoja = idLoja;
        this.valor = valor;
        this.dataPagamento = dataPagamento;
        this.tipoPagamento = tipoPagamento;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getIdOriginalLoja() { return idOriginalLoja; }
    public void setIdOriginalLoja(int idOriginalLoja) { this.idOriginalLoja = idOriginalLoja; }
    public int getIdOriginalRemessa() { return idOriginalRemessa; }
    public void setIdOriginalRemessa(int idOriginalRemessa) { this.idOriginalRemessa = idOriginalRemessa; }
    public String getIdLoja() { return idLoja; }
    public void setIdLoja(String idLoja) { this.idLoja = idLoja; }
    public double getValor() { return valor; }
    public void setValor(double valor) { this.valor = valor; }
    public LocalDate getDataPagamento() { return dataPagamento; }
    public void setDataPagamento(LocalDate dataPagamento) { this.dataPagamento = dataPagamento; }
    public TipoPagamento getTipoPagamento() { return tipoPagamento; }
    public void setTipoPagamento(TipoPagamento tipoPagamento) { this.tipoPagamento = tipoPagamento; }
}
