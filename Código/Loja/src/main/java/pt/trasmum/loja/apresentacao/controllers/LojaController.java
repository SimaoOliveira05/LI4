package pt.trasmum.loja.apresentacao.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import pt.trasmum.loja.apresentacao.DialogoUtil;
import pt.trasmum.loja.app.AppContext;
import pt.trasmum.loja.dominio.core.Loja;
import pt.trasmum.loja.dominio.core.Utilizador;

public class LojaController {

    @FXML private Label     lblId;
    @FXML private TextField fNome;
    @FXML private TextField fMorada;
    @FXML private TextField fLocalidade;
    @FXML private TextField fNif;
    @FXML private TextField fEmail;
    @FXML private TextField fLimiteCaixa;
    @FXML private TextField fDiasValidade;

    @FXML
    public void initialize() {
        Loja loja = AppContext.getInstance().lojaServico.obter();
        if (loja == null) return;
        lblId.setText(loja.getId());
        fNome.setText(loja.getNome() != null ? loja.getNome() : "");
        fMorada.setText(loja.getMorada() != null ? loja.getMorada() : "");
        fLocalidade.setText(loja.getLocalidade() != null ? loja.getLocalidade() : "");
        fNif.setText(loja.getNif() != null ? loja.getNif() : "");
        fEmail.setText(loja.getEmail() != null ? loja.getEmail() : "");
        fLimiteCaixa.setText(String.valueOf(loja.getLimiteMaximoCaixa()));
        fDiasValidade.setText(String.valueOf(loja.getDiasAlertaValidade()));
    }

    @FXML
    public void onGuardar() {
        Utilizador utilizador = AppContext.getInstance().getUtilizadorAtual();
        Loja loja = AppContext.getInstance().lojaServico.obter();
        if (loja == null) { DialogoUtil.erro("Dados da loja não encontrados na base de dados."); return; }

        double limite;
        int dias;
        try {
            limite = Double.parseDouble(fLimiteCaixa.getText().trim().replace(',', '.'));
            dias   = Integer.parseInt(fDiasValidade.getText().trim());
        } catch (NumberFormatException e) {
            DialogoUtil.erro("Limite de caixa deve ser um número decimal e dias de validade um número inteiro.");
            return;
        }

        loja.setNome(fNome.getText().trim());
        loja.setMorada(fMorada.getText().trim());
        loja.setLocalidade(fLocalidade.getText().trim());
        loja.setNif(fNif.getText().trim());
        loja.setEmail(fEmail.getText().trim());
        loja.setLimiteMaximoCaixa(limite);
        loja.setDiasAlertaValidade(dias);

        AppContext.getInstance().lojaServico.atualizar(utilizador, loja);
        AppContext.getInstance().configuracao.setLimiteMaximoCaixa(limite);
        AppContext.getInstance().configuracao.setDiasAlertaValidade(dias);
        DialogoUtil.info("Informações da loja guardadas com sucesso.");
    }
}
