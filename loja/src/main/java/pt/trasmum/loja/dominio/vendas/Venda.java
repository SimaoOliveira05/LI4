package pt.trasmum.loja.dominio.vendas;

import pt.trasmum.loja.dominio.core.RegistoSincronizavel;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Venda extends RegistoSincronizavel {

    private LocalDateTime dataHora;
    private MetodoPagamento metodoPagamento;
    private double totalFaturado;
    private EstadoVenda estado;
    private int idUtilizador;
    private List<LinhaVenda> linhas;
    private Fatura fatura;

    public Venda() {
        super();
        this.linhas = new ArrayList<>();
        this.estado = EstadoVenda.EM_CURSO;
        this.dataHora = LocalDateTime.now();
    }

    public Venda(String idLoja, int idUtilizador, MetodoPagamento metodoPagamento) {
        super(idLoja);
        this.idUtilizador = idUtilizador;
        this.metodoPagamento = metodoPagamento;
        this.estado = EstadoVenda.EM_CURSO;
        this.dataHora = LocalDateTime.now();
        this.linhas = new ArrayList<>();
    }

    public void adicionarLinha(LinhaVenda linha) {
        this.linhas.add(linha);
    }

    public void emitirFatura(String nifCliente) {
        this.fatura = new Fatura(this.id, this.idLoja, nifCliente);
    }

    public LocalDateTime getDataHora() { return dataHora; }
    public void setDataHora(LocalDateTime dataHora) { this.dataHora = dataHora; }

    public MetodoPagamento getMetodoPagamento() { return metodoPagamento; }
    public void setMetodoPagamento(MetodoPagamento metodoPagamento) { this.metodoPagamento = metodoPagamento; }

    public double getTotalFaturado() { return totalFaturado; }
    public void setTotalFaturado(double totalFaturado) { this.totalFaturado = totalFaturado; }

    public EstadoVenda getEstado() { return estado; }
    public void setEstado(EstadoVenda estado) { this.estado = estado; }

    public int getIdUtilizador() { return idUtilizador; }
    public void setIdUtilizador(int idUtilizador) { this.idUtilizador = idUtilizador; }

    public List<LinhaVenda> getLinhas() { return linhas; }
    public void setLinhas(List<LinhaVenda> linhas) { this.linhas = linhas; }

    public Fatura getFatura() { return fatura; }
    public void setFatura(Fatura fatura) { this.fatura = fatura; }
}
