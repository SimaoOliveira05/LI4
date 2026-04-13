package pt.trasmum.loja.dominio;

public class AcessoNegadoException extends RuntimeException {

    public AcessoNegadoException(String message) {
        super(message);
    }

    public AcessoNegadoException(String message, Throwable cause) {
        super(message, cause);
    }
}
