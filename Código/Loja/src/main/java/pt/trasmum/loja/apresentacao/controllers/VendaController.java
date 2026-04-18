package pt.trasmum.loja.apresentacao.controllers;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import pt.trasmum.loja.apresentacao.DialogoUtil;
import pt.trasmum.loja.app.AppContext;
import pt.trasmum.loja.apresentacao.GeradorFaturaPdf;
import pt.trasmum.loja.dominio.catalogo.Lote;
import pt.trasmum.loja.dominio.catalogo.Produto;
import pt.trasmum.loja.dominio.core.Utilizador;
import pt.trasmum.loja.dominio.tesouraria.SessaoCaixa;
import pt.trasmum.loja.dominio.vendas.*;
import pt.trasmum.loja.dominio.vendas.Venda.MetodoPagamento;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class VendaController {

    // ── Painel esquerdo ───────────────────────────────────────────────
    @FXML private TextField txtFiltro;
    @FXML private ComboBox<String> cmbFiltroCategoria;
    @FXML private FlowPane  painelProdutos;

    // ── Devoluções ────────────────────────────────────────────────────
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

    // ── Painel direito ────────────────────────────────────────────────
    @FXML private TextField txtCodigoBarras;
    @FXML private TextField txtQuantidade;
    @FXML private TableView<LinhaExibicao>         tblLinhas;
    @FXML private TableColumn<LinhaExibicao, String>  colLote;
    @FXML private TableColumn<LinhaExibicao, Integer> colQtd;
    @FXML private TableColumn<LinhaExibicao, Double>  colPreco;
    @FXML private TableColumn<LinhaExibicao, Double>  colSubtotal;
    @FXML private TableColumn<LinhaExibicao, Void>    colAcoes;
    @FXML private Label   lblTotal;
    @FXML private RadioButton rbNumerario;
    @FXML private RadioButton rbMultibanco;
    @FXML private TextField txtValorEntregue;
    @FXML private TextField txtNif;
    @FXML private Button  btnIniciar;
    @FXML private Button  btnAdicionar;
    @FXML private Button  btnFinalizar;
    @FXML private Button  btnAnular;

    private Venda vendaAtual;
    private final ObservableList<LinhaExibicao> linhasExibicao = FXCollections.observableArrayList();
    private List<Produto> todosProdutos;

    // ─────────────────────────────────────────────────────────────────
    // Classe de apresentação: agrega LinhaVenda com o mesmo produto E
    // mesmo preço unitário numa só linha visível na tabela.
    // Linhas do mesmo produto mas preços diferentes (e.g. lote em
    // desconto vs. lote a preço normal) ficam em linhas separadas.
    // ─────────────────────────────────────────────────────────────────
    static final class LinhaExibicao {
        final String nome;
        final int    quantidade;
        final double precoUnitario;
        final double subtotal;

        LinhaExibicao(String nome, int quantidade, double precoUnitario, double subtotal) {
            this.nome = nome;
            this.quantidade = quantidade;
            this.precoUnitario = precoUnitario;
            this.subtotal = subtotal;
        }
    }

    // ── Inicialização ─────────────────────────────────────────────────

    @FXML
    public void initialize() {
        colLote.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().nome));
        colQtd.setCellValueFactory(c ->
                new SimpleIntegerProperty(c.getValue().quantidade).asObject());
        colPreco.setCellValueFactory(c ->
                new SimpleDoubleProperty(c.getValue().precoUnitario).asObject());
        colSubtotal.setCellValueFactory(c ->
                new SimpleDoubleProperty(c.getValue().subtotal).asObject());
        colAcoes.setCellFactory(tc -> new TableCell<>() {
            private final Button btn = new Button("✕");
            {
                btn.getStyleClass().add("btn-danger");
                btn.setStyle("-fx-font-size: 10px; -fx-padding: 2 6 2 6;");
                btn.setOnAction(e -> removerLinhaExibicao(getTableView().getItems().get(getIndex())));
            }
            @Override protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                setGraphic(empty ? null : btn);
            }
        });
        tblLinhas.setItems(linhasExibicao);

        ToggleGroup tg = new ToggleGroup();
        rbNumerario.setToggleGroup(tg);
        rbMultibanco.setToggleGroup(tg);
        rbNumerario.setSelected(true);
        txtValorEntregue.disableProperty().bind(rbMultibanco.selectedProperty());

        // Devoluções
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

        todosProdutos = AppContext.getInstance().produtoRepo.listarAtivos();
        
        // Inicializar filtro de categorias
        List<String> categorias = todosProdutos.stream()
                .map(Produto::getCategoria)
                .filter(c -> c != null && !c.isBlank())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
        cmbFiltroCategoria.getItems().add(0, "Todas");
        cmbFiltroCategoria.getItems().addAll(categorias);
        cmbFiltroCategoria.setValue("Todas");
        
        atualizarPainelProdutos("");

        // Restaura venda em curso se o utilizador navegou para outra vista e voltou
        vendaAtual = AppContext.getInstance().vendaEmCurso;
        if (vendaAtual != null) {
            setCarrinhoAtivo(true);
            atualizarTabela();
        } else {
            setCarrinhoAtivo(false);
        }
    }

    // ── Painel de produtos / filtro ───────────────────────────────────

    @FXML
    public void onFiltrar() {
        atualizarPainelProdutos(txtFiltro.getText().trim());
    }

    @FXML
    public void onFiltrarCategoria() {
        atualizarPainelProdutos(txtFiltro.getText().trim());
    }

    private void atualizarPainelProdutos(String filtro) {
        painelProdutos.getChildren().clear();
        String f = filtro.toLowerCase();
        String categoriaSelecionada = cmbFiltroCategoria.getValue();
        
        todosProdutos.stream()
                .filter(p -> (categoriaSelecionada == null || categoriaSelecionada.equals("Todas") 
                        || p.getCategoria().equals(categoriaSelecionada)))
                .filter(p -> f.isBlank()
                        || p.getNome().toLowerCase().contains(f)
                        || p.getCodigoBarras().toLowerCase().contains(f)
                        || p.getCategoria().toLowerCase().contains(f))
                .forEach(p -> painelProdutos.getChildren().add(criarBotaoProduto(p)));
    }

    private Button criarBotaoProduto(Produto produto) {
        Button btn = new Button(produto.getNome() + "\n€ " + String.format("%.2f", produto.getPrecoBase()));
        btn.getStyleClass().add("produto-btn");
        btn.setOnAction(e -> onClicarProduto(produto));
        return btn;
    }

    private void onClicarProduto(Produto produto) {
        garantirVendaAtiva();
        if (vendaAtual == null) return;
        try {
            AppContext.getInstance().vendaServico.adicionarLinha(vendaAtual, produto.getCodigoBarras(), 1);
            atualizarTabela();
        } catch (Exception e) {
            mostrarErro(e.getMessage());
        }
    }

    // ── Venda ─────────────────────────────────────────────────────────

    @FXML
    public void onIniciarVenda() {
        try {
            Utilizador u = AppContext.getInstance().getUtilizadorAtual();
            vendaAtual = AppContext.getInstance().vendaServico.iniciarVenda(u);
            AppContext.getInstance().vendaEmCurso = vendaAtual;
            linhasExibicao.clear();
            lblTotal.setText("Total: 0.00 €");
            setCarrinhoAtivo(true);
        } catch (Exception e) {
            mostrarErro(e.getMessage());
        }
    }

    @FXML
    public void onAdicionarLinha() {
        garantirVendaAtiva();
        String codigo = txtCodigoBarras.getText().trim();
        if (codigo.isBlank()) return;
        try {
            int qtd = txtQuantidade.getText().isBlank() ? 1
                    : Integer.parseInt(txtQuantidade.getText().trim());
            AppContext.getInstance().vendaServico.adicionarLinha(vendaAtual, codigo, qtd);
            atualizarTabela();
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
            mostrarErro("Não há artigos na venda.");
            return;
        }
        MetodoPagamento metodo = rbNumerario.isSelected()
                ? MetodoPagamento.NUMERARIO : MetodoPagamento.MULTIBANCO;
        double total = vendaAtual.getLinhas().stream()
                .mapToDouble(LinhaVenda::calcularSubtotal).sum();
        double valorEntregue = 0;
        if (metodo == MetodoPagamento.NUMERARIO) {
            try {
                valorEntregue = Double.parseDouble(txtValorEntregue.getText().replace(",", "."));
            } catch (NumberFormatException e) {
                mostrarErro("Valor entregue inválido.");
                return;
            }
        }

        String nif = txtNif.getText().trim();
        vendaAtual.emitirFatura(nif.isBlank() ? null : nif);

        try {
            Fatura fatura = AppContext.getInstance().vendaServico
                    .finalizarVenda(vendaAtual, metodo, valorEntregue);

            double troco = metodo == MetodoPagamento.NUMERARIO
                    ? AppContext.getInstance().vendaServico.calcularTroco(total, valorEntregue)
                    : -1;

            Path pdfPath = null;
            try {
                pdfPath = new GeradorFaturaPdf(AppContext.getInstance().configuracao)
                        .gerar(fatura, vendaAtual, troco);
            } catch (Exception ignored) {}

            mostrarDialogoFatura(fatura, total, troco, pdfPath);
            verificarLimiteCaixa();
            limparVenda();
        } catch (Exception e) {
            mostrarErro(e.getMessage());
        }
    }

    @FXML
    public void onAnularVenda() {
        if (vendaAtual == null) return;
        DialogoUtil.confirmar("Anular venda em curso?").ifPresent(bt -> {
            if (bt == ButtonType.YES) {
                try { AppContext.getInstance().vendaServico.anularVenda(vendaAtual); }
                catch (Exception ignored) {}
                limparVenda();
            }
        });
    }

    // ── Tabela agregada ───────────────────────────────────────────────

    /**
     * Agrega as LinhaVenda por (produto, preço unitário) e actualiza a tabela.
     * O serviço pode gerar várias LinhaVenda para o mesmo produto por FEFO
     * (uma por lote consumido). Linhas com o mesmo produto E mesmo preço
     * colapsam numa única linha visual; linhas com preços diferentes (p.ex.
     * lote em desconto vs. lote a preço normal) ficam em linhas separadas.
     */
    private void atualizarTabela() {
        record Chave(String nome, double preco) {}
        // [0] = quantidade total, [1] = subtotal total
        Map<Chave, double[]> mapa = new LinkedHashMap<>();

        for (LinhaVenda l : vendaAtual.getLinhas()) {
            String nome  = resolverNomeProduto(l.getIdLote());
            double preco = round2(l.getPrecoUnitario());
            mapa.compute(new Chave(nome, preco), (k, v) -> {
                if (v == null) return new double[]{l.getQuantidade(), round2(l.calcularSubtotal())};
                v[0] += l.getQuantidade();
                v[1]  = round2(v[1] + round2(l.calcularSubtotal()));
                return v;
            });
        }

        linhasExibicao.setAll(
            mapa.entrySet().stream()
                .map(e -> new LinhaExibicao(
                        e.getKey().nome,
                        (int) e.getValue()[0],
                        e.getKey().preco,
                        e.getValue()[1]))
                .toList()
        );

        double total = round2(linhasExibicao.stream().mapToDouble(l -> l.subtotal).sum());
        lblTotal.setText(String.format("Total: %.2f €", total));
    }

    private static double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }

    private String resolverNomeProduto(int idLote) {
        Lote lote = AppContext.getInstance().loteRepo.buscarPorId(idLote);
        if (lote == null) return "Artigo";
        Produto p = AppContext.getInstance().produtoRepo.buscarPorId(lote.getIdProduto());
        return p != null ? p.getNome() : "Artigo";
    }

    // ── Auxiliares ────────────────────────────────────────────────────

    private void removerLinhaExibicao(LinhaExibicao linha) {
        if (vendaAtual == null) return;
        // Remove as LinhaVenda correspondentes e repõe o stock em cada lote
        vendaAtual.getLinhas().removeIf(lv -> {
            if (resolverNomeProduto(lv.getIdLote()).equals(linha.nome)
                    && round2(lv.getPrecoUnitario()) == round2(linha.precoUnitario)) {
                Lote lote = AppContext.getInstance().loteRepo.buscarPorId(lv.getIdLote());
                if (lote != null) {
                    lote.setQuantidade(lote.getQuantidade() + lv.getQuantidade());
                    AppContext.getInstance().loteRepo.atualizar(lote);
                }
                return true;
            }
            return false;
        });
        atualizarTabela();
    }

    private void garantirVendaAtiva() {
        if (vendaAtual == null) {
            Utilizador u = AppContext.getInstance().getUtilizadorAtual();
            try {
                vendaAtual = AppContext.getInstance().vendaServico.iniciarVenda(u);
                AppContext.getInstance().vendaEmCurso = vendaAtual;
                setCarrinhoAtivo(true);
            } catch (Exception e) {
                mostrarErro(e.getMessage());
            }
        }
    }

    private void limparVenda() {
        vendaAtual = null;
        AppContext.getInstance().vendaEmCurso = null;
        linhasExibicao.clear();
        lblTotal.setText("Total: 0.00 €");
        txtNif.clear();
        setCarrinhoAtivo(false);
    }

    private void verificarLimiteCaixa() {
        Utilizador u = AppContext.getInstance().getUtilizadorAtual();
        SessaoCaixa sessao = AppContext.getInstance().caixaServico.buscarSessaoAtiva(u.getId());
        if (sessao != null && AppContext.getInstance().caixaServico
                .verificarLimite(sessao, AppContext.getInstance().configuracao)) {
            mostrarAviso("Atenção: saldo da caixa excede o limite máximo. Efectue uma sangria.");
        }
    }

    private void setCarrinhoAtivo(boolean ativo) {
        txtCodigoBarras.setDisable(!ativo);
        txtQuantidade.setDisable(!ativo);
        btnAdicionar.setDisable(!ativo);
        btnFinalizar.setDisable(!ativo);
        btnAnular.setDisable(!ativo);
        btnIniciar.setDisable(ativo);
    }

    private void mostrarDialogoFatura(Fatura fatura, double total, double troco, Path pdfPath) {
        StringBuilder sb = new StringBuilder();
        sb.append("Fatura N.º:  ").append(fatura.getNumeroFatura()).append("\n");
        sb.append("Total:       ").append(String.format("%.2f €", total)).append("\n");
        if (troco >= 0) sb.append("Troco:       ").append(String.format("%.2f €", troco)).append("\n");
        if (pdfPath != null) sb.append("\nFatura PDF guardada em:\n").append(pdfPath);
        else sb.append("\n(Não foi possível gerar o PDF da fatura.)");

        Alert alert = DialogoUtil.comOwner(new Alert(Alert.AlertType.INFORMATION));
        alert.setTitle("Venda Concluída");
        alert.setHeaderText("Venda finalizada com sucesso!");
        alert.setContentText(sb.toString());

        if (pdfPath != null) {
            ButtonType btnAbrir = new ButtonType("Abrir PDF");
            alert.getButtonTypes().add(btnAbrir);
            Path finalPath = pdfPath;
            alert.showAndWait().ifPresent(bt -> {
                if (bt == btnAbrir) abrirFicheiro(finalPath);
            });
        } else {
            alert.showAndWait();
        }
    }

    private void abrirFicheiro(Path ficheiro) {
        String so = System.getProperty("os.name", "").toLowerCase();
        String[] cmd;
        if (so.contains("win")) {
            cmd = new String[]{"rundll32", "url.dll,FileProtocolHandler", ficheiro.toString()};
        } else if (so.contains("mac")) {
            cmd = new String[]{"open", ficheiro.toString()};
        } else {
            cmd = new String[]{"xdg-open", ficheiro.toString()};
        }
        Thread t = new Thread(() -> {
            try {
                new ProcessBuilder(cmd).redirectErrorStream(true).start();
            } catch (Exception ex) {
                javafx.application.Platform.runLater(() ->
                        mostrarErro("Não foi possível abrir o PDF:\n" + ex.getMessage()));
            }
        }, "abrir-pdf");
        t.setDaemon(true);
        t.start();
    }

    // ── Devoluções ────────────────────────────────────────────────────

    @FXML
    public void onPesquisarFatura() {
        String num = txtNumeroFatura.getText().trim();
        if (num.isBlank()) { mostrarErro("Insira o número de fatura."); return; }

        Venda venda = AppContext.getInstance().vendaRepo.buscarPorNumeroFatura(num);
        if (venda == null || venda.getFatura() == null) {
            mostrarErro("Fatura não encontrada: " + num);
            esconderSecoesDevolucao();
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

        tblLinhasFatura.setItems(linhas);
        tblLinhasFatura.getSelectionModel().clearSelection();
        formDevolucao.setVisible(false);
        formDevolucao.setManaged(false);
        lblInfoFatura.setText("Fatura: " + num + "  ·  " + linhas.size() + " artigo(s)");
        secaoFatura.setVisible(true);
        secaoFatura.setManaged(true);
        lblResultadoDevolucao.setText("");
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
            lblResultadoDevolucao.setText(String.format(
                    "Devolução processada. Valor restituído: %.2f €", d.getValorRestituido()));
            dpDataValidade.setValue(null);
            txtQtdDevolucao.clear();
            formDevolucao.setVisible(false);
            formDevolucao.setManaged(false);
            tblLinhasFatura.getSelectionModel().clearSelection();
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

    private void mostrarErro(String msg)  { DialogoUtil.erro(msg); }
    private void mostrarAviso(String msg) { DialogoUtil.aviso(msg); }
}
