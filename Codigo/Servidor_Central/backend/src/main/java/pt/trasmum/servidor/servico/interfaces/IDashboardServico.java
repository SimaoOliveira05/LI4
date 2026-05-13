package pt.trasmum.servidor.servico.interfaces;

import pt.trasmum.servidor.dto.api.DashboardGlobalDTO;
import pt.trasmum.servidor.dto.api.DashboardLojaDTO;

import java.time.LocalDate;

public interface IDashboardServico {
    DashboardGlobalDTO obterDashboardGlobal(LocalDate inicio, LocalDate fim);
    DashboardLojaDTO obterDashboardLoja(String idLoja, LocalDate inicio, LocalDate fim);
}
