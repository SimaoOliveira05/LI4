package pt.trasmum.loja.apresentacao;

import javafx.scene.control.*;
import pt.trasmum.loja.app.Navigator;

import java.util.Optional;

/**
 * Utilitário centralizado para criação de diálogos com initOwner aplicado,
 * evitando que as janelas de popup apareçam sem pai e causem redimensionamento
 * da janela principal.
 */
public final class DialogoUtil {

    private DialogoUtil() {}

    public static void erro(String mensagem) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.initOwner(Navigator.getStage());
        a.setTitle("Erro"); a.setHeaderText(null); a.setContentText(mensagem);
        a.showAndWait();
    }

    public static void info(String mensagem) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.initOwner(Navigator.getStage());
        a.setTitle("Informação"); a.setHeaderText(null); a.setContentText(mensagem);
        a.showAndWait();
    }

    public static void aviso(String mensagem) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.initOwner(Navigator.getStage());
        a.setTitle("Aviso"); a.setHeaderText(null); a.setContentText(mensagem);
        a.showAndWait();
    }

    public static Optional<ButtonType> confirmar(String mensagem) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION, mensagem, ButtonType.YES, ButtonType.NO);
        a.initOwner(Navigator.getStage());
        a.setHeaderText(null);
        return a.showAndWait();
    }

    /** Aplica owner a qualquer Dialog antes de showAndWait(). */
    public static <D extends Dialog<?>> D comOwner(D dlg) {
        dlg.initOwner(Navigator.getStage());
        return dlg;
    }
}
