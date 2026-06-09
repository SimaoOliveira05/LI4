package pt.trasmum.servidor.dto.api;

import java.util.List;

public class MonitorRedeDTO {
    public int lojasEsperadas;
    public int lojasRecebidas;
    public int lojasPendentes;
    public List<EstadoLojaDTO> estadoPorLoja;
}
