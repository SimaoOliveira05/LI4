package pt.trasmum.loja.dominio.tesouraria;

import pt.trasmum.loja.dominio.core.RegistoSincronizavel;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SessaoCaixa extends RegistoSincronizavel {

    private int idUtilizador;
    private List<DetalheNumerario> fundoInicial;
    private List<Sangria> sangrias;
    private double saldoAtual;
    private LocalDateTime dataAbertura;
    private LocalDateTime dataEncerramento;

    public SessaoCaixa() {
        super();
        this.fundoInicial = new ArrayList<>();
        this.sangrias = new ArrayList<>();
        this.dataAbertura = LocalDateTime.now();
    }

    public SessaoCaixa(String idLoja, int idUtilizador, List<DetalheNumerario> fundoInicial) {
        super(idLoja);
        this.idUtilizador = idUtilizador;
        this.fundoInicial = fundoInicial != null ? fundoInicial : new ArrayList<>();
        this.sangrias = new ArrayList<>();
        this.dataAbertura = LocalDateTime.now();
        this.saldoAtual = this.fundoInicial.stream().mapToDouble(DetalheNumerario::getSubtotal).sum();
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

    public List<DetalheNumerario> getFundoInicial() { return fundoInicial; }
    public void setFundoInicial(List<DetalheNumerario> fundoInicial) { this.fundoInicial = fundoInicial; }

    public List<Sangria> getSangrias() { return sangrias; }
    public void setSangrias(List<Sangria> sangrias) { this.sangrias = sangrias; }

    public double getSaldoAtual() { return saldoAtual; }
    public void setSaldoAtual(double saldoAtual) { this.saldoAtual = saldoAtual; }

    public LocalDateTime getDataAbertura() { return dataAbertura; }
    public void setDataAbertura(LocalDateTime dataAbertura) { this.dataAbertura = dataAbertura; }

    public LocalDateTime getDataEncerramento() { return dataEncerramento; }
    public void setDataEncerramento(LocalDateTime dataEncerramento) { this.dataEncerramento = dataEncerramento; }
}
