package pt.trasmum.loja.apresentacao.controllers;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import pt.trasmum.loja.apresentacao.DialogoUtil;
import pt.trasmum.loja.app.AppContext;
import pt.trasmum.loja.dominio.core.Utilizador;
import pt.trasmum.loja.dominio.fornecedores.Fornecedor;
import pt.trasmum.loja.dominio.fornecedores.Pagamento;

public class PagamentoController {

    @FXML private TableView<Pagamento> tblPagamentos;
    @FXML private TableColumn<Pagamento, Integer> colId;
    @FXML private TableColumn<Pagamento, String>  colFornecedor;
    @FXML private TableColumn<Pagamento, Double>  colValor;
    @FXML private TableColumn<Pagamento, String>  colEstado;

    private final ObservableList<Pagamento> pagamentos = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colId.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getId()).asObject());
        colFornecedor.setCellValueFactory(c -> {
            Fornecedor f = AppContext.getInstance().fornecedorRepo.buscarPorId(c.getValue().getIdFornecedor());
            return new SimpleStringProperty(f != null ? f.getNome() : "—");
        });
        colValor.setCellValueFactory(c -> new SimpleDoubleProperty(c.getValue().getValor()).asObject());
        colEstado.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getEstadoPagamento().name()));
        tblPagamentos.setItems(pagamentos);
        carregar();
    }

    @FXML
    public void onLiquidar() {
        Pagamento sel = tblPagamentos.getSelectionModel().getSelectedItem();
        if (sel == null) { mostrarErro("Selecione um pagamento."); return; }

        DialogoUtil.confirmar(String.format("Liquidar pagamento #%d no valor de %.2f €?", sel.getId(), sel.getValor())).ifPresent(bt -> {
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
        pagamentos.setAll(AppContext.getInstance().pagamentoServico.listarNaoPagos());
    }

    private void mostrarErro(String m) { DialogoUtil.erro(m); }
    private void mostrarInfo(String m)  { DialogoUtil.info(m); }
}
