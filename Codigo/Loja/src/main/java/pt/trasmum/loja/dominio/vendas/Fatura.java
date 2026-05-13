package pt.trasmum.loja.dominio.vendas;

import java.time.LocalDateTime;

public class Fatura {

    private int id;
    private String numeroFatura;
    private LocalDateTime dataEmissao;
    private String nifCliente;
    private int idVenda;

    public Fatura() {}

    public Fatura(int idVenda, String idLoja, String nifCliente) {
        this.idVenda = idVenda;
        this.nifCliente = nifCliente;
        this.dataEmissao = LocalDateTime.now();
        this.numeroFatura = "FAT-" + idLoja + "-" + dataEmissao.toInstant(java.time.ZoneOffset.UTC).toEpochMilli();
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNumeroFatura() { return numeroFatura; }
    public void setNumeroFatura(String numeroFatura) { this.numeroFatura = numeroFatura; }

    public LocalDateTime getDataEmissao() { return dataEmissao; }
    public void setDataEmissao(LocalDateTime dataEmissao) { this.dataEmissao = dataEmissao; }

    public String getNifCliente() { return nifCliente; }
    public void setNifCliente(String nifCliente) { this.nifCliente = nifCliente; }

    public int getIdVenda() { return idVenda; }
    public void setIdVenda(int idVenda) { this.idVenda = idVenda; }
}
