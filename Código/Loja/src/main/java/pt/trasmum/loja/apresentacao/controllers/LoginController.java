package pt.trasmum.loja.apresentacao.controllers;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.util.Duration;
import pt.trasmum.loja.app.AppContext;
import pt.trasmum.loja.app.Navigator;
import pt.trasmum.loja.dominio.core.Utilizador;

public class LoginController {

    private static final int MAX_TENTATIVAS = 5;
    private static final long DURACAO_BLOQUEIO_MS = 15 * 60 * 1000L;

    private static int tentativasFalhadas = 0;
    private static long tempoDesbloqueio = 0;

    @FXML private TextField txtNomeUtilizador;
    @FXML private PasswordField txtPalavraPasse;
    @FXML private Label lblErro;
    @FXML private Button btnEntrar;

    private Timeline cronometro;

    @FXML
    public void initialize() {
        txtNomeUtilizador.setText("admin");
        txtPalavraPasse.setText("admin123");

        if (estaBloqueado()) {
            iniciarCronometroBloqueio();
        }
    }

    @FXML
    public void onLoginSubmit() {
        if (estaBloqueado()) return;

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
            tentativasFalhadas = 0;
            tempoDesbloqueio = 0;
            AppContext.getInstance().setUtilizadorAtual(utilizador);
            Navigator.navegarParaMain();
        } catch (Exception e) {
            tentativasFalhadas++;
            if (tentativasFalhadas >= MAX_TENTATIVAS) {
                tempoDesbloqueio = System.currentTimeMillis() + DURACAO_BLOQUEIO_MS;
                iniciarCronometroBloqueio();
            } else {
                int restantes = MAX_TENTATIVAS - tentativasFalhadas;
                lblErro.setText(e.getMessage() + " (" + restantes + " tentativa(s) restante(s))");
            }
        }
    }

    @FXML
    public void onCampoAlterado() {
        if (estaBloqueado()) return;
        txtNomeUtilizador.pseudoClassStateChanged(javafx.css.PseudoClass.getPseudoClass("invalid"), false);
        txtPalavraPasse.pseudoClassStateChanged(javafx.css.PseudoClass.getPseudoClass("invalid"), false);
        lblErro.setText("");
    }

    private boolean estaBloqueado() {
        return System.currentTimeMillis() < tempoDesbloqueio;
    }

    private void iniciarCronometroBloqueio() {
        setFormularioBloqueado(true);
        cronometro = new Timeline(new KeyFrame(Duration.seconds(1), e -> atualizarContagem()));
        cronometro.setCycleCount(Timeline.INDEFINITE);
        cronometro.play();
        atualizarContagem();
    }

    private void atualizarContagem() {
        long restanteMs = tempoDesbloqueio - System.currentTimeMillis();
        if (restanteMs <= 0) {
            cronometro.stop();
            tentativasFalhadas = 0;
            tempoDesbloqueio = 0;
            setFormularioBloqueado(false);
            lblErro.setText("");
        } else {
            long minutos = restanteMs / 60000;
            long segundos = (restanteMs % 60000) / 1000;
            lblErro.setText(String.format(
                    "Conta bloqueada por demasiadas tentativas. Tente novamente em %d:%02d.", minutos, segundos));
        }
    }

    private void setFormularioBloqueado(boolean bloqueado) {
        txtNomeUtilizador.setDisable(bloqueado);
        txtPalavraPasse.setDisable(bloqueado);
        btnEntrar.setDisable(bloqueado);
    }
}
