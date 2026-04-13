package pt.trasmum.loja.dominio;

public class ProdutoInativoException extends RuntimeException {

    public ProdutoInativoException(String message) {
        super(message);
    }

    public ProdutoInativoException(String message, Throwable cause) {
        super(message, cause);
    }
}
