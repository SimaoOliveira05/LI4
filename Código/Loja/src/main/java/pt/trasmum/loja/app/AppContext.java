package pt.trasmum.loja.app;

import org.mindrot.jbcrypt.BCrypt;
import pt.trasmum.loja.dominio.core.ConfiguracaoTerminal;
import pt.trasmum.loja.dominio.core.PerfilUtilizador;
import pt.trasmum.loja.dominio.core.Utilizador;
import pt.trasmum.loja.dominio.core.Loja;
import pt.trasmum.loja.repositorio.impl.*;
import pt.trasmum.loja.repositorio.interfaces.*;
import pt.trasmum.loja.servico.impl.*;
import pt.trasmum.loja.servico.interfaces.*;
import pt.trasmum.loja.sincronizacao.HttpSincronizacaoGateway;
import pt.trasmum.loja.sincronizacao.SincronizacaoGateway;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
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
    public final FornecedorProdutoRepositorio fornecedorProdutoRepo;
    public final RemessaRepositorio remessaRepo;
    public final PedidoRemessaRepositorio pedidoRemessaRepo;
    public final PagamentoRepositorio pagamentoRepo;
    public final VendaRepositorio vendaRepo;
    public final DevolucaoRepositorio devolucaoRepo;
    public final SessaoCaixaRepositorio sessaoCaixaRepo;
    public final FechoDiaRepositorio fechoDiaRepo;
    public final LojaRepositorio lojaRepo;

    // Serviços
    public final IAutorizacaoServico autorizacaoServico;
    public final IAuditoriaServico auditoriaServico;
    public final IAutenticacaoServico autenticacaoServico;
    public final IVendaServico vendaServico;
    public final IDevolucaoServico devolucaoServico;
    public final ICaixaServico caixaServico;
    public final ICatalogoServico catalogoServico;
    public final IFornecedorServico fornecedorServico;
    public final IRemessaServico remessaServico;
    public final IPagamentoServico pagamentoServico;
    public final IUtilizadorServico utilizadorServico;
    public final IFechoDiaServico fechoDiaServico;
    public final ILojaServico lojaServico;

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
        this.fornecedorProdutoRepo = new FornecedorProdutoRepositorioImpl(conexao);
        this.remessaRepo           = new RemessaRepositorioImpl(conexao);
        this.pedidoRemessaRepo     = new PedidoRemessaRepositorioImpl(conexao);
        this.pagamentoRepo         = new PagamentoRepositorioImpl(conexao);
        this.vendaRepo             = new VendaRepositorioImpl(conexao);
        this.devolucaoRepo         = new DevolucaoRepositorioImpl(conexao);
        this.sessaoCaixaRepo       = new SessaoCaixaRepositorioImpl(conexao);
        this.fechoDiaRepo          = new FechoDiaRepositorioImpl(conexao);
        this.lojaRepo              = new LojaRepositorioImpl(conexao);

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
        this.catalogoServico     = new CatalogoServico(produtoRepo, loteRepo, autorizacaoServico, auditoriaServico);
        this.vendaServico        = new VendaServico(vendaRepo, produtoRepo, loteRepo, sessaoCaixaRepo, auditoriaServico, configuracao);
        this.devolucaoServico    = new DevolucaoServico(vendaRepo, devolucaoRepo, loteRepo, produtoRepo, auditoriaServico, configuracao);
        this.fornecedorServico   = new FornecedorServico(fornecedorRepo, fornecedorProdutoRepo, produtoRepo, autorizacaoServico, auditoriaServico);
        this.remessaServico      = new RemessaServico(remessaRepo, pedidoRemessaRepo, pagamentoRepo, produtoRepo, fornecedorProdutoRepo, autorizacaoServico, auditoriaServico, configuracao);
        this.pagamentoServico    = new PagamentoServico(pagamentoRepo, autorizacaoServico);
        this.utilizadorServico   = new UtilizadorServico(utilizadorRepo, autorizacaoServico, auditoriaServico);
        this.lojaServico         = new LojaServico(lojaRepo, autorizacaoServico, auditoriaServico, configuracao.getIdLoja());
        this.fechoDiaServico     = new FechoDiaServico(
                vendaServico, devolucaoServico, caixaServico, remessaServico,
                auditoriaServico, autorizacaoServico, fechoDiaRepo,
                vendaRepo, devolucaoRepo, remessaRepo, pagamentoRepo, sessaoCaixaRepo, logAuditoriaRepo,
                produtoRepo, loteRepo, fornecedorRepo, utilizadorRepo,
                sincronizacaoGateway, configuracao);

        limparSessoesOrfas();
        garantirContaAdmin();
        reverterEstadosEmTransito();
        bootstrapLoja();
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
        boolean adminExiste = utilizadorRepo.listarAtivos().stream()
                .anyMatch(u -> "admin".equals(u.getNomeUtilizador()));
        if (!adminExiste) {
            String hash = BCrypt.hashpw("admin123", BCrypt.gensalt());
            Utilizador admin = new Utilizador("admin", hash, PerfilUtilizador.CEO);
            utilizadorRepo.guardar(admin);
        }

    }

    private void reverterEstadosEmTransito() {
        try {
            fechoDiaServico.reverterEmTransito();
        } catch (Exception e) {
            // Silencia — não deve bloquear o arranque
        }
    }

    private void bootstrapLoja() {
        try {
            Loja loja = lojaRepo.obter(configuracao.getIdLoja());
            if (loja == null) {
                loja = new Loja(
                        configuracao.getIdLoja(),
                        configuracao.getNomeLoja(),
                        configuracao.getMorada(),
                        configuracao.getLocalidade(),
                        configuracao.getNif(),
                        configuracao.getEmail(),
                        configuracao.getLimiteMaximoCaixa(),
                        configuracao.getDiasAlertaValidade()
                );
                lojaRepo.guardar(loja);
            }
            configuracao.setLimiteMaximoCaixa(loja.getLimiteMaximoCaixa());
            configuracao.setDiasAlertaValidade(loja.getDiasAlertaValidade());
        } catch (Exception e) {
            // Silencia — tabela pode ainda não existir em DBs antigos
        }
    }

    private Properties carregarProps() {
        Properties props = new Properties();
        try (InputStream is = ClassLoader.getSystemResourceAsStream("config.properties")) {
            if (is != null) props.load(new InputStreamReader(is, StandardCharsets.UTF_8));
        } catch (IOException e) { /* ignorar */ }
        return props;
    }
}
