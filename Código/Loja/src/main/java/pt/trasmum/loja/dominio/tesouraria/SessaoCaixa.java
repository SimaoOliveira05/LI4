package pt.trasmum.loja.dominio.tesouraria;

import pt.trasmum.loja.dominio.core.RegistoSincronizavel;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SessaoCaixa extends RegistoSincronizavel {

    private int idUtilizador;
    private double fundoInicial;
    private List<DetalheNumerario> detalheFundoInicial;
    private List<Sangria> sangrias;
    private double saldoAtual;
    private Double saldoContado;
    private Double diferencaFecho;
    private LocalDateTime dataAbertura;
    private LocalDateTime dataEncerramento;

    public SessaoCaixa() {
        super();
        this.detalheFundoInicial = new ArrayList<>();
        this.sangrias = new ArrayList<>();
        this.dataAbertura = LocalDateTime.now();
    }

    public SessaoCaixa(String idLoja, int idUtilizador, double fundo) {
        super(idLoja);
        this.idUtilizador = idUtilizador;
        this.fundoInicial = fundo;
        this.detalheFundoInicial = new ArrayList<>();
        this.sangrias = new ArrayList<>();
        this.dataAbertura = LocalDateTime.now();
        this.saldoAtual = fundo;
    }

    public void registarSangria(Sangria sangria) {
        this.sangrias.add(sangria);
        this.saldoAtual -= sangria.getTotal();
    }

    public boolean excedeLimite(double limiteMaximo) {
        return this.saldoAtual > limiteMaximo;
    }

    public int getIdUtilizador() { return idUtilizador; }
    public void setIdUtilizador(int idUtilizador) { this.idUtilizador = idUtilizador; }

    public double getFundoInicial() { return fundoInicial; }
    public void setFundoInicial(double fundoInicial) { this.fundoInicial = fundoInicial; }

    public List<DetalheNumerario> getDetalheFundoInicial() { return detalheFundoInicial; }
    public void setDetalheFundoInicial(List<DetalheNumerario> detalheFundoInicial) { this.detalheFundoInicial = detalheFundoInicial; }

    public List<Sangria> getSangrias() { return sangrias; }
    public void setSangrias(List<Sangria> sangrias) { this.sangrias = sangrias; }

    public double getSaldoAtual() { return saldoAtual; }
    public void setSaldoAtual(double saldoAtual) { this.saldoAtual = saldoAtual; }

    public Double getSaldoContado() { return saldoContado; }
    public void setSaldoContado(Double saldoContado) { this.saldoContado = saldoContado; }

    public Double getDiferencaFecho() { return diferencaFecho; }
    public void setDiferencaFecho(Double diferencaFecho) { this.diferencaFecho = diferencaFecho; }

    public LocalDateTime getDataAbertura() { return dataAbertura; }
    public void setDataAbertura(LocalDateTime dataAbertura) { this.dataAbertura = dataAbertura; }

    public LocalDateTime getDataEncerramento() { return dataEncerramento; }
    public void setDataEncerramento(LocalDateTime dataEncerramento) { this.dataEncerramento = dataEncerramento; }
}
