package pt.trasmum.loja.dominio;

public class SessaoCaixaNaoEncontradaException extends RuntimeException {

    public SessaoCaixaNaoEncontradaException(String message) {
        super(message);
    }

    public SessaoCaixaNaoEncontradaException(String message, Throwable cause) {
        super(message, cause);
    }
}
