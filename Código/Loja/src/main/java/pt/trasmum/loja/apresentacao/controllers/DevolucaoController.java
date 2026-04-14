package pt.trasmum.loja.apresentacao.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import pt.trasmum.loja.app.AppContext;
import pt.trasmum.loja.dominio.core.Utilizador;
import pt.trasmum.loja.dominio.vendas.Devolucao;

import java.time.LocalDate;

public class DevolucaoController {

    @FXML private TextField txtNumeroFatura;
    @FXML private TextField txtCodigoBarras;
    @FXML private DatePicker dpDataValidade;
    @FXML private TextField txtQuantidade;
    @FXML private Label lblResultado;

    @FXML
    public void onProcessarDevolucao() {
        lblResultado.setText("");
        String numFatura = txtNumeroFatura.getText().trim();
        String codigo    = txtCodigoBarras.getText().trim();
        LocalDate data   = dpDataValidade.getValue();
        String qtdStr    = txtQuantidade.getText().trim();

        boolean invalido = false;
        if (numFatura.isBlank()) { marcarInvalido(txtNumeroFatura); invalido = true; }
        if (codigo.isBlank())    { marcarInvalido(txtCodigoBarras); invalido = true; }
        if (data == null)        { invalido = true; }
        if (qtdStr.isBlank())    { marcarInvalido(txtQuantidade); invalido = true; }
        if (invalido) { mostrarErro("Preencha todos os campos."); return; }

        try {
            int quantidade = Integer.parseInt(qtdStr);
            Utilizador u = AppContext.getInstance().getUtilizadorAtual();
            Devolucao d = AppContext.getInstance().devolucaoServico.processar(u, numFatura, codigo, data, quantidade);
            lblResultado.setText(String.format(
                    "Devolução processada com sucesso.\nValor restituído: %.2f €", d.getValorRestituido()));
            limparFormulario();
        } catch (NumberFormatException e) {
            mostrarErro("Quantidade inválida.");
        } catch (Exception e) {
            mostrarErro(e.getMessage());
        }
    }

    private void limparFormulario() {
        txtNumeroFatura.clear();
        txtCodigoBarras.clear();
        dpDataValidade.setValue(null);
        txtQuantidade.clear();
    }

    private void marcarInvalido(TextField campo) {
        campo.pseudoClassStateChanged(javafx.css.PseudoClass.getPseudoClass("invalid"), true);
    }

    private void mostrarErro(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR); a.setTitle("Erro"); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }
}
