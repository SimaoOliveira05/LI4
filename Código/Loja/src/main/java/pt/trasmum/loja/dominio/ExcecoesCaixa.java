package pt.trasmum.loja.dominio;

public final class ExcecoesCaixa {
    private ExcecoesCaixa() {}

    public static class SessaoCaixaJaAbertaException extends RuntimeException {
        public SessaoCaixaJaAbertaException(String message) { super(message); }
    }

    public static class SessaoCaixaNaoEncontradaException extends RuntimeException {
        public SessaoCaixaNaoEncontradaException(String message) { super(message); }
    }
}
