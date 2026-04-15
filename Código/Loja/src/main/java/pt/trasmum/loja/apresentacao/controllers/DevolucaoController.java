package pt.trasmum.loja.apresentacao.controllers;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import pt.trasmum.loja.app.AppContext;
import pt.trasmum.loja.dominio.catalogo.Lote;
import pt.trasmum.loja.dominio.catalogo.Produto;
import pt.trasmum.loja.dominio.core.Utilizador;
import pt.trasmum.loja.dominio.vendas.Devolucao;
import pt.trasmum.loja.dominio.vendas.LinhaVenda;
import pt.trasmum.loja.dominio.vendas.Venda;

import java.time.LocalDate;

public class DevolucaoController {

    @FXML private TextField txtNumeroFatura;
    @FXML private HBox secaoFatura;
    @FXML private Label lblInfoFatura;
    @FXML private TableView<LinhaFaturaExibicao> tblLinhas;
    @FXML private TableColumn<LinhaFaturaExibicao, String>  colProduto;
    @FXML private TableColumn<LinhaFaturaExibicao, String>  colCodigo;
    @FXML private TableColumn<LinhaFaturaExibicao, Integer> colQtdVendida;
    @FXML private TableColumn<LinhaFaturaExibicao, Double>  colPrecoUnit;
    @FXML private VBox formDevolucao;
    @FXML private DatePicker dpDataValidade;
    @FXML private TextField txtQuantidade;
    @FXML private Label lblResultado;

    private String numFaturaAtual;

    public static class LinhaFaturaExibicao {
        final String nomeProduto;
        final String codigoBarras;
        final int quantidade;
        final double precoUnitario;

        LinhaFaturaExibicao(String nomeProduto, String codigoBarras, int quantidade, double precoUnitario) {
            this.nomeProduto = nomeProduto;
            this.codigoBarras = codigoBarras;
            this.quantidade = quantidade;
            this.precoUnitario = precoUnitario;
        }
    }

    @FXML
    public void initialize() {
        colProduto.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().nomeProduto));
        colCodigo.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().codigoBarras));
        colQtdVendida.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().quantidade).asObject());
        colPrecoUnit.setCellValueFactory(c -> new SimpleDoubleProperty(c.getValue().precoUnitario).asObject());

        tblLinhas.getSelectionModel().selectedItemProperty().addListener((obs, old, novo) -> {
            if (novo != null) {
                dpDataValidade.setValue(null);
                txtQuantidade.clear();
                formDevolucao.setVisible(true);
                formDevolucao.setManaged(true);
            } else {
                formDevolucao.setVisible(false);
                formDevolucao.setManaged(false);
            }
        });
    }

    @FXML
    public void onPesquisarFatura() {
        String num = txtNumeroFatura.getText().trim();
        if (num.isBlank()) { mostrarErro("Insira o número de fatura."); return; }

        Venda venda = AppContext.getInstance().vendaRepo.buscarPorNumeroFatura(num);
        if (venda == null || venda.getFatura() == null) {
            mostrarErro("Fatura não encontrada: " + num);
            esconderSecoes();
            return;
        }

        numFaturaAtual = num;
        ObservableList<LinhaFaturaExibicao> linhas = FXCollections.observableArrayList();
        for (LinhaVenda lv : venda.getLinhas()) {
            Lote lote = AppContext.getInstance().loteRepo.buscarPorId(lv.getIdLote());
            if (lote == null) continue;
            Produto produto = AppContext.getInstance().produtoRepo.buscarPorId(lote.getIdProduto());
            if (produto == null) continue;
            linhas.add(new LinhaFaturaExibicao(
                    produto.getNome(), produto.getCodigoBarras(),
                    lv.getQuantidade(), lv.getPrecoUnitario()));
        }

        tblLinhas.setItems(linhas);
        tblLinhas.getSelectionModel().clearSelection();
        formDevolucao.setVisible(false);
        formDevolucao.setManaged(false);
        lblInfoFatura.setText("Fatura: " + num + "  ·  " + linhas.size() + " artigo(s)");
        secaoFatura.setVisible(true);
        secaoFatura.setManaged(true);
        lblResultado.setText("");
    }

    @FXML
    public void onProcessarDevolucao() {
        LinhaFaturaExibicao sel = tblLinhas.getSelectionModel().getSelectedItem();
        if (sel == null) { mostrarErro("Selecione um produto."); return; }

        LocalDate data = dpDataValidade.getValue();
        String qtdStr  = txtQuantidade.getText().trim();

        if (data == null)     { mostrarErro("Indique a validade da embalagem."); return; }
        if (qtdStr.isBlank()) { mostrarErro("Indique a quantidade."); return; }

        try {
            int quantidade = Integer.parseInt(qtdStr);
            Utilizador u = AppContext.getInstance().getUtilizadorAtual();
            Devolucao d = AppContext.getInstance().devolucaoServico
                    .processar(u, numFaturaAtual, sel.codigoBarras, data, quantidade);
            lblResultado.setText(String.format(
                    "Devolução processada. Valor restituído: %.2f €", d.getValorRestituido()));
            dpDataValidade.setValue(null);
            txtQuantidade.clear();
            formDevolucao.setVisible(false);
            formDevolucao.setManaged(false);
            tblLinhas.getSelectionModel().clearSelection();
        } catch (NumberFormatException e) {
            mostrarErro("Quantidade inválida.");
        } catch (Exception e) {
            mostrarErro(e.getMessage());
        }
    }

    @FXML
    public void onCancelar() {
        dpDataValidade.setValue(null);
        txtQuantidade.clear();
        formDevolucao.setVisible(false);
        formDevolucao.setManaged(false);
        tblLinhas.getSelectionModel().clearSelection();
    }

    private void esconderSecoes() {
        secaoFatura.setVisible(false);
        secaoFatura.setManaged(false);
        formDevolucao.setVisible(false);
        formDevolucao.setManaged(false);
    }

    private void mostrarErro(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("Erro"); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }
}
