package pt.trasmum.servidor.servico.impl;

import pt.trasmum.servidor.dominio.FechoDiaCentral;
import pt.trasmum.servidor.dominio.Loja;
import pt.trasmum.servidor.dto.api.EstadoLojaDTO;
import pt.trasmum.servidor.dto.api.MonitorRedeDTO;
import pt.trasmum.servidor.repositorio.interfaces.FechoDiaCentralRepositorio;
import pt.trasmum.servidor.repositorio.interfaces.LojaRepositorio;
import pt.trasmum.servidor.servico.interfaces.IMonitorServico;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class MonitorServico implements IMonitorServico {
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final LojaRepositorio lojaRepo;
    private final FechoDiaCentralRepositorio fechoRepo;

    public MonitorServico(LojaRepositorio lojaRepo, FechoDiaCentralRepositorio fechoRepo) {
        this.lojaRepo = lojaRepo;
        this.fechoRepo = fechoRepo;
    }

    @Override
    public MonitorRedeDTO obterEstadoRede(LocalDate data) {
        List<Loja> lojas = lojaRepo.listarTodas();
        List<FechoDiaCentral> fechos = fechoRepo.buscarPorData(data);
        Map<String, FechoDiaCentral> porLoja = new HashMap<>();
        for (FechoDiaCentral f : fechos) porLoja.put(f.getIdLoja(), f);

        MonitorRedeDTO dto = new MonitorRedeDTO();
        dto.lojasEsperadas = lojas.size();
        dto.lojasRecebidas = porLoja.size();
        dto.lojasPendentes = Math.max(0, lojas.size() - porLoja.size());
        dto.estadoPorLoja = new ArrayList<>();
        for (Loja l : lojas) {
            FechoDiaCentral f = porLoja.get(l.getIdLoja());
            dto.estadoPorLoja.add(new EstadoLojaDTO(
                    l.getIdLoja(),
                    l.getNomeLoja(),
                    f != null ? "received" : "missing",
                    f != null ? f.getDataRecepcao().format(FMT) : null
            ));
        }
        return dto;
    }
}
