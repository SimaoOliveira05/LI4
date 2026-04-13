package pt.trasmum.loja.dominio;

public class CredenciaisInvalidasException extends RuntimeException {

    public CredenciaisInvalidasException(String message) {
        super(message);
    }

    public CredenciaisInvalidasException(String message, Throwable cause) {
        super(message, cause);
    }
}
