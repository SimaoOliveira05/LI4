package pt.trasmum.servidor.servico.interfaces;

import pt.trasmum.servidor.dto.api.RelatorioLinhaDTO;

import java.time.LocalDate;
import java.util.List;

public interface IRelatorioServico {
    List<RelatorioLinhaDTO> gerarRelatorio(String idLoja, LocalDate inicio, LocalDate fim, String categoria);
}
