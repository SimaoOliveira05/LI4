package pt.trasmum.loja.apresentacao.controllers;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import pt.trasmum.loja.apresentacao.DialogoUtil;
import pt.trasmum.loja.app.AppContext;
import pt.trasmum.loja.dominio.catalogo.Produto;
import pt.trasmum.loja.dominio.core.Utilizador;
import pt.trasmum.loja.dominio.fornecedores.*;
import pt.trasmum.loja.dominio.fornecedores.PedidoRemessa.EstadoPedido;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class RemessaController {

    // ── Painel de gestão (lista + detalhe lateral) ─────────────────
    @FXML private HBox  painelGestao;
    @FXML private TableView<PedidoRemessa>            tblPedidos;
    @FXML private TableColumn<PedidoRemessa, Integer> colId;
    @FXML private TableColumn<PedidoRemessa, String>  colFornecedor;
    @FXML private TableColumn<PedidoRemessa, String>  colData;
    @FXML private TableColumn<PedidoRemessa, String>  colEstado;
    @FXML private TableColumn<PedidoRemessa, Integer> colNrLinhas;
    @FXML private ComboBox<String> cmbFiltro;

    // Detalhe
    @FXML private VBox  painelDetalhe;
    @FXML private Label lblDetFornecedor;
    @FXML private Label lblDetData;
    @FXML private Label lblDetEstado;
    @FXML private TableView<LinhaPedidoRemessa>            tblLinhasDetalhe;
    @FXML private TableColumn<LinhaPedidoRemessa, String>  colDetProduto;
    @FXML private TableColumn<LinhaPedidoRemessa, Integer> colDetQtd;
    @FXML private Button btnRegistarChegada;

    // ── Painel de novo pedido ──────────────────────────────────────
    @FXML private HBox  painelNovoPedido;
    @FXML private TextField txtFiltroNP;
    @FXML private FlowPane  painelProdutosNP;
    @FXML private ComboBox<Fornecedor> cmbFornecedorNP;
    @FXML private TextField txtQtdNP;
    @FXML private TableView<LinhaNPExibicao>            tblLinhasNP;
    @FXML private TableColumn<LinhaNPExibicao, String>  colNPProduto;
    @FXML private TableColumn<LinhaNPExibicao, Integer> colNPQtd;
    @FXML private TableColumn<LinhaNPExibicao, String>  colNPRemover;

    private final ObservableList<PedidoRemessa>   pedidos  = FXCollections.observableArrayList();
    private final ObservableList<LinhaNPExibicao> linhasNP = FXCollections.observableArrayList();
    private List<Produto> produtosFornecedor = new ArrayList<>();

    // ── Classe de apresentação para linhas do novo pedido ─────────
    static final class LinhaNPExibicao {
        final int    idProduto;
        final String nome;
        int          quantidade;

        LinhaNPExibicao(int idProduto, String nome, int quantidade) {
            this.idProduto  = idProduto;
            this.nome       = nome;
            this.quantidade = quantidade;
        }
    }

    // ── Inicialização ─────────────────────────────────────────────

    @FXML
    public void initialize() {
        // Tabela de pedidos
        colId.setCellValueFactory(c ->
                new SimpleIntegerProperty(c.getValue().getId()).asObject());
        colFornecedor.setCellValueFactory(c -> {
            Fornecedor f = AppContext.getInstance().fornecedorRepo.buscarPorId(c.getValue().getIdFornecedor());
            return new SimpleStringProperty(f != null ? f.getNome() : "—");
        });
        colData.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getDataCriacao().toString()));
        colEstado.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getEstado().name()));
        colNrLinhas.setCellValueFactory(c ->
                new SimpleIntegerProperty(c.getValue().getLinhas().size()).asObject());
        tblPedidos.setItems(pedidos);

        // Selecção → painel de detalhe
        tblPedidos.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
            if (sel == null) {
                painelDetalhe.setVisible(false);
                painelDetalhe.setManaged(false);
            } else {
                mostrarDetalhe(sel);
            }
        });

        // Tabela de detalhe
        colDetProduto.setCellValueFactory(c -> {
            Produto p = AppContext.getInstance().produtoRepo.buscarPorId(c.getValue().getIdProduto());
            return new SimpleStringProperty(p != null ? p.getNome() : "—");
        });
        colDetQtd.setCellValueFactory(c ->
                new SimpleIntegerProperty(c.getValue().getQuantidadePretendida()).asObject());

        // Filtro
        cmbFiltro.setItems(FXCollections.observableArrayList("TODOS", "PENDENTE", "CONCLUIDO"));
        cmbFiltro.setValue("TODOS");

        // Tabela de novo pedido
        colNPProduto.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().nome));
        colNPQtd.setCellValueFactory(c ->
                new SimpleIntegerProperty(c.getValue().quantidade).asObject());
        colNPRemover.setCellFactory(tc -> new TableCell<>() {
            private final Button btn = new Button("✕");
            {
                btn.setOnAction(e -> {
                    LinhaNPExibicao item = getTableView().getItems().get(getIndex());
                    linhasNP.remove(item);
                });
                btn.setStyle("-fx-font-size: 10px; -fx-padding: 2 6 2 6;");
                btn.getStyleClass().add("btn-danger");
            }
            @Override protected void updateItem(String s, boolean empty) {
                super.updateItem(s, empty);
                setGraphic(empty ? null : btn);
            }
        });
        tblLinhasNP.setItems(linhasNP);

        // Fornecedores para novo pedido
        List<Fornecedor> fornecedores = AppContext.getInstance().fornecedorRepo.listarTodos();
        cmbFornecedorNP.setItems(FXCollections.observableArrayList(fornecedores));
        cmbFornecedorNP.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(Fornecedor f, boolean empty) {
                super.updateItem(f, empty); setText(empty || f == null ? null : f.getNome());
            }
        });
        cmbFornecedorNP.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(Fornecedor f, boolean empty) {
                super.updateItem(f, empty); setText(empty || f == null ? null : f.getNome());
            }
        });
        cmbFornecedorNP.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
            linhasNP.clear();
            if (sel == null) {
                produtosFornecedor = new ArrayList<>();
            } else {
                produtosFornecedor = AppContext.getInstance().fornecedorServico.listarProdutosFornecidos(sel.getId());
            }
            atualizarPainelProdutosNP(txtFiltroNP.getText() == null ? "" : txtFiltroNP.getText().trim());
        });

        try { carregarPedidos(); } catch (Exception e) { mostrarErro("Erro ao carregar pedidos: " + e.getMessage()); }
    }

    // ── Lista de pedidos ──────────────────────────────────────────

    @FXML
    public void onFiltrar() { carregarPedidos(); }

    private void carregarPedidos() {
        String filtro = cmbFiltro.getValue();
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
        painelDetalhe.setVisible(false);
        painelDetalhe.setManaged(false);
    }

    // ── Painel de detalhe ─────────────────────────────────────────

    private void mostrarDetalhe(PedidoRemessa pedido) {
        Fornecedor f = AppContext.getInstance().fornecedorRepo.buscarPorId(pedido.getIdFornecedor());
        lblDetFornecedor.setText(f != null ? f.getNome() : "—");
        lblDetData.setText(pedido.getDataCriacao().toString());
        lblDetEstado.setText(pedido.getEstado().name());
        tblLinhasDetalhe.setItems(FXCollections.observableArrayList(pedido.getLinhas()));

        boolean pendente = pedido.getEstado() == EstadoPedido.PENDENTE;
        btnRegistarChegada.setVisible(pendente);
        btnRegistarChegada.setManaged(pendente);

        painelDetalhe.setVisible(true);
        painelDetalhe.setManaged(true);
    }

    // ── Registar chegada ──────────────────────────────────────────

    @FXML
    public void onRegistarChegada() {
        PedidoRemessa pedido = tblPedidos.getSelectionModel().getSelectedItem();
        if (pedido == null || pedido.getEstado() != EstadoPedido.PENDENTE) return;

        Dialog<Boolean> dlg = DialogoUtil.comOwner(new Dialog<>());
        dlg.setTitle("Registar Chegada");
        dlg.setHeaderText("Confirme as quantidades e validades recebidas.");
        dlg.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(12); grid.setVgap(8);
        grid.setPadding(new Insets(12));

        // Cabeçalho
        grid.add(boldLabel("Produto"),        0, 0);
        grid.add(boldLabel("Qtd. pedida"),    1, 0);
        grid.add(boldLabel("Qtd. recebida"),  2, 0);
        grid.add(boldLabel("Validade"),       3, 0);

        List<TextField>  camposQtd      = new ArrayList<>();
        List<DatePicker> camposValidade = new ArrayList<>();
        List<Integer>    idsProduto     = new ArrayList<>();

        int row = 1;
        for (LinhaPedidoRemessa linha : pedido.getLinhas()) {
            Produto p = AppContext.getInstance().produtoRepo.buscarPorId(linha.getIdProduto());
            idsProduto.add(linha.getIdProduto());

            TextField tfQtd = new TextField(String.valueOf(linha.getQuantidadePretendida()));
            tfQtd.setPrefWidth(70);
            DatePicker dp = new DatePicker(LocalDate.now().plusMonths(6));
            dp.setPrefWidth(150);

            grid.add(new Label(p != null ? p.getNome() : "—"),                        0, row);
            grid.add(new Label(String.valueOf(linha.getQuantidadePretendida())),        1, row);
            grid.add(tfQtd,                                                             2, row);
            grid.add(dp,                                                                3, row);

            camposQtd.add(tfQtd);
            camposValidade.add(dp);
            row++;
        }

        grid.add(boldLabel("Valor da guia (€):"), 0, row);
        TextField tfGuia = new TextField();
        tfGuia.setPrefWidth(100);
        grid.add(tfGuia, 1, row);

        Label lblSugerido = new Label("Sugerido: —");
        lblSugerido.setStyle("-fx-font-style: italic; -fx-text-fill: #555;");
        grid.add(lblSugerido, 2, row, 2, 1);

        Runnable atualizarSugerido = () -> {
            double total = 0.0;
            boolean algumDesconhecido = false;
            for (int i = 0; i < camposQtd.size(); i++) {
                int q;
                try { q = Integer.parseInt(camposQtd.get(i).getText().trim()); }
                catch (NumberFormatException ex) { continue; }
                if (q <= 0) continue;
                double preco = AppContext.getInstance().fornecedorServico
                        .precoDoFornecedor(pedido.getIdFornecedor(), idsProduto.get(i));
                if (preco < 0) algumDesconhecido = true;
                else total += preco * q;
            }
            String txt = String.format("Sugerido: %.2f €", total);
            if (algumDesconhecido) txt += " (+ preços em falta)";
            lblSugerido.setText(txt);
        };
        for (TextField tf : camposQtd) {
            tf.textProperty().addListener((o, a, b) -> atualizarSugerido.run());
        }
        atualizarSugerido.run();

        ScrollPane scroll = new ScrollPane(grid);
        scroll.setFitToWidth(true);
        scroll.setPrefHeight(300);
        dlg.getDialogPane().setContent(scroll);

        // Validação antes de confirmar
        Button btnOk = (Button) dlg.getDialogPane().lookupButton(ButtonType.OK);
        btnOk.addEventFilter(javafx.event.ActionEvent.ACTION, ev -> {
            for (int i = 0; i < camposQtd.size(); i++) {
                try {
                    int q = Integer.parseInt(camposQtd.get(i).getText().trim());
                    if (q < 0) throw new NumberFormatException();
                } catch (NumberFormatException ex) {
                    mostrarErro("Quantidade inválida na linha " + (i + 1));
                    ev.consume(); return;
                }
                if (camposValidade.get(i).getValue() == null) {
                    mostrarErro("Selecione a validade na linha " + (i + 1));
                    ev.consume(); return;
                }
            }
            try { Double.parseDouble(tfGuia.getText().replace(",", ".")); }
            catch (NumberFormatException ex) { mostrarErro("Valor de guia inválido."); ev.consume(); }
        });

        dlg.setResultConverter(bt -> bt == ButtonType.OK);

        dlg.showAndWait().ifPresent(ok -> {
            if (!ok) return;
            try {
                List<LinhaRemessa> linhas = new ArrayList<>();
                for (int i = 0; i < idsProduto.size(); i++) {
                    int qtd = Integer.parseInt(camposQtd.get(i).getText().trim());
                    if (qtd > 0) {
                        linhas.add(new LinhaRemessa(0, idsProduto.get(i), qtd, camposValidade.get(i).getValue()));
                    }
                }
                if (linhas.isEmpty()) { mostrarErro("Nenhuma quantidade recebida."); return; }
                double valorGuia = Double.parseDouble(tfGuia.getText().replace(",", "."));
                Fornecedor forn  = AppContext.getInstance().fornecedorRepo.buscarPorId(pedido.getIdFornecedor());
                Utilizador u     = AppContext.getInstance().getUtilizadorAtual();
                AppContext.getInstance().remessaServico.registarRemessa(u, forn, linhas, valorGuia);
                mostrarInfo("Remessa registada com sucesso.");
                carregarPedidos();
            } catch (Exception ex) { mostrarErro(ex.getMessage()); }
        });
    }

    // ── Novo pedido ───────────────────────────────────────────────

    @FXML
    public void onIniciarNovoPedido() {
        linhasNP.clear();
        txtFiltroNP.clear();
        txtQtdNP.setText("1");
        // Recarrega fornecedores (pode ter havido alterações no catálogo)
        List<Fornecedor> fornecedores = AppContext.getInstance().fornecedorRepo.listarTodos();
        cmbFornecedorNP.setItems(FXCollections.observableArrayList(fornecedores));
        cmbFornecedorNP.setValue(null);
        produtosFornecedor = new ArrayList<>();
        atualizarPainelProdutosNP("");
        painelGestao.setVisible(false);
        painelGestao.setManaged(false);
        painelNovoPedido.setVisible(true);
        painelNovoPedido.setManaged(true);
    }

    @FXML
    public void onCancelarNovoPedido() {
        painelNovoPedido.setVisible(false);
        painelNovoPedido.setManaged(false);
        painelGestao.setVisible(true);
        painelGestao.setManaged(true);
    }

    @FXML
    public void onFiltrarNP() {
        atualizarPainelProdutosNP(txtFiltroNP.getText().trim());
    }

    private void atualizarPainelProdutosNP(String filtro) {
        painelProdutosNP.getChildren().clear();
        if (cmbFornecedorNP.getValue() == null) {
            Label hint = new Label("Selecione primeiro um fornecedor.");
            hint.setStyle("-fx-font-style: italic; -fx-text-fill: #777;");
            painelProdutosNP.getChildren().add(hint);
            return;
        }
        String f = filtro.toLowerCase();
        produtosFornecedor.stream()
                .filter(p -> f.isBlank()
                        || p.getNome().toLowerCase().contains(f)
                        || p.getCodigoBarras().toLowerCase().contains(f)
                        || p.getCategoria().toLowerCase().contains(f))
                .forEach(p -> painelProdutosNP.getChildren().add(criarBotaoProdutoNP(p)));
    }

    private Button criarBotaoProdutoNP(Produto produto) {
        Button btn = new Button(produto.getNome());
        btn.getStyleClass().add("produto-btn");
        btn.setOnAction(e -> adicionarLinhaOrdem(produto));
        return btn;
    }

    private void adicionarLinhaOrdem(Produto produto) {
        int qtd;
        try {
            qtd = Integer.parseInt(txtQtdNP.getText().trim());
            if (qtd <= 0) { mostrarErro("Quantidade deve ser > 0."); return; }
        } catch (NumberFormatException e) { mostrarErro("Quantidade inválida."); return; }

        LinhaNPExibicao existente = linhasNP.stream()
                .filter(l -> l.idProduto == produto.getId())
                .findFirst().orElse(null);
        if (existente != null) {
            existente.quantidade += qtd;
            tblLinhasNP.refresh();
        } else {
            linhasNP.add(new LinhaNPExibicao(produto.getId(), produto.getNome(), qtd));
        }
        txtQtdNP.setText("1");
    }

    @FXML
    public void onFinalizarNovoPedido() {
        Fornecedor forn = cmbFornecedorNP.getValue();
        if (forn == null)        { mostrarErro("Selecione um fornecedor."); return; }
        if (linhasNP.isEmpty())  { mostrarErro("Adicione pelo menos um produto."); return; }

        List<LinhaPedidoRemessa> linhas = linhasNP.stream()
                .map(l -> new LinhaPedidoRemessa(0, l.idProduto, l.quantidade))
                .toList();

        Utilizador u = AppContext.getInstance().getUtilizadorAtual();
        try {
            AppContext.getInstance().remessaServico.criarPedidoRemessa(u, forn, linhas);
            mostrarInfo("Pedido de remessa criado com sucesso.");
            onCancelarNovoPedido();
            carregarPedidos();
        } catch (Exception e) { mostrarErro(e.getMessage()); }
    }

    // ── Auxiliares ────────────────────────────────────────────────

    private static Label boldLabel(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-font-weight: bold;");
        return l;
    }

    private void mostrarErro(String m) { DialogoUtil.erro(m); }
    private void mostrarInfo(String m) { DialogoUtil.info(m); }
}
