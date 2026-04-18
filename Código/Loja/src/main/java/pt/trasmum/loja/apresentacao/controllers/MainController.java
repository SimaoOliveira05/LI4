package pt.trasmum.loja.apresentacao.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
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
    @FXML private Button btnRemessa;
    @FXML private Button btnFornecedor;
    @FXML private Button btnPagamento;
    @FXML private Button btnUtilizadores;
    @FXML private Button btnFechoDia;
    @FXML private Button btnLogsAuditoria;

    @FXML
    public void initialize() {
        Navigator.setRootLayout(mainBorderPane);

        Utilizador u = AppContext.getInstance().getUtilizadorAtual();
        if (u != null) {
            lblUtilizador.setText(u.getNomeUtilizador() + " (" + u.getPerfil() + ")");
        }
        lblLoja.setText(AppContext.getInstance().configuracao.getNomeLoja());

        boolean gestor = u != null && (u.getPerfil() == PerfilUtilizador.GESTOR || u.getPerfil() == PerfilUtilizador.CEO);
        btnRemessa.setVisible(gestor);     btnRemessa.setManaged(gestor);
        btnFornecedor.setVisible(gestor);  btnFornecedor.setManaged(gestor);
        btnPagamento.setVisible(gestor);   btnPagamento.setManaged(gestor);
        btnUtilizadores.setVisible(gestor); btnUtilizadores.setManaged(gestor);
        btnFechoDia.setVisible(gestor);    btnFechoDia.setManaged(gestor);
        btnLogsAuditoria.setVisible(gestor); btnLogsAuditoria.setManaged(gestor);

        GestorEscala gestor2 = GestorEscala.getInstance();
        atualizarLblEscala(gestor2.getEscala());
        gestor2.escalaProperty().addListener((obs, ant, novo) -> atualizarLblEscala(novo.doubleValue()));

        navegarVenda();
    }

    private void atualizarLblEscala(double fator) {
        lblEscala.setText(String.format("%.0f%%", fator * 100));
    }

    @FXML public void navegarVenda()       { Navigator.navegarParaCentro("/fxml/VendaView.fxml"); }
    @FXML public void navegarCatalogo()    { Navigator.navegarParaCentro("/fxml/CatalogoView.fxml"); }
    @FXML public void navegarCaixa()       { Navigator.navegarParaCentro("/fxml/CaixaView.fxml"); }
    @FXML public void navegarRemessa()     { Navigator.navegarParaCentro("/fxml/RemessaView.fxml"); }
    @FXML public void navegarFornecedor()  { Navigator.navegarParaCentro("/fxml/FornecedorView.fxml"); }
    @FXML public void navegarPagamento()   { Navigator.navegarParaCentro("/fxml/PagamentoView.fxml"); }
    @FXML public void navegarUtilizadores(){ Navigator.navegarParaCentro("/fxml/UtilizadorView.fxml"); }
    @FXML public void navegarFechoDia()    { Navigator.navegarParaCentro("/fxml/FechoDiaView.fxml"); }
    @FXML public void navegarLogsAuditoria(){ Navigator.navegarParaCentro("/fxml/LogAuditoriaView.fxml"); }

    @FXML public void aumentarEscala() { GestorEscala.getInstance().aumentar(); }
    @FXML public void diminuirEscala() { GestorEscala.getInstance().diminuir(); }

    @FXML
    public void onLogout() {
        Utilizador u = AppContext.getInstance().getUtilizadorAtual();
        if (u != null) {
            AppContext.getInstance().autenticacaoServico.encerrarSessao(u);
            AppContext.getInstance().setUtilizadorAtual(null);
        }
        Navigator.navegarParaLogin();
    }
}
