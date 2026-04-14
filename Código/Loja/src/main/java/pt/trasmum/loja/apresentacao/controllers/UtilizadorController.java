package pt.trasmum.loja.apresentacao.controllers;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import pt.trasmum.loja.app.AppContext;
import pt.trasmum.loja.dominio.core.PerfilUtilizador;
import pt.trasmum.loja.dominio.core.Utilizador;
import pt.trasmum.loja.servico.UtilizadorDTO;

import java.util.Optional;

public class UtilizadorController {

    @FXML private TableView<Utilizador> tblUtilizadores;
    @FXML private TableColumn<Utilizador, String>  colNome;
    @FXML private TableColumn<Utilizador, String>  colPerfil;
    @FXML private TableColumn<Utilizador, Boolean> colAtivo;

    private final ObservableList<Utilizador> utilizadores = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colNome.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNomeUtilizador()));
        colPerfil.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getPerfil().name()));
        colAtivo.setCellValueFactory(c -> new SimpleBooleanProperty(c.getValue().isAtivo()).asObject());
        tblUtilizadores.setItems(utilizadores);
        carregar();
    }

    @FXML
    public void onCriar() {
        Utilizador gestor = AppContext.getInstance().getUtilizadorAtual();
        Optional<UtilizadorDTO> dto = mostrarDialogoUtilizador(null);
        dto.ifPresent(d -> {
            try {
                AppContext.getInstance().utilizadorServico.criar(gestor, d);
                carregar();
                mostrarInfo("Utilizador criado.");
            } catch (Exception e) { mostrarErro(e.getMessage()); }
        });
    }

    @FXML
    public void onEditar() {
        Utilizador sel = tblUtilizadores.getSelectionModel().getSelectedItem();
        if (sel == null) { mostrarErro("Selecione um utilizador."); return; }
        Utilizador gestor = AppContext.getInstance().getUtilizadorAtual();
        Optional<UtilizadorDTO> dto = mostrarDialogoUtilizador(sel);
        dto.ifPresent(d -> {
            try {
                AppContext.getInstance().utilizadorServico.editar(gestor, sel.getId(), d);
                carregar();
                mostrarInfo("Utilizador atualizado.");
            } catch (Exception e) { mostrarErro(e.getMessage()); }
        });
    }

    @FXML
    public void onDesativar() {
        Utilizador sel = tblUtilizadores.getSelectionModel().getSelectedItem();
        if (sel == null) { mostrarErro("Selecione um utilizador."); return; }
        Utilizador gestor = AppContext.getInstance().getUtilizadorAtual();
        try {
            AppContext.getInstance().utilizadorServico.desativar(gestor, sel.getId());
            carregar();
            mostrarInfo("Utilizador desativado.");
        } catch (Exception e) { mostrarErro(e.getMessage()); }
    }

    private void carregar() {
        utilizadores.setAll(AppContext.getInstance().utilizadorRepo.listarAtivos());
    }

    private Optional<UtilizadorDTO> mostrarDialogoUtilizador(Utilizador existente) {
        Dialog<UtilizadorDTO> dlg = new Dialog<>();
        dlg.setTitle(existente == null ? "Criar Utilizador" : "Editar Utilizador");
        dlg.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextField fNome   = new TextField(existente != null ? existente.getNomeUtilizador() : "");
        PasswordField fPasse = new PasswordField();
        ComboBox<PerfilUtilizador> cmbPerfil = new ComboBox<>(
                FXCollections.observableArrayList(PerfilUtilizador.values()));
        if (existente != null) cmbPerfil.setValue(existente.getPerfil());

        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
        grid.setHgap(8); grid.setVgap(8);
        grid.add(new Label("Nome:"), 0, 0);        grid.add(fNome, 1, 0);
        grid.add(new Label("Palavra-passe:"), 0, 1); grid.add(fPasse, 1, 1);
        grid.add(new Label("Perfil:"), 0, 2);      grid.add(cmbPerfil, 1, 2);
        dlg.getDialogPane().setContent(grid);

        dlg.setResultConverter(bt -> {
            if (bt == ButtonType.OK) {
                PerfilUtilizador perfil = cmbPerfil.getValue() != null ? cmbPerfil.getValue() : PerfilUtilizador.FUNCIONARIO;
                return new UtilizadorDTO(fNome.getText().trim(), fPasse.getText(), perfil);
            }
            return null;
        });
        return dlg.showAndWait();
    }

    private void mostrarErro(String m) { Alert a = new Alert(Alert.AlertType.ERROR); a.setTitle("Erro"); a.setHeaderText(null); a.setContentText(m); a.showAndWait(); }
    private void mostrarInfo(String m)  { Alert a = new Alert(Alert.AlertType.INFORMATION); a.setTitle("Informação"); a.setHeaderText(null); a.setContentText(m); a.showAndWait(); }
}
