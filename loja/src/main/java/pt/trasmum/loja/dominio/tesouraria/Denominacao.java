package pt.trasmum.loja.dominio.tesouraria;

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
