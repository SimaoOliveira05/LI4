package pt.trasmum.loja.dominio.tesouraria;

public class DetalheNumerario {

    private int id;
    private int idSessaoCaixa;
    private int idSangria;
    private Denominacao denominacao;
    private int quantidade;
    private double subtotal;

    public DetalheNumerario() {}

    public DetalheNumerario(Denominacao denominacao, int quantidade) {
        this.denominacao = denominacao;
        this.quantidade = quantidade;
        this.subtotal = calcularSubtotal();
    }

    public double calcularSubtotal() {
        return denominacao != null ? denominacao.getValor() * quantidade : 0.0;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getIdSessaoCaixa() { return idSessaoCaixa; }
    public void setIdSessaoCaixa(int idSessaoCaixa) { this.idSessaoCaixa = idSessaoCaixa; }

    public int getIdSangria() { return idSangria; }
    public void setIdSangria(int idSangria) { this.idSangria = idSangria; }

    public Denominacao getDenominacao() { return denominacao; }
    public void setDenominacao(Denominacao denominacao) { this.denominacao = denominacao; }

    public int getQuantidade() { return quantidade; }
    public void setQuantidade(int quantidade) { this.quantidade = quantidade; }

    public double getSubtotal() { return subtotal; }
    public void setSubtotal(double subtotal) { this.subtotal = subtotal; }
}
