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
import pt.trasmum.loja.apresentacao.DialogoUtil;
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
    @FXML private Label lblFundoInicial;
    @FXML private Button btnAbrirSessao;
    @FXML private Button btnRegistarSangria;
    @FXML private Button btnFecharSessao;

    @FXML private VBox formNumerario;
    @FXML private Label lblFormTitulo;
    @FXML private TableView<LinhaForm> tblNumerarioForm;
    @FXML private TableColumn<LinhaForm, String>  colFormDenom;
    @FXML private TableColumn<LinhaForm, Integer> colFormQtd;
    @FXML private TableColumn<LinhaForm, Double>  colFormSubtotal;
    @FXML private Label lblFormTotal;

    private SessaoCaixa sessaoAtual;
    private boolean modoFecho = false;
    private final ObservableList<LinhaForm> linhasForm = FXCollections.observableArrayList();

    public static class LinhaForm {
        final Denominacao denominacao;
        final SimpleIntegerProperty quantidade = new SimpleIntegerProperty(0);

        LinhaForm(Denominacao d) { this.denominacao = d; }

        double getSubtotal() { return denominacao.getValor() * quantidade.get(); }
    }

    @FXML
    public void initialize() {
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
        TextInputDialog dialog = new TextInputDialog("50.00");
        dialog.setTitle("Abrir Sessão de Caixa");
        dialog.setHeaderText("Fundo Inicial");
        dialog.setContentText("Valor do fundo (€):");
        dialog.showAndWait().ifPresent(input -> {
            try {
                double fundo = Double.parseDouble(input.replace(",", ".").trim());
                if (fundo < 0) throw new NumberFormatException();
                Utilizador u = AppContext.getInstance().getUtilizadorAtual();
                ConfiguracaoTerminal config = AppContext.getInstance().configuracao;
                sessaoAtual = AppContext.getInstance().caixaServico.abrirSessao(u, fundo, config);
                mostrarInfo("Sessão de caixa aberta. Saldo inicial: " + String.format("%.2f €", sessaoAtual.getSaldoAtual()));
                atualizarEstado();
            } catch (NumberFormatException e) {
                mostrarErro("Valor inválido. Introduza um número positivo.");
            } catch (Exception e) {
                mostrarErro(e.getMessage());
            }
        });
    }

    @FXML
    public void onRegistarSangria() {
        if (sessaoAtual == null) { mostrarErro("Não existe sessão de caixa aberta."); return; }
        lblFormTitulo.setText("Sangria de Caixa");
        abrirFormNumerario();
    }

    @FXML
    public void onFecharSessao() {
        if (sessaoAtual == null) { mostrarErro("Não existe sessão de caixa aberta."); return; }
        modoFecho = true;
        lblFormTitulo.setText("Contagem Final de Caixa");
        abrirFormNumerario();
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
            if (modoFecho) {
                double contado = lista.stream().mapToDouble(d -> d.getSubtotal()).sum();
                double esperado = sessaoAtual.getSaldoAtual();
                double diferenca = contado - esperado;
                AppContext.getInstance().caixaServico.fecharSessao(sessaoAtual, u, lista);
                sessaoAtual = null;
                mostrarInfo(String.format(
                        "Sessão encerrada.%n%nEsperado:  %.2f €%nContado:   %.2f €%nDiferença: %+.2f €%s",
                        esperado, contado, diferenca,
                        Math.abs(diferenca) > 0.01 ? "\n\n⚠ Diferença detectada — registe a ocorrência." : ""));
            } else {
                AppContext.getInstance().caixaServico.registarSangria(sessaoAtual, u, lista);
                mostrarInfo("Sangria registada.");
            }
            modoFecho = false;
            atualizarEstado();
        } catch (Exception e) { mostrarErro(e.getMessage()); }
    }

    @FXML
    public void onCancelarNumerario() {
        modoFecho = false;
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
            lblFundoInicial.setText(String.format("Fundo inicial: %.2f €", sessaoAtual.getSaldoAtual() +
                    sessaoAtual.getSangrias().stream().mapToDouble(Sangria::getTotal).sum()));
            btnAbrirSessao.setDisable(true);
            btnRegistarSangria.setDisable(false);
            btnFecharSessao.setDisable(false);
        } else {
            lblEstadoSessao.setText("Sessão: FECHADA");
            lblSaldo.setText("—");
            lblFundoInicial.setText("—");
            btnAbrirSessao.setDisable(false);
            btnRegistarSangria.setDisable(true);
            btnFecharSessao.setDisable(true);
        }
    }

    private void mostrarErro(String m) { DialogoUtil.erro(m); }
    private void mostrarInfo(String m)  { DialogoUtil.info(m); }
}
