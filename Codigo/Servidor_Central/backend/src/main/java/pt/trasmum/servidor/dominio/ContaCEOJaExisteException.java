package pt.trasmum.servidor.dominio;

public class ContaCEOJaExisteException extends RuntimeException {
    public ContaCEOJaExisteException(String mensagem) {
        super(mensagem);
    }
}
