package pt.trasmum.servidor.dto.ingestao;

import java.util.List;

public class VendaDTO {
    public int idOriginalLoja;
    public String dataHora;
    public double totalFaturado;
    public String metodoPagamento;
    public List<LinhaVendaDTO> linhas;
}
