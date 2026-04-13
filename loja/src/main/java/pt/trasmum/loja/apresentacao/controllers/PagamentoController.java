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
import pt.trasmum.loja.dominio.fornecedores.Pagamento;

public class PagamentoController {

    @FXML private TableView<Pagamento> tblPagamentos;
    @FXML private TableColumn<Pagamento, Integer> colId;
    @FXML private TableColumn<Pagamento, Integer> colFornecedor;
    @FXML private TableColumn<Pagamento, Double>  colValor;
    @FXML private TableColumn<Pagamento, String>  colEstado;

    private final ObservableList<Pagamento> pagamentos = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colId.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getId()).asObject());
        colFornecedor.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getIdFornecedor()).asObject());
        colValor.setCellValueFactory(c -> new SimpleDoubleProperty(c.getValue().getValor()).asObject());
        colEstado.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getEstadoPagamento().name()));
        tblPagamentos.setItems(pagamentos);
        carregar();
    }

    @FXML
    public void onLiquidar() {
        Pagamento sel = tblPagamentos.getSelectionModel().getSelectedItem();
        if (sel == null) { mostrarErro("Selecione um pagamento."); return; }

        Alert c = new Alert(Alert.AlertType.CONFIRMATION,
                String.format("Liquidar pagamento #%d no valor de %.2f €?", sel.getId(), sel.getValor()),
                ButtonType.YES, ButtonType.NO);
        c.setTitle("Confirmação"); c.setHeaderText(null);
        c.showAndWait().ifPresent(bt -> {
            if (bt == ButtonType.YES) {
                Utilizador u = AppContext.getInstance().getUtilizadorAtual();
                try {
                    AppContext.getInstance().pagamentoServico.liquidar(u, sel.getId());
                    carregar();
                    mostrarInfo("Pagamento liquidado.");
                } catch (Exception e) { mostrarErro(e.getMessage()); }
            }
        });
    }

    @FXML
    public void onRefrescar() { carregar(); }

    private void carregar() {
        pagamentos.setAll(AppContext.getInstance().pagamentoServico.listarPendentes());
    }

    private void mostrarErro(String m) { Alert a = new Alert(Alert.AlertType.ERROR); a.setTitle("Erro"); a.setHeaderText(null); a.setContentText(m); a.showAndWait(); }
    private void mostrarInfo(String m)  { Alert a = new Alert(Alert.AlertType.INFORMATION); a.setTitle("Informação"); a.setHeaderText(null); a.setContentText(m); a.showAndWait(); }
}
