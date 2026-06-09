package pt.trasmum.servidor.dominio;

public class IntegridadeInvalidaException extends RuntimeException {
    public IntegridadeInvalidaException(String mensagem) {
        super(mensagem);
    }
}
