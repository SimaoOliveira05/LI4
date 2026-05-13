package pt.trasmum.servidor.servico.impl;

import pt.trasmum.servidor.dominio.EstadoPagamentoRemessa;
import pt.trasmum.servidor.dominio.Loja;
import pt.trasmum.servidor.dominio.RemessaCentral;
import pt.trasmum.servidor.dto.api.RemessaCentralDTO;
import pt.trasmum.servidor.repositorio.interfaces.LojaRepositorio;
import pt.trasmum.servidor.repositorio.interfaces.RemessaCentralRepositorio;
import pt.trasmum.servidor.servico.interfaces.IRemessaServico;

import java.util.*;

public class RemessaServico implements IRemessaServico {
    private final RemessaCentralRepositorio remessaRepo;
    private final LojaRepositorio lojaRepo;

    public RemessaServico(RemessaCentralRepositorio remessaRepo, LojaRepositorio lojaRepo) {
        this.remessaRepo = remessaRepo;
        this.lojaRepo = lojaRepo;
    }

    @Override
    public List<RemessaCentralDTO> listarRemessas(String idLoja, String estadoPagamento) {
        EstadoPagamentoRemessa estado = null;
        if (estadoPagamento != null && !estadoPagamento.isBlank()) {
            estado = EstadoPagamentoRemessa.valueOf(estadoPagamento);
        }

        List<RemessaCentral> remessas;
        boolean temLoja = idLoja != null && !idLoja.isBlank();
        if (temLoja && estado != null) remessas = remessaRepo.buscarPorLojaEEstado(idLoja, estado);
        else if (temLoja) remessas = remessaRepo.buscarPorLoja(idLoja);
        else if (estado != null) remessas = remessaRepo.buscarPorEstado(estado);
        else remessas = remessaRepo.listarTodas();

        Map<String, String> nomes = new HashMap<>();
        for (Loja l : lojaRepo.listarTodas()) nomes.put(l.getIdLoja(), l.getNomeLoja());

        List<RemessaCentralDTO> out = new ArrayList<>();
        for (RemessaCentral r : remessas) {
            out.add(new RemessaCentralDTO(
                    r.getId(), r.getIdLoja(),
                    nomes.getOrDefault(r.getIdLoja(), r.getIdLoja()),
                    r.getNomeFornecedor(),
                    r.getDataRecepcao().toString(),
                    r.getValorTotalGuia(),
                    r.getEstadoPagamento().name()
            ));
        }
        return out;
    }
}
