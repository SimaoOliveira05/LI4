package pt.trasmum.loja.sincronizacao;

public interface SincronizacaoGateway {
    boolean enviarFechoDia(PacoteFechoDTO pacote);
    boolean verificarConexao();
}
