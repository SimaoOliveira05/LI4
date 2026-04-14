package pt.trasmum.loja.dominio;

public final class ExcecoesVenda {
    private ExcecoesVenda() {}

    public static class StockInsuficienteException extends RuntimeException {
        public StockInsuficienteException(String message) { super(message); }
    }

    public static class FaturaNaoEncontradaException extends RuntimeException {
        public FaturaNaoEncontradaException(String message) { super(message); }
    }

    public static class ArtigoNaoConstaNaFaturaException extends RuntimeException {
        public ArtigoNaoConstaNaFaturaException(String message) { super(message); }
    }

    public static class DevolucaoJaProcessadaException extends RuntimeException {
        public DevolucaoJaProcessadaException(String message) { super(message); }
    }

    public static class QuantidadeDevolucaoExcedidaException extends RuntimeException {
        public QuantidadeDevolucaoExcedidaException(String message) { super(message); }
    }
}
