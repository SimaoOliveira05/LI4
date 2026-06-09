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
import pt.trasmum.loja.apresentacao.DialogoUtil;
import pt.trasmum.loja.app.AppContext;
import pt.trasmum.loja.dominio.catalogo.Lote;
import pt.trasmum.loja.dominio.catalogo.Produto;
import pt.trasmum.loja.dominio.core.Utilizador;
import pt.trasmum.loja.dominio.vendas.Devolucao;
import pt.trasmum.loja.dominio.vendas.LinhaVenda;
import pt.trasmum.loja.dominio.vendas.Venda;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class DevolucaoController {

    @FXML private TextField  txtNumeroFatura;
    @FXML private HBox       secaoFatura;
    @FXML private Label      lblInfoFatura;
    @FXML private TableView<LinhaFaturaExibicao>            tblLinhasFatura;
    @FXML private TableColumn<LinhaFaturaExibicao, String>  colFProduto;
    @FXML private TableColumn<LinhaFaturaExibicao, String>  colFCodigo;
    @FXML private TableColumn<LinhaFaturaExibicao, Integer> colFQtdVendida;
    @FXML private TableColumn<LinhaFaturaExibicao, Double>  colFPrecoUnit;
    @FXML private VBox       formDevolucao;
    @FXML private DatePicker dpDataValidade;
    @FXML private TextField  txtQtdDevolucao;
    @FXML private Label      lblResultadoDevolucao;

    private String numFaturaAtual;

    static final class LinhaFaturaExibicao {
        final String nomeProduto;
        final String codigoBarras;
        final int    quantidade;
        final double precoUnitario;

        LinhaFaturaExibicao(String nomeProduto, String codigoBarras, int quantidade, double precoUnitario) {
            this.nomeProduto   = nomeProduto;
            this.codigoBarras  = codigoBarras;
            this.quantidade    = quantidade;
            this.precoUnitario = precoUnitario;
        }
    }

    @FXML
    public void initialize() {
        colFProduto.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().nomeProduto));
        colFCodigo.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().codigoBarras));
        colFQtdVendida.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().quantidade).asObject());
        colFPrecoUnit.setCellValueFactory(c -> new SimpleDoubleProperty(c.getValue().precoUnitario).asObject());

        tblLinhasFatura.getSelectionModel().selectedItemProperty().addListener((obs, old, novo) -> {
            if (novo != null) {
                dpDataValidade.setValue(null);
                txtQtdDevolucao.clear();
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
        carregarFatura(num);
        lblResultadoDevolucao.setText("");
    }

    /**
     * Carrega/recarrega a fatura, agregando as linhas por produto e mostrando
     * apenas a quantidade ainda devolvível (vendida − já devolvida).
     */
    private void carregarFatura(String num) {
        Venda venda = AppContext.getInstance().vendaServico.buscarPorNumeroFatura(num);
        if (venda == null || venda.getFatura() == null) {
            mostrarErro("Fatura não encontrada: " + num);
            esconderSecoesDevolucao();
            return;
        }
        numFaturaAtual = num;

        // Quantidade já devolvida, por produto
        Map<Integer, Integer> devolvidoPorProduto = new HashMap<>();
        for (Devolucao dv : AppContext.getInstance().devolucaoRepo.buscarPorFatura(num)) {
            Lote lote = AppContext.getInstance().loteRepo.buscarPorId(dv.getIdLote());
            if (lote == null) continue;
            devolvidoPorProduto.merge(lote.getIdProduto(), dv.getQuantidade(), Integer::sum);
        }

        // Agregação das linhas da fatura por produto
        Map<Integer, LinhaFaturaExibicao> agregadas = new LinkedHashMap<>();
        for (LinhaVenda lv : venda.getLinhas()) {
            Lote lote = AppContext.getInstance().loteRepo.buscarPorId(lv.getIdLote());
            if (lote == null) continue;
            Produto produto = AppContext.getInstance().produtoRepo.buscarPorId(lote.getIdProduto());
            if (produto == null) continue;
            LinhaFaturaExibicao acc = agregadas.get(produto.getId());
            int qtd = (acc == null ? 0 : acc.quantidade) + lv.getQuantidade();
            agregadas.put(produto.getId(), new LinhaFaturaExibicao(
                    produto.getNome(), produto.getCodigoBarras(), qtd, lv.getPrecoUnitario()));
        }

        ObservableList<LinhaFaturaExibicao> linhas = FXCollections.observableArrayList();
        for (Map.Entry<Integer, LinhaFaturaExibicao> e : agregadas.entrySet()) {
            LinhaFaturaExibicao a = e.getValue();
            int restante = a.quantidade - devolvidoPorProduto.getOrDefault(e.getKey(), 0);
            if (restante <= 0) continue;
            linhas.add(new LinhaFaturaExibicao(a.nomeProduto, a.codigoBarras, restante, a.precoUnitario));
        }

        tblLinhasFatura.setItems(linhas);
        tblLinhasFatura.getSelectionModel().clearSelection();
        formDevolucao.setVisible(false);
        formDevolucao.setManaged(false);
        lblInfoFatura.setText("Fatura: " + num + "  ·  " + linhas.size() + " artigo(s)");
        secaoFatura.setVisible(true);
        secaoFatura.setManaged(true);
    }

    @FXML
    public void onProcessarDevolucao() {
        LinhaFaturaExibicao sel = tblLinhasFatura.getSelectionModel().getSelectedItem();
        if (sel == null) { mostrarErro("Selecione um produto."); return; }

        LocalDate data  = dpDataValidade.getValue();
        String qtdStr   = txtQtdDevolucao.getText().trim();

        if (data == null)     { mostrarErro("Indique a validade da embalagem."); return; }
        if (qtdStr.isBlank()) { mostrarErro("Indique a quantidade."); return; }

        try {
            int quantidade = Integer.parseInt(qtdStr);
            Utilizador u = AppContext.getInstance().getUtilizadorAtual();
            Devolucao d = AppContext.getInstance().devolucaoServico
                    .processar(u, numFaturaAtual, sel.codigoBarras, data, quantidade);
            dpDataValidade.setValue(null);
            txtQtdDevolucao.clear();
            carregarFatura(numFaturaAtual);
            lblResultadoDevolucao.setText(String.format(
                    "Devolução processada. Valor restituído: %.2f €", d.getValorRestituido()));
        } catch (NumberFormatException e) {
            mostrarErro("Quantidade inválida.");
        } catch (Exception e) {
            mostrarErro(e.getMessage());
        }
    }

    @FXML
    public void onCancelarDevolucao() {
        dpDataValidade.setValue(null);
        txtQtdDevolucao.clear();
        formDevolucao.setVisible(false);
        formDevolucao.setManaged(false);
        tblLinhasFatura.getSelectionModel().clearSelection();
    }

    private void esconderSecoesDevolucao() {
        secaoFatura.setVisible(false);
        secaoFatura.setManaged(false);
        formDevolucao.setVisible(false);
        formDevolucao.setManaged(false);
    }

    private void mostrarErro(String msg) { DialogoUtil.erro(msg); }
}
