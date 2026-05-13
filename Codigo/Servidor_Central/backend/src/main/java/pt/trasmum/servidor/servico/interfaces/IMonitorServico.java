package pt.trasmum.servidor.servico.interfaces;

import pt.trasmum.servidor.dto.api.MonitorRedeDTO;

import java.time.LocalDate;

public interface IMonitorServico {
    MonitorRedeDTO obterEstadoRede(LocalDate data);
}
