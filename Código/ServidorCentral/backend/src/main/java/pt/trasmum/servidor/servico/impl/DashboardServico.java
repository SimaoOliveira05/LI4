package pt.trasmum.servidor.servico.impl;

import pt.trasmum.servidor.dominio.Loja;
import pt.trasmum.servidor.dto.api.DashboardGlobalDTO;
import pt.trasmum.servidor.dto.api.DashboardLojaDTO;
import pt.trasmum.servidor.dto.api.DecomposicaoLojaDTO;
import pt.trasmum.servidor.repositorio.interfaces.*;
import pt.trasmum.servidor.servico.interfaces.IDashboardServico;

import java.time.LocalDate;
import java.util.*;

public class DashboardServico implements IDashboardServico {
    private final LojaRepositorio lojaRepo;
    private final VendaCentralRepositorio vendaRepo;
    private final DevolucaoCentralRepositorio devolucaoRepo;
    private final FechoDiaCentralRepositorio fechoRepo;
    private final LogAuditoriaCentralRepositorio logRepo;

    public DashboardServico(LojaRepositorio lojaRepo, VendaCentralRepositorio vendaRepo,
                            DevolucaoCentralRepositorio devolucaoRepo,
                            FechoDiaCentralRepositorio fechoRepo,
                            LogAuditoriaCentralRepositorio logRepo) {
        this.lojaRepo = lojaRepo;
        this.vendaRepo = vendaRepo;
        this.devolucaoRepo = devolucaoRepo;
        this.fechoRepo = fechoRepo;
        this.logRepo = logRepo;
    }

    @Override
    public DashboardGlobalDTO obterDashboardGlobal(LocalDate inicio, LocalDate fim) {
        DashboardGlobalDTO dto = new DashboardGlobalDTO();
        dto.vendasTotais = vendaRepo.totalVendasPorPeriodo(inicio, fim);
        dto.totalTransacoes = vendaRepo.totalTransacoesPorPeriodo(inicio, fim);
        dto.ticketMedio = dto.totalTransacoes > 0 ? dto.vendasTotais / dto.totalTransacoes : 0.0;
        dto.totalDevolucoes = devolucaoRepo.totalDevolucoesPorPeriodo(inicio, fim);

        Map<String, Double> mp = vendaRepo.totaisPorMetodoPagamento(inicio, fim);
        double num = mp.getOrDefault("NUMERARIO", 0.0);
        double mb = mp.getOrDefault("MULTIBANCO", 0.0);
        double tot = num + mb;
        dto.percentagemNumerario = tot > 0 ? (num / tot) * 100.0 : 0.0;
        dto.percentagemMultibanco = tot > 0 ? (mb / tot) * 100.0 : 0.0;

        List<Loja> lojas = lojaRepo.listarTodas();
        List<DecomposicaoLojaDTO> decomp = new ArrayList<>();
        for (Loja l : lojas) {
            decomp.add(new DecomposicaoLojaDTO(
                    l.getIdLoja(), l.getNomeLoja(),
                    vendaRepo.totalVendasPorLojaEPeriodo(l.getIdLoja(), inicio, fim),
                    vendaRepo.totalTransacoesPorLojaEPeriodo(l.getIdLoja(), inicio, fim),
                    devolucaoRepo.totalDevolucoesPorLojaEPeriodo(l.getIdLoja(), inicio, fim)
            ));
        }
        dto.decomposicaoPorLoja = decomp;

        int ano = fim.getYear();
        if (lojas.size() > 0) dto.vendasMensaisLoja1 = vendaRepo.vendasMensaisPorLoja(lojas.get(0).getIdLoja(), ano);
        else dto.vendasMensaisLoja1 = new LinkedHashMap<>();
        if (lojas.size() > 1) dto.vendasMensaisLoja2 = vendaRepo.vendasMensaisPorLoja(lojas.get(1).getIdLoja(), ano);
        else dto.vendasMensaisLoja2 = new LinkedHashMap<>();

        dto.vendasPorCategoria = vendaRepo.vendasPorCategoria(inicio, fim);

        dto.lojasEsperadas = lojas.size();
        dto.lojasSincronizadas = fechoRepo.buscarPorData(LocalDate.now()).size();

        return dto;
    }

    @Override
    public DashboardLojaDTO obterDashboardLoja(String idLoja, LocalDate inicio, LocalDate fim) {
        DashboardLojaDTO dto = new DashboardLojaDTO();
        Loja loja = lojaRepo.buscarPorId(idLoja);
        dto.idLoja = idLoja;
        dto.nomeLoja = loja != null ? loja.getNomeLoja() : idLoja;
        dto.vendasTotais = vendaRepo.totalVendasPorLojaEPeriodo(idLoja, inicio, fim);
        dto.totalTransacoes = vendaRepo.totalTransacoesPorLojaEPeriodo(idLoja, inicio, fim);
        dto.totalDevolucoes = devolucaoRepo.totalDevolucoesPorLojaEPeriodo(idLoja, inicio, fim);
        dto.ticketMedio = dto.totalTransacoes > 0 ? dto.vendasTotais / dto.totalTransacoes : 0.0;
        dto.historicoFechos = fechoRepo.buscarPorLoja(idLoja);
        dto.logsAuditoria = logRepo.buscarPorLojaEPeriodo(idLoja, inicio, fim);
        dto.vendasMensais = vendaRepo.vendasMensaisPorLoja(idLoja, fim.getYear());
        dto.vendasPorCategoria = vendaRepo.vendasPorCategoriaPorLoja(idLoja, inicio, fim);
        return dto;
    }
}
