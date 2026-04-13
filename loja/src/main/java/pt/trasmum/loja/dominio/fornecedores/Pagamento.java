package pt.trasmum.loja.dominio.fornecedores;

import pt.trasmum.loja.dominio.core.RegistoSincronizavel;

import java.time.LocalDateTime;

public class Pagamento extends RegistoSincronizavel {

    private int idFornecedor;
    private int idRemessa;
    private int idUtilizador;
    private double valor;
    private LocalDateTime dataHora;
    private EstadoPagamento estadoPagamento;

    public Pagamento() {
        super();
        this.estadoPagamento = EstadoPagamento.PENDENTE;
    }

    public Pagamento(String idLoja, int idFornecedor, int idRemessa, double valor) {
        super(idLoja);
        this.idFornecedor = idFornecedor;
        this.idRemessa = idRemessa;
        this.valor = valor;
        this.estadoPagamento = EstadoPagamento.PENDENTE;
    }

    public int getIdFornecedor() { return idFornecedor; }
    public void setIdFornecedor(int idFornecedor) { this.idFornecedor = idFornecedor; }

    public int getIdRemessa() { return idRemessa; }
    public void setIdRemessa(int idRemessa) { this.idRemessa = idRemessa; }

    public int getIdUtilizador() { return idUtilizador; }
    public void setIdUtilizador(int idUtilizador) { this.idUtilizador = idUtilizador; }

    public double getValor() { return valor; }
    public void setValor(double valor) { this.valor = valor; }

    public LocalDateTime getDataHora() { return dataHora; }
    public void setDataHora(LocalDateTime dataHora) { this.dataHora = dataHora; }

    public EstadoPagamento getEstadoPagamento() { return estadoPagamento; }
    public void setEstadoPagamento(EstadoPagamento estadoPagamento) { this.estadoPagamento = estadoPagamento; }
}
