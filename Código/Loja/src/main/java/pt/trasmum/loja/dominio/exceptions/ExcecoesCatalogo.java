package pt.trasmum.loja.dominio.exceptions;

public final class ExcecoesCatalogo {
    private ExcecoesCatalogo() {}

    public static class ProdutoInativoException extends RuntimeException {
        public ProdutoInativoException(String message) { super(message); }
    }

    public static class ProdutoNaoEncontradoException extends RuntimeException {
        public ProdutoNaoEncontradoException(String message) { super(message); }
    }

    public static class ProdutoNaoFornecidoException extends RuntimeException {
        public ProdutoNaoFornecidoException(String message) { super(message); }
    }
}
