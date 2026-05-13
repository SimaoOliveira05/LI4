package pt.trasmum.servidor.dto.api;

import pt.trasmum.servidor.dominio.FechoDiaCentral;
import pt.trasmum.servidor.dominio.LogAuditoriaCentral;

import java.util.List;
import java.util.Map;

public class DashboardLojaDTO {
    public String idLoja;
    public String nomeLoja;
    public double vendasTotais;
    public int totalTransacoes;
    public double totalDevolucoes;
    public double ticketMedio;
    public List<FechoDiaCentral> historicoFechos;
    public List<LogAuditoriaCentral> logsAuditoria;
    public Map<String, Double> vendasMensais;
    public Map<String, Double> vendasPorCategoria;
    public double totalDespesas;
    public double lucroLiquido;
}
