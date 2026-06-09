package pt.trasmum.loja.dominio.exceptions;

public class EnvioFechoFalhouException extends RuntimeException {

    public EnvioFechoFalhouException(String message) {
        super(message);
    }

    public EnvioFechoFalhouException(String message, Throwable cause) {
        super(message, cause);
    }
}
