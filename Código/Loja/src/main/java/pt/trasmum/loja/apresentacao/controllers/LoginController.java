package pt.trasmum.loja.apresentacao.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import pt.trasmum.loja.app.AppContext;
import pt.trasmum.loja.app.Navigator;
import pt.trasmum.loja.dominio.core.Utilizador;

public class LoginController {

    @FXML private TextField txtNomeUtilizador;
    @FXML private PasswordField txtPalavraPasse;
    @FXML private Label lblErro;

    @FXML
    public void initialize() {
        txtNomeUtilizador.setText("admin");
        txtPalavraPasse.setText("admin123");
    }

    @FXML
    public void onLoginSubmit() {
        lblErro.setText("");
        String nome = txtNomeUtilizador.getText().trim();
        String passe = txtPalavraPasse.getText();

        boolean invalido = false;
        if (nome.isBlank()) {
            txtNomeUtilizador.pseudoClassStateChanged(
                    javafx.css.PseudoClass.getPseudoClass("invalid"), true);
            invalido = true;
        }
        if (passe.isBlank()) {
            txtPalavraPasse.pseudoClassStateChanged(
                    javafx.css.PseudoClass.getPseudoClass("invalid"), true);
            invalido = true;
        }
        if (invalido) return;

        try {
            Utilizador utilizador = AppContext.getInstance().autenticacaoServico.autenticar(nome, passe);
            AppContext.getInstance().setUtilizadorAtual(utilizador);
            Navigator.navegarParaMain();
        } catch (Exception e) {
            lblErro.setText(e.getMessage());
        }
    }

    @FXML
    public void onCampoAlterado() {
        txtNomeUtilizador.pseudoClassStateChanged(javafx.css.PseudoClass.getPseudoClass("invalid"), false);
        txtPalavraPasse.pseudoClassStateChanged(javafx.css.PseudoClass.getPseudoClass("invalid"), false);
        lblErro.setText("");
    }
}
