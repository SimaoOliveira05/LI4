package pt.trasmum.loja.dominio.fornecedores;

import pt.trasmum.loja.dominio.core.RegistoSincronizavel;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Remessa extends RegistoSincronizavel {

    private LocalDate dataRecepcao;
    private double valorTotalGuia;
    private int idFornecedor;
    private int idUtilizador;
    private List<LinhaRemessa> linhas;

    public Remessa() {
        super();
        this.linhas = new ArrayList<>();
    }

    public Remessa(String idLoja, int idFornecedor, int idUtilizador, LocalDate dataRecepcao, double valorTotalGuia) {
        super(idLoja);
        this.idFornecedor = idFornecedor;
        this.idUtilizador = idUtilizador;
        this.dataRecepcao = dataRecepcao;
        this.valorTotalGuia = valorTotalGuia;
        this.linhas = new ArrayList<>();
    }

    public void adicionarLinha(LinhaRemessa linha) {
        this.linhas.add(linha);
    }

    public LocalDate getDataRecepcao() { return dataRecepcao; }
    public void setDataRecepcao(LocalDate dataRecepcao) { this.dataRecepcao = dataRecepcao; }

    public double getValorTotalGuia() { return valorTotalGuia; }
    public void setValorTotalGuia(double valorTotalGuia) { this.valorTotalGuia = valorTotalGuia; }

    public int getIdFornecedor() { return idFornecedor; }
    public void setIdFornecedor(int idFornecedor) { this.idFornecedor = idFornecedor; }

    public int getIdUtilizador() { return idUtilizador; }
    public void setIdUtilizador(int idUtilizador) { this.idUtilizador = idUtilizador; }

    public List<LinhaRemessa> getLinhas() { return linhas; }
    public void setLinhas(List<LinhaRemessa> linhas) { this.linhas = linhas; }
}
