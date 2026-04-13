package pt.trasmum.loja.apresentacao.controllers;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import pt.trasmum.loja.app.AppContext;
import pt.trasmum.loja.dominio.core.Utilizador;
import pt.trasmum.loja.dominio.tesouraria.SessaoCaixa;
import pt.trasmum.loja.dominio.vendas.*;

public class VendaController {

    @FXML private TextField txtCodigoBarras;
    @FXML private TextField txtQuantidade;
    @FXML private TableView<LinhaVenda> tblLinhas;
    @FXML private TableColumn<LinhaVenda, Integer> colLote;
    @FXML private TableColumn<LinhaVenda, Integer> colQtd;
    @FXML private TableColumn<LinhaVenda, Double>  colPreco;
    @FXML private TableColumn<LinhaVenda, Double>  colSubtotal;
    @FXML private Label lblTotal;
    @FXML private RadioButton rbNumerario;
    @FXML private RadioButton rbMultibanco;
    @FXML private TextField txtValorEntregue;
    @FXML private Label lblTroco;
    @FXML private TextField txtNif;
    @FXML private Button btnIniciar;
    @FXML private Button btnAdicionar;
    @FXML private Button btnFinalizar;
    @FXML private Button btnAnular;

    private Venda vendaAtual;
    private final ObservableList<LinhaVenda> linhas = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colLote.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getIdLote()).asObject());
        colQtd.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getQuantidade()).asObject());
        colPreco.setCellValueFactory(c -> new SimpleDoubleProperty(c.getValue().getPrecoUnitario()).asObject());
        colSubtotal.setCellValueFactory(c -> new SimpleDoubleProperty(c.getValue().calcularSubtotal()).asObject());
        tblLinhas.setItems(linhas);
        setFormAtivo(false);

        ToggleGroup tg = new ToggleGroup();
        rbNumerario.setToggleGroup(tg);
        rbMultibanco.setToggleGroup(tg);
        rbNumerario.setSelected(true);
        txtValorEntregue.disableProperty().bind(rbMultibanco.selectedProperty());
    }

    @FXML
    public void onIniciarVenda() {
        Utilizador u = AppContext.getInstance().getUtilizadorAtual();
        vendaAtual = AppContext.getInstance().vendaServico.iniciarVenda(u);
        linhas.clear();
        lblTotal.setText("Total: 0.00 €");
        lblTroco.setText("");
        setFormAtivo(true);
    }

    @FXML
    public void onAdicionarLinha() {
        if (vendaAtual == null) return;
        String codigo = txtCodigoBarras.getText().trim();
        String qtdStr = txtQuantidade.getText().trim();
        if (codigo.isBlank() || qtdStr.isBlank()) {
            mostrarErro("Preencha o código de barras e a quantidade.");
            return;
        }
        try {
            int qtd = Integer.parseInt(qtdStr);
            AppContext.getInstance().vendaServico.adicionarLinha(vendaAtual, codigo, qtd);
            linhas.setAll(vendaAtual.getLinhas());
            atualizarTotal();
            txtCodigoBarras.clear();
            txtQuantidade.setText("1");
            txtCodigoBarras.requestFocus();
        } catch (NumberFormatException e) {
            mostrarErro("Quantidade inválida.");
        } catch (Exception e) {
            mostrarErro(e.getMessage());
        }
    }

    @FXML
    public void onFinalizarVenda() {
        if (vendaAtual == null || vendaAtual.getLinhas().isEmpty()) {
            mostrarErro("Não há linhas na venda.");
            return;
        }
        MetodoPagamento metodo = rbNumerario.isSelected() ? MetodoPagamento.NUMERARIO : MetodoPagamento.MULTIBANCO;
        double total = vendaAtual.getLinhas().stream().mapToDouble(LinhaVenda::calcularSubtotal).sum();
        double valorEntregue = 0;
        if (metodo == MetodoPagamento.NUMERARIO) {
            try {
                valorEntregue = Double.parseDouble(txtValorEntregue.getText().replace(",", "."));
            } catch (NumberFormatException e) {
                mostrarErro("Valor entregue inválido.");
                return;
            }
        }

        // Emite fatura com NIF opcional
        String nif = txtNif.getText().trim();
        vendaAtual.emitirFatura(nif.isBlank() ? null : nif);

        try {
            Fatura fatura = AppContext.getInstance().vendaServico.finalizarVenda(vendaAtual, metodo, valorEntregue);
            if (metodo == MetodoPagamento.NUMERARIO) {
                double troco = AppContext.getInstance().vendaServico.calcularTroco(total, valorEntregue);
                lblTroco.setText(String.format("Troco: %.2f €", troco));
            }
            mostrarInfo("Venda finalizada!\nFatura: " + fatura.getNumeroFatura()
                    + "\nTotal: " + String.format("%.2f €", total));

            // Verifica limite de caixa
            Utilizador u = AppContext.getInstance().getUtilizadorAtual();
            SessaoCaixa sessao = AppContext.getInstance().caixaServico.buscarSessaoAtiva(u.getId());
            if (sessao != null && AppContext.getInstance().caixaServico.verificarLimite(
                    sessao, AppContext.getInstance().configuracao)) {
                mostrarAviso("Atenção: o saldo da caixa excede o limite máximo. Efectue uma sangria.");
            }

            vendaAtual = null;
            linhas.clear();
            lblTotal.setText("Total: 0.00 €");
            setFormAtivo(false);
        } catch (Exception e) {
            mostrarErro(e.getMessage());
        }
    }

    @FXML
    public void onAnularVenda() {
        if (vendaAtual == null) return;
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Anular venda?", ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Confirmação");
        confirm.setHeaderText(null);
        confirm.showAndWait().ifPresent(bt -> {
            if (bt == ButtonType.YES) {
                try {
                    AppContext.getInstance().vendaServico.anularVenda(vendaAtual);
                } catch (Exception e) { /* venda ainda em memória — repõe stock se possível */ }
                vendaAtual = null;
                linhas.clear();
                lblTotal.setText("Total: 0.00 €");
                lblTroco.setText("");
                setFormAtivo(false);
            }
        });
    }

    private void atualizarTotal() {
        double total = vendaAtual.getLinhas().stream().mapToDouble(LinhaVenda::calcularSubtotal).sum();
        lblTotal.setText(String.format("Total: %.2f €", total));
    }

    private void setFormAtivo(boolean ativo) {
        txtCodigoBarras.setDisable(!ativo);
        txtQuantidade.setDisable(!ativo);
        btnAdicionar.setDisable(!ativo);
        btnFinalizar.setDisable(!ativo);
        btnAnular.setDisable(!ativo);
        btnIniciar.setDisable(ativo);
    }

    private void mostrarErro(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR); a.setTitle("Erro"); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }
    private void mostrarInfo(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION); a.setTitle("Confirmação"); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }
    private void mostrarAviso(String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING); a.setTitle("Aviso"); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }
}
