package pt.trasmum.loja.dominio;

public class ArtigoNaoConstaNaFaturaException extends RuntimeException {

    public ArtigoNaoConstaNaFaturaException(String message) {
        super(message);
    }

    public ArtigoNaoConstaNaFaturaException(String message, Throwable cause) {
        super(message, cause);
    }
}
