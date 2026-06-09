package pt.trasmum.loja.dominio.vendas;

import pt.trasmum.loja.dominio.core.RegistoSincronizavel;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Devolucao extends RegistoSincronizavel {

    private LocalDateTime dataHora;
    private int quantidade;
    private LocalDate dataValidadeEmbalagem;
    private String numeroFatura;
    private int idUtilizador;
    private int idFatura;
    private int idLote;
    private double valorRestituido;

    public Devolucao() {
        super();
    }

    public Devolucao(String idLoja, int idFatura, String numeroFatura, int idUtilizador,
                     int idLote, int quantidade, LocalDate dataValidadeEmbalagem, double valorRestituido) {
        super(idLoja);
        this.idFatura = idFatura;
        this.numeroFatura = numeroFatura;
        this.idUtilizador = idUtilizador;
        this.idLote = idLote;
        this.quantidade = quantidade;
        this.dataValidadeEmbalagem = dataValidadeEmbalagem;
        this.valorRestituido = valorRestituido;
        this.dataHora = LocalDateTime.now();
    }

    public LocalDateTime getDataHora() { return dataHora; }
    public void setDataHora(LocalDateTime dataHora) { this.dataHora = dataHora; }

    public int getQuantidade() { return quantidade; }
    public void setQuantidade(int quantidade) { this.quantidade = quantidade; }

    public LocalDate getDataValidadeEmbalagem() { return dataValidadeEmbalagem; }
    public void setDataValidadeEmbalagem(LocalDate dataValidadeEmbalagem) { this.dataValidadeEmbalagem = dataValidadeEmbalagem; }

    public String getNumeroFatura() { return numeroFatura; }
    public void setNumeroFatura(String numeroFatura) { this.numeroFatura = numeroFatura; }

    public int getIdUtilizador() { return idUtilizador; }
    public void setIdUtilizador(int idUtilizador) { this.idUtilizador = idUtilizador; }

    public int getIdFatura() { return idFatura; }
    public void setIdFatura(int idFatura) { this.idFatura = idFatura; }

    public int getIdLote() { return idLote; }
    public void setIdLote(int idLote) { this.idLote = idLote; }

    public double getValorRestituido() { return valorRestituido; }
    public void setValorRestituido(double valorRestituido) { this.valorRestituido = valorRestituido; }
}
