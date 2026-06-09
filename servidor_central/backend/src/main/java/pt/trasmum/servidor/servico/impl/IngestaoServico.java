package pt.trasmum.servidor.servico.impl;

import com.google.gson.Gson;
import pt.trasmum.servidor.dominio.*;
import pt.trasmum.servidor.dto.ingestao.*;
import pt.trasmum.servidor.repositorio.interfaces.*;
import pt.trasmum.servidor.servico.interfaces.IIngestaoServico;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class IngestaoServico implements IIngestaoServico {
    private final pt.trasmum.servidor.infra.DatabaseConnection db;
    private final LojaRepositorio lojaRepo;
    private final FechoDiaCentralRepositorio fechoRepo;
    private final VendaCentralRepositorio vendaRepo;
    private final DevolucaoCentralRepositorio devolucaoRepo;
    private final RemessaCentralRepositorio remessaRepo;
    private final PagamentoCentralRepositorio pagamentoRepo;
    private final SessaoCaixaCentralRepositorio sessaoRepo;
    private final LogAuditoriaCentralRepositorio logRepo;
    private final Gson gson;

    public IngestaoServico(pt.trasmum.servidor.infra.DatabaseConnection db,
                           LojaRepositorio lojaRepo,
                           FechoDiaCentralRepositorio fechoRepo,
                           VendaCentralRepositorio vendaRepo,
                           DevolucaoCentralRepositorio devolucaoRepo,
                           RemessaCentralRepositorio remessaRepo,
                           PagamentoCentralRepositorio pagamentoRepo,
                           SessaoCaixaCentralRepositorio sessaoRepo,
                           LogAuditoriaCentralRepositorio logRepo,
                           Gson gson) {
        this.db = db;
        this.lojaRepo = lojaRepo;
        this.fechoRepo = fechoRepo;
        this.vendaRepo = vendaRepo;
        this.devolucaoRepo = devolucaoRepo;
        this.remessaRepo = remessaRepo;
        this.pagamentoRepo = pagamentoRepo;
        this.sessaoRepo = sessaoRepo;
        this.logRepo = logRepo;
        this.gson = gson;
    }

    @Override
    public void processarPacote(PacoteFechoDTO pacote) {
        verificarHash(pacote);

        Connection conn = null;
        try {
            conn = db.obter();
            conn.setAutoCommit(false);

            lojaRepo.criarSeNaoExistir(conn, new Loja(pacote.idLoja, pacote.nomeLoja));

            FechoDiaCentral fecho = new FechoDiaCentral(
                    0, pacote.idLoja,
                    LocalDate.parse(pacote.dataFecho),
                    LocalDateTime.now()
            );
            fechoRepo.guardar(conn, fecho);

            if (pacote.vendas != null) for (VendaDTO v : pacote.vendas) vendaRepo.guardar(conn, mapearVenda(pacote.idLoja, v));
            if (pacote.devolucoes != null) for (DevolucaoDTO d : pacote.devolucoes) devolucaoRepo.guardar(conn, mapearDevolucao(pacote.idLoja, d));
            if (pacote.remessas != null) for (RemessaDTO r : pacote.remessas) remessaRepo.guardar(conn, mapearRemessa(pacote.idLoja, r));
            if (pacote.pagamentos != null) for (PagamentoDTO p : pacote.pagamentos) {
                pagamentoRepo.guardar(conn, mapearPagamento(pacote.idLoja, p));
                if (p.dataPagamento != null) {
                    remessaRepo.atualizarEstadoPagamento(conn, pacote.idLoja, p.idOriginalRemessa, EstadoPagamentoRemessa.PAGA);
                }
            }
            if (pacote.sessoesCaixa != null) for (SessaoCaixaDTO s : pacote.sessoesCaixa) sessaoRepo.guardar(conn, mapearSessao(pacote.idLoja, s));
            if (pacote.logs != null) for (LogAuditoriaDTO l : pacote.logs) logRepo.guardar(conn, mapearLog(pacote.idLoja, l));

            conn.commit();
        } catch (SQLException e) {
            try { if (conn != null) conn.rollback(); } catch (SQLException ignored) {}
            throw new RuntimeException("Erro a processar pacote: " + e.getMessage(), e);
        } catch (RuntimeException e) {
            try { if (conn != null) conn.rollback(); } catch (SQLException ignored) {}
            throw e;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ignored) {}
            }
        }
    }

    private void verificarHash(PacoteFechoDTO pacote) {
        String hashRecebido = pacote.hashIntegridade;
        if (hashRecebido == null || hashRecebido.isBlank()) {
            throw new IntegridadeInvalidaException("Hash de integridade ausente");
        }
        pacote.hashIntegridade = null;
        String json = gson.toJson(pacote);
        pacote.hashIntegridade = hashRecebido;
        String calculado = sha256(json);
        if (!calculado.equalsIgnoreCase(hashRecebido)) {
            throw new IntegridadeInvalidaException("Hash de integridade inválido");
        }
    }

    private static String sha256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private VendaCentral mapearVenda(String idLoja, VendaDTO v) {
        List<LinhaVendaCentral> linhas = new ArrayList<>();
        if (v.linhas != null) {
            for (LinhaVendaDTO l : v.linhas) {
                linhas.add(new LinhaVendaCentral(
                        0, 0, l.idOriginalLoja, idLoja,
                        l.nomeProduto, l.categoria, l.quantidade, l.precoUnitario, l.subtotal
                ));
            }
        }
        return new VendaCentral(
                0, v.idOriginalLoja, idLoja,
                LocalDateTime.parse(v.dataHora),
                v.totalFaturado,
                MetodoPagamento.valueOf(v.metodoPagamento),
                linhas
        );
    }

    private DevolucaoCentral mapearDevolucao(String idLoja, DevolucaoDTO d) {
        return new DevolucaoCentral(
                0, d.idOriginalLoja, d.idOriginalVenda, idLoja,
                LocalDateTime.parse(d.dataHora),
                d.quantidade, d.valorRestituido
        );
    }

    private RemessaCentral mapearRemessa(String idLoja, RemessaDTO r) {
        EstadoPagamentoRemessa estado;
        switch (r.estadoPagamento) {
            case "PENDENTE", "PENDENTE_PAGAMENTO" -> estado = EstadoPagamentoRemessa.PENDENTE_PAGAMENTO;
            case "PAGO", "PAGA" -> estado = EstadoPagamentoRemessa.PAGA;
            default -> throw new IllegalArgumentException("Estado de pagamento desconhecido: " + r.estadoPagamento);
        }
        return new RemessaCentral(
                0, r.idOriginalLoja, idLoja, r.nomeFornecedor,
                LocalDate.parse(r.dataRecepcao), r.valorTotalGuia, estado
        );
    }

    private PagamentoCentral mapearPagamento(String idLoja, PagamentoDTO p) {
        return new PagamentoCentral(
                0, p.idOriginalLoja, p.idOriginalRemessa, idLoja, p.valor,
                p.dataPagamento != null ? LocalDate.parse(p.dataPagamento.substring(0, 10)) : null,
                p.tipoPagamento != null ? TipoPagamento.valueOf(p.tipoPagamento) : null
        );
    }

    private SessaoCaixaCentral mapearSessao(String idLoja, SessaoCaixaDTO s) {
        return new SessaoCaixaCentral(
                0, s.idOriginalLoja, idLoja, s.idUtilizador, s.saldoFinal,
                LocalDateTime.parse(s.dataAbertura),
                s.dataEncerramento != null ? LocalDateTime.parse(s.dataEncerramento) : null
        );
    }

    private LogAuditoriaCentral mapearLog(String idLoja, LogAuditoriaDTO l) {
        return new LogAuditoriaCentral(
                0, l.idOriginalLoja, idLoja,
                TipoAcao.valueOf(l.acao),
                LocalDateTime.parse(l.dataHora),
                l.nomeUtilizador,
                l.descricao
        );
    }
}
