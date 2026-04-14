package pt.trasmum.loja.dominio;

public class RemessaSemLinhasException extends RuntimeException {

    public RemessaSemLinhasException(String message) {
        super(message);
    }

    public RemessaSemLinhasException(String message, Throwable cause) {
        super(message, cause);
    }
}
