package pt.trasmum.loja.apresentacao.controllers;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import pt.trasmum.loja.app.AppContext;
import pt.trasmum.loja.dominio.core.LogAuditoria;
import pt.trasmum.loja.dominio.core.TipoAcao;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class LogAuditoriaController {

    private static final DateTimeFormatter FORMATO_DATA_HORA = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @FXML private ComboBox<TipoAcao> cmbFiltroTipo;
    @FXML private TableView<LogAuditoria> tblLogs;
    @FXML private TableColumn<LogAuditoria, String> colDataHora;
    @FXML private TableColumn<LogAuditoria, String> colTipo;
    @FXML private TableColumn<LogAuditoria, String> colEntidade;
    @FXML private TableColumn<LogAuditoria, Integer> colIdEntidade;
    @FXML private TableColumn<LogAuditoria, Integer> colIdUtilizador;
    @FXML private TableColumn<LogAuditoria, String> colEstado;

    private final ObservableList<LogAuditoria> logs = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Configurar colunas
        colDataHora.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getDataHora() != null ? c.getValue().getDataHora().format(FORMATO_DATA_HORA) : "—"));
        colTipo.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getAcao() != null ? c.getValue().getAcao().name() : "—"));
        colEntidade.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getEntidade() != null ? c.getValue().getEntidade() : "—"));
        colIdEntidade.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getIdEntidade()));
        colIdUtilizador.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getIdUtilizador()));
        colEstado.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getEstadoSincronizacao() != null ? c.getValue().getEstadoSincronizacao().name() : "—"));

        tblLogs.setItems(logs);

        // Configurar filtro - opção "Todos" + tipos de ação
        cmbFiltroTipo.getItems().add(null); // Opção "Todos"
        cmbFiltroTipo.getItems().addAll(TipoAcao.values());
        cmbFiltroTipo.setValue(null);

        // Listener para filtrar ao selecionar
        cmbFiltroTipo.valueProperty().addListener((obs, oldVal, newVal) -> aplicarFiltro());

        // Carregar todos os logs inicialmente
        carregarLogs();
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
                // Sem filtro - mostrar todos
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
        aplicarFiltro();
    }
}