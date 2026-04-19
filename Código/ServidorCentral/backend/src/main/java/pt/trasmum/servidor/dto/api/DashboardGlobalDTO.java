package pt.trasmum.servidor.dto.api;

import java.util.List;
import java.util.Map;

public class DashboardGlobalDTO {
    public double vendasTotais;
    public int totalTransacoes;
    public double ticketMedio;
    public double totalDevolucoes;
    public double percentagemNumerario;
    public double percentagemMultibanco;
    public List<DecomposicaoLojaDTO> decomposicaoPorLoja;
    public Map<String, Double> vendasMensaisLoja1;
    public Map<String, Double> vendasMensaisLoja2;
    public Map<String, Double> vendasPorCategoria;
    public int lojasEsperadas;
    public int lojasSincronizadas;
}
