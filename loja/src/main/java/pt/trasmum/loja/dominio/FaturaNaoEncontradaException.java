package pt.trasmum.loja.dominio;

public class FaturaNaoEncontradaException extends RuntimeException {

    public FaturaNaoEncontradaException(String message) {
        super(message);
    }

    public FaturaNaoEncontradaException(String message, Throwable cause) {
        super(message, cause);
    }
}
