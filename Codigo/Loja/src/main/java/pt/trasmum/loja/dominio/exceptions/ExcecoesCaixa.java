package pt.trasmum.loja.dominio.exceptions;

public final class ExcecoesCaixa {
    private ExcecoesCaixa() {}

    public static class SessaoCaixaJaAbertaException extends RuntimeException {
        public SessaoCaixaJaAbertaException(String message) { super(message); }
    }

    public static class SessaoCaixaNaoEncontradaException extends RuntimeException {
        public SessaoCaixaNaoEncontradaException(String message) { super(message); }
    }

    public static class SangriaInvalidaException extends RuntimeException {
        public SangriaInvalidaException(String message) { super(message); }
    }
}
