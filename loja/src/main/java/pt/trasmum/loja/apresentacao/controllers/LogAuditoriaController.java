package pt.trasmum.loja.apresentacao.controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.util.StringConverter;
import pt.trasmum.loja.app.AppContext;
import pt.trasmum.loja.dominio.core.LogAuditoria;
import pt.trasmum.loja.dominio.core.TipoAcao;
import pt.trasmum.loja.dominio.core.Utilizador;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LogAuditoriaController {

    private static final DateTimeFormatter FORMATO_DATA_HORA = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @FXML private ComboBox<TipoAcao> cmbFiltroTipo;
    @FXML private TableView<LogAuditoria> tblLogs;
    @FXML private TableColumn<LogAuditoria, String> colDataHora;
    @FXML private TableColumn<LogAuditoria, String> colTipo;
    @FXML private TableColumn<LogAuditoria, String> colEntidade;
    @FXML private TableColumn<LogAuditoria, String> colUtilizador;
    @FXML private TableColumn<LogAuditoria, String> colEstado;

    private final ObservableList<LogAuditoria> logs = FXCollections.observableArrayList();
    private final Map<Integer, String> cacheNomesUtilizadores = new HashMap<>();

    @FXML
    public void initialize() {
        colDataHora.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getDataHora() != null ? c.getValue().getDataHora().format(FORMATO_DATA_HORA) : "—"));
        colTipo.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getAcao() != null ? c.getValue().getAcao().toString() : "—"));
        colEntidade.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getDescricao() != null ? c.getValue().getDescricao() : "—"));
        colUtilizador.setCellValueFactory(c -> new SimpleStringProperty(
                nomeUtilizador(c.getValue().getIdUtilizador())));
        colEstado.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getEstadoSincronizacao() != null ? c.getValue().getEstadoSincronizacao().toString() : "—"));

        tblLogs.setItems(logs);

        cmbFiltroTipo.setConverter(new StringConverter<>() {
            @Override public String toString(TipoAcao t) { return t == null ? "Todos" : t.toString(); }
            @Override public TipoAcao fromString(String s) { return null; }
        });
        cmbFiltroTipo.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(TipoAcao item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : (item == null ? "Todos" : item.toString()));
            }
        });
        cmbFiltroTipo.getItems().add(null);
        cmbFiltroTipo.getItems().addAll(TipoAcao.values());
        cmbFiltroTipo.setValue(null);

        cmbFiltroTipo.valueProperty().addListener((obs, oldVal, newVal) -> aplicarFiltro());

        carregarLogs();
    }

    private String nomeUtilizador(Integer id) {
        if (id == null) return "—";
        return cacheNomesUtilizadores.computeIfAbsent(id, i -> {
            try {
                Utilizador u = AppContext.getInstance().utilizadorRepo.buscarPorId(i);
                return u != null ? u.getNomeUtilizador() : ("#" + i);
            } catch (Exception e) {
                return "#" + i;
            }
        });
    }

    private void carregarLogs() {
        try {
            var auditoriaServico = AppContext.getInstance().auditoriaServico;
            List<LogAuditoria> todosLogs = auditoriaServico.obterTodosLogs();
            logs.setAll(todosLogs);
        } catch (Exception e) {
            System.err.println("Erro ao carregar logs: " + e.getMessage());
        }
    }

    @FXML
    private void aplicarFiltro() {
        try {
            var auditoriaServico = AppContext.getInstance().auditoriaServico;
            TipoAcao tipoSelecionado = cmbFiltroTipo.getValue();

            List<LogAuditoria> logsFiltrados;
            if (tipoSelecionado == null) {
                logsFiltrados = auditoriaServico.obterTodosLogs();
            } else {
                logsFiltrados = auditoriaServico.obterLogsPorTipo(tipoSelecionado);
            }
            logs.setAll(logsFiltrados);
        } catch (Exception e) {
            System.err.println("Erro ao filtrar logs: " + e.getMessage());
        }
    }

    @FXML
    private void onAtualizar() {
        cacheNomesUtilizadores.clear();
        aplicarFiltro();
    }
}
