package pt.trasmum.loja.dominio.tesouraria;

public class DetalheNumerario {

    public enum Denominacao {
        CEM_EUROS      (100.0,  "Cem Euros"),
        CINQUENTA_EUROS( 50.0,  "Cinquenta Euros"),
        VINTE_EUROS    ( 20.0,  "Vinte Euros"),
        DEZ_EUROS      ( 10.0,  "Dez Euros"),
        CINCO_EUROS    (  5.0,  "Cinco Euros"),
        DOIS_EUROS     (  2.0,  "Dois Euros"),
        UM_EURO        (  1.0,  "Um Euro"),
        CINQUENTA_CENTIMOS(0.50, "Cinquenta Cêntimos"),
        VINTE_CENTIMOS (  0.20, "Vinte Cêntimos"),
        DEZ_CENTIMOS   (  0.10, "Dez Cêntimos"),
        CINCO_CENTIMOS (  0.05, "Cinco Cêntimos"),
        DOIS_CENTIMOS  (  0.02, "Dois Cêntimos"),
        UM_CENTIMO     (  0.01, "Um Cêntimo");

        private final double valor;
        private final String label;

        Denominacao(double valor, String label) {
            this.valor = valor;
            this.label = label;
        }

        public double getValor() { return valor; }
        public String getLabel() { return label; }
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
