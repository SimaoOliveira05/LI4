package pt.trasmum.servidor.dominio;

import java.time.LocalDateTime;

public class SessaoCaixaCentral {
    private int id;
    private int idOriginalLoja;
    private String idLoja;
    private int idUtilizador;
    private double saldoFinal;
    private LocalDateTime dataAbertura;
    private LocalDateTime dataEncerramento;

    public SessaoCaixaCentral() {}

    public SessaoCaixaCentral(int id, int idOriginalLoja, String idLoja, int idUtilizador,
                              double saldoFinal, LocalDateTime dataAbertura, LocalDateTime dataEncerramento) {
        this.id = id;
        this.idOriginalLoja = idOriginalLoja;
        this.idLoja = idLoja;
        this.idUtilizador = idUtilizador;
        this.saldoFinal = saldoFinal;
        this.dataAbertura = dataAbertura;
        this.dataEncerramento = dataEncerramento;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getIdOriginalLoja() { return idOriginalLoja; }
    public void setIdOriginalLoja(int idOriginalLoja) { this.idOriginalLoja = idOriginalLoja; }
    public String getIdLoja() { return idLoja; }
    public void setIdLoja(String idLoja) { this.idLoja = idLoja; }
    public int getIdUtilizador() { return idUtilizador; }
    public void setIdUtilizador(int idUtilizador) { this.idUtilizador = idUtilizador; }
    public double getSaldoFinal() { return saldoFinal; }
    public void setSaldoFinal(double saldoFinal) { this.saldoFinal = saldoFinal; }
    public LocalDateTime getDataAbertura() { return dataAbertura; }
    public void setDataAbertura(LocalDateTime dataAbertura) { this.dataAbertura = dataAbertura; }
    public LocalDateTime getDataEncerramento() { return dataEncerramento; }
    public void setDataEncerramento(LocalDateTime dataEncerramento) { this.dataEncerramento = dataEncerramento; }
}
