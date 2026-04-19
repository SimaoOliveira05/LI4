package pt.trasmum.loja.dominio.exceptions;

public final class ExcecoesSeguranca {
    private ExcecoesSeguranca() {}

    public static class AcessoNegadoException extends RuntimeException {
        public AcessoNegadoException(String message) { super(message); }
    }

    public static class CredenciaisInvalidasException extends RuntimeException {
        public CredenciaisInvalidasException(String message) { super(message); }
    }

    public static class SessaoDuplicadaException extends RuntimeException {
        public SessaoDuplicadaException(String message) { super(message); }
    }
}
