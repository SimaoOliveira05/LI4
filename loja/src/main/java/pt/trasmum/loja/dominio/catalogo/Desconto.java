package pt.trasmum.loja.dominio.catalogo;

import java.time.LocalDate;

public class Desconto {

    private int id;
    private int idLote;
    private double percentagem;
    private LocalDate dataAplicacao;
    private int idUtilizadorAplicou;

    public Desconto() {}

    public Desconto(int idLote, double percentagem, LocalDate dataAplicacao, int idUtilizadorAplicou) {
        this.idLote = idLote;
        this.percentagem = percentagem;
        this.dataAplicacao = dataAplicacao;
        this.idUtilizadorAplicou = idUtilizadorAplicou;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getIdLote() { return idLote; }
    public void setIdLote(int idLote) { this.idLote = idLote; }

    public double getPercentagem() { return percentagem; }
    public void setPercentagem(double percentagem) { this.percentagem = percentagem; }

    public LocalDate getDataAplicacao() { return dataAplicacao; }
    public void setDataAplicacao(LocalDate dataAplicacao) { this.dataAplicacao = dataAplicacao; }

    public int getIdUtilizadorAplicou() { return idUtilizadorAplicou; }
    public void setIdUtilizadorAplicou(int idUtilizadorAplicou) { this.idUtilizadorAplicou = idUtilizadorAplicou; }
}
