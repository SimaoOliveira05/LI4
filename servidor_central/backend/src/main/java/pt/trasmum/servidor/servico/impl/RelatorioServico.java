package pt.trasmum.servidor.servico.impl;

import pt.trasmum.servidor.dto.api.RelatorioLinhaDTO;
import pt.trasmum.servidor.repositorio.interfaces.VendaCentralRepositorio;
import pt.trasmum.servidor.servico.interfaces.IRelatorioServico;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class RelatorioServico implements IRelatorioServico {
    private final VendaCentralRepositorio vendaRepo;

    public RelatorioServico(VendaCentralRepositorio vendaRepo) {
        this.vendaRepo = vendaRepo;
    }

    @Override
    public List<RelatorioLinhaDTO> gerarRelatorio(String idLoja, LocalDate inicio, LocalDate fim, String categoria) {
        List<Object[]> linhas = vendaRepo.relatorioAgregado(idLoja, inicio, fim, categoria);
        List<RelatorioLinhaDTO> out = new ArrayList<>();
        for (Object[] linha : linhas) {
            out.add(new RelatorioLinhaDTO(
                    linha[0].toString(),
                    (String) linha[1],
                    (String) linha[2],
                    (String) linha[3],
                    ((Number) linha[4]).doubleValue()
            ));
        }
        return out;
    }
}
