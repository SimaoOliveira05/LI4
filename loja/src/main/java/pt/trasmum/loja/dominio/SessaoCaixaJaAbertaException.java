package pt.trasmum.loja.dominio;

public class SessaoCaixaJaAbertaException extends RuntimeException {

    public SessaoCaixaJaAbertaException(String message) {
        super(message);
    }

    public SessaoCaixaJaAbertaException(String message, Throwable cause) {
        super(message, cause);
    }
}
