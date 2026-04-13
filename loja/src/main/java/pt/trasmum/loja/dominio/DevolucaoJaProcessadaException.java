package pt.trasmum.loja.dominio;

public class DevolucaoJaProcessadaException extends RuntimeException {

    public DevolucaoJaProcessadaException(String message) {
        super(message);
    }

    public DevolucaoJaProcessadaException(String message, Throwable cause) {
        super(message, cause);
    }
}
