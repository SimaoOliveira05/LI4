package pt.trasmum.loja.dominio.tesouraria;

public class DetalheNumerario {

    public enum Denominacao {
        CEM_EUROS(100.0),
        CINQUENTA_EUROS(50.0),
        VINTE_EUROS(20.0),
        DEZ_EUROS(10.0),
        CINCO_EUROS(5.0),
        DOIS_EUROS(2.0),
        UM_EURO(1.0),
        CINQUENTA_CENTIMOS(0.50),
        VINTE_CENTIMOS(0.20),
        DEZ_CENTIMOS(0.10),
        CINCO_CENTIMOS(0.05),
        DOIS_CENTIMOS(0.02),
        UM_CENTIMO(0.01);

        private final double valor;

        Denominacao(double valor) {
            this.valor = valor;
        }

        public double getValor() {
            return valor;
        }
    }

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
