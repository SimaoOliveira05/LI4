package pt.trasmum.servidor.dominio;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class VendaCentral {
    private int id;
    private int idOriginalLoja;
    private String idLoja;
    private LocalDateTime dataHora;
    private double totalFaturado;
    private MetodoPagamento metodoPagamento;
    private List<LinhaVendaCentral> linhas = new ArrayList<>();

    public VendaCentral() {}

    public VendaCentral(int id, int idOriginalLoja, String idLoja, LocalDateTime dataHora,
                        double totalFaturado, MetodoPagamento metodoPagamento,
                        List<LinhaVendaCentral> linhas) {
        this.id = id;
        this.idOriginalLoja = idOriginalLoja;
        this.idLoja = idLoja;
        this.dataHora = dataHora;
        this.totalFaturado = totalFaturado;
        this.metodoPagamento = metodoPagamento;
        this.linhas = linhas != null ? linhas : new ArrayList<>();
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getIdOriginalLoja() { return idOriginalLoja; }
    public void setIdOriginalLoja(int idOriginalLoja) { this.idOriginalLoja = idOriginalLoja; }
    public String getIdLoja() { return idLoja; }
    public void setIdLoja(String idLoja) { this.idLoja = idLoja; }
    public LocalDateTime getDataHora() { return dataHora; }
    public void setDataHora(LocalDateTime dataHora) { this.dataHora = dataHora; }
    public double getTotalFaturado() { return totalFaturado; }
    public void setTotalFaturado(double totalFaturado) { this.totalFaturado = totalFaturado; }
    public MetodoPagamento getMetodoPagamento() { return metodoPagamento; }
    public void setMetodoPagamento(MetodoPagamento metodoPagamento) { this.metodoPagamento = metodoPagamento; }
    public List<LinhaVendaCentral> getLinhas() { return linhas; }
    public void setLinhas(List<LinhaVendaCentral> linhas) { this.linhas = linhas; }
}
