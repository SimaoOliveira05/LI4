package pt.trasmum.loja.dominio;

public class SessaoDuplicadaException extends RuntimeException {

    public SessaoDuplicadaException(String message) {
        super(message);
    }

    public SessaoDuplicadaException(String message, Throwable cause) {
        super(message, cause);
    }
}
