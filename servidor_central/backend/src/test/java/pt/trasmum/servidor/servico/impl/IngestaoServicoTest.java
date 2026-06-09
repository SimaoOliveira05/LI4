package pt.trasmum.servidor.servico.impl;

import com.google.gson.Gson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pt.trasmum.servidor.dominio.IntegridadeInvalidaException;
import pt.trasmum.servidor.dto.ingestao.*;
import pt.trasmum.servidor.infra.DatabaseConnection;
import pt.trasmum.servidor.repositorio.interfaces.*;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IngestaoServicoTest {

    @Mock DatabaseConnection db;
    @Mock Connection conn;
    @Mock LojaRepositorio lojaRepo;
    @Mock FechoDiaCentralRepositorio fechoRepo;
    @Mock VendaCentralRepositorio vendaRepo;
    @Mock DevolucaoCentralRepositorio devolucaoRepo;
    @Mock RemessaCentralRepositorio remessaRepo;
    @Mock PagamentoCentralRepositorio pagamentoRepo;
    @Mock SessaoCaixaCentralRepositorio sessaoRepo;
    @Mock LogAuditoriaCentralRepositorio logRepo;

    IngestaoServico servico;
    Gson gson;

    @BeforeEach
    void setUp() throws SQLException {
        gson = new Gson();
        servico = new IngestaoServico(db, lojaRepo, fechoRepo, vendaRepo, devolucaoRepo,
                remessaRepo, pagamentoRepo, sessaoRepo, logRepo, gson);
        lenient().when(db.obter()).thenReturn(conn);
    }

    // ── verificarHash (testado via processarPacote) ─────────────────

    @Test
    void processarPacote_hashAusente_lancaIntegridadeInvalidaException() {
        PacoteFechoDTO pacote = pacoteMinimo();
        pacote.hashIntegridade = null;
        assertThrows(IntegridadeInvalidaException.class,
                () -> servico.processarPacote(pacote));
    }

    @Test
    void processarPacote_hashEmBranco_lancaIntegridadeInvalidaException() {
        PacoteFechoDTO pacote = pacoteMinimo();
        pacote.hashIntegridade = "   ";
        assertThrows(IntegridadeInvalidaException.class,
                () -> servico.processarPacote(pacote));
    }

    @Test
    void processarPacote_hashIncorreto_lancaIntegridadeInvalidaException() {
        PacoteFechoDTO pacote = pacoteComHash();
        pacote.hashIntegridade = "0000000000000000000000000000000000000000000000000000000000000000";
        assertThrows(IntegridadeInvalidaException.class,
                () -> servico.processarPacote(pacote));
    }

    // ── hash correto ─────────────────────────────────────────────────

    @Test
    void processarPacote_hashCorreto_conclui_commitInvocado_rollbackNunca() throws Exception {
        PacoteFechoDTO pacote = pacoteComHash();

        assertDoesNotThrow(() -> servico.processarPacote(pacote));

        verify(conn).commit();
        verify(conn, never()).rollback();
    }

    // ── SQLException → rollback ──────────────────────────────────────

    @Test
    void processarPacote_sqlExceptionNoCommit_lancaRuntimeExceptionEFazRollback() throws Exception {
        PacoteFechoDTO pacote = pacoteComHash();
        doThrow(new SQLException("commit falhou")).when(conn).commit();

        assertThrows(RuntimeException.class, () -> servico.processarPacote(pacote));

        verify(conn).rollback();
    }

    // ── mapeadores (via processarPacote com listas preenchidas) ─────

    @Test
    void processarPacote_comTodosTiposDto_delegaParaRepositorios() throws Exception {
        PacoteFechoDTO pacote = pacoteMinimo();

        VendaDTO venda = new VendaDTO();
        venda.idOriginalLoja = 1;
        venda.dataHora = LocalDate.now().atStartOfDay().toString();
        venda.totalFaturado = 10.0;
        venda.metodoPagamento = "NUMERARIO";
        venda.linhas = List.of();
        pacote.vendas = List.of(venda);

        DevolucaoDTO dev = new DevolucaoDTO();
        dev.idOriginalLoja = 1;
        dev.idOriginalVenda = 1;
        dev.dataHora = LocalDate.now().atStartOfDay().toString();
        dev.quantidade = 1;
        dev.valorRestituido = 5.0;
        pacote.devolucoes = List.of(dev);

        RemessaDTO remessa = new RemessaDTO();
        remessa.idOriginalLoja = 1;
        remessa.nomeFornecedor = "Forn A";
        remessa.dataRecepcao = LocalDate.now().toString();
        remessa.valorTotalGuia = 100.0;
        remessa.estadoPagamento = "PENDENTE_PAGAMENTO";
        pacote.remessas = List.of(remessa);

        PagamentoDTO pag = new PagamentoDTO();
        pag.idOriginalLoja = 1;
        pag.idOriginalRemessa = 1;
        pag.valor = 100.0;
        pag.dataPagamento = LocalDate.now().toString();
        pacote.pagamentos = List.of(pag);

        SessaoCaixaDTO sessao = new SessaoCaixaDTO();
        sessao.idOriginalLoja = 1;
        sessao.idUtilizador = 1;
        sessao.saldoFinal = 200.0;
        sessao.dataAbertura = LocalDate.now().atStartOfDay().toString();
        pacote.sessoesCaixa = List.of(sessao);

        LogAuditoriaDTO log = new LogAuditoriaDTO();
        log.idOriginalLoja = 1;
        log.acao = "VENDA";
        log.dataHora = LocalDate.now().atStartOfDay().toString();
        log.nomeUtilizador = "op";
        log.descricao = "desc";
        pacote.logs = List.of(log);

        // Recompute hash after adding all DTOs
        pacote.hashIntegridade = null;
        pacote.hashIntegridade = sha256(gson.toJson(pacote));

        assertDoesNotThrow(() -> servico.processarPacote(pacote));

        verify(conn).commit();
        verify(vendaRepo).guardar(eq(conn), any());
        verify(devolucaoRepo).guardar(eq(conn), any());
        verify(remessaRepo).guardar(eq(conn), any());
        verify(pagamentoRepo).guardar(eq(conn), any());
        verify(sessaoRepo).guardar(eq(conn), any());
        verify(logRepo).guardar(eq(conn), any());
    }

    // ── helpers ─────────────────────────────────────────────────────

    private PacoteFechoDTO pacoteMinimo() {
        PacoteFechoDTO p = new PacoteFechoDTO();
        p.idLoja = "L01";
        p.nomeLoja = "Loja Teste";
        p.dataFecho = LocalDate.now().toString();
        return p;
    }

    private PacoteFechoDTO pacoteComHash() {
        PacoteFechoDTO p = pacoteMinimo();
        p.hashIntegridade = null;
        String json = gson.toJson(p);
        p.hashIntegridade = sha256(json);
        return p;
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
}
