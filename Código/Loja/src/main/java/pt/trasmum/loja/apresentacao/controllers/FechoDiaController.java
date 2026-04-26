package pt.trasmum.loja.apresentacao.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import pt.trasmum.loja.apresentacao.DialogoUtil;
import pt.trasmum.loja.app.AppContext;
import pt.trasmum.loja.app.Navigator;
import pt.trasmum.loja.dominio.core.ConfiguracaoTerminal;
import pt.trasmum.loja.dominio.core.Utilizador;
import pt.trasmum.loja.dominio.exceptions.EnvioFechoFalhouException;
import pt.trasmum.loja.dominio.tesouraria.FechoDia;

public class FechoDiaController {

    @FXML private Label lblEstado;
    @FXML private Button btnIniciarFecho;
    @FXML private Button btnReenviar;
    @FXML private ProgressIndicator progressIndicador;

    private int idFechoFalhado = -1;

    @FXML
    public void initialize() {
        btnReenviar.setVisible(false);
        progressIndicador.setVisible(false);
        lblEstado.setText("Pronto para iniciar fecho de dia.");
    }

    @FXML
    public void onIniciarFecho() {
        Utilizador u = AppContext.getInstance().getUtilizadorAtual();
        ConfiguracaoTerminal config = AppContext.getInstance().configuracao;

        if (!AppContext.getInstance().caixaServico.obterSessoesAbertas().isEmpty()) {
            mostrarErro("Existem sessões de caixa abertas. Todos os funcionários devem fechar a caixa antes de encerrar o dia.");
            return;
        }

        lblEstado.setText("A processar fecho de dia...");
        progressIndicador.setVisible(true);
        btnIniciarFecho.setDisable(true);

        // Executa em thread de background para não bloquear o UI
        javafx.concurrent.Task<FechoDia> task = new javafx.concurrent.Task<>() {
            @Override protected FechoDia call() {
                return AppContext.getInstance().fechoDiaServico.executarFecho(u, config);
            }
        };
        task.setOnSucceeded(ev -> {
            progressIndicador.setVisible(false);
            FechoDia fecho = task.getValue();
            lblEstado.setText("Fecho confirmado — " + fecho.getDataFecho());
            mostrarInfo("Fecho de dia realizado com sucesso.\nData: " + fecho.getDataFecho());
            AppContext.getInstance().autenticacaoServico.encerrarSessao(u);
            AppContext.getInstance().setUtilizadorAtual(null);
            Navigator.navegarParaLogin();
        });
        task.setOnFailed(ev -> {
            progressIndicador.setVisible(false);
            btnIniciarFecho.setDisable(false);
            Throwable ex = task.getException();
            if (ex instanceof EnvioFechoFalhouException) {
                lblEstado.setText("Falha no envio. Os dados ficaram como PENDENTE.");
                btnReenviar.setVisible(true);
                idFechoFalhado = 0; // sem id concreto — reenviar usa pendentes
                mostrarErro("Falha no envio ao servidor: " + ex.getMessage()
                        + "\n\nOs dados estão guardados localmente. Utilize 'Reenviar' quando houver conectividade.");
            } else {
                lblEstado.setText("Erro: " + ex.getMessage());
                mostrarErro(ex.getMessage());
            }
        });
        new Thread(task).start();
    }

    @FXML
    public void onReenviar() {
        lblEstado.setText("A reenviar...");
        progressIndicador.setVisible(true);
        btnReenviar.setDisable(true);

        javafx.concurrent.Task<Boolean> task = new javafx.concurrent.Task<>() {
            @Override protected Boolean call() {
                return AppContext.getInstance().fechoDiaServico.reenviar(idFechoFalhado);
            }
        };
        task.setOnSucceeded(ev -> {
            progressIndicador.setVisible(false);
            if (Boolean.TRUE.equals(task.getValue())) {
                lblEstado.setText("Reenvio bem-sucedido.");
                btnReenviar.setVisible(false);
                mostrarInfo("Fecho de dia reenviado com sucesso.");
                Utilizador u = AppContext.getInstance().getUtilizadorAtual();
                if (u != null) AppContext.getInstance().autenticacaoServico.encerrarSessao(u);
                AppContext.getInstance().setUtilizadorAtual(null);
                Navigator.navegarParaLogin();
            } else {
                lblEstado.setText("Reenvio falhou. Tente mais tarde.");
                btnReenviar.setDisable(false);
                mostrarErro("O reenvio falhou. Verifique a conectividade com o servidor.");
            }
        });
        task.setOnFailed(ev -> {
            progressIndicador.setVisible(false);
            btnReenviar.setDisable(false);
            mostrarErro("Erro inesperado no reenvio: " + task.getException().getMessage());
        });
        new Thread(task).start();
    }

    private void mostrarErro(String m) { DialogoUtil.erro(m); }
    private void mostrarInfo(String m)  { DialogoUtil.info(m); }
}
