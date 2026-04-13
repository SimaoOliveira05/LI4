package pt.trasmum.loja.app;

import org.mindrot.jbcrypt.BCrypt;
import pt.trasmum.loja.dominio.core.ConfiguracaoTerminal;
import pt.trasmum.loja.dominio.core.PerfilUtilizador;
import pt.trasmum.loja.dominio.core.Utilizador;
import pt.trasmum.loja.repositorio.impl.*;
import pt.trasmum.loja.repositorio.interfaces.*;
import pt.trasmum.loja.servico.impl.*;
import pt.trasmum.loja.servico.interfaces.*;
import pt.trasmum.loja.sincronizacao.HttpSincronizacaoGateway;
import pt.trasmum.loja.sincronizacao.SincronizacaoGateway;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.util.Properties;

/**
 * Composition root da aplicação. Singleton que instancia e injeta todos os componentes.
 */
public class AppContext {

    private static AppContext instance;

    // Infraestrutura
    public final Connection conexao;
    public final ConfiguracaoTerminal configuracao;

    // Repositórios
    public final UtilizadorRepositorio utilizadorRepo;
    public final LogAuditoriaRepositorio logAuditoriaRepo;
    public final ProdutoRepositorio produtoRepo;
    public final LoteRepositorio loteRepo;
    public final FornecedorRepositorio fornecedorRepo;
    public final RemessaRepositorio remessaRepo;
    public final PedidoRemessaRepositorio pedidoRemessaRepo;
    public final PagamentoRepositorio pagamentoRepo;
    public final VendaRepositorio vendaRepo;
    public final DevolucaoRepositorio devolucaoRepo;
    public final SessaoCaixaRepositorio sessaoCaixaRepo;
    public final FechoDiaRepositorio fechoDiaRepo;

    // Serviços
    public final IAutorizacaoServico autorizacaoServico;
    public final IAuditoriaServico auditoriaServico;
    public final IAutenticacaoServico autenticacaoServico;
    public final IVendaServico vendaServico;
    public final IDevolucaoServico devolucaoServico;
    public final ICaixaServico caixaServico;
    public final ICatalogoServico catalogoServico;
    public final IRemessaServico remessaServico;
    public final IPagamentoServico pagamentoServico;
    public final IUtilizadorServico utilizadorServico;
    public final IFechoDiaServico fechoDiaServico;

    // Gateway
    public final SincronizacaoGateway sincronizacaoGateway;

    // Estado de sessão
    public volatile Utilizador utilizadorAtual;

    /** Venda em curso — sobrevive a transições de vista. Null se não houver venda activa. */
    public volatile pt.trasmum.loja.dominio.vendas.Venda vendaEmCurso;

    private AppContext() {
        this.configuracao = ConfiguracaoTerminal.carregar();
        this.conexao = DatabaseConnection.getInstance().getConnection();

        // Repositórios
        this.utilizadorRepo        = new UtilizadorRepositorioImpl(conexao);
        this.logAuditoriaRepo      = new LogAuditoriaRepositorioImpl(conexao);
        this.produtoRepo           = new ProdutoRepositorioImpl(conexao);
        this.loteRepo              = new LoteRepositorioImpl(conexao);
        this.fornecedorRepo        = new FornecedorRepositorioImpl(conexao);
        this.remessaRepo           = new RemessaRepositorioImpl(conexao);
        this.pedidoRemessaRepo     = new PedidoRemessaRepositorioImpl(conexao);
        this.pagamentoRepo         = new PagamentoRepositorioImpl(conexao);
        this.vendaRepo             = new VendaRepositorioImpl(conexao);
        this.devolucaoRepo         = new DevolucaoRepositorioImpl(conexao);
        this.sessaoCaixaRepo       = new SessaoCaixaRepositorioImpl(conexao);
        this.fechoDiaRepo          = new FechoDiaRepositorioImpl(conexao);

        // Gateway
        Properties p = carregarProps();
        this.sincronizacaoGateway  = new HttpSincronizacaoGateway(
                configuracao.getUrlServidor(),
                p.getProperty("servidor.truststore.path", ""),
                p.getProperty("servidor.truststore.password", ""));

        // Serviços (por ordem de dependência)
        this.autorizacaoServico  = new AutorizacaoServico();
        this.auditoriaServico    = new AuditoriaServico(logAuditoriaRepo, configuracao);
        this.autenticacaoServico = new AutenticacaoServico(utilizadorRepo, auditoriaServico);
        this.caixaServico        = new CaixaServico(sessaoCaixaRepo);
        this.catalogoServico     = new CatalogoServico(produtoRepo, loteRepo, autorizacaoServico, auditoriaServico, configuracao);
        this.vendaServico        = new VendaServico(vendaRepo, produtoRepo, loteRepo, sessaoCaixaRepo, auditoriaServico, configuracao);
        this.devolucaoServico    = new DevolucaoServico(vendaRepo, devolucaoRepo, loteRepo, produtoRepo, auditoriaServico, configuracao);
        this.remessaServico      = new RemessaServico(remessaRepo, pedidoRemessaRepo, pagamentoRepo, produtoRepo, autorizacaoServico, auditoriaServico, configuracao);
        this.pagamentoServico    = new PagamentoServico(pagamentoRepo, autorizacaoServico);
        this.utilizadorServico   = new UtilizadorServico(utilizadorRepo, autorizacaoServico, auditoriaServico);
        this.fechoDiaServico     = new FechoDiaServico(
                vendaServico, devolucaoServico, caixaServico, remessaServico,
                auditoriaServico, autorizacaoServico, fechoDiaRepo,
                vendaRepo, devolucaoRepo, remessaRepo, pagamentoRepo, sessaoCaixaRepo, logAuditoriaRepo,
                sincronizacaoGateway, configuracao);

        limparSessoesOrfas();
        garantirContaAdmin();
        reverterEstadosEmTransito();
    }

    public static synchronized AppContext getInstance() {
        if (instance == null) {
            instance = new AppContext();
        }
        return instance;
    }

    /** Ponto de inicialização explícito chamado pelo MainApp. */
    public static void initialize() {
        getInstance();
    }

    public void setUtilizadorAtual(Utilizador utilizador) {
        this.utilizadorAtual = utilizador;
    }

    public Utilizador getUtilizadorAtual() {
        return utilizadorAtual;
    }

    private void limparSessoesOrfas() {
        try {
            utilizadorRepo.limparSessoesAtivas();
        } catch (Exception e) {
            // Silencia — não deve bloquear o arranque
        }
    }

    private void garantirContaAdmin() {
        try {
            boolean adminExiste = utilizadorRepo.listarAtivos().stream()
                    .anyMatch(u -> "admin".equals(u.getNomeUtilizador()));
            if (!adminExiste) {
                String hash = BCrypt.hashpw("admin123", BCrypt.gensalt());
                Utilizador admin = new Utilizador("admin", hash, PerfilUtilizador.CEO);
                utilizadorRepo.guardar(admin);
            }
        } catch (Exception e) {
            // Silencia — tabela pode ainda não existir
        }
    }

    private void reverterEstadosEmTransito() {
        try {
            fechoDiaServico.reverterEmTransito();
        } catch (Exception e) {
            // Silencia — não deve bloquear o arranque
        }
    }

    private Properties carregarProps() {
        Properties props = new Properties();
        try (InputStream is = ClassLoader.getSystemResourceAsStream("config.properties")) {
            if (is != null) props.load(is);
        } catch (IOException e) { /* ignorar */ }
        return props;
    }
}
