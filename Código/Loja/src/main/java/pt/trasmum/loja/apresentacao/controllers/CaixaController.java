package pt.trasmum.loja.apresentacao.controllers;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.VBox;
import javafx.util.converter.IntegerStringConverter;
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

    @FXML private VBox formNumerario;
    @FXML private Label lblFormTitulo;
    @FXML private TableView<LinhaForm> tblNumerarioForm;
    @FXML private TableColumn<LinhaForm, String>  colFormDenom;
    @FXML private TableColumn<LinhaForm, Integer> colFormQtd;
    @FXML private TableColumn<LinhaForm, Double>  colFormSubtotal;
    @FXML private Label lblFormTotal;

    private SessaoCaixa sessaoAtual;
    private boolean modoFundo = true;
    private final ObservableList<DetalheNumerario> detalhes = FXCollections.observableArrayList();
    private final ObservableList<LinhaForm> linhasForm = FXCollections.observableArrayList();

    public static class LinhaForm {
        final Denominacao denominacao;
        final SimpleIntegerProperty quantidade = new SimpleIntegerProperty(0);

        LinhaForm(Denominacao d) { this.denominacao = d; }

        double getSubtotal() { return denominacao.getValor() * quantidade.get(); }
    }

    @FXML
    public void initialize() {
        // Tabela de fundo da sessão
        colDenominacao.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDenominacao().getLabel()));
        colQuantidade.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getQuantidade()).asObject());
        colSubtotal.setCellValueFactory(c -> new SimpleDoubleProperty(c.getValue().getSubtotal()).asObject());
        tblFundo.setItems(detalhes);

        // Tabela do formulário inline
        colFormDenom.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().denominacao.getLabel()));
        colFormQtd.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter() {
            @Override public Integer fromString(String s) {
                try { return Integer.parseInt(s.trim()); } catch (NumberFormatException e) { return 0; }
            }
        }));
        colFormQtd.setCellValueFactory(c -> c.getValue().quantidade.asObject());
        colFormQtd.setOnEditCommit(e -> {
            e.getRowValue().quantidade.set(e.getNewValue() != null ? e.getNewValue() : 0);
            atualizarTotalForm();
            tblNumerarioForm.refresh();
        });
        colFormSubtotal.setCellValueFactory(c -> new SimpleDoubleProperty(c.getValue().getSubtotal()).asObject());
        tblNumerarioForm.setItems(linhasForm);

        atualizarEstado();
    }

    @FXML
    public void onAbrirSessao() {
        modoFundo = true;
        lblFormTitulo.setText("Fundo Inicial de Caixa");
        abrirFormNumerario();
    }

    @FXML
    public void onRegistarSangria() {
        if (sessaoAtual == null) { mostrarErro("Não existe sessão de caixa aberta."); return; }
        modoFundo = false;
        lblFormTitulo.setText("Sangria de Caixa");
        abrirFormNumerario();
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

    @FXML
    public void onConfirmarNumerario() {
        List<DetalheNumerario> lista = new ArrayList<>();
        for (LinhaForm l : linhasForm) {
            if (l.quantidade.get() > 0) lista.add(new DetalheNumerario(l.denominacao, l.quantidade.get()));
        }
        fecharFormNumerario();
        Utilizador u = AppContext.getInstance().getUtilizadorAtual();
        try {
            if (modoFundo) {
                ConfiguracaoTerminal config = AppContext.getInstance().configuracao;
                sessaoAtual = AppContext.getInstance().caixaServico.abrirSessao(u, lista, config);
                mostrarInfo("Sessão de caixa aberta. Saldo inicial: " + String.format("%.2f €", sessaoAtual.getSaldoAtual()));
            } else {
                AppContext.getInstance().caixaServico.registarSangria(sessaoAtual, u, lista);
                mostrarInfo("Sangria registada.");
            }
            atualizarEstado();
        } catch (Exception e) { mostrarErro(e.getMessage()); }
    }

    @FXML
    public void onCancelarNumerario() {
        fecharFormNumerario();
    }

    private void abrirFormNumerario() {
        linhasForm.clear();
        for (Denominacao d : Denominacao.values()) linhasForm.add(new LinhaForm(d));
        lblFormTotal.setText("Total: 0,00 €");
        formNumerario.setVisible(true);
        formNumerario.setManaged(true);
        btnAbrirSessao.setDisable(true);
        btnRegistarSangria.setDisable(true);
        btnFecharSessao.setDisable(true);
    }

    private void fecharFormNumerario() {
        formNumerario.setVisible(false);
        formNumerario.setManaged(false);
        atualizarEstado();
    }

    private void atualizarTotalForm() {
        double total = linhasForm.stream().mapToDouble(LinhaForm::getSubtotal).sum();
        lblFormTotal.setText(String.format("Total: %.2f €", total));
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

    private void mostrarErro(String m) { Alert a = new Alert(Alert.AlertType.ERROR); a.setTitle("Erro"); a.setHeaderText(null); a.setContentText(m); a.showAndWait(); }
    private void mostrarInfo(String m)  { Alert a = new Alert(Alert.AlertType.INFORMATION); a.setTitle("Informação"); a.setHeaderText(null); a.setContentText(m); a.showAndWait(); }
}
