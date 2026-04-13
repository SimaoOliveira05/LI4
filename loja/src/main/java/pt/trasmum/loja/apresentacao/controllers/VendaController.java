package pt.trasmum.loja.apresentacao.controllers;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import pt.trasmum.loja.app.AppContext;
import pt.trasmum.loja.apresentacao.GeradorFaturaPdf;
import pt.trasmum.loja.dominio.catalogo.Lote;
import pt.trasmum.loja.dominio.catalogo.Produto;
import pt.trasmum.loja.dominio.core.Utilizador;
import pt.trasmum.loja.dominio.tesouraria.SessaoCaixa;
import pt.trasmum.loja.dominio.vendas.*;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class VendaController {

    // ── Painel esquerdo ───────────────────────────────────────────────
    @FXML private TextField txtFiltro;
    @FXML private FlowPane  painelProdutos;

    // ── Painel direito ────────────────────────────────────────────────
    @FXML private TextField txtCodigoBarras;
    @FXML private TextField txtQuantidade;
    @FXML private TableView<LinhaExibicao>         tblLinhas;
    @FXML private TableColumn<LinhaExibicao, String>  colLote;
    @FXML private TableColumn<LinhaExibicao, Integer> colQtd;
    @FXML private TableColumn<LinhaExibicao, Double>  colPreco;
    @FXML private TableColumn<LinhaExibicao, Double>  colSubtotal;
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
        tblLinhas.setItems(linhasExibicao);

        ToggleGroup tg = new ToggleGroup();
        rbNumerario.setToggleGroup(tg);
        rbMultibanco.setToggleGroup(tg);
        rbNumerario.setSelected(true);
        txtValorEntregue.disableProperty().bind(rbMultibanco.selectedProperty());

        todosProdutos = AppContext.getInstance().produtoRepo.listarAtivos();
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

    private void atualizarPainelProdutos(String filtro) {
        painelProdutos.getChildren().clear();
        String f = filtro.toLowerCase();
        todosProdutos.stream()
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
        Utilizador u = AppContext.getInstance().getUtilizadorAtual();
        vendaAtual = AppContext.getInstance().vendaServico.iniciarVenda(u);
        AppContext.getInstance().vendaEmCurso = vendaAtual;
        linhasExibicao.clear();
        lblTotal.setText("Total: 0.00 €");
        setCarrinhoAtivo(true);
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
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Anular venda em curso?", ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Confirmação");
        confirm.setHeaderText(null);
        confirm.showAndWait().ifPresent(bt -> {
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

    private void garantirVendaAtiva() {
        if (vendaAtual == null) {
            Utilizador u = AppContext.getInstance().getUtilizadorAtual();
            vendaAtual = AppContext.getInstance().vendaServico.iniciarVenda(u);
            AppContext.getInstance().vendaEmCurso = vendaAtual;
            setCarrinhoAtivo(true);
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

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Venda Concluída");
        alert.setHeaderText("Venda finalizada com sucesso!");
        alert.setContentText(sb.toString());

        if (pdfPath != null) {
            ButtonType btnAbrir = new ButtonType("Abrir PDF");
            alert.getButtonTypes().add(btnAbrir);
            Path finalPath = pdfPath;
            alert.showAndWait().ifPresent(bt -> {
                if (bt == btnAbrir) {
                    try { java.awt.Desktop.getDesktop().open(finalPath.toFile()); }
                    catch (Exception ex) { mostrarErro("Não foi possível abrir o PDF:\n" + ex.getMessage()); }
                }
            });
        } else {
            alert.showAndWait();
        }
    }

    private void mostrarErro(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("Erro"); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }
    private void mostrarAviso(String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setTitle("Aviso"); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }
}
