package pt.trasmum.loja.apresentacao.controllers;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import pt.trasmum.loja.app.AppContext;
import pt.trasmum.loja.dominio.catalogo.Produto;
import pt.trasmum.loja.dominio.core.Utilizador;
import pt.trasmum.loja.dominio.fornecedores.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class RemessaController {

    // Aba Remessa
    @FXML private ComboBox<Fornecedor> cmbFornecedorRemessa;
    @FXML private TextField txtProdutoId;
    @FXML private TextField txtQtdRemessa;
    @FXML private DatePicker dpValidade;
    @FXML private TextField txtValorGuia;
    @FXML private TableView<LinhaRemessa> tblLinhasRemessa;
    @FXML private TableColumn<LinhaRemessa, Integer> colProdutoR;
    @FXML private TableColumn<LinhaRemessa, Integer> colQtdR;
    @FXML private TableColumn<LinhaRemessa, String>  colValidadeR;

    // Aba Pedidos
    @FXML private TableView<PedidoRemessa> tblPedidos;
    @FXML private TableColumn<PedidoRemessa, Integer> colIdPedido;
    @FXML private TableColumn<PedidoRemessa, String>  colFornecedorPedido;
    @FXML private TableColumn<PedidoRemessa, String>  colEstadoPedido;
    @FXML private ComboBox<String> cmbFiltroPedido;

    private final ObservableList<LinhaRemessa> linhasRemessa = FXCollections.observableArrayList();
    private final ObservableList<PedidoRemessa> pedidos = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colProdutoR.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getIdProduto()).asObject());
        colQtdR.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getQuantidade()).asObject());
        colValidadeR.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDataValidade().toString()));
        tblLinhasRemessa.setItems(linhasRemessa);

        colIdPedido.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getId()).asObject());
        colFornecedorPedido.setCellValueFactory(c -> new SimpleStringProperty("Forn. " + c.getValue().getIdFornecedor()));
        colEstadoPedido.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getEstado().name()));
        tblPedidos.setItems(pedidos);

        cmbFiltroPedido.setItems(FXCollections.observableArrayList("TODOS", "PENDENTE", "CONCLUIDO"));
        cmbFiltroPedido.setValue("TODOS");

        List<Fornecedor> fornecedores = AppContext.getInstance().fornecedorRepo.listarTodos();
        cmbFornecedorRemessa.setItems(FXCollections.observableArrayList(fornecedores));
        cmbFornecedorRemessa.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(Fornecedor f, boolean empty) {
                super.updateItem(f, empty); setText(empty || f == null ? null : f.getNome());
            }
        });
        cmbFornecedorRemessa.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(Fornecedor f, boolean empty) {
                super.updateItem(f, empty); setText(empty || f == null ? null : f.getNome());
            }
        });

        carregarPedidos();
    }

    @FXML
    public void onAdicionarLinhaRemessa() {
        try {
            int idProduto = Integer.parseInt(txtProdutoId.getText().trim());
            int qtd = Integer.parseInt(txtQtdRemessa.getText().trim());
            LocalDate validade = dpValidade.getValue();
            if (validade == null) { mostrarErro("Selecione a data de validade."); return; }
            LinhaRemessa linha = new LinhaRemessa(0, idProduto, qtd, validade);
            linhasRemessa.add(linha);
            txtProdutoId.clear(); txtQtdRemessa.clear(); dpValidade.setValue(null);
        } catch (NumberFormatException e) { mostrarErro("ID de produto ou quantidade inválida."); }
    }

    @FXML
    public void onRegistarRemessa() {
        Fornecedor forn = cmbFornecedorRemessa.getValue();
        if (forn == null) { mostrarErro("Selecione um fornecedor."); return; }
        if (linhasRemessa.isEmpty()) { mostrarErro("Adicione pelo menos uma linha."); return; }
        double valorGuia;
        try { valorGuia = Double.parseDouble(txtValorGuia.getText().replace(",", ".")); }
        catch (NumberFormatException e) { mostrarErro("Valor de guia inválido."); return; }

        Utilizador u = AppContext.getInstance().getUtilizadorAtual();
        try {
            AppContext.getInstance().remessaServico.registarRemessa(u, forn, new ArrayList<>(linhasRemessa), valorGuia);
            linhasRemessa.clear();
            mostrarInfo("Remessa registada com sucesso.");
        } catch (Exception e) { mostrarErro(e.getMessage()); }
    }

    @FXML
    public void onNovoPedido() {
        // Diálogo para criar um PedidoRemessa
        Dialog<List<LinhaPedidoRemessa>> dlg = new Dialog<>();
        dlg.setTitle("Novo Pedido de Remessa");
        dlg.setHeaderText("Selecione o fornecedor e adicione os produtos pretendidos.");
        dlg.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // Fornecedor
        ComboBox<Fornecedor> cmbForn = new ComboBox<>();
        cmbForn.setItems(FXCollections.observableArrayList(AppContext.getInstance().fornecedorRepo.listarTodos()));
        cmbForn.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(Fornecedor f, boolean empty) {
                super.updateItem(f, empty); setText(empty || f == null ? null : f.getNome());
            }
        });
        cmbForn.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(Fornecedor f, boolean empty) {
                super.updateItem(f, empty); setText(empty || f == null ? null : f.getNome());
            }
        });
        cmbForn.setPrefWidth(220);

        // Linhas do pedido (produto + quantidade)
        ObservableList<LinhaPedidoRemessa> linhasPedido = FXCollections.observableArrayList();
        TableView<LinhaPedidoRemessa> tbl = new TableView<>(linhasPedido);
        tbl.setPrefHeight(180);
        TableColumn<LinhaPedidoRemessa, Integer> cProd = new TableColumn<>("ID Produto");
        cProd.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getIdProduto()).asObject());
        cProd.setPrefWidth(100);
        TableColumn<LinhaPedidoRemessa, Integer> cQtd = new TableColumn<>("Qtd. pretendida");
        cQtd.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getQuantidadePretendida()).asObject());
        cQtd.setPrefWidth(130);
        tbl.getColumns().addAll(cProd, cQtd);

        // Campos para adicionar linha
        TextField fProdId = new TextField(); fProdId.setPromptText("ID do produto"); fProdId.setPrefWidth(110);
        TextField fQtd   = new TextField(); fQtd.setPromptText("Quantidade");       fQtd.setPrefWidth(90);
        Button btnAdd = new Button("Adicionar");
        btnAdd.setOnAction(e -> {
            try {
                int idP = Integer.parseInt(fProdId.getText().trim());
                int qtd = Integer.parseInt(fQtd.getText().trim());
                if (qtd <= 0) { mostrarErro("Quantidade deve ser > 0."); return; }
                // Verifica se produto existe
                Produto p = AppContext.getInstance().produtoRepo.buscarPorId(idP);
                if (p == null) { mostrarErro("Produto " + idP + " não encontrado."); return; }
                linhasPedido.add(new LinhaPedidoRemessa(0, idP, qtd));
                fProdId.clear(); fQtd.clear();
            } catch (NumberFormatException ex) { mostrarErro("ID ou quantidade inválidos."); }
        });

        VBox conteudo = new VBox(10);
        conteudo.setPadding(new Insets(12));
        HBox hForn = new HBox(8, new Label("Fornecedor:"), cmbForn);
        hForn.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        HBox hLinha = new HBox(8, new Label("Produto:"), fProdId, new Label("Qtd.:"), fQtd, btnAdd);
        hLinha.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        conteudo.getChildren().addAll(hForn, new Label("Linhas do pedido:"), hLinha, tbl);
        dlg.getDialogPane().setContent(conteudo);

        // Validação ao confirmar
        Button btnOk = (Button) dlg.getDialogPane().lookupButton(ButtonType.OK);
        btnOk.addEventFilter(javafx.event.ActionEvent.ACTION, e -> {
            if (cmbForn.getValue() == null) {
                mostrarErro("Selecione um fornecedor."); e.consume();
            } else if (linhasPedido.isEmpty()) {
                mostrarErro("Adicione pelo menos uma linha."); e.consume();
            }
        });

        dlg.setResultConverter(bt -> bt == ButtonType.OK ? new ArrayList<>(linhasPedido) : null);

        dlg.showAndWait().ifPresent(linhas -> {
            Utilizador u = AppContext.getInstance().getUtilizadorAtual();
            try {
                AppContext.getInstance().remessaServico.criarPedidoRemessa(u, cmbForn.getValue(), linhas);
                carregarPedidos();
                mostrarInfo("Pedido de remessa criado.");
            } catch (Exception e) { mostrarErro(e.getMessage()); }
        });
    }

    @FXML
    public void onFiltrarPedidos() { carregarPedidos(); }

    private void carregarPedidos() {
        String filtro = cmbFiltroPedido.getValue();
        List<PedidoRemessa> lista;
        if ("PENDENTE".equals(filtro)) {
            lista = AppContext.getInstance().remessaServico.listarPedidos(EstadoPedido.PENDENTE);
        } else if ("CONCLUIDO".equals(filtro)) {
            lista = AppContext.getInstance().remessaServico.listarPedidos(EstadoPedido.CONCLUIDO);
        } else {
            lista = new ArrayList<>();
            lista.addAll(AppContext.getInstance().remessaServico.listarPedidos(EstadoPedido.PENDENTE));
            lista.addAll(AppContext.getInstance().remessaServico.listarPedidos(EstadoPedido.CONCLUIDO));
        }
        pedidos.setAll(lista);
    }

    private void mostrarErro(String m) { Alert a = new Alert(Alert.AlertType.ERROR); a.setTitle("Erro"); a.setHeaderText(null); a.setContentText(m); a.showAndWait(); }
    private void mostrarInfo(String m)  { Alert a = new Alert(Alert.AlertType.INFORMATION); a.setTitle("Informação"); a.setHeaderText(null); a.setContentText(m); a.showAndWait(); }
}
