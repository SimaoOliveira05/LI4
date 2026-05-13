package pt.trasmum.servidor.dominio;

import java.time.LocalDateTime;

public class DevolucaoCentral {
    private int id;
    private int idOriginalLoja;
    private int idOriginalVenda;
    private String idLoja;
    private LocalDateTime dataHora;
    private int quantidade;
    private double valorRestituido;

    public DevolucaoCentral() {}

    public DevolucaoCentral(int id, int idOriginalLoja, int idOriginalVenda, String idLoja,
                            LocalDateTime dataHora, int quantidade, double valorRestituido) {
        this.id = id;
        this.idOriginalLoja = idOriginalLoja;
        this.idOriginalVenda = idOriginalVenda;
        this.idLoja = idLoja;
        this.dataHora = dataHora;
        this.quantidade = quantidade;
        this.valorRestituido = valorRestituido;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getIdOriginalLoja() { return idOriginalLoja; }
    public void setIdOriginalLoja(int idOriginalLoja) { this.idOriginalLoja = idOriginalLoja; }
    public int getIdOriginalVenda() { return idOriginalVenda; }
    public void setIdOriginalVenda(int idOriginalVenda) { this.idOriginalVenda = idOriginalVenda; }
    public String getIdLoja() { return idLoja; }
    public void setIdLoja(String idLoja) { this.idLoja = idLoja; }
    public LocalDateTime getDataHora() { return dataHora; }
    public void setDataHora(LocalDateTime dataHora) { this.dataHora = dataHora; }
    public int getQuantidade() { return quantidade; }
    public void setQuantidade(int quantidade) { this.quantidade = quantidade; }
    public double getValorRestituido() { return valorRestituido; }
    public void setValorRestituido(double valorRestituido) { this.valorRestituido = valorRestituido; }
}
