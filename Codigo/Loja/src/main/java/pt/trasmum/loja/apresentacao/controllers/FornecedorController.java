package pt.trasmum.loja.apresentacao.controllers;

import javafx.beans.property.SimpleDoubleProperty;
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
import pt.trasmum.loja.apresentacao.DialogoUtil;
import pt.trasmum.loja.app.AppContext;
import pt.trasmum.loja.dominio.catalogo.Produto;
import pt.trasmum.loja.dominio.core.Utilizador;
import pt.trasmum.loja.dominio.fornecedores.Fornecedor;
import pt.trasmum.loja.dominio.fornecedores.FornecedorProduto;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class FornecedorController {

    @FXML private TableView<Fornecedor> tblFornecedores;
    @FXML private TableColumn<Fornecedor, Integer> colFId;
    @FXML private TableColumn<Fornecedor, String>  colFNome;
    @FXML private TableColumn<Fornecedor, String>  colFNif;
    @FXML private TableColumn<Fornecedor, String>  colFEmail;
    @FXML private TableColumn<Fornecedor, String>  colFTel;

    @FXML private VBox painelCatalogo;
    @FXML private Label lblCatalogoTitulo;
    @FXML private ComboBox<Produto> cmbProdutoNovo;
    @FXML private TextField txtPreco;
    @FXML private TableView<FornecedorProduto> tblCatalogo;
    @FXML private TableColumn<FornecedorProduto, String>  colCatProduto;
    @FXML private TableColumn<FornecedorProduto, Double>  colCatPreco;
    @FXML private TableColumn<FornecedorProduto, Void>    colCatAcoes;

    private final ObservableList<Fornecedor>        fornecedores = FXCollections.observableArrayList();
    private final ObservableList<FornecedorProduto> catalogo     = FXCollections.observableArrayList();
    private Map<Integer, Produto> produtosById = new HashMap<>();

    @FXML
    public void initialize() {
        colFId.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getId()).asObject());
        colFNome.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNome()));
        colFNif.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNif()));
        colFEmail.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getEmail() == null ? "" : c.getValue().getEmail()));
        colFTel.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTelefone() == null ? "" : c.getValue().getTelefone()));
        tblFornecedores.setItems(fornecedores);

        tblFornecedores.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
            if (sel == null) {
                painelCatalogo.setVisible(false);
                painelCatalogo.setManaged(false);
            } else {
                mostrarCatalogo(sel);
            }
        });

        colCatProduto.setCellValueFactory(c -> {
            Produto p = produtosById.get(c.getValue().getIdProduto());
            return new SimpleStringProperty(p != null ? p.getNome() : "—");
        });
        colCatPreco.setCellValueFactory(c -> new SimpleDoubleProperty(c.getValue().getPrecoFornecedor()).asObject());
        colCatAcoes.setCellFactory(tc -> new TableCell<>() {
            private final Button btnEditar = new Button("Preço");
            private final Button btnRemover = new Button("✕");
            private final HBox box = new HBox(4, btnEditar, btnRemover);
            {
                btnEditar.getStyleClass().add("btn-secondary");
                btnRemover.getStyleClass().add("btn-danger");
                btnEditar.setStyle("-fx-font-size: 10px; -fx-padding: 2 6 2 6;");
                btnRemover.setStyle("-fx-font-size: 10px; -fx-padding: 2 6 2 6;");
                btnEditar.setOnAction(e -> onAtualizarPreco(getTableView().getItems().get(getIndex())));
                btnRemover.setOnAction(e -> onDesassociar(getTableView().getItems().get(getIndex())));
            }
            @Override protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                setGraphic(empty ? null : box);
            }
        });
        tblCatalogo.setItems(catalogo);

        cmbProdutoNovo.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(Produto p, boolean empty) {
                super.updateItem(p, empty); setText(empty || p == null ? null : p.getNome());
            }
        });
        cmbProdutoNovo.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(Produto p, boolean empty) {
                super.updateItem(p, empty); setText(empty || p == null ? null : p.getNome());
            }
        });

        carregarFornecedores();
    }

    private void carregarFornecedores() {
        fornecedores.setAll(AppContext.getInstance().fornecedorServico.listarTodos());
        List<Produto> prods = AppContext.getInstance().produtoRepo.listarAtivos();
        produtosById.clear();
        for (Produto p : prods) produtosById.put(p.getId(), p);
    }

    private void mostrarCatalogo(Fornecedor f) {
        lblCatalogoTitulo.setText("Catálogo de " + f.getNome());
        catalogo.setAll(AppContext.getInstance().fornecedorServico.listarCatalogoDetalhado(f.getId()));
        atualizarComboProdutosDisponiveis(f);
        painelCatalogo.setVisible(true);
        painelCatalogo.setManaged(true);
    }

    private void atualizarComboProdutosDisponiveis(Fornecedor f) {
        List<Produto> todos = AppContext.getInstance().produtoRepo.listarAtivos();
        ObservableList<Produto> disponiveis = FXCollections.observableArrayList();
        for (Produto p : todos) {
            boolean jaAssociado = catalogo.stream().anyMatch(fp -> fp.getIdProduto() == p.getId());
            if (!jaAssociado) disponiveis.add(p);
        }
        cmbProdutoNovo.setItems(disponiveis);
        cmbProdutoNovo.setValue(null);
        txtPreco.clear();
    }

    @FXML
    public void onCriarFornecedor() {
        Fornecedor f = new Fornecedor();
        Optional<Fornecedor> result = editarDialogo("Novo Fornecedor", f);
        result.ifPresent(novo -> {
            try {
                Utilizador u = AppContext.getInstance().getUtilizadorAtual();
                AppContext.getInstance().fornecedorServico.criarFornecedor(u, novo);
                carregarFornecedores();
                mostrarInfo("Fornecedor criado.");
            } catch (Exception e) { mostrarErro(e.getMessage()); }
        });
    }

    @FXML
    public void onEditarFornecedor() {
        Fornecedor sel = tblFornecedores.getSelectionModel().getSelectedItem();
        if (sel == null) { mostrarErro("Selecione um fornecedor."); return; }
        Optional<Fornecedor> result = editarDialogo("Editar Fornecedor", sel);
        result.ifPresent(alterado -> {
            try {
                Utilizador u = AppContext.getInstance().getUtilizadorAtual();
                AppContext.getInstance().fornecedorServico.editarFornecedor(u, alterado);
                carregarFornecedores();
                mostrarInfo("Fornecedor atualizado.");
            } catch (Exception e) { mostrarErro(e.getMessage()); }
        });
    }

    @FXML
    public void onAssociarProduto() {
        Fornecedor forn = tblFornecedores.getSelectionModel().getSelectedItem();
        if (forn == null) { mostrarErro("Selecione um fornecedor."); return; }
        Produto p = cmbProdutoNovo.getValue();
        if (p == null)   { mostrarErro("Escolha um produto."); return; }
        double preco;
        try { preco = Double.parseDouble(txtPreco.getText().replace(",", ".")); }
        catch (NumberFormatException e) { mostrarErro("Preço inválido."); return; }
        if (preco < 0) { mostrarErro("Preço não pode ser negativo."); return; }
        try {
            Utilizador u = AppContext.getInstance().getUtilizadorAtual();
            AppContext.getInstance().fornecedorServico.associarProduto(u, forn.getId(), p.getId(), preco);
            mostrarCatalogo(forn);
        } catch (Exception e) { mostrarErro(e.getMessage()); }
    }

    private void onAtualizarPreco(FornecedorProduto fp) {
        Fornecedor forn = tblFornecedores.getSelectionModel().getSelectedItem();
        if (forn == null) return;
        TextInputDialog d = DialogoUtil.comOwner(new TextInputDialog(String.valueOf(fp.getPrecoFornecedor())));
        d.setTitle("Atualizar Preço");
        d.setHeaderText(null);
        Produto p = produtosById.get(fp.getIdProduto());
        d.setContentText("Novo preço para " + (p != null ? p.getNome() : "#" + fp.getIdProduto()) + ":");
        d.showAndWait().ifPresent(txt -> {
            try {
                double novo = Double.parseDouble(txt.replace(",", "."));
                Utilizador u = AppContext.getInstance().getUtilizadorAtual();
                AppContext.getInstance().fornecedorServico.atualizarPrecoFornecedor(u, forn.getId(), fp.getIdProduto(), novo);
                mostrarCatalogo(forn);
            } catch (NumberFormatException e) { mostrarErro("Preço inválido."); }
            catch (Exception e) { mostrarErro(e.getMessage()); }
        });
    }

    private void onDesassociar(FornecedorProduto fp) {
        Fornecedor forn = tblFornecedores.getSelectionModel().getSelectedItem();
        if (forn == null) return;
        DialogoUtil.confirmar("Remover produto do catálogo do fornecedor?").ifPresent(bt -> {
            if (bt != ButtonType.YES) return;
            try {
                Utilizador u = AppContext.getInstance().getUtilizadorAtual();
                AppContext.getInstance().fornecedorServico.desassociarProduto(u, forn.getId(), fp.getIdProduto());
                mostrarCatalogo(forn);
            } catch (Exception e) { mostrarErro(e.getMessage()); }
        });
    }

    private Optional<Fornecedor> editarDialogo(String titulo, Fornecedor base) {
        Dialog<Fornecedor> dlg = DialogoUtil.comOwner(new Dialog<>());
        dlg.setTitle(titulo);
        dlg.setHeaderText(null);
        dlg.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextField tfNome = new TextField(base.getNome() == null ? "" : base.getNome());
        TextField tfNif  = new TextField(base.getNif()  == null ? "" : base.getNif());
        TextField tfMor  = new TextField(base.getMorada()   == null ? "" : base.getMorada());
        TextField tfTel  = new TextField(base.getTelefone() == null ? "" : base.getTelefone());
        TextField tfMail = new TextField(base.getEmail()    == null ? "" : base.getEmail());
        TextField tfIban = new TextField(base.getIban()     == null ? "" : base.getIban());

        GridPane g = new GridPane();
        g.setHgap(8); g.setVgap(6); g.setPadding(new Insets(12));
        int r = 0;
        g.add(new Label("Nome*:"),     0, r); g.add(tfNome, 1, r++);
        g.add(new Label("NIF*:"),      0, r); g.add(tfNif,  1, r++);
        g.add(new Label("Morada:"),    0, r); g.add(tfMor,  1, r++);
        g.add(new Label("Telefone:"),  0, r); g.add(tfTel,  1, r++);
        g.add(new Label("Email:"),     0, r); g.add(tfMail, 1, r++);
        g.add(new Label("IBAN:"),      0, r); g.add(tfIban, 1, r++);
        dlg.getDialogPane().setContent(g);

        Button ok = (Button) dlg.getDialogPane().lookupButton(ButtonType.OK);
        ok.addEventFilter(javafx.event.ActionEvent.ACTION, ev -> {
            if (tfNome.getText().isBlank() || tfNif.getText().isBlank()) {
                mostrarErro("Nome e NIF são obrigatórios."); ev.consume();
            }
        });

        dlg.setResultConverter(bt -> {
            if (bt != ButtonType.OK) return null;
            base.setNome(tfNome.getText().trim());
            base.setNif(tfNif.getText().trim());
            base.setMorada(tfMor.getText().trim());
            base.setTelefone(tfTel.getText().trim());
            base.setEmail(tfMail.getText().trim());
            base.setIban(tfIban.getText().trim());
            return base;
        });
        return dlg.showAndWait();
    }

    private void mostrarErro(String m) { DialogoUtil.erro(m); }
    private void mostrarInfo(String m) { DialogoUtil.info(m); }
}
