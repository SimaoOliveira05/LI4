package pt.trasmum.loja.apresentacao.controllers;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import pt.trasmum.loja.apresentacao.DialogoUtil;
import pt.trasmum.loja.app.AppContext;
import pt.trasmum.loja.dominio.catalogo.Lote;
import pt.trasmum.loja.dominio.catalogo.Produto;
import pt.trasmum.loja.dominio.core.Utilizador;
import pt.trasmum.loja.servico.ProdutoDTO;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class CatalogoController {

    @FXML private TextField txtPesquisa;
    @FXML private ComboBox<String> cmbFiltroCategoria;
    @FXML private TableView<Produto> tblProdutos;
    @FXML private TableColumn<Produto, String>  colCodigo;
    @FXML private TableColumn<Produto, String>  colNome;
    @FXML private TableColumn<Produto, String>  colCategoria;
    @FXML private TableColumn<Produto, Double>  colPreco;
    @FXML private TableColumn<Produto, Integer> colStock;
    @FXML private TableColumn<Produto, Boolean> colAtivo;
    @FXML private TextArea txtAlertas;

    @FXML private VBox detalhesSection;
    @FXML private TableView<Lote> tblLotes;
    @FXML private TableColumn<Lote, Number> colLoteId;
    @FXML private TableColumn<Lote, Number> colLoteQtd;
    @FXML private TableColumn<Lote, String> colLoteValidade;
    @FXML private TableColumn<Lote, String> colLotePreco;
    @FXML private TableColumn<Lote, String> colLoteDesconto;
    @FXML private TableColumn<Lote, Void>   colLoteAbater;

    private final ObservableList<Produto> produtos = FXCollections.observableArrayList();
    private Map<Integer, Integer> stockTotais = new HashMap<>();

    @FXML
    public void initialize() {
        colCodigo.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCodigoBarras()));
        colNome.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNome()));
        colCategoria.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCategoria()));
        colPreco.setCellValueFactory(c -> new SimpleDoubleProperty(c.getValue().getPrecoBase()).asObject());
        colStock.setCellValueFactory(c -> new SimpleIntegerProperty(
                stockTotais.getOrDefault(c.getValue().getId(), 0)).asObject());
        colAtivo.setCellValueFactory(c -> new SimpleBooleanProperty(c.getValue().isAtivo()).asObject());
        colLoteId.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getId()));
        colLoteQtd.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getQuantidade()));
        colLoteValidade.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getDataValidade() != null ? c.getValue().getDataValidade().toString() : "-"));
        colLotePreco.setCellValueFactory(c -> new SimpleStringProperty(
                String.format("%.2f €", c.getValue().getPrecoFinal())));
        colLoteDesconto.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().temDesconto() ? String.format("%.1f%%", c.getValue().getDesconto().getPercentagem()) : "—"));
        colLoteAbater.setCellFactory(tc -> new TableCell<>() {
            private final Button btn = new Button("Abater");
            {
                btn.getStyleClass().add("btn-danger");
                btn.setStyle("-fx-font-size: 10px; -fx-padding: 2 6 2 6;");
                btn.setOnAction(e -> onAbaterLote(getTableView().getItems().get(getIndex())));
            }
            @Override protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                setGraphic(empty ? null : btn);
            }
        });

        tblProdutos.setItems(produtos);
        tblProdutos.getSelectionModel().selectedItemProperty().addListener(
                (obs, old, novo) -> atualizarDetalhes(novo));
        
        carregarProdutos();
        inicializarFiltroCategorias();
        carregarAlertas();
    }

    private void inicializarFiltroCategorias() {
        // Extrair categorias distintas de todos os produtos ativos
        List<String> categorias = AppContext.getInstance().produtoRepo.listarAtivos().stream()
                .map(Produto::getCategoria)
                .filter(c -> c != null && !c.isBlank())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
        cmbFiltroCategoria.getItems().clear();
        cmbFiltroCategoria.getItems().add(0, "Todas");
        cmbFiltroCategoria.getItems().addAll(categorias);
        cmbFiltroCategoria.setValue("Todas");
    }

    @FXML
    public void onFiltrarCategoria() {
        onPesquisar();
    }

    @FXML
    public void onPesquisar() {
        String q = txtPesquisa.getText().trim();
        String categoriaSelecionada = cmbFiltroCategoria.getValue();
        
        List<Produto> todos = q.isBlank()
                ? AppContext.getInstance().produtoRepo.listarAtivos()
                : AppContext.getInstance().catalogoServico.pesquisarProduto(q);
        
        List<Produto> resultado = todos.stream()
                .filter(p -> categoriaSelecionada == null || categoriaSelecionada.equals("Todas") 
                        || p.getCategoria().equals(categoriaSelecionada))
                .collect(Collectors.toList());
        
        produtos.setAll(resultado);
    }

    @FXML
    public void onCriarProduto() {
        Utilizador u = AppContext.getInstance().getUtilizadorAtual();
        Optional<ProdutoDTO> dto = mostrarDialogoProduto(null);
        dto.ifPresent(d -> {
            try {
                AppContext.getInstance().catalogoServico.criarProduto(u, d);
                carregarProdutos();
                mostrarInfo("Produto criado com sucesso.");
            } catch (Exception e) { mostrarErro(e.getMessage()); }
        });
    }

    @FXML
    public void onEditarProduto() {
        Produto sel = tblProdutos.getSelectionModel().getSelectedItem();
        if (sel == null) { mostrarErro("Selecione um produto."); return; }
        Utilizador u = AppContext.getInstance().getUtilizadorAtual();
        Optional<ProdutoDTO> dto = mostrarDialogoProduto(sel);
        dto.ifPresent(d -> {
            try {
                AppContext.getInstance().catalogoServico.editarProduto(u, sel.getId(), d);
                carregarProdutos();
                mostrarInfo("Produto atualizado.");
            } catch (Exception e) { mostrarErro(e.getMessage()); }
        });
    }

    @FXML
    public void onDesativarProduto() {
        Produto sel = tblProdutos.getSelectionModel().getSelectedItem();
        if (sel == null) { mostrarErro("Selecione um produto."); return; }
        Utilizador u = AppContext.getInstance().getUtilizadorAtual();
        try {
            AppContext.getInstance().catalogoServico.desativarProduto(u, sel.getId());
            carregarProdutos();
            mostrarInfo("Produto desativado.");
        } catch (Exception e) { mostrarErro(e.getMessage()); }
    }

    @FXML
    public void onAplicarDesconto() {
        Produto sel = tblProdutos.getSelectionModel().getSelectedItem();
        if (sel == null) { mostrarErro("Selecione um produto."); return; }

        // Pede idLote e percentagem
        TextInputDialog dlg = DialogoUtil.comOwner(new TextInputDialog("10"));
        dlg.setTitle("Aplicar Desconto");
        dlg.setHeaderText("Percentagem de desconto para o lote mais antigo de: " + sel.getNome());
        dlg.setContentText("Percentagem (%):");
        dlg.showAndWait().ifPresent(pctStr -> {
            try {
                double pct = Double.parseDouble(pctStr.replace(",", "."));
                List<Lote> lotes = AppContext.getInstance().loteRepo.buscarLotesFEFO(sel.getId());
                if (lotes.isEmpty()) { mostrarErro("Sem lotes disponíveis."); return; }
                Utilizador u = AppContext.getInstance().getUtilizadorAtual();
                AppContext.getInstance().catalogoServico.aplicarDesconto(u, lotes.get(0).getId(), pct);
                mostrarInfo("Desconto de " + pct + "% aplicado.");
                carregarAlertas();
            } catch (NumberFormatException e) {
                mostrarErro("Percentagem inválida.");
            } catch (Exception e) {
                mostrarErro(e.getMessage());
            }
        });
    }

    private void onAbaterLote(Lote lote) {
        Produto p = AppContext.getInstance().produtoRepo.buscarPorId(lote.getIdProduto());
        String nomeProduto = p != null ? p.getNome() : "Lote " + lote.getId();

        TextInputDialog dlg = DialogoUtil.comOwner(new TextInputDialog(String.valueOf(lote.getQuantidade())));
        dlg.setTitle("Abater Lote");
        dlg.setHeaderText(nomeProduto + " — val. " + (lote.getDataValidade() != null ? lote.getDataValidade() : "sem validade"));
        dlg.setContentText("Quantidade a abater (máx. " + lote.getQuantidade() + "):");
        dlg.showAndWait().ifPresent(txt -> {
            try {
                int qtd = Integer.parseInt(txt.trim());
                Utilizador u = AppContext.getInstance().getUtilizadorAtual();
                AppContext.getInstance().catalogoServico.abaterLote(u, lote.getId(), qtd);
                Produto sel = tblProdutos.getSelectionModel().getSelectedItem();
                carregarProdutos();
                if (sel != null) atualizarDetalhes(
                        AppContext.getInstance().produtoRepo.buscarPorId(sel.getId()));
                carregarAlertas();
                mostrarInfo("Abate registado: " + qtd + " un. do lote #" + lote.getId() + ".");
            } catch (NumberFormatException e) {
                mostrarErro("Quantidade inválida.");
            } catch (Exception e) {
                mostrarErro(e.getMessage());
            }
        });
    }

    private void atualizarDetalhes(Produto p) {
        if (p == null) {
            tblLotes.getItems().clear();
            detalhesSection.setVisible(false);
            detalhesSection.setManaged(false);
            return;
        }
        List<Lote> lotes = AppContext.getInstance().loteRepo.buscarLotesFEFO(p.getId());
        tblLotes.setItems(FXCollections.observableArrayList(lotes));
        detalhesSection.setVisible(true);
        detalhesSection.setManaged(true);
    }

    private void carregarProdutos() {
        stockTotais = AppContext.getInstance().loteRepo.stockPorProduto();
        produtos.setAll(AppContext.getInstance().produtoRepo.listarAtivos());
    }

    private void carregarAlertas() {
        StringBuilder sb = new StringBuilder();

        List<Produto> semStock = AppContext.getInstance().catalogoServico.gerarAlertasStockMinimo();
        if (!semStock.isEmpty()) {
            sb.append("⚠ Stock mínimo:\n");
            semStock.forEach(p -> sb.append("  • ").append(p.getNome()).append("\n"));
        }

        int dias = AppContext.getInstance().configuracao.getDiasAlertaValidade();
        List<Lote> janela = AppContext.getInstance().catalogoServico.gerarAlertasValidade(dias);

        LocalDate hoje = LocalDate.now();
        List<Lote> expirados = janela.stream().filter(l -> l.getDataValidade().isBefore(hoje)).toList();
        List<Lote> aVencer   = janela.stream().filter(l -> !l.getDataValidade().isBefore(hoje)).toList();

        if (!expirados.isEmpty()) {
            sb.append("\n🚨 Fora de validade (remover do stock):\n");
            expirados.forEach(l -> {
                Produto p = AppContext.getInstance().produtoRepo.buscarPorId(l.getIdProduto());
                String nome = p != null ? p.getNome() : "Lote " + l.getId();
                sb.append("  • ").append(nome)
                  .append(" — expirou ").append(l.getDataValidade())
                  .append(" (").append(l.getQuantidade()).append(" un.)\n");
            });
        }

        if (!aVencer.isEmpty()) {
            sb.append("\n⚠ A vencer (").append(dias).append(" dias):\n");
            aVencer.forEach(l -> {
                Produto p = AppContext.getInstance().produtoRepo.buscarPorId(l.getIdProduto());
                String nome = p != null ? p.getNome() : "Lote " + l.getId();
                sb.append("  • ").append(nome)
                  .append(" — val. ").append(l.getDataValidade())
                  .append(" (").append(l.getQuantidade()).append(" un.)\n");
            });
        }

        if (sb.isEmpty()) sb.append("Sem alertas activos.");
        txtAlertas.setText(sb.toString());
    }

    private Optional<ProdutoDTO> mostrarDialogoProduto(Produto existente) {
        Dialog<ProdutoDTO> dlg = DialogoUtil.comOwner(new Dialog<>());
        dlg.setTitle(existente == null ? "Criar Produto" : "Editar Produto");
        dlg.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextField fCodigo    = new TextField(existente != null ? existente.getCodigoBarras() : "");
        TextField fNome      = new TextField(existente != null ? existente.getNome() : "");
        TextField fCategoria = new TextField(existente != null ? existente.getCategoria() : "");
        TextField fPreco     = new TextField(existente != null ? String.valueOf(existente.getPrecoBase()) : "");
        TextField fStock     = new TextField(existente != null ? String.valueOf(existente.getStockMinimo()) : "0");
        CheckBox  fValidade  = new CheckBox("Tem data de validade");
        fValidade.setSelected(existente == null || existente.isTemValidade());

        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
        grid.setHgap(8); grid.setVgap(8);
        grid.add(new Label("Código de barras:"), 0, 0); grid.add(fCodigo, 1, 0);
        grid.add(new Label("Nome:"), 0, 1);             grid.add(fNome, 1, 1);
        grid.add(new Label("Categoria:"), 0, 2);        grid.add(fCategoria, 1, 2);
        grid.add(new Label("Preço base (€):"), 0, 3);   grid.add(fPreco, 1, 3);
        grid.add(new Label("Stock mínimo:"), 0, 4);     grid.add(fStock, 1, 4);
        grid.add(fValidade, 1, 5);
        dlg.getDialogPane().setContent(grid);

        dlg.setResultConverter(bt -> {
            if (bt == ButtonType.OK) {
                try {
                    return new ProdutoDTO(fCodigo.getText().trim(), fNome.getText().trim(),
                            fCategoria.getText().trim(),
                            Double.parseDouble(fPreco.getText().replace(",", ".")),
                            Integer.parseInt(fStock.getText().trim()),
                            fValidade.isSelected());
                } catch (NumberFormatException e) { return null; }
            }
            return null;
        });
        return dlg.showAndWait();
    }

    private void mostrarErro(String m) { DialogoUtil.erro(m); }
    private void mostrarInfo(String m) { DialogoUtil.info(m); }
}
