package pt.trasmum.loja.apresentacao;

import javafx.scene.control.*;
import javafx.stage.Modality;
import pt.trasmum.loja.app.Navigator;

import java.util.Optional;

/**
 * Utilitário centralizado para criação de diálogos com initOwner e initModality
 * aplicados, evitando que as janelas de popup apareçam sem pai e causem
 * redimensionamento da janela principal.
 */
public final class DialogoUtil {

    private DialogoUtil() {}

    public static void erro(String mensagem) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        preparar(a);
        a.setTitle("Erro"); a.setHeaderText(null); a.setContentText(mensagem);
        a.showAndWait();
    }

    public static void info(String mensagem) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        preparar(a);
        a.setTitle("Informação"); a.setHeaderText(null); a.setContentText(mensagem);
        a.showAndWait();
    }

    public static void aviso(String mensagem) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        preparar(a);
        a.setTitle("Aviso"); a.setHeaderText(null); a.setContentText(mensagem);
        a.showAndWait();
    }

    public static Optional<ButtonType> confirmar(String mensagem) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION, mensagem, ButtonType.YES, ButtonType.NO);
        preparar(a);
        a.setHeaderText(null);
        return a.showAndWait();
    }

    /** Aplica owner e modality a qualquer Dialog antes de showAndWait(). */
    public static <D extends Dialog<?>> D comOwner(D dlg) {
        preparar(dlg);
        return dlg;
    }

    private static void preparar(Dialog<?> dlg) {
        javafx.stage.Stage stage = Navigator.getStage();
        dlg.initOwner(stage);
        dlg.initModality(Modality.WINDOW_MODAL);
        dlg.setResizable(true);
        dlg.getDialogPane().setMinWidth(480);
        dlg.getDialogPane().setMinHeight(220);

    }
}
