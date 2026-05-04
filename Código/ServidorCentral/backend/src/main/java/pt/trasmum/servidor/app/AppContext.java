package pt.trasmum.servidor.app;

import com.google.gson.Gson;
import org.mindrot.jbcrypt.BCrypt;
import pt.trasmum.servidor.dominio.CEO;
import pt.trasmum.servidor.handler.*;
import pt.trasmum.servidor.infra.DatabaseConnection;
import pt.trasmum.servidor.infra.JsonUtil;
import pt.trasmum.servidor.repositorio.impl.*;
import pt.trasmum.servidor.repositorio.interfaces.*;
import pt.trasmum.servidor.servico.impl.*;
import pt.trasmum.servidor.servico.interfaces.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class AppContext {
    public final Properties config;
    public final Gson gson;
    public final SessaoManager sessoes;

    public final CEORepositorio ceoRepo;
    public final LojaRepositorio lojaRepo;
    public final FechoDiaCentralRepositorio fechoRepo;
    public final VendaCentralRepositorio vendaRepo;
    public final DevolucaoCentralRepositorio devolucaoRepo;
    public final RemessaCentralRepositorio remessaRepo;
    public final PagamentoCentralRepositorio pagamentoRepo;
    public final SessaoCaixaCentralRepositorio sessaoRepo;
    public final LogAuditoriaCentralRepositorio logRepo;

    public final IAutenticacaoCEOServico autenticacaoServico;
    public final IIngestaoServico ingestaoServico;
    public final IDashboardServico dashboardServico;
    public final IMonitorServico monitorServico;
    public final IRemessaServico remessaServico;
    public final IRelatorioServico relatorioServico;

    public final AuthHandler authHandler;
    public final IngestaoHandler ingestaoHandler;
    public final DashboardHandler dashboardHandler;
    public final MonitorHandler monitorHandler;
    public final RemessasHandler remessasHandler;
    public final RelatorioHandler relatorioHandler;
    public final LojaHandler lojaHandler;

    private AppContext(Properties config) {
        this.config = config;
        this.gson = JsonUtil.criarGson();
        long horas = Long.parseLong(config.getProperty("auth.duracaoSessaoHoras", "8"));
        this.sessoes = new SessaoManager(horas);

        DatabaseConnection db = new DatabaseConnection(config);

        this.ceoRepo = new CEORepositorioImpl(db);
        this.lojaRepo = new LojaRepositorioImpl(db);
        this.fechoRepo = new FechoDiaCentralRepositorioImpl(db);
        this.vendaRepo = new VendaCentralRepositorioImpl(db);
        this.devolucaoRepo = new DevolucaoCentralRepositorioImpl(db);
        this.remessaRepo = new RemessaCentralRepositorioImpl(db);
        this.pagamentoRepo = new PagamentoCentralRepositorioImpl(db);
        this.sessaoRepo = new SessaoCaixaCentralRepositorioImpl(db);
        this.logRepo = new LogAuditoriaCentralRepositorioImpl(db);

        this.autenticacaoServico = new AutenticacaoCEOServico(ceoRepo, config);
        this.ingestaoServico = new IngestaoServico(db, lojaRepo, fechoRepo, vendaRepo, devolucaoRepo,
                remessaRepo, pagamentoRepo, sessaoRepo, logRepo, gson);
        this.dashboardServico = new DashboardServico(lojaRepo, vendaRepo, devolucaoRepo, fechoRepo, logRepo, remessaRepo);
        this.monitorServico = new MonitorServico(lojaRepo, fechoRepo);
        this.remessaServico = new RemessaServico(remessaRepo, lojaRepo);
        this.relatorioServico = new RelatorioServico(vendaRepo);

        this.authHandler = new AuthHandler(autenticacaoServico, sessoes);
        this.ingestaoHandler = new IngestaoHandler(ingestaoServico);
        this.dashboardHandler = new DashboardHandler(dashboardServico);
        this.monitorHandler = new MonitorHandler(monitorServico);
        this.remessasHandler = new RemessasHandler(remessaServico);
        this.relatorioHandler = new RelatorioHandler(relatorioServico);
        this.lojaHandler = new LojaHandler(lojaRepo);

        bootstrap();
    }

    private static void overrideWithEnv(Properties props) {
        override(props, "db.url", "DB_URL");
        override(props, "db.user", "DB_USER");
        override(props, "db.password", "DB_PASSWORD");

        override(props, "frontend.origem", "FRONTEND_ORIGEM");

        override(props, "servidor.porta", "SERVER_PORT");

        override(props, "db.retry.maxTentativas", "DB_RETRY_MAX_TENTATIVAS");
        override(props, "db.retry.delayMs", "DB_RETRY_DELAY_MS");
    }

    private static void override(Properties props, String key, String envKey) {
        String value = System.getenv(envKey);
        if (value != null && !value.isBlank()) {
            props.setProperty(key, value);
        }
    }

    public static AppContext initialize() {
        Properties props = new Properties();
        try (InputStream in = AppContext.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (in == null) throw new IllegalStateException("config.properties não encontrado");
            props.load(in);
            overrideWithEnv(props);
        } catch (IOException e) {
            throw new RuntimeException("Erro a carregar config.properties: " + e.getMessage(), e);
        }
        return new AppContext(props);
    }

    private void bootstrap() {
        if (ceoRepo.contarTodos() == 0) {
            ceoRepo.criarSeNaoExistir(new CEO("admin", BCrypt.hashpw("admin123", BCrypt.gensalt()), 0, null));
        }
    }
}
