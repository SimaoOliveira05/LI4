package pt.trasmum.loja.apresentacao.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import pt.trasmum.loja.apresentacao.DialogoUtil;
import pt.trasmum.loja.app.AppContext;
import pt.trasmum.loja.app.GestorEscala;
import pt.trasmum.loja.app.Navigator;
import pt.trasmum.loja.dominio.core.PerfilUtilizador;
import pt.trasmum.loja.dominio.core.Utilizador;

public class MainController {

    @FXML private BorderPane mainBorderPane;
    @FXML private VBox menuLateral;
    @FXML private Label lblUtilizador;
    @FXML private Label lblLoja;
    @FXML private Label lblEscala;

    // Botões de gestão (visíveis apenas para GESTOR/CEO)
    @FXML private Separator sepGestao;
    @FXML private Button btnAprovisionamento;
    @FXML private Button btnPagamento;
    @FXML private Button btnAdministracao;
    @FXML private Button btnFechoDia;

    @FXML
    public void initialize() {
        Navigator.setRootLayout(mainBorderPane);

        Utilizador u = AppContext.getInstance().getUtilizadorAtual();
        if (u != null) {
            lblUtilizador.setText(u.getNomeUtilizador() + " (" + u.getPerfil() + ")");
        }
        lblLoja.setText(AppContext.getInstance().configuracao.getNomeLoja());

        boolean gestor = u != null && (u.getPerfil() == PerfilUtilizador.GESTOR || u.getPerfil() == PerfilUtilizador.CEO);
        sepGestao.setVisible(gestor);            sepGestao.setManaged(gestor);
        btnAprovisionamento.setVisible(gestor);  btnAprovisionamento.setManaged(gestor);
        btnPagamento.setVisible(gestor);         btnPagamento.setManaged(gestor);
        btnAdministracao.setVisible(gestor);     btnAdministracao.setManaged(gestor);
        btnFechoDia.setVisible(gestor);          btnFechoDia.setManaged(gestor);

        GestorEscala gestor2 = GestorEscala.getInstance();
        atualizarLblEscala(gestor2.getEscala());
        gestor2.escalaProperty().addListener((obs, ant, novo) -> atualizarLblEscala(novo.doubleValue()));

        navegarVenda();
    }

    private void atualizarLblEscala(double fator) {
        lblEscala.setText(String.format("%.0f%%", fator * 100));
    }

    @FXML public void navegarVenda()            { Navigator.navegarParaCentro("/fxml/VendaView.fxml"); }
    @FXML public void navegarCatalogo()         { Navigator.navegarParaCentro("/fxml/CatalogoView.fxml"); }
    @FXML public void navegarCaixa()            { Navigator.navegarParaCentro("/fxml/CaixaView.fxml"); }
    @FXML public void navegarAprovisionamento() { Navigator.navegarParaCentro("/fxml/AprovisionamentoView.fxml"); }
    @FXML public void navegarPagamento()        { Navigator.navegarParaCentro("/fxml/PagamentoView.fxml"); }
    @FXML public void navegarAdministracao()    { Navigator.navegarParaCentro("/fxml/AdministracaoView.fxml"); }
    @FXML public void navegarFechoDia()         { Navigator.navegarParaCentro("/fxml/FechoDiaView.fxml"); }

    @FXML public void aumentarEscala() { GestorEscala.getInstance().aumentar(); }
    @FXML public void diminuirEscala() { GestorEscala.getInstance().diminuir(); }

    @FXML
    public void onLogout() {
        Utilizador u = AppContext.getInstance().getUtilizadorAtual();
        if (u != null) {
            if (AppContext.getInstance().caixaServico.buscarSessaoAtiva(u.getId()) != null) {
                DialogoUtil.erro("Não é possível terminar sessão com uma sessão de caixa aberta.\nFeche a caixa primeiro.");
                return;
            }
            AppContext.getInstance().autenticacaoServico.encerrarSessao(u);
            AppContext.getInstance().setUtilizadorAtual(null);
        }
        Navigator.navegarParaLogin();
    }
}
