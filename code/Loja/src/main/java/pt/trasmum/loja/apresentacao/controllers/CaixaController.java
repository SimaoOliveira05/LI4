package pt.trasmum.loja.apresentacao.controllers;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import pt.trasmum.loja.app.AppContext;
import pt.trasmum.loja.dominio.core.ConfiguracaoTerminal;
import pt.trasmum.loja.dominio.core.Utilizador;
import pt.trasmum.loja.dominio.tesouraria.*;
import pt.trasmum.loja.dominio.tesouraria.DetalheNumerario.Denominacao;

import java.util.ArrayList;
import java.util.List;

public class CaixaController {

    @FXML private Label lblEstadoSessao;
    @FXML private Label lblSaldo;
    @FXML private Button btnAbrirSessao;
    @FXML private Button btnRegistarSangria;
    @FXML private Button btnFecharSessao;
    @FXML private TableView<DetalheNumerario> tblFundo;
    @FXML private TableColumn<DetalheNumerario, String>  colDenominacao;
    @FXML private TableColumn<DetalheNumerario, Integer> colQuantidade;
    @FXML private TableColumn<DetalheNumerario, Double>  colSubtotal;

    private SessaoCaixa sessaoAtual;
    private final ObservableList<DetalheNumerario> detalhes = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colDenominacao.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDenominacao().name()));
        colQuantidade.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getQuantidade()).asObject());
        colSubtotal.setCellValueFactory(c -> new SimpleDoubleProperty(c.getValue().getSubtotal()).asObject());
        tblFundo.setItems(detalhes);
        atualizarEstado();
    }

    @FXML
    public void onAbrirSessao() {
        List<DetalheNumerario> fundo = pedirNumerario("Fundo Inicial de Caixa");
        if (fundo == null) return;
        Utilizador u = AppContext.getInstance().getUtilizadorAtual();
        ConfiguracaoTerminal config = AppContext.getInstance().configuracao;
        try {
            sessaoAtual = AppContext.getInstance().caixaServico.abrirSessao(u, fundo, config);
            atualizarEstado();
            mostrarInfo("Sessão de caixa aberta. Saldo inicial: " + String.format("%.2f €", sessaoAtual.getSaldoAtual()));
        } catch (Exception e) { mostrarErro(e.getMessage()); }
    }

    @FXML
    public void onRegistarSangria() {
        if (sessaoAtual == null) { mostrarErro("Não existe sessão de caixa aberta."); return; }
        List<DetalheNumerario> valor = pedirNumerario("Sangria de Caixa");
        if (valor == null) return;
        Utilizador u = AppContext.getInstance().getUtilizadorAtual();
        try {
            AppContext.getInstance().caixaServico.registarSangria(sessaoAtual, u, valor);
            atualizarEstado();
            mostrarInfo("Sangria registada.");
        } catch (Exception e) { mostrarErro(e.getMessage()); }
    }

    @FXML
    public void onFecharSessao() {
        if (sessaoAtual == null) { mostrarErro("Não existe sessão de caixa aberta."); return; }
        Alert c = new Alert(Alert.AlertType.CONFIRMATION, "Fechar sessão de caixa?", ButtonType.YES, ButtonType.NO);
        c.setHeaderText(null);
        c.showAndWait().ifPresent(bt -> {
            if (bt == ButtonType.YES) {
                try {
                    AppContext.getInstance().caixaServico.fecharSessao(sessaoAtual);
                    sessaoAtual = null;
                    atualizarEstado();
                    mostrarInfo("Sessão de caixa encerrada.");
                } catch (Exception e) { mostrarErro(e.getMessage()); }
            }
        });
    }

    private void atualizarEstado() {
        Utilizador u = AppContext.getInstance().getUtilizadorAtual();
        if (u != null) sessaoAtual = AppContext.getInstance().caixaServico.buscarSessaoAtiva(u.getId());
        if (sessaoAtual != null) {
            lblEstadoSessao.setText("Sessão: ABERTA — desde " + sessaoAtual.getDataAbertura().toLocalDate());
            lblSaldo.setText(String.format("Saldo actual: %.2f €", sessaoAtual.getSaldoAtual()));
            detalhes.setAll(sessaoAtual.getFundoInicial());
            btnAbrirSessao.setDisable(true);
            btnRegistarSangria.setDisable(false);
            btnFecharSessao.setDisable(false);
        } else {
            lblEstadoSessao.setText("Sessão: FECHADA");
            lblSaldo.setText("—");
            detalhes.clear();
            btnAbrirSessao.setDisable(false);
            btnRegistarSangria.setDisable(true);
            btnFecharSessao.setDisable(true);
        }
    }

    private List<DetalheNumerario> pedirNumerario(String titulo) {
        Dialog<List<DetalheNumerario>> dlg = new Dialog<>();
        dlg.setTitle(titulo);
        dlg.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
        grid.setHgap(8); grid.setVgap(4);
        Denominacao[] dens = Denominacao.values();
        TextField[] campos = new TextField[dens.length];
        for (int i = 0; i < dens.length; i++) {
            grid.add(new Label(dens[i].name() + " (" + dens[i].getValor() + " €):"), 0, i);
            campos[i] = new TextField("0");
            campos[i].setPrefWidth(60);
            grid.add(campos[i], 1, i);
        }
        dlg.getDialogPane().setContent(new javafx.scene.control.ScrollPane(grid));

        dlg.setResultConverter(bt -> {
            if (bt != ButtonType.OK) return null;
            List<DetalheNumerario> lista = new ArrayList<>();
            for (int i = 0; i < dens.length; i++) {
                try {
                    int qtd = Integer.parseInt(campos[i].getText().trim());
                    if (qtd > 0) lista.add(new DetalheNumerario(dens[i], qtd));
                } catch (NumberFormatException ignored) {}
            }
            return lista;
        });
        return dlg.showAndWait().orElse(null);
    }

    private void mostrarErro(String m) { Alert a = new Alert(Alert.AlertType.ERROR); a.setTitle("Erro"); a.setHeaderText(null); a.setContentText(m); a.showAndWait(); }
    private void mostrarInfo(String m)  { Alert a = new Alert(Alert.AlertType.INFORMATION); a.setTitle("Informação"); a.setHeaderText(null); a.setContentText(m); a.showAndWait(); }
}
