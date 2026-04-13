package pt.trasmum.loja.dominio;

public class QuantidadeDevolucaoExcedidaException extends RuntimeException {

    public QuantidadeDevolucaoExcedidaException(String message) {
        super(message);
    }

    public QuantidadeDevolucaoExcedidaException(String message, Throwable cause) {
        super(message, cause);
    }
}
